package change.impact.graph.ast.parser;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;

import change.impact.graph.Method;

public class ASTparser {	  
	//wrap Eclipse AST objects into our AST class
	public static AST generateAST(String url) {
		return null;
	}
	
	public static boolean methodExists(Method method, AST ast) throws JavaModelException {
		/*IType[] allTypes = cUnit.getAllTypes();
		
		for (IType type : allTypes) {
			IMethod[] methods = type.getMethods();
			
			for (IMethod m : methods) {
				System.out.println("Method name " + m.getElementName());
			    System.out.println("Signature " + m.getSignature());
			    System.out.println("Return Type " + m=getReturnType());
			}
		}*/
		
		MethodDeclaration m; 
		
		return false;
	}

	private static Method generateMethod(Method method, AST ast) {
		return null;
	}
	
	public static Method findMethodContainingLine(int lineNumber, AST ast) throws IOException {
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
	public static Map<Method, Set<Method>> getMethodsCalledByMethodsInLines(Map<Integer,String> lines, AST ast) {
		return null;
	}
}
