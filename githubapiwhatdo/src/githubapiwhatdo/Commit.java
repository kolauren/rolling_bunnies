package githubapiwhatdo;

import githubapiwhatdo.ParseCommitUtils.CommitFileStatus;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;


//commit data we're interested in for our visualization
public class Commit {
	private int commitNumber;
	private List<String> addedJavaFiles;
	private List<String> removedJavaFiles;
	private List<String> modifiedJavaFiles;
	private List<String> renamedJavaFiles; 
	
	private static final Map<CommitFileStatus, Method> statusSetMethods = Maps.newHashMap();
	static {
		for(CommitFileStatus status : CommitFileStatus.values()) {
			boolean found = false;
			for(Method m : Commit.class.getMethods()) {
				if(StringUtils.containsIgnoreCase(m.getName(), "set"+status.getName())) {
					statusSetMethods.put(status, m);
					found = true;
					break;
				} 
			}
			if(!found)
				throw new NoSuchElementException("no set method found for status: "+status.getName());
		}
	}
	
	public static Method getSetMethodByStatus(CommitFileStatus status) {
		return statusSetMethods.get(status);
	}
	
	public List<String> getRenamedJavaFiles() {
		return renamedJavaFiles;
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
	
	public void setRenamedJavaFiles(List<String> renamedJavaFiles) {
		this.renamedJavaFiles = renamedJavaFiles;
	}
}
