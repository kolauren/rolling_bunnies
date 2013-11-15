package change.impace.graph.ast.parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.stmt.BlockStmt;
import change.impact.graph.Method;

public class ASTparser {
	private static Method generateMethod(CompilationUnit ast, MethodDeclaration methodDec) {
		return null;
	}
	
	public static Set<Method> getMethodsCalledByMethod(CompilationUnit ast, Method method) {
		return null;
	}
	
	public static Method findMethodContainingLine(CompilationUnit ast, int lineNumber) throws IOException {
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
		}
		throw new IOException("couldn't find method containing line: "+lineNumber+" in types: "+Arrays.toString(ast.getTypes().toArray()));
	}
}
