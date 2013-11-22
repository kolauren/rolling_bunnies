package change.impact.graph.ast.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.google.common.collect.Sets;

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
	 * If the Java file was removed, return a list of all the MethodDeclaration from the wrapper and map it all to null.
	 * If the Java file was renamed, grab currSourceLoc and use that to get the wrapper from wrapperMap.
	 * If the Java file did not change, use the wrapper's sourceLoc as the key to the wrapperMap.
	 * 
	 * 
	 * Return Cases:
	 * 
	 * method -> null				method was removed or renamed
	 * method -> {}					method was changed but contains no method invocations (perhaps removed in this commit)
	 * method -> {... }		 		method was changed and contains some method invocations
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
		List<String> allClasses = generateProjectClassNames(wrapperMap);

		// current AST that represents the same AST in the single wrapper
		ASTWrapper currWrapper = wrapperMap.get(wrapper.getSourceLoc());
		// class still exists in current state
		if (currWrapper != null) {

			currMethods = getMethodDeclarations(currWrapper);

			// For each of the line number provided, cross reference with all the MethodDeclarations and determine which MethodDeclaration it is under.
			for (int lineNumber : lineNumbers) {
				for (MethodDeclaration method : prevMethods) {
					int startLine = wrapper.getCompilationUnit().getLineNumber(method.getStartPosition());
					int endLine = wrapper.getCompilationUnit().getLineNumber(method.getStartPosition() + method.getLength());

					Set<Method> bodyMethodsInvoked = Sets.newHashSet(); 

					if (lineNumber >= startLine && lineNumber < endLine) {
						// Get the similar methods between the two ASTWrappers.
						MethodDeclaration mapMethod = getMatchingMethodDeclaration(currMethods, method);

						// if the current method declaration is null, it was removed/renamed
						if (mapMethod == null) {
							foundMethods.put(generateMethod(method, wrapper), null);
							continue;
						}

						// Grab every MethodInvocation node and extract information.
						Block block = mapMethod.getBody();
						ASTExplorerVisitor visitor = new ASTExplorerVisitor();
						block.accept(visitor);
						List<MethodInvocationDetails> methodInvocations = getActualMethodPositions(visitor.getMethodInvocations(), currWrapper);

						// Grab every VariableDeclaration, SingleVariableDeclaration from the MethodDeclaration body.
						List<VariableDetails> variableDeclarations = getActualVariablePositions(visitor.getVariableDeclarations(), currWrapper);
						List<VariableDetails> singleVariableDeclarations = getActualVariablePositions(visitor.getSingleVariableDeclarations(), currWrapper);

						// Combine both types of variables into one list.
						List<VariableDetails> variableDetails = new ArrayList<VariableDetails>();
						variableDetails.addAll(variableDeclarations);
						variableDetails.addAll(singleVariableDeclarations);

						// For each of the MethodInvocation, add in the methods that are part of the workspace into bodyMethodsInvoked.
						for (MethodInvocationDetails methodInvocation : methodInvocations) {
							String methodName = methodInvocation.getMethodName();
							String objectName = methodInvocation.getObjectName();

							String objectClassName = null;
							if (objectName == null) {
								// if null then we are calling a method in the same class
								objectClassName = currWrapper.getClassName();
							} else {
								VariableDetails variableDetail = findRelatedVariable(objectName, variableDetails);

								if(variableDetail != null)
									objectClassName = variableDetail.getVariableType();
							}


							if (objectClassName != null) {

								ASTWrapper wrapperMethodIsIn = getRelatedWrapper(allClasses, wrapperMap, objectClassName);

								if (wrapperMethodIsIn != null) {
									List<MethodDeclaration> methodDeclarations = getMethodDeclarations(wrapperMethodIsIn);

									if (methodDeclarations != null) {
										for (MethodDeclaration methodDeclaration : methodDeclarations) {
											if (methodDeclaration.getName().toString().equals(methodName)) {
												bodyMethodsInvoked.add(generateMethod(methodDeclaration, wrapperMethodIsIn));
											}
										}
									}
								}
							}
						}

						// For the MethodDeclaration found, transfer the information into a Method object.
						Method currentMethodDeclaration = generateMethod(mapMethod, currWrapper);

						// Store the <Method, HashSet<Method>>
						foundMethods.put(currentMethodDeclaration, bodyMethodsInvoked);

						// Stop iterating once MethodDeclaration found.
						break;
					}
				}
			}
		} else {
			// class was removed; indicate that every method is removed too by mapping to null
			for (MethodDeclaration method : prevMethods) {
				Method m = generateMethod(method, wrapper);
				foundMethods.put(m, null);
			}
		}

		return foundMethods;
	}
	
	/**
	 * Given a list of line numbers, determine which MethodDeclaration it is and grab all the MethodInvocations.
	 * If the Java file was removed, return a list of all the MethodDeclaration from the wrapper and map it all to null.
	 * If the Java file was renamed, grab currSourceLoc and use that to get the wrapper from wrapperMap.
	 * If the Java file did not change, use the wrapper's sourceLoc as the key to the wrapperMap.
	 * 
	 * Return Cases:
	 * 
	 * method -> null				method was removed or renamed
	 * method -> {}					method was changed but contains no method invocations (perhaps removed in this commit)
	 * method -> {... }		 		method was changed and contains some method invocations
	 * 
	 * This method will be taking a list of line numbers that determine all the line changes (added/removed) in a certain commit. 
	 * 
	 * @param lineNumbers Contains all the line numbers of changes (added/removed) in a specific commit.
	 * @param wrapperMap Mapping of all the classes in the project. Key is the source location which is paired with the ASTWrapper for the corresponding location.
	 * @param wrapper Contains the previous state of a code. This might not exist anymore if the commit involved the deletion of the class.
	 * @return
	 */
	public static Map<Method, Set<Method>> getMethodInvocationsB(List<Integer> lineNumbers, Map<String, ASTWrapper> wrapperMap, ASTWrapper wrapper) {
		// Get all the MethodDeclarations from the AST.
		List<MethodDeclaration> prevMethodDeclarations = getMethodDeclarations(wrapper);
		List<MethodDeclaration> currMethodDeclarations = new ArrayList<MethodDeclaration>();
		Map<Method, Set<Method>> foundMethods = new HashMap<Method, Set<Method>>();

		// Current AST that represents the same AST in the single wrapper.
		// Grab the same wrapper from wrapperMap using the sourceLoc as the key.
		ASTWrapper currWrapper = wrapperMap.get(wrapper.getSourceLoc());
		
		// Check to see if wrapperMap has the value for the provided sourceLoc key.
		// Path between [method -> null] or ( [method -> {}] and [method -> {...}] )
		// If currWrapper is null, it means that the sourceLoc of the single wrapper does not exist anymore.
		if (currWrapper != null) {
			// Since we found a wrapper with the exact same sourceLoc, get all the MethodDeclaration from currWrapper.
			currMethodDeclarations = getMethodDeclarations(currWrapper);
			
			// Go through each line and check it against the prevMethodDeclarations list to figure out which MethodDeclaration the line is part of.
			for (Integer lineNumber : lineNumbers) {
				for (MethodDeclaration methodDeclaration : prevMethodDeclarations) {
					// Get the start and end lines for the methodDeclaration against the previous wrapper.
					int startLine = wrapper.getCompilationUnit().getLineNumber(methodDeclaration.getStartPosition());
					int endLine = wrapper.getCompilationUnit().getLineNumber(methodDeclaration.getStartPosition() + methodDeclaration.getLength());
					
					// If the lineNumber sits the start and endLine, we are at the MethodDeclaration we are looking for.
					// Otherwise we don't care about it.
					// Determine which MethodDeclaration the lineNumber is part of.
					if (lineNumber >= startLine && lineNumber < endLine) {
						// Because we found which MethodDeclaration the lineNumber belongs to, we can initalize an empty Set<Method>.
						// This Set will be empty if the MethodDeclaration doesn't have any MethodInvocations in it.
						// Otherwise, the contents of this Set will be the MethodInvocations that are defined within the same project.
						Set<Method> methodBodyInvocations = Sets.newHashSet();
						
						// At this point, we've found what MethodDeclaration we should be working on.
						// Now we grab the exact same MethodDeclaration in the currMethodDeclarations list.
						MethodDeclaration methodDeclarationMatch = getMatchingMethodDeclaration(currMethodDeclarations, methodDeclaration);
						
						// If we can't find a matching MethodDeclaration from currMethodDeclarations (match comes back null), we return [method -> null]
						// This would be a case when a method was removed.
						if (methodDeclarationMatch == null) {
							Method method = generateMethod(methodDeclaration, wrapper);
							foundMethods.put(method, null);
						} else {
							// Since we found a matching MethodDeclaration from currMethodDeclarations, we should now go through the method body and grab all the MethodInvocations.
							
							// Get the method body.
							Block methodBody = methodDeclarationMatch.getBody();
							
							// Set a visitor to go through the body.
							ASTExplorerVisitor visitor = new ASTExplorerVisitor();
							methodBody.accept(visitor);
							
							// Grab all the MethodInvocations and Variables from the body and extract the needed information.
							List<MethodInvocationDetails> methodInvocationDetails = getActualMethodPositions(visitor.getMethodInvocations(), currWrapper);
							List<VariableDetails> variableDeclarationDetails = getActualVariablePositions(visitor.getVariableDeclarations(), currWrapper);
							List<VariableDetails> singleVariableDeclarationDetails = getActualVariablePositions(visitor.getSingleVariableDeclarations(), currWrapper);
							
							// Combine both types of variables into one list.
							List<VariableDetails> variableDetails = new ArrayList<VariableDetails>();
							variableDetails.addAll(variableDeclarationDetails);
							variableDetails.addAll(singleVariableDeclarationDetails);
							
							// With all the MethodInvocation and Variable details in hand, determine which methods should be put into foundMethods.
							// Only the methods that are declared in the project should be included in foundMethods.
							for (MethodInvocationDetails methodInvocationDetail : methodInvocationDetails) {
								// Get the methodInvocation's name and the object linked to it (eg Object.MethodName())
								// We need this because we want to figure out if the method is part of the project.
								String methodName = methodInvocationDetail.getMethodName();
								String objectName = methodInvocationDetail.getObjectName();
								
								// To figure out if the object is part of the project, we need to know what class type it is.
								// To do this, we have to get all the classes available in this project first.
								List<String> projectClasses = generateProjectClassNames(wrapperMap);
								
								// Now that we have all the names of the classes in the project, we can filter the variableDetails list to only contain variableTypes that are part of the project.
								List<VariableDetails> projectVariables = new ArrayList<VariableDetails>();
								
								// Go through each of the variableDetails and only add in the variableDetail that has a variable type that is part of the project..
								for (VariableDetails variableDetail : variableDetails) {
									// If projectClasses contains the variableType from this variableDetail, we store it into projectVariableDetails.
									// Otherwise, we ignore it.
									if (projectClasses.contains(variableDetail.getVariableType())) {
										projectVariables.add(variableDetail);
									}
								}
								
								// Now that we only have the variableTypes that we care about (the ones that are part of this project), we now filter the MethodInvocations for the same reason.
								// Get rid of all the MethodInvocations with objectName not being part of the variableTypes we care about.
								// Before this though, there are cases when objectName is null. This happens when the method itself is in the same class that we are currently working on.
								if (objectName == null) {
									// In this case, we already have the wrapper (currWrapper) we are looking for.
									// We still have to find the MethodDeclaration within currWrapper though.
									// To do this we get the list of MethodDeclarations from currWrapper.
									List<MethodDeclaration> methods = getMethodDeclarations(currWrapper);
									
									// We then go through all of the MethodDeclarations and find the matching method.
									for (MethodDeclaration method : methods) {
										// If the MethodDeclaration's name matched the methodName we got from the MethodInvocation, we have found the MethodDeclaration we are looking for.
										// Otherwise, we just move on to the next method
										// Note: Currently, this will be only matching the names so if there are two methods with the same name but different parameters, this will just match with whatever comes first.
										if (method.getName().toString().equals(methodName)) {
											// In a perfect world, at some point, we will always reach this part of the code.
											// This is because we already know that the method exists in the same source code we are working on based off the fact that objectName is null.
											methodBodyInvocations.add(generateMethod(method, currWrapper));
											break;
										}
									}
								} else {
									// This will be the case for all the other MethodInvocations.
									// We will need to figure out which MethodInvocation is of a class that is part of this project.
									// As for the ones that are not part of this project, we just ignore those.

									// Out of all the VariableDetails, we want to get the VariableDetail that has the same objectName we found.
									VariableDetails varDetails = findRelatedVariable(objectName, variableDetails);
									
									// With this VariableDetail, we now know what class type it is.
									String variableType = varDetails.getVariableType();
									
									// Using the variableType, we have to find the class from the list of projectClasses and then grab the wrapper for it.
									ASTWrapper relatedWrapper = getRelatedWrapper(projectClasses, wrapperMap, variableType);
									
									// TODO: Consider refactoring this. Similar code found on the if statement.
									// Now that we have the wrapper we want. We want to go through all the MethodDeclarations on it and find the matching methodName.
									List<MethodDeclaration> methodDeclarations = getMethodDeclarations(relatedWrapper);
									
									for (MethodDeclaration method : methodDeclarations) {
										// If the MethodDeclaration's name matched the methodName we got from the MethodInvocation, we have found the MethodDeclaration we are looking for.
										// Otherwise, we just move on to the next method
										// Note: Currently, this will be only matching the names so if there are two methods with the same name but different parameters, this will just match with whatever comes first.
										if (method.getName().toString().equals(methodName)) {
											// In a perfect world, at some point, we will always reach this part of the code.
											// This is because we already know that the method exists in the same source code we are working on based off the fact that objectName is null.
											methodBodyInvocations.add(generateMethod(method, relatedWrapper));
											break;
										}
									}
								}
							}
						}

						// At this point, we should have contents in methodBodyInvocations (or maybe an empty one if there are no MethodInvocations in the MethodDeclaration).
						// We should now store methodBodyInvocations into the Map and have the Method the MethodInvocations belongs to as the key.
						// First we generate the Method object from the methodDeclaration.
						Method currMethod = generateMethod(methodDeclarationMatch, currWrapper);
						
						// After that, we now have our key. We just have to put methodBodyInvocations into the map.
						foundMethods.put(currMethod, methodBodyInvocations);
						
						// We would have found the MethodDeclaration the line belongs to at this point so we should stop iterating.
						break;
					}
				}
			}
		} else {
			// Since the class does not exist in wrapperMap anymore, return [method -> null]
			
			// Get all the MethodDeclaration from prevMethodDeclarations and create a Method object for every single one.
			// For each of the Method created, put it in foundMethods and pair it with a null value.
			for (MethodDeclaration methodDeclaration : prevMethodDeclarations) {
				Method method = generateMethod(methodDeclaration, wrapper);
				
				foundMethods.put(method, null);
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
	private static List<String> generateProjectClassNames(Map<String, ASTWrapper> wrapperMap) {
		List<String> classes = new ArrayList<String>();

		for (String key : wrapperMap.keySet()) {
			ASTWrapper wrapper = wrapperMap.get(key);
			classes.add(wrapper.getClassName());
		}

		return classes;
	}

	private static ASTWrapper getRelatedWrapper(List<String> allClasses, Map<String, ASTWrapper> wrapperMap, String classToSearch) {
		for (String key : wrapperMap.keySet()) {
			ASTWrapper wrapper = wrapperMap.get(key);

			if (classToSearch.equals(wrapper.getClassName())) {
				return wrapper;
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
	private static MethodDeclaration getMatchingMethodDeclaration(List<MethodDeclaration> currMethods, MethodDeclaration method) {
		String methodName = method.getName().toString();
		List<String> methodParams = getParameterTypes(method);

		for (MethodDeclaration m : currMethods) {
			String currMethodName = m.getName().toString();
			List<String> currMethodParams = getParameterTypes(m);

			if (methodName.equals(currMethodName) && methodParams.size() == currMethodParams.size()) {
				return m;
				// refactor this later to account for parameter types
				//				for (int i = 0; i < methodParams.size(); i++) {
				//					if (!methodParams.get(i).equals(currMethodParams.get(i))) {
				//						break;
				//					}
				//					
				//					if (i == methodParams.size() - 1) {
				//						return m;
				//					}
				//				}
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
		String delimiter = "-";

		packageName = packageName == null ? "NOPACKAGENAME" : packageName;
		className = className == null ? "NOCLASSNAME" : className;

		id = packageName + delimiter + className + delimiter + methodName;

		for(String parameter : parameters) {
			id += delimiter+parameter;
		}

		return id;
	}
}
