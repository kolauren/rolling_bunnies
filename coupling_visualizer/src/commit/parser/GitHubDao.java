package commit.parser;

import java.io.IOException;
import java.util.List;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
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
	//get all full commits for a [repo]/[branch]
	public List<RepositoryCommit> queryCommits(String owner, String repoName, String sha) throws IOException {
		return queryCommits(owner, repoName, sha, null);
	}
	
	//get full commits that contain [path] for [repo]/[branch]
	public List<RepositoryCommit> queryCommits(String owner, String repoName, String sha, String path) throws IOException {
		List<RepositoryCommit> commits = Lists.newArrayList();
		
		RepositoryService repoService = new RepositoryService(client);
		Repository repo = repoService.getRepository(owner, repoName);
		CommitService commitService = new CommitService(client);
		List<RepositoryCommit> repositoryCommits = commitService.getCommits(repo, sha, path);
		for(RepositoryCommit commit : repositoryCommits) {
			RepositoryCommit detailedCommit = commitService.getCommit(repo, commit.getSha());
			commits.add(detailedCommit);
		}
		
		return commits;
	}
	
	/**
	 * Get the previous commit to the target commit (commitSHA).
	 * 
	 * Returns null if there were no previous commit for the target commit.
	 * 
	 * @param owner
	 * @param repoName
	 * @param sha
	 * @param filename
	 * @param commitSHA
	 * @return
	 * @throws IOException
	 */
	public RepositoryCommit getPreviousCommit(String owner, String repoName, String sha, String filename) throws IOException {
		RepositoryService repoService = new RepositoryService(client);
		Repository repo = repoService.getRepository(owner, repoName);
		CommitService commitService = new CommitService(client);
		//ordered from newest -> oldest starting at sha
		List<RepositoryCommit> repositoryCommits = commitService.getCommits(repo, sha, filename);
		
		RepositoryCommit prevCommit = null;
		//TODO: deal with < 2 cases
		if(repositoryCommits.isEmpty()) {
			//phantom commit
			assert false;
		} else if(repositoryCommits.size() == 1) {
			//merged with no changes on this file
		
		} else {
			prevCommit = commitService.getCommit(repo, repositoryCommits.get(1).getSha());
		}
		return prevCommit;
	}
}
