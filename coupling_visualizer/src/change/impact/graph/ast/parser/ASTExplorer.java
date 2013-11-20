package change.impact.graph.ast.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import change.impact.graph.Method;

public class ASTExplorer {
	/**
	 * Wrap Eclipse AST objects into our AST class.
	 * 
	 * @param urlString The URL in String form.
	 * @return a new ASTWrapper
	 * @throws IOException
	 */
	public static ASTWrapper generateAST(String urlString, String sourceLoc) throws IOException {
		// Get the code from the URL provided and parse it into a String.
		String code = parseURLContents(urlString);
		
		// Initial parser setup.
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(code.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		String unitName = "";
		parser.setUnitName(unitName);
		
		// Create a CompilationUnit from the ASTParser.
		CompilationUnit cUnit = (CompilationUnit) parser.createAST(null);
		
		return new ASTWrapper(parser, cUnit, sourceLoc);
	}
	
	/**
	 * Given the URL, access the raw code and build a String off of it.
	 * 
	 * @param urlString
	 * @return
	 * @throws IOException
	 */
	private static String parseURLContents(String urlString) throws IOException {
		URL url = new URL(urlString);
		InputStreamReader inputReader = new InputStreamReader(url.openStream());
        BufferedReader bufferedReader = new BufferedReader(inputReader);
		StringBuilder builder = new StringBuilder();
        String code;
        
        // Read each line of the raw code and put it into the StringBuilder.
		while ((code = bufferedReader.readLine()) != null) {
			builder.append(code + " \n ");
		}
		
		bufferedReader.close();
		inputReader.close();

		return builder.toString();
	}
	
	/**
	 * Given a Method, check to see if the method exists in the source code.
	 * 
	 * @param method
	 * @param ast
	 * @return
	 * @throws JavaModelException
	 */
	public static boolean methodExists(Method m, ASTWrapper wrapper) throws JavaModelException {
		// Get a complete list of all the method declarations.
		List<Method> methods = generateMethodsList(wrapper);
		
		// Check the method being requested against all the method declarations.
		for (Method method : methods) {
			if (method.equals(m)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Generate a list of Methods from all the MethodDeclarations in the source code.
	 * 
	 * @param wrapper
	 * @return
	 */
	private static List<Method> generateMethodsList(ASTWrapper wrapper) {
		List<Method> methods = new ArrayList<Method>();
		
		// For each of the MethodDeclarations found, generate a Method object and  put it into the Methods list.
		for (MethodDeclaration method : getMethodDeclarations(wrapper)) {
			methods.add(generateMethod(method, wrapper));
		}
		
		return methods;
	}
	
	/**
	 * Given a list of line numbers, determine which MethodDeclaration it is and grab all the MethodInvocations.
	 * 
	 * @param lineNumbers
	 * @param wrapper
	 * @return
	 */
	public static Map<Method, Set<Method>> getMethodInvocations(List<Integer> lineNumbers, Map<String, ASTWrapper> wrapperMap, ASTWrapper wrapper, String currSourceLoc) {
		// Get all the MethodDeclarations from the AST.
		List<MethodDeclaration> prevMethods = getMethodDeclarations(wrapper);
		List<MethodDeclaration> currMethods = new ArrayList<MethodDeclaration>();
		Map<Method, Set<Method>> foundMethods = new HashMap<Method, Set<Method>>();
		
		if (wrapperMap.get(wrapper.getSourceLoc()) != null || currSourceLoc != null) {
			ASTWrapper wrapperToUse = wrapperMap.get(wrapper.getSourceLoc());
			
			if (wrapperMap.get(wrapper.getSourceLoc()) == null) {
				wrapperToUse = wrapperMap.get(currSourceLoc);
			}
			
			currMethods = getMethodDeclarations(wrapperToUse);
			
			// For each of the line number provided, cross reference with all the MethodDeclarations and determine which MethodDeclaration it is under.
			for (int lineNumber : lineNumbers) {
				for (MethodDeclaration method : prevMethods) {
					int startLine = wrapper.getCompilationUnit().getLineNumber(method.getStartPosition());
					int endLine = wrapper.getCompilationUnit().getLineNumber(method.getStartPosition() + method.getLength());
					Set<Method> bodyMethodsInvoked = null;
					
					if (lineNumber > startLine && lineNumber < endLine) {
						bodyMethodsInvoked = new HashSet<Method>();
						
						// Get the similar methods between the two ASTWrappers.
						MethodDeclaration mapMethod = getSameMethodDeclaration(currMethods, method);
						
						// If the map is null, then just skip this iteration.
						if (mapMethod == null) {
							break;
						}
						
						// Grab every MethodInvocation node and extract information.
						Block block = mapMethod.getBody();
						ASTExplorerVisitor visitor = new ASTExplorerVisitor();
						block.accept(visitor);
						List<MethodInvocationDetails> methodInvocations = getActualMethodPositions(visitor.getMethodInvocations(), wrapperToUse);
						
						// Grab every VariableDeclaration, SingleVariableDeclaration from the MethodDeclaration body.
						List<VariableDetails> variableDeclarations = getActualVariablePositions(visitor.getVariableDeclarations(), wrapperToUse);
						List<VariableDetails> singleVariableDeclarations = getActualVariablePositions(visitor.getSingleVariableDeclarations(), wrapperToUse);
						
						// Combine both types of variables into one list.
						List<VariableDetails> variableDetails = new ArrayList<VariableDetails>();
						variableDetails.addAll(variableDeclarations);
						variableDetails.addAll(singleVariableDeclarations);
						
						// For each of the MethodInvocation, add in the methods that are part of the workspace into bodyMethodsInvoked.
						for (MethodInvocationDetails methodInvocation : methodInvocations) {
							String methodName = methodInvocation.getMethodName();
							String objectName = methodInvocation.getObjectName();
							List<String> allClasses = generateClassNames(wrapperMap);
							
							if (objectName != null) {
								// Find all the variables that the MethodInvocation uses.
								VariableDetails variableDetail = findRelatedVariable(objectName, variableDetails);
								
								if (variableDetail != null) {
									List<MethodDeclaration> methodDeclarations = getRelatedMethodDeclarations(allClasses, wrapperMap, variableDetail.getVariableType());
									
									if (methodDeclarations != null) {
										for (MethodDeclaration methodDeclaration : methodDeclarations) {
											if (methodDeclaration.getName().toString().equals(methodName)) {
												bodyMethodsInvoked.add(generateMethod(methodDeclaration, wrapperToUse));
											}
										}
									}
								}
							}
						}
						
						// For the MethodDeclaration found, transfer the information into a Method object.
						Method currentMethodDeclaration = generateMethod(method, wrapperToUse);
						
						// Store the <Method, HashSet<Method>>
						foundMethods.put(currentMethodDeclaration, bodyMethodsInvoked);
						
						// Stop iterating once MethodDeclaration found.
						break;
					}
				}
			}
		} else {
			for (MethodDeclaration method : currMethods) {
				Method m = generateMethod(method, wrapper);
				foundMethods.put(m, null);
			}
		}
		
		return foundMethods;
	}

	/**
	 * Generate a list of all the classes in the Map of ASTWrappers.
	 * 
	 * @param wrapperMap
	 * @return
	 */
	private static List<String> generateClassNames(Map<String, ASTWrapper> wrapperMap) {
		List<String> classes = new ArrayList<String>();
		
		for (String key : wrapperMap.keySet()) {
			ASTWrapper wrapper = wrapperMap.get(key);
			classes.add(wrapper.getClassName());
		}
		
		return classes;
	}
	
	private static List<MethodDeclaration> getRelatedMethodDeclarations(List<String> allClasses, Map<String, ASTWrapper> wrapperMap, String classToSearch) {
		for (String key : wrapperMap.keySet()) {
			ASTWrapper wrapper = wrapperMap.get(key);
			
			if (classToSearch.equals(wrapper.getClassName())) {
				List<MethodDeclaration> methods = getMethodDeclarations(wrapper);
				
				return methods;
			}
		}
		
		return null;
	}

	/**
	 * Get all the matching MethodDeclaration from the list of MethodDeclarations.
	 * 
	 * @param currMethods
	 * @param method
	 * @return
	 */
	private static MethodDeclaration getSameMethodDeclaration(List<MethodDeclaration> currMethods, MethodDeclaration method) {
		for (MethodDeclaration m : currMethods) {
			if (method.equals(m)) {
				return m;
			}
		}
		
		return null;
	}

	/**
	 * Get all the MethodDeclarations given the ASTWrapper.
	 * 
	 * @param wrapper
	 * @return
	 */
	private static List<MethodDeclaration> getMethodDeclarations(ASTWrapper wrapper) {
		// Using the MethodDeclarationVisitor, visit all the MethodDeclaration nodes.
		ASTExplorerVisitor visitor = new ASTExplorerVisitor();
		//methodVisitor.visit(wrapper.getCompilationUnit());
		wrapper.getCompilationUnit().accept(visitor);
		
		return visitor.getMethodDeclarations();
	}
	
	/**
	 * Get the actual positions of the method.
	 * 
	 * @param list
	 * @param wrapper
	 * @return
	 */
	private static List<MethodInvocationDetails> getActualMethodPositions(List<MethodInvocationDetails> details, ASTWrapper wrapper) {
		List<MethodInvocationDetails> newDetails = new ArrayList<MethodInvocationDetails>();
		
		for (MethodInvocationDetails detail : details) {
			int actual = wrapper.getCompilationUnit().getLineNumber(detail.getStartLine());
			newDetails.add(new MethodInvocationDetails(detail.getMethodInvocation(), detail.getMethodName(), detail.getObjectName(), actual));
		}
		
		return newDetails;
	}
	
	/**
	 * Get the actual positions of the variables.
	 * 
	 * @param list
	 * @param wrapper
	 * @return
	 */
	private static List<VariableDetails> getActualVariablePositions(List<VariableDetails> details, ASTWrapper wrapper) {
		List<VariableDetails> newDetails = new ArrayList<VariableDetails>();
		
		for (VariableDetails detail : details) {
			int actual = wrapper.getCompilationUnit().getLineNumber(detail.getStartLine());
			newDetails.add(new VariableDetails(detail.getVariableType(), detail.getVariableName(), actual));
		}
		
		return newDetails;
	}
	
	/**
	 * Find the variable relation between the String and a Triplet.
	 * 
	 * @param objectName
	 * @param variableTriplets
	 * @return
	 */
	private static VariableDetails findRelatedVariable(String objectName, List<VariableDetails> variableDetails) {
		for (VariableDetails variableDetail : variableDetails) {
			if (objectName.equals(variableDetail.getVariableName())) {
				return variableDetail; 
			}
		}
		return null;
	}
	
	/**
	 * Generate a Method object from the provided MethodDeclaration and ASTWrapper.
	 * 
	 * @param method
	 * @param wrapper
	 * @return
	 */
	private static Method generateMethod(MethodDeclaration method, ASTWrapper wrapper) {
		String packageName = null;
		
		if (wrapper.getCompilationUnit().getPackage() != null) {
			packageName = wrapper.getCompilationUnit().getPackage().getName().getFullyQualifiedName();
		}
		
		String className = wrapper.getClassName();
		String methodName = method.getName().toString();
		List<String> parameters = getParameterTypes(method);
		String id = generateMethodID(packageName, className, methodName, parameters);
		int startLine = wrapper.getCompilationUnit().getLineNumber(method.getStartPosition());
		int endLine = wrapper.getCompilationUnit().getLineNumber(method.getStartPosition() + method.getLength());
		
		return new Method(id, packageName, className, methodName, parameters, startLine, endLine);
	}
	
	/**
	 * Get all the parameters given a MethodDeclaration.
	 * 
	 * @param method
	 * @return
	 */
	private static List<String> getParameterTypes(MethodDeclaration method) {
		@SuppressWarnings({ "rawtypes" })
		List parameters = method.parameters();
		List<String> params = new ArrayList<String>();
		
		// For each parameter, only take the class type and add it into the list of parameters.
		for (Object param : parameters) {
			String[] splitString = param.toString().split("\\s+");
			params.add(splitString[0]);
		}
		
		return params;
	}
	
	/**
	 * Given the various information for a method, generate the unique ID for a method.
	 * 
	 * @param packageName
	 * @param className
	 * @param methodName
	 * @param parameters
	 * @return
	 */
	private static String generateMethodID(String packageName, String className, String methodName, List<String> parameters) {
		String id = "";
		
		if (packageName != null) {
			id = packageName + " ";
		}
		
		if (className != null) {
			id = className + " ";
		}
		
		id = id + methodName;
		
		// String the various information into a String.
		for (String parameter : parameters) {
			id = id + " " + parameter;
		}
		
		return id;
	}
}
