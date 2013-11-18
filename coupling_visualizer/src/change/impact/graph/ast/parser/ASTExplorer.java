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
import org.eclipse.jdt.core.dom.MethodInvocation;

import change.impact.graph.ChangeStatus;
import change.impact.graph.Method;

public class ASTExplorer {	  
	/**
	 * Wrap Eclipse AST objects into our AST class.
	 * 
	 * @param urlString The URL in String form.
	 * @return a new ASTWrapper
	 * @throws IOException
	 */
	public static ASTWrapper generateAST(String urlString) throws IOException {
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
		
		return new ASTWrapper(parser, cUnit);
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
	public static Map<Method, Set<Method>> getMethodInvocations(List<Integer> lineNumbers, ASTWrapper wrapper) {
		// Get all the MethodDeclarations from the AST.
		List<MethodDeclaration> methods = getMethodDeclarations(wrapper);
		Map<Method, Set<Method>> foundMethods = new HashMap<Method, Set<Method>>();
		
		// For each of the line number provided, cross reference with all the MethodDeclarations and determine which MethodDeclaration it is.
		for (int lineNumber : lineNumbers) {
			for (MethodDeclaration method : methods) {
				int startLine = wrapper.getCompilationUnit().getLineNumber(method.getStartPosition());
				int endLine = wrapper.getCompilationUnit().getLineNumber(method.getStartPosition() + method.getLength());
				Set<Method> bodyMethodsInvoked = new HashSet<Method>();
				
				if (lineNumber > startLine && lineNumber < endLine) {
					// With the MethodDeclaration, visit every MethodInvocation node and extract the information.
					Block block = method.getBody();
					MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
					block.accept(methodInvocationVisitor);
					
					List<String> methodsInvoked = methodInvocationVisitor.getMethods();
					
					// Add all the MethodInvocation to the HashSet.
					for (String methodInvoked : methodsInvoked) {
						bodyMethodsInvoked.add(new Method("", "", "", methodInvoked, null, 0, 0));
					}
					
					// Stop iterating once MethodDeclaration found.
					break;
				}

				// For the MethodDeclaration found, transfer the information into a Method object.
				Method currentMethodDeclaration = generateMethod(method, wrapper);
				
				// Store the <Method, HashSet<Method>>
				foundMethods.put(currentMethodDeclaration, bodyMethodsInvoked);
			}
		}
		
		return foundMethods;
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
		methodVisitor.visit(wrapper.getCompilationUnit());
		
		return methodVisitor.getMethods();
	}
	
	/**
	 * Generate a Method object from the provided MethodDeclaration and ASTWrapper.
	 * 
	 * @param method
	 * @param wrapper
	 * @return
	 */
	private static Method generateMethod(MethodDeclaration method, ASTWrapper wrapper) {
		String packageName = wrapper.getCompilationUnit().getPackage().getName().getFullyQualifiedName();
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
		@SuppressWarnings("unchecked")
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
	 * TODO: Need to figure out where to get the various information from the MethodInvocation.
	 * 
	 * @param method
	 * @param wrapper
	 * @return
	 */
	private static Method generateMethod(MethodInvocation method, ASTWrapper wrapper) {
		return null;
	}
	
	/**
	 * TODO: Need to figure out how to get all the parameter types given a MethodInvocation.
	 * 
	 * @param method
	 * @return
	 */
	private static List<String> getParameterTypes(MethodInvocation method) {
		return null;
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
		String id = packageName + " " + className + " " + methodName;
		//String id = className + " " + methodName;
		
		// String the various information into a String.
		for (String parameter : parameters) {
			id = id + " " + parameter;
		}
		
		return id;
	}
}
