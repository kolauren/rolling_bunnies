package githubapiwhatdo;

import githubapiwhatdo.ParseCommitUtils.CommitFileStatus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

import com.google.common.collect.Lists;

/**
 * wrapper for the egit github api java wrapper.
 * 
 * if methods have the prefix "query" they are making api calls to github which are limited;
 * try not to spam query methods cause your ip becomes blocked for an hour or so.
 * 
 * @author p
 *
 */
public class GitHubDao {
	
	private static final String COMMIT_URI = "/repos/#OWNER#/#REPO#/commits/#SHA#";
	private GitHubClient client;
	
	public GitHubDao(String user, String password){
		this.client = basicAuthGitHub(user, password);
	}
	
	public GitHubDao(String user, String password, String oAuth2Token) {
		this.client = basicAuthGitHub(user, password, oAuth2Token);
	}
	
	private GitHubClient basicAuthGitHub(String user, String password) {
		GitHubClient client = new GitHubClient();
		client.setCredentials(user, password);
		return client;
	}
	
	private GitHubClient basicAuthGitHub(String user, String password, String oAuth2Token) {
		GitHubClient client = basicAuthGitHub(user, password);
		client.setOAuth2Token(oAuth2Token);
		return client;
	}
	
	//cache results; do not call this more than once because github api calls are limited
	public List<RepositoryCommit> queryCommits(String owner, String repoName) throws IOException {
		List<RepositoryCommit> commits = null;
		
		RepositoryService repoService = new RepositoryService(client);
		Repository repo = repoService.getRepository(owner, repoName);
		CommitService commitService = new CommitService(client);
		commits = commitService.getCommits(repo);
		
		return commits;
	}
	
	public String queryCommitJson(String owner, String repoName, String sha) throws IOException {
		String uri = COMMIT_URI.replaceFirst("#OWNER#", owner).replaceFirst("#REPO#", repoName).replaceFirst("#SHA#", sha);
		GitHubRequest request = new GitHubRequest();
		request.setUri(uri);
		InputStream in = client.getStream(request);
		return IOUtils.toString(in, "UTF-8");
	}
	
	public List<Commit> getCommits(String owner, String repoName, int numCommits) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<Commit> commits = Lists.newArrayList();
		List<RepositoryCommit> githubCommits = queryCommits(owner, repoName);
		
		int max;
		if((numCommits < 0) || (numCommits > githubCommits.size()))
			max = githubCommits.size();
		else
			max = numCommits;
		
		int count = 0;
		for(int i=0; i<max; i++) {
			RepositoryCommit githubCommit = githubCommits.get(i);
			System.out.println(githubCommit.getUrl());
			String jsonCommit = queryCommitJson(owner, repoName, githubCommit.getSha());
			Map<CommitFileStatus, List<String>> javaFiles = ParseCommitUtils.getJavaFileNames(jsonCommit);
			if(javaFiles != null) {
				Commit commit = new Commit();	
				for(CommitFileStatus status : CommitFileStatus.values()) {
					Commit.getSetMethodByStatus(status).invoke(commit, javaFiles.get(status));
				}
				commit.setCommitNumber(count);
				count++;
				commits.add(commit);
			}
		}
		return commits;
	}
	
	//get all of them
	public List<Commit> getCommits(String owner, String repoName) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return getCommits(owner, repoName, -1);
	}
	
	//git file statuses: added, removed, modified, renamed
}
