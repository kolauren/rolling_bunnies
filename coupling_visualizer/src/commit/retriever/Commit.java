package commit.retriever;

import java.util.Collection;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


//commit data we're interested in for our visualization
public class Commit {
	private String sha;
	//please don't rename these. thx.
	private Collection<String> addedJavaFiles;
	private Collection<String> removedJavaFiles;
	private Collection<String> modifiedJavaFiles;
	private Map<String, String> renamedJavaFiles;

	private Map<String, Diff> diffs;

	public Commit() {
		addedJavaFiles = Lists.newArrayList();
		removedJavaFiles = Lists.newArrayList();
		modifiedJavaFiles = Lists.newArrayList();
		renamedJavaFiles = Maps.newHashMap();
		diffs = Maps.newHashMap();
	}

	public Map<String,String> getRenamedJavaFiles() {
		return renamedJavaFiles;
	}

	public String getSha() {
		return sha;
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

	public void setSha(String sha) {
		this.sha = sha;
	}

	public void setAddedJavaFiles(Collection<String> addedJavaFiles) {
		this.addedJavaFiles = addedJavaFiles;
	}

	public void setModifiedJavaFiles(Collection<String> modifiedJavaFiles) {
		this.modifiedJavaFiles = modifiedJavaFiles;
	}

	public void setRemovedJavaFiles(Collection<String> removedJavaFiles) {
		this.removedJavaFiles = removedJavaFiles;
	}

	public void setRenamedJavaFiles(Map<String,String> renamedJavaFiles) {
		this.renamedJavaFiles = renamedJavaFiles;
	}

	public void addJavaFile(CommitFileStatus status, String ... file) {
		switch(status) {
		case ADDED:
			addedJavaFiles.add(file[0]);
			break;
		case MODIFIED:
			modifiedJavaFiles.add(file[0]);
			break;
		case REMOVED:
			removedJavaFiles.add(file[0]);
			break;
		case RENAMED:
			renamedJavaFiles.put(file[0], file[1]);
			break;
		}
	}

	public void addDiff(String filename, Diff diff) {
		diffs.put(filename, diff);
	}

	public boolean isEmpty() {
		if(addedJavaFiles.isEmpty() && removedJavaFiles.isEmpty() && modifiedJavaFiles.isEmpty() && renamedJavaFiles.isEmpty())
			return true;
		else 
			return false;
	}

	public void removeJavaFile(String filename) {
		addedJavaFiles.remove(filename);
		removedJavaFiles.remove(filename);
		modifiedJavaFiles.remove(filename);
		renamedJavaFiles.remove(filename);
		diffs.remove(filename);
	}

	public Map<String, Diff> getDiffs() {
		return diffs;
	}
}
