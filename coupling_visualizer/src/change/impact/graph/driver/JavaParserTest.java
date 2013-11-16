package change.impact.graph.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import change.impact.graph.ast.parser.ASTExplorer;
import change.impact.graph.ast.parser.MethodVisitor;

public class JavaParserTest {
	public static void main(String[] args) throws IOException, ExecutionException {
		File file = new File("C:\\Users\\Richard\\Desktop\\Code2.java");
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		
		FileInputStream inputStream = new FileInputStream(file);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		StringBuilder builder = new StringBuilder();
		String code;	
		
		while ((code = bufferedReader.readLine()) != null) {
			builder.append(code.trim() + " \\n ");
		}
		
		code = builder.toString();
		
		parser.setSource(code.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		final CompilationUnit cUnit = (CompilationUnit) parser.createAST(null);
		MethodVisitor visitor = new MethodVisitor();
		
		cUnit.accept(visitor);
		
		for (MethodDeclaration method : visitor.getMethods()) {
			System.out.println("Method Name: " + method.getName());
			System.out.println("Return Type: " + method.getReturnType2());
		}
		
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
