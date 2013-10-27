
import java.util.List;


//commit data we're interested in for our visualization
public class Commit {
	private String commitHash;
	private List<String> addedJavaFiles;
	private List<String> removedJavaFiles;
	private List<String> modifiedJavaFiles;

	
	public String getCommitHash() {
		return commitHash;
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
	
	public void setCommitNumber(String commitHash) {
		this.commitHash = commitHash;
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
