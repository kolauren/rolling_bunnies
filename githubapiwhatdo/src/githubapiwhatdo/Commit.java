package githubapiwhatdo;

import java.util.List;

import org.eclipse.egit.github.core.RepositoryCommit;

//commit data we're interested in for our visualization
public class Commit {
	private RepositoryCommit githubCommit;
	private int commitNumber;
	private List<String> addedJavaFiles;
	private List<String> removedJavaFiles;
	private List<String> modifiedJavaFiles;
	
	public RepositoryCommit getGithubCommit() {
		return githubCommit;
	}
	
	public int getCommitNumber() {
		return commitNumber;
	}
	
	public List<String> getAddedJavaFiles() {
		return addedJavaFiles;
	}
	
	public List<String> getModifiedJavaFiles() {
		return modifiedJavaFiles;
	}
	
	public List<String> getRemovedJavaFiles() {
		return removedJavaFiles;
	}
	
	public void setGithubCommit(RepositoryCommit githubCommit) {
		this.githubCommit = githubCommit;
	}
	
	public void setCommitNumber(int commitNumber) {
		this.commitNumber = commitNumber;
	}
	
	public void setAddedJavaFiles(List<String> addedJavaFiles) {
		this.addedJavaFiles = addedJavaFiles;
	}
	
	public void setModifiedJavaFiles(List<String> modifiedJavaFiles) {
		this.modifiedJavaFiles = modifiedJavaFiles;
	}
	
	public void setRemovedJavaFiles(List<String> removedJavaFiles) {
		this.removedJavaFiles = removedJavaFiles;
	}
}
