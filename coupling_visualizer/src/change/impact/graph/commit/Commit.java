package change.impact.graph.commit;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


//commit data we're interested in for our visualization
public class Commit {
	private String sha;

	private Collection<String> addedJavaFiles;
	private Collection<String> removedJavaFiles;
	private Collection<String> modifiedJavaFiles;
	private Collection<String> renamedJavaFiles;

	private Map<String, Diff> diffs;
	
	private Set<String> renamedProject;
	private Map<String, String> renamedMap;

	public Commit() {
		addedJavaFiles = Lists.newArrayList();
		removedJavaFiles = Lists.newArrayList();
		modifiedJavaFiles = Lists.newArrayList();
		renamedJavaFiles = Lists.newArrayList();

		diffs = Maps.newHashMap();
		
		renamedProject = Sets.newHashSet();
		renamedMap = Maps.newHashMap();
	}

	public Collection<String> getRenamedJavaFiles() {
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

	public void setRenamedJavaFiles(Collection<String> renamedJavaFiles) {
		this.renamedJavaFiles = renamedJavaFiles;
	}

	public void addJavaFile(CommitFileStatus status, String filename) {
		switch(status) {
		case ADDED:
			addedJavaFiles.add(filename);
			break;
		case MODIFIED:
			modifiedJavaFiles.add(filename);
			break;
		case REMOVED:
			removedJavaFiles.add(filename);
			break;
		case RENAMED:
			renamedJavaFiles.add(filename);
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
	
	public Diff getDiff(String clazz) {
		return diffs.get(clazz);
	}
	
	public void addRenamedProject(String filename) {
		renamedProject.add(filename);
	}
	
	public Set<String> getRenamedProject() {
		return renamedProject;
	}
	
	//returns all classes that had a diff
	public Collection<String> getClassesWithDiff() {
		return diffs.keySet();
	}
	
	public void addOldFileName(String newFileName, String oldFileName) {
		renamedMap.put(newFileName, oldFileName);
	}
	
	//returns null if the file was not renamed
	public String getOldFileName(String newFileName) {
		return renamedMap.get(newFileName);
	}
	
	public boolean isProjectRenamed(String filename) {
		return renamedProject.contains(filename);
	}
	
	//project directory changed, package changed, class changed
	//note that modified can be renamed
	public boolean isFileRenamed(String newFileName) {
		return getOldFileName(newFileName) != null;
	}
}
