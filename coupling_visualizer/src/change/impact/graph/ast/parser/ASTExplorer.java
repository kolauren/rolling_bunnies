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
import org.javatuples.Triplet;

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
	public static Map<Method, Set<Method>> getMethodInvocations(List<Integer> lineNumbers, Map<String, ASTWrapper> wrapperMap, ASTWrapper wrapper) {
		// Get all the MethodDeclarations from the AST.
		List<MethodDeclaration> prevMethods = getMethodDeclarations(wrapper);
		List<MethodDeclaration> currMethods = new ArrayList<MethodDeclaration>();
		Map<Method, Set<Method>> foundMethods = new HashMap<Method, Set<Method>>();
		
		if (wrapperMap.get(wrapper.getSourceLoc()) != null ) {
			currMethods = getMethodDeclarations(wrapperMap.get(wrapper.getSourceLoc()));
			
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
						MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
						block.accept(methodInvocationVisitor);
						List<Triplet<String, String, Integer>> methodInvocationTriplets = getActualPositions(methodInvocationVisitor.getMethodInvocations(), wrapper);
						
						// Grab every VariableDeclaration, SingleVariableDeclaration from the MethodDeclaration body.
						VariableDeclarationStatementVisitor variableDeclarationStatementVisitor = new VariableDeclarationStatementVisitor();
						block.accept(variableDeclarationStatementVisitor);
						SingleVariableDeclarationVisitor singleVariableDeclarationVisitor = new SingleVariableDeclarationVisitor();
						block.accept(singleVariableDeclarationVisitor);
						List<Triplet<String, String, Integer>> variableDeclarationTriplets = getActualPositions(variableDeclarationStatementVisitor.getVariableTriplets(), wrapper);
						List<Triplet<String, String, Integer>> singleVariableDeclarationTriplets = getActualPositions(singleVariableDeclarationVisitor.getVariableTriplets(), wrapper);
						
						// Combine both types of variables into one list.
						List<Triplet<String, String, Integer>> variableTriplets = new ArrayList<Triplet<String, String, Integer>>();
						variableTriplets.addAll(variableDeclarationTriplets);
						variableTriplets.addAll(singleVariableDeclarationTriplets);
						
						// For each of the MethodInvocation, add in the methods that are part of the workspace into bodyMethodsInvoked.
						for (Triplet<String, String, Integer> triplet : methodInvocationTriplets) {
							String methodName = triplet.getValue0();
							String objectName = triplet.getValue1();
							int position = triplet.getValue2();
							List<String> allClasses = generateClasses(wrapperMap);
							
							if (objectName == null) {
								String packageName = wrapper.getCompilationUnit().getPackage().getName().getFullyQualifiedName();
								bodyMethodsInvoked.add(generateMethod(packageName, wrapper.getClassName(), methodName, position));
							} else {
								// Find all the variables that the MethodInvocation uses.
								Triplet<String, String, Integer> varTriplet = findRelatedVariable(objectName, variableTriplets);
								
								// If the className from the triplet is in the list of classes, add it in to the list too.
								if (varTriplet != null && allClasses.contains(varTriplet.getValue0())) {
									bodyMethodsInvoked.add(generateMethod(null, varTriplet.getValue0(), methodName, position));
								}
							}
						}
						
						// For the MethodDeclaration found, transfer the information into a Method object.
						Method currentMethodDeclaration = generateMethod(method, wrapper);
						
						// Store the <Method, HashSet<Method>>
						foundMethods.put(currentMethodDeclaration, bodyMethodsInvoked);
						
						// Stop iterating once MethodDeclaration found.
						break;
					}
				}
			}
		} else {
			for (MethodDeclaration method : prevMethods) {
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
	private static List<String> generateClasses(Map<String, ASTWrapper> wrapperMap) {
		List<String> classes = new ArrayList<String>();
		
		for (String key : wrapperMap.keySet()) {
			ASTWrapper wrapper = wrapperMap.get(key);
			classes.add(wrapper.getClassName());
		}
		
		return classes;
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
		MethodDeclarationVisitor methodVisitor = new MethodDeclarationVisitor();
		//methodVisitor.visit(wrapper.getCompilationUnit());
		wrapper.getCompilationUnit().accept(methodVisitor);
		
		return methodVisitor.getMethods();
	}
	
	/**
	 * Get the actual positions of the method.
	 * 
	 * @param triplets
	 * @param wrapper
	 * @return
	 */
	private static List<Triplet<String, String, Integer>> getActualPositions(List<Triplet<String, String, Integer>> triplets, ASTWrapper wrapper) {
		List<Triplet<String, String, Integer>> newTriplets = new ArrayList<Triplet<String, String, Integer>>();
		
		for (Triplet<String, String, Integer> triplet : triplets) {
			int actual = wrapper.getCompilationUnit().getLineNumber(triplet.getValue2());
			newTriplets.add(new Triplet<String, String, Integer>(triplet.getValue0(), triplet.getValue1(), actual));
		}
		
		return newTriplets;
	}
	
	/**
	 * Find the variable relation between the String and a Triplet.
	 * 
	 * @param objectName
	 * @param variableTriplets
	 * @return
	 */
	private static Triplet<String, String, Integer> findRelatedVariable(String objectName, List<Triplet<String, String, Integer>> variableTriplets) {
		for (Triplet<String, String, Integer> triplet : variableTriplets) {
			if (objectName.equals(triplet.getValue1())) {
				return triplet; 
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
	 * TODO: Need to figure out where to get the various information from the MethodInvocation.
	 * 
	 * @param method
	 * @param wrapper
	 * @return
	 */
	private static Method generateMethod(String packageName, String className, String methodName, int startLine) {
		List<String> parameters = new ArrayList<String>();
		String id = generateMethodID(packageName, className, methodName, parameters);
		
		return new Method(id, packageName, className, methodName, null, startLine, 0);
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
