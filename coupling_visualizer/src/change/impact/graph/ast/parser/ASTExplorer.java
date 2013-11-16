package change.impact.graph.ast.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

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
		URL url = new URL(urlString);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		InputStreamReader inputReader = new InputStreamReader(url.openStream());
        BufferedReader bufferedReader = new BufferedReader(inputReader);
		StringBuilder builder = new StringBuilder();
        String code;
        
		while ((code = bufferedReader.readLine()) != null) {
			builder.append(code.trim() + " \\n ");
		}
		
		bufferedReader.close();
		inputReader.close();

		code = builder.toString();
		
		parser.setSource(code.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		CompilationUnit cUnit = (CompilationUnit) parser.createAST(null);
		
		return new ASTWrapper(parser, cUnit);
	}
	
	/**
	 * 
	 * 
	 * @param method
	 * @param ast
	 * @return
	 * @throws JavaModelException
	 */
	public static boolean methodExists(Method m, ASTWrapper wrapper) throws JavaModelException {
		List<Method> methods = generateMethodsList(wrapper);
		
		for (Method method : methods) {
			if (method.getId().equals(m.getId())) {
				return true;
			}
		}
		
		return false;
	}
	
	//return the method calls found in the given list of lines.
	//note: i'm assuming you can parse a line individually and then cross check with the full ast
	//to get method information like class and package
	public static Map<Method, Set<Method>> getMethodsCalledByMethodsInLines(List<Integer> lineNumbers, ASTWrapper wrapper) {
		List<Method> methods = generateMethodsList(wrapper);
		
		
		return null;
	}

	private static List<Method> generateMethodsList(ASTWrapper wrapper) {
		List<Method> methods = new ArrayList<Method>();
		
		MethodVisitor methodVisitor = new MethodVisitor();
		TypeVisitor typeVisitor = new TypeVisitor();
		
		methodVisitor.visit(wrapper.getCompilationUnit());
		typeVisitor.visit(wrapper.getCompilationUnit());
		
		for (MethodDeclaration method : methodVisitor.getMethods()) {
			String packageName = wrapper.getCompilationUnit().getPackage().getName().getFullyQualifiedName();
			String className = typeVisitor.getClassName();
			String methodName = method.getName().toString();
			List<String> parameters = getParameterTypes(method.parameters());
			String id = generateMethodID(packageName, className, methodName, parameters);
			
			methods.add(new Method(id, packageName, className, methodName, parameters));
		}
		
		return methods;
	}
	
	private static List<String> getParameterTypes(List<String> parameters) {
		List<String> params = new ArrayList<String>();
		
		for (String param : parameters) {
			String[] splitString = param.split("\\s+");
			params.add(splitString[0]);
		}
		
		return params;
	}
	
	private static String generateMethodID(String packageName, String className, String methodName, List<String> parameters) {
		String id = packageName + " " + className + " " + methodName;
		
		for (String parameter : parameters) {
			id = id + " " + parameter;
		}
		
		return id;
	}
}
