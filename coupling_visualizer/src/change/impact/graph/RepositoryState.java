package change.impact.graph;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import change.impact.graph.ast.parser.ASTExplorer;
import change.impact.graph.ast.parser.ASTWrapper;
import change.impact.graph.commit.Commit;

public class RepositoryState {
	//filepath -> ast
	private Map<String,ASTWrapper> currentFiles;
	private Map<String,ASTWrapper> previousFiles;
	//method id -> [method_id]
	private Map<Method,Set<Method>> currentMethodDependencies;
	
	public RepositoryState() {
		currentFiles = Maps.newHashMap();
		previousFiles = Maps.newHashMap();
		currentMethodDependencies = Maps.newHashMap();
	}
	
	public Map<Method, Set<Method>> getCurrentMethodDependencies() {
		return currentMethodDependencies;
	}
	
	// updates the repository state with commit
	// returns a set of methods affected by the commit
	public Set<Method> update(Commit commit) throws MalformedURLException, IOException {
		updateFiles(commit);
		Set<Method> changedMethods = updateCurrentMethodDependencies(commit);
		return changedMethods;
	}
	
	private void updateFiles(Commit commit) throws MalformedURLException, IOException {
		//update renamed and moved files (Files are keyed by file path)
		for(String newFileName : commit.getRenamedJavaFiles()) {
			String oldFileName = commit.getOldFileName(newFileName);
			updateFilePath(newFileName, oldFileName);
			updateMethodsInMethodDependencies(newFileName, oldFileName);
		}
		
		//add new and modified files
		Set<String> addedModifiedRenamed = Sets.newHashSet();
		addedModifiedRenamed.addAll(commit.getAddedJavaFiles());
		addedModifiedRenamed.addAll(commit.getModifiedJavaFiles());
		addedModifiedRenamed.addAll(commit.getRenamedJavaFiles());

		for(String newFileName : addedModifiedRenamed) {
			ASTWrapper previousFile = null;
			previousFile = currentFiles.get(newFileName);
			//update the files
			previousFiles.put(newFileName, previousFile);
			String url = commit.getDiff(newFileName).getRawCodeURL();
			ASTWrapper currentFile = ASTExplorer.generateAST(url, newFileName);
			currentFiles.put(newFileName, currentFile);
		}

		for(String newFileName : commit.getRemovedJavaFiles()) {
			ASTWrapper previousFile = currentFiles.get(newFileName);
			previousFiles.put(newFileName, previousFile);
			currentFiles.remove(newFileName);
		}
	}
	
	private void updateFilePath(String newFileName, String oldFileName) {
		ASTWrapper previousFile = previousFiles.get(oldFileName);
		ASTWrapper currentFile = currentFiles.get(oldFileName);

		ASTWrapper futurepreviousFile = previousFiles.get(newFileName);

		// this commit is a merge; files already updated
		// in a previous commit
		if(null == previousFile && null == currentFile && futurepreviousFile != null)
			return;

		//file was new in previous commit
		if(previousFile != null)
			previousFile.setSourceLoc(newFileName);
		currentFile.setSourceLoc(newFileName);

		previousFiles.put(newFileName, previousFile);
		currentFiles.put(newFileName, currentFile);

		previousFiles.remove(oldFileName);
		currentFiles.remove(oldFileName);
	}
	
	private void updateMethodsInMethodDependencies(String newFileName, String oldFileName) {
		String[] idParts = generateIDreplacementParts(newFileName, oldFileName);
		String oldIDpart = idParts[0];
		String newIDpart = idParts[1];
		
		String newPackageName = newIDpart.split(Method.DELIMITER)[0];
		String newClassName = newIDpart.split(Method.DELIMITER)[1];
	
		//the keys are immutable so make a new map
		Map<Method, Set<Method>> renamedMethodDependencies = Maps.newHashMap();
		for(Method method : currentMethodDependencies.keySet()) {
			Set<Method> dependencies = currentMethodDependencies.get(method);
			for(Method dependency : dependencies) {
				// this should match the way method ids are generated by ASTExplorer
				if(dependency.getId().contains(oldIDpart)) {
					String newID = dependency.getId().replace(oldIDpart, newIDpart);
					dependency.setId(newID);
					dependency.setClassName(newClassName);
					dependency.setPackageName(newPackageName);
				} 
			}
			
			if(method.getId().contains(oldIDpart)) {
				String newID = method.getId().replace(oldIDpart, newIDpart);
				method.setId(newID);
				method.setClassName(newClassName);
				method.setPackageName(newPackageName);
			}
			renamedMethodDependencies.put(method, dependencies);
		}
		currentMethodDependencies = renamedMethodDependencies;
	}
	
	private String[] generateIDreplacementParts(String newFileName, String oldFileName) {
		String regex = "\\w*?/src/(?<packageName>[\\w/]*/)?(?<className>\\w+)\\.java";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(oldFileName);

		m.find();

		String oldPackageName = m.group("packageName");
		if(oldPackageName != null) {
			oldPackageName = oldPackageName.substring(0 , oldPackageName.length()-1);
			oldPackageName = oldPackageName.replace("/", Method.PACKAGE_DELIMITER);
		} else {
			oldPackageName = Method.NO_PACKAGE_NAME;
		}

		String oldClassName = m.group("className");

		m = p.matcher(newFileName);
		m.find();

		String newPackageName = m.group("packageName");
		if(newPackageName != null) {
			newPackageName = newPackageName.substring(0, newPackageName.length()-1);
			newPackageName = newPackageName.replace("/", Method.PACKAGE_DELIMITER);
		} else {
			newPackageName = Method.NO_PACKAGE_NAME;
		}

		String newClassName = m.group("className");

		String oldIDpart = oldPackageName+Method.DELIMITER+oldClassName;
		String newIDpart = newPackageName+Method.DELIMITER+newClassName;

		//TODO: this should be an object
		return new String[]{ oldIDpart, newIDpart };
	}

	// updates the currentAdjacencyList and currentMethods
	// returns a list of changed methods
	private Set<Method> updateCurrentMethodDependencies(Commit commit) throws IOException {

		Set<Method> changedMethods = Sets.newHashSet();

		for(String newFileName : commit.getDiffs().keySet()) {
			// find modified modified methods mapped to a set of its method invocations
			Map<Method, Set<Method>> changedMethodDependencies = Maps.newHashMap();
			
			// map added lines to modified methods -> {method invocations}
			Map<Integer,String> addedLines = commit.getDiff(newFileName).getAddedLines();
			List<Integer> addedLineNumbers = Lists.newArrayList();
			addedLineNumbers.addAll(addedLines.keySet());
			ASTWrapper currentFile = currentFiles.get(newFileName);
			if(!addedLineNumbers.isEmpty())
				changedMethodDependencies.putAll(ASTExplorer.getMethodInvocations(addedLineNumbers, currentFiles, currentFile));

			// map removed lines to modified methods -> {method invocations}
			Map<Integer,String> removedLines = commit.getDiff(newFileName).getRemovedLines();
			List<Integer> removedLineNumbers = Lists.newArrayList();
			removedLineNumbers.addAll(removedLines.keySet());
			ASTWrapper previousFile = previousFiles.get(newFileName);
			
			//if null, that means merge, and the file was added in the other branch
			if(previousFile == null)
				removedLines.clear();
			
			if(!removedLines.isEmpty())
				changedMethodDependencies.putAll(ASTExplorer.getMethodInvocations(removedLineNumbers, currentFiles, previousFile)); 

			// method -> null		method was removed or renamed (can't tell)
			// method -> {}			method was modified but has no method invocations in body
			// method -> {...}		method was modified and contains method invocations in body

			for(Method changedMethod : changedMethodDependencies.keySet()) {
				Set<Method> changedDependencies = changedMethodDependencies.get(changedMethod);
				if(changedDependencies == null) {
					currentMethodDependencies.remove(changedMethod);
					for(Method method : currentMethodDependencies.keySet()) {
						currentMethodDependencies.get(method).remove(changedMethod);
					}
				}
				else { 
					currentMethodDependencies.put(changedMethod, changedDependencies);
					changedMethods.add(changedMethod);
				}
			}
		}

		return changedMethods;
	}

}