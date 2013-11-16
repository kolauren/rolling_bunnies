package change.impact.graph.ast.parser;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;

import change.impact.graph.Method;

public class ASTExplorer {	  
	//wrap Eclipse AST objects into our AST class
	public static ASTWrapper generateAST(String url) {
		return null;
	}
	
	public static boolean methodExists(Method method, ASTWrapper ast) throws JavaModelException {
		return false;
	}

	private static Method generateMethod(Method method, ASTWrapper ast) {
		return null;
	}
	
	public static Method findMethodContainingLine(int lineNumber, ASTWrapper ast) throws IOException {
		/*
		List<TypeDeclaration> types = ast.getTypes();
		
		for(TypeDeclaration type : types) {
			List<BodyDeclaration> members = type.getMembers();
			for(BodyDeclaration member : members) {
				if(member instanceof MethodDeclaration) {
					MethodDeclaration method = (MethodDeclaration) member;
					BlockStmt body = method.getBody();
					boolean inBody = body.getBeginLine() <= lineNumber && body.getEndLine() >= lineNumber;
					boolean inDeclaration = method.getBeginLine() == lineNumber;

					if(inDeclaration || inBody) {
						return generateMethod(ast, method);
					}
				}
			}
		}*/
		//line not part of a method
		return null;
	}
	
	//return the method calls found in the given list of lines.
	//note: i'm assuming you can parse a line individually and then cross check with the full ast
	//to get method information like class and package
	public static Map<Method, Set<Method>> getMethodsCalledByMethodsInLines(Map<Integer,String> lines, ASTWrapper ast) {
		return null;
	}
}
