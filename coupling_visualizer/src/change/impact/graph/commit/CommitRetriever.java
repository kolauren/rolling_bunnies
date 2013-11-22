package change.impact.graph.commit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import change.impact.graph.utils.Utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
			Map<String, Integer> fileToDiffNumber = retrieveFiles(githubCommit, commit);
			//retrieve relevant commit data
			retrieveDiffs(githubCommit, commit);
			//find all old file paths
			findOldFileName(commit, fileToDiffNumber);

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

	private Map<String, Integer> retrieveFiles(RepositoryCommit githubCommit, Commit commit) {
		Map<String, Integer> fileToDiffNumber = Maps.newHashMap();
		int i=0;
		for(CommitFile file : githubCommit.getFiles()) {
			String filename = file.getFilename();
			//only interested in .java files
			if(filename.endsWith(".java")) {
				CommitFileStatus status = CommitFileStatus.fromString(file.getStatus());
				commit.addJavaFile(status, filename);
				fileToDiffNumber.put(filename, i);
			}
			i++;
		}
		return fileToDiffNumber;
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

	//probably  moved project directory which code cannot see
	//hack to parse the github html for old file name
	public void findOldFileName(Commit commit, Map<String, Integer> fileToDiffNumber) throws IOException {
		Set<String> renamedFiles = Sets.newHashSet(); 
		renamedFiles.addAll(commit.getRenamedJavaFiles());
		renamedFiles.addAll(commit.getRenamedProject());
		Document doc = null;
		for(String newFileName : renamedFiles) {
			if(doc == null) {
				String url = "https://github.com/"+owner+"/"+repo+"/commit/"+commit.getSha();
				//jsoup can't access the url for some reason
				String html = Utils.getHtml(url);
				doc = Jsoup.parse(html);
			}

			String diffNumber = "#diff-"+fileToDiffNumber.get(newFileName);
			String oldFileName = null;

			for(Element link : doc.getElementsByTag("a")) {
				String linkHref = link.attr("href");
				if(linkHref.equals(diffNumber)) {
					if(!link.hasAttr("class")) {
						String linkText = StringEscapeUtils.escapeHtml4(link.text());
						String regex = "\\{(?<oldText>.*?) &rarr; (?<newText>.*?)}";
						Pattern p = Pattern.compile(regex);
						Matcher m = p.matcher(linkText);
						m.find();

						String newText = m.group("newText");
						String oldText = m.group("oldText");

						oldFileName = newFileName.replace(newText, oldText);
						break;
					}
				}
			}
			commit.addOldFileName(newFileName, oldFileName);
		}
	}
}
