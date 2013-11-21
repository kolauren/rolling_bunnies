package change.impact.graph.commit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.RepositoryCommit;

import change.impact.graph.utils.Utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

//gets commit info from somewhere (github) and outputs Commit objects
public class CommitRetriever {
	private GitHubDao githubDao;
	String owner;
	String repo;
	String branch;

	public CommitRetriever(String owner, String repo, String branch) {
		//TODO: move args to properties file
		String user = "pammil";
		String password = "610fe6b39158d3ec2699cdb0bbd52bf24b9f3913";

		this.owner = owner;
		this.repo = repo;
		this.branch = branch;

		//basic authentication
		githubDao = new GitHubDao(user, password);
	}

	//returns numCommits newest commits ordered from oldest -> recent
	public List<Commit> getCommits(int numCommits) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Deque<Commit> commits = Queues.newArrayDeque();
		List<RepositoryCommit> githubCommits = githubDao.queryCommits(owner, repo, branch);

		int max;
		if((numCommits < 0) || (numCommits > githubCommits.size()))
			max = githubCommits.size();
		else
			max = numCommits;

		for(int i=0; i<max; i++) {
			RepositoryCommit githubCommit = githubCommits.get(i);

			Commit commit = new Commit();
			commit.setSha(githubCommit.getSha());
			//retrieve relevant commit data
			retrieveFiles(githubCommit, commit);
			retrieveDiffs(githubCommit, commit);
			//find all old file paths
			findOldFileName(commit);

			if(!commit.isEmpty()) {
				commits.push(commit);
			}
		}
		List<Commit> commitList = Lists.newArrayList();
		commitList.addAll(commits);
		return commitList;
	}

	//get all commits
	public List<Commit> getCommits() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return getCommits(-1);
	}

	private void retrieveFiles(RepositoryCommit githubCommit, Commit commit) {
		for(CommitFile file : githubCommit.getFiles()) {
			String filename = file.getFilename();
			//only interested in .java files
			if(filename.endsWith(".java")) {
				CommitFileStatus status = CommitFileStatus.fromString(file.getStatus());
				commit.addJavaFile(status, filename);
			}
		}
	}

	private void retrieveDiffs(RepositoryCommit githubCommit, Commit commit) throws IOException {
		for(CommitFile file : githubCommit.getFiles()) { 
			String filename = file.getFilename();

			if(filename.endsWith(".java")) {
				String patch = file.getPatch();
				//if the patch is null, that means the project directory was moved
				Diff diff;
				if(patch == null) {
					diff = new Diff();
					commit.addRenamedProject(filename);
				} else {
					diff = UnifiedDiffParser.parse(file.getPatch());
				}
				diff.setRawCodeURL(file.getRawUrl());
				commit.addDiff(filename, diff);
			}
		}
	}

	//	private void findOldFileName(Commit commit) {
	//		//MODIFIED CAN BE RENAMED
	//		Set<String> renamedFiles = Sets.newHashSet(); 
	//		renamedFiles.addAll(commit.getRenamedJavaFiles().keySet());
	//		renamedFiles.addAll(commit.getModifiedJavaFiles());
	//		for(String newFileName : renamedFiles) {
	//			Diff diff = commit.getDiff(newFileName);
	//
	//			Collection<String> removedLines = diff.getRemovedLines().values();
	//
	//			//removed lines are ordered by line number
	//			for(String removedLine : removedLines) {
	//				// check if removedLine contains class declaration
	//				// "class" " interface "
	//
	//
	//				//find old package location
	//				String regex = "package (?<packageName>.*);";
	//				Pattern p = Pattern.compile(regex);
	//				Matcher m = p.matcher(removedLine);
	//
	//				if(m.find()) {
	//					String oldPackageName = m.group("packageName");
	//
	//					regex = "(?<className>\\w*\\.java)";
	//					p = Pattern.compile(regex);
	//					m = p.matcher(newFileName);
	//					m.find();
	//					String className = m.group("className");
	//
	//					//project path
	//					regex = "(?<projectPath>.*/)src/";
	//					p = Pattern.compile(regex);
	//					m = p.matcher(newFileName);
	//					m.find();
	//					String projectPath = m.group("projectPath");
	//
	//					String oldPath = projectPath+"src/"+oldPackageName.replaceAll("\\.", "/")+"/"+className;
	//					commit.getRenamedJavaFiles().put(newFileName, oldPath);
	//					continue;
	//				}
	//
	//				//now find old class name
	//				regex = " (class|interface) (?<oldClassName>\\w*)[^\\n]*?\\{";
	//				p = Pattern.compile(regex);
	//				m = p.matcher(removedLine);
	//
	//				if(m.find()) {
	//					String oldClassName = m.group("oldClassName");
	//					String filePathRegex = "(?<filePath>.*/)";
	//
	//					p = Pattern.compile(filePathRegex);
	//					String partialOldPath = commit.getRenamedJavaFiles().get(newFileName);
	//
	//					//could have package name modified
	//					String fileName = partialOldPath == null ? newFileName : partialOldPath;
	//
	//					m = p.matcher(fileName);
	//					m.find();
	//					oldClassName = m.group("filePath") + oldClassName+".java";
	//
	//					commit.getRenamedJavaFiles().put(newFileName, oldClassName);
	//					break;
	//				}
	//			}
	//		}
	//	}

	//probably  moved project directory which code cannot see
	//hack to parse the github html for old file name
	public void findOldFileName(Commit commit) throws IOException {
		Set<String> renamedFiles = Sets.newHashSet(); 
		renamedFiles.addAll(commit.getRenamedJavaFiles());
		renamedFiles.addAll(commit.getRenamedProject());
		String html = null;
		for(String newFileName : renamedFiles) {
			if(html == null)
				html = Utils.getHtml("https://github.com/"+owner+"/"+repo+"/commit/"+commit.getSha());

			String regex = "\\{(?<oldSegment>.*?) &rarr; (?<newSegment>.*?)\\}";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(html);
			if(m.find()) {
				String oldSegment= m.group("oldSegment");
				String newSegment = m.group("newSegment"); 
				String oldFileName = newFileName.replaceFirst(newSegment, oldSegment);
				commit.addOldFileName(newFileName, oldFileName);
			}
		}
	}
}
