package githubapiwhatdo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

/**
 * if methods have the prefix "query" they are making api calls to github which are limited;
 * try not to spam query methods cause your ip becomes blocked for an hour or so.
 * 
 * @author p
 *
 */
public class GitHubDao {
	
	private static String COMMIT_URI = "/repos/#OWNER#/#REPO#/commits/#SHA#";
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
	
	public String queryCommitJson(String owner, String repo, String sha) throws IOException {
		String uri = COMMIT_URI.replaceFirst("#OWNER#", owner).replaceFirst("#REPO#", repo).replaceFirst("#SHA#", sha);
		GitHubRequest request = new GitHubRequest();
		request.setUri(uri);
		InputStream in = client.getStream(request);
		return IOUtils.toString(in, "UTF-8");
	}
	
	//git file statuses: added, removed, modified
}
