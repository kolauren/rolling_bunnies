package change.impact.graph.commit;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.RepositoryCommit;

import com.google.common.collect.Queues;

//gets commit info from somewhere (github) and outputs Commit objects
public class CommitRetriever {
	private GitHubDao githubDao;
	String owner;
	String repo;
	String branch;

	public CommitRetriever(String owner, String repo, String branch) {
		//TODO: move args to properties file
		String user = "pammil";
		String password = "5cd8f20e47dfc2ffc846e82c652450c61f0a41a9";

		this.owner = owner;
		this.repo = repo;
		this.branch = branch;

		//basic authentication
		githubDao = new GitHubDao(user, password);
	}

	//returns numCommits newest commits ordered from oldest -> recent
	public Collection<Commit> getCommits(int numCommits) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Deque<Commit> commits = Queues.newArrayDeque();
		List<RepositoryCommit> githubCommits = githubDao.queryCommits(owner, repo, branch);

		int max;
		if((numCommits < 0) || (numCommits > githubCommits.size()))
			max = githubCommits.size();
		else
			max = numCommits;

		for(int i=0; i<max; i++) {
			RepositoryCommit githubCommit = githubCommits.get(i);
			System.out.println(githubCommit.getUrl());

			Commit commit = new Commit();
			commit.setSha(githubCommit.getSha());
			//retrieve relevant commit data
			retrieveFiles(githubCommit, commit);
			retrieveDiffs(githubCommit, commit);
			if(!commit.isEmpty()) {
				commits.push(commit);
			}
		}
		return commits;
	}

	//get all commits
	public Collection<Commit> getCommits() throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return getCommits(-1);
	}

	private void retrieveFiles(RepositoryCommit githubCommit, Commit commit) {
		for(CommitFile file : githubCommit.getFiles()) {
			String filename = file.getFilename();
			//only interested in .java files
			if(filename.endsWith(".java")) {
				CommitFileStatus status = CommitFileStatus.fromString(file.getStatus());
				if(status == CommitFileStatus.RENAMED) {
					//TODO:find old name
					String oldName = "";
					commit.addJavaFile(status, file.getFilename(), oldName);
				} else {
					commit.addJavaFile(status, file.getFilename());
				}
			}
		}
	}

	private void retrieveDiffs(RepositoryCommit githubCommit, Commit commit) throws IOException {
		for(CommitFile file : githubCommit.getFiles()) { 
			String filename = file.getFilename();
			if(filename.endsWith(".java")) {
				String patch = file.getPatch();
				//status is added or renamed or removed(?)
				//i assume there are no code changes if the diff is null; therefore
				//just remove it from the commit
				if(patch == null) {
					commit.removeJavaFile(filename);
					continue;
				}
				Diff diff = UnifiedDiffParser.parse(file.getPatch());
				diff.setRawCodeURL(file.getRawUrl());
				commit.addDiff(filename, diff);
			}
		}
	}
}
