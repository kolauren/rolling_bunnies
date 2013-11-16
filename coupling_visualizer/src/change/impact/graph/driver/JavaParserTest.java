package change.impact.graph.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import change.impact.graph.ast.parser.ASTExplorer;
import change.impact.graph.ast.parser.MethodVisitor;
import change.impact.graph.ast.parser.TypeVisitor;

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
		parser.setResolveBindings(true);
		
		final CompilationUnit cUnit = (CompilationUnit) parser.createAST(null);
		System.out.println();
		
		MethodVisitor visitor = new MethodVisitor();
		TypeVisitor visiter = new TypeVisitor();
		
		cUnit.accept(visiter);
		System.out.println("Class Name : " + visiter.getClassName());
		System.out.println();
		
		cUnit.accept(visitor);
		
		for (MethodDeclaration method : visitor.getMethods()) {
			System.out.println("Method Name: " + method.getName().toString());
			System.out.println("Return Type: " + method.getReturnType2().toString());
			System.out.print("Parameter  : ");
			
			for (Object param : method.parameters()) {
				String p = param.toString();
				String[] splitString = p.split("\\s+");
				System.out.print(splitString[0] + " ");
			}

			System.out.println();
			
			int start = cUnit.getLineNumber(method.getStartPosition()) - 1;
			int methodLength = method.getLength();
			int end = cUnit.getLineNumber(method.getStartPosition() + methodLength) - 1;
			
			System.out.println("Starting   : " + start);
			System.out.println("End        : " + end);
			System.out.println();
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
