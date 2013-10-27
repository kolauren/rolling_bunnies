package githubapiwhatdo;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.eclipse.egit.github.core.RepositoryCommit;

public class Main {
	public static void main(String[] args) throws IOException {
		String owner = "iambchan";
		String repo = "Team-02";
		GitHubDao githubDao = new GitHubDao("pammil", "5cd8f20e47dfc2ffc846e82c652450c61f0a41a9");
		Collection<RepositoryCommit> commits = githubDao.queryCommits(owner, repo);
		for(RepositoryCommit commit : commits) {

		}
	}
}