package change.impact.graph.ast.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

import change.impact.graph.Method;

public class ASTparser {
	public static boolean methodExists(Method method, CompilationUnit ast) {
		return false;
	}

	private static Method generateMethod(CompilationUnit ast, MethodDeclaration methodDec) {
		return null;
	}
	
	public static Set<Method> getMethodsCalledByMethod(Method method, CompilationUnit ast) {
		return null;
	}
	
	public static Method findMethodContainingLine(CompilationUnit ast, int lineNumber) throws IOException {
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
	public static Set<Method> getMethodsCalledByMethodInLines(Method method, List<String> lines, CompilationUnit ast) {
		return null;
	}
}
