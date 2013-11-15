package change.impact.graph.driver;

import java.io.FileInputStream;
import java.io.IOException;

public class JavaParserTest {
	public static void main(String[] args) throws IOException {
		/*
		FileInputStream in = new FileInputStream("src/change/impact/graph/Method.java");
		
		CompilationUnit cu;
		try{
			cu = JavaParser.parse(in);
		} finally {
			in.close();
		}
		
		new MethodVisitor().visit(cu, null);
	}
	
	private static class MethodVisitor extends VoidVisitorAdapter {
		public void visit(MethodDeclaration n, Object arg) {
			System.out.println("method :"+n.getName());
			Type type = n.getType();
			if(type instanceof ClassOrInterfaceType) {
				ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType)type;
				System.out.println(classOrInterfaceType.getName());
			}
		}*/
	}
}
