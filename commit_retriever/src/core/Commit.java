package core;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;

import core.ParseGitHubCommit.CommitFileStatus;


//commit data we're interested in for our visualization
public class Commit {
	private int commitNumber;
	//please don't rename these. thx.
	private Deque<String> addedJavaFiles;
	private Deque<String> removedJavaFiles;
	private Deque<String> modifiedJavaFiles;
	private Deque<String> renamedJavaFiles; 
	
	//returns the corresponding set method based on the status
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
	
	public Collection<String> getRenamedJavaFiles() {
		return renamedJavaFiles;
	}
	
	public int getCommitNumber() {
		return commitNumber;
	}
	
	public Collection<String> getAddedJavaFiles() {
		return addedJavaFiles;
	}
	
	public Collection<String> getModifiedJavaFiles() {
		return modifiedJavaFiles;
	}
	
	public Collection<String> getRemovedJavaFiles() {
		return removedJavaFiles;
	}
	
	public void setCommitNumber(int commitNumber) {
		this.commitNumber = commitNumber;
	}
	
	public void setAddedJavaFiles(Deque<String> addedJavaFiles) {
		this.addedJavaFiles = addedJavaFiles;
	}
	
	public void setModifiedJavaFiles(Deque<String> modifiedJavaFiles) {
		this.modifiedJavaFiles = modifiedJavaFiles;
	}
	
	public void setRemovedJavaFiles(Deque<String> removedJavaFiles) {
		this.removedJavaFiles = removedJavaFiles;
	}
	
	public void setRenamedJavaFiles(Deque<String> renamedJavaFiles) {
		this.renamedJavaFiles = renamedJavaFiles;
	}
}
