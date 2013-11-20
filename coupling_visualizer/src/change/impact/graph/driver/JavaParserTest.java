package change.impact.graph.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import change.impact.graph.ast.parser.ASTExplorerVisitor;
import change.impact.graph.ast.parser.MethodInvocationDetails;
import change.impact.graph.ast.parser.VariableDetails;

public class JavaParserTest {
	public static void main(String[] args) throws ExecutionException, IOException {

		astExplorerTest();

		/*
		 * FileInputStream in = new
		 * FileInputStream("src/change/impact/graph/Method.java");
		 * 
		 * CompilationUnit cu; try{ cu = JavaParser.parse(in); } finally {
		 * in.close(); }
		 * 
		 * new MethodVisitor().visit(cu, null); }
		 * 
		 * private static class MethodVisitor extends VoidVisitorAdapter {
		 * public void visit(MethodDeclaration n, Object arg) {
		 * System.out.println("method :"+n.getName()); Type type = n.getType();
		 * if(type instanceof ClassOrInterfaceType) { ClassOrInterfaceType
		 * classOrInterfaceType = (ClassOrInterfaceType)type;
		 * System.out.println(classOrInterfaceType.getName()); } }
		 */
	}

	private static void astExplorerTest() throws IOException {
		// Convert the Java file to a String.
		String code;
		code = parseJavaFile();
		// System.out.println(code);

		// Setup the parser and get the CompilationUnit back.
		final CompilationUnit cUnit = setupASTParser(code);
		// testBindings(cUnit);
		// runTrialErrorTest(cUnit);
		testASTExplorer(cUnit);
	}

	private static void testASTExplorer(CompilationUnit cUnit) {
		ASTExplorerVisitor visitor = new ASTExplorerVisitor();
		cUnit.accept(visitor);
		
		System.out.println("MethodDeclarations in " + visitor.getClassName() + ":");
		for (MethodDeclaration method : visitor.getMethodDeclarations()) {
			System.out.println(method.getName());
			
			visitor = new ASTExplorerVisitor();
			method.accept(visitor);
			
			System.out.println("\tMethodInvocations:");
			for (MethodInvocationDetails methodInvocation : visitor.getMethodInvocations()) {
				System.out.println("\t\t" + methodInvocation.getObjectName() + " " + methodInvocation.getMethodName() + " " + methodInvocation.getStartLine());
			}

			System.out.println();
			System.out.println("\tSingleVariableDeclarations:");
			for (VariableDetails variableDetail : visitor.getSingleVariableDeclarations()) {
				System.out.println("\t\t" + variableDetail.getVariableType() + " " + variableDetail.getVariableName() + " " + variableDetail.getStartLine());
			}

			System.out.println();
			System.out.println("\tVariableDeclarations:");
			for (VariableDetails variableDetail : visitor.getVariableDeclarations()) {
				System.out.println("\t\t" + variableDetail.getVariableType() + " " + variableDetail.getVariableName() + " " + variableDetail.getStartLine());
			}
			System.out.println();
			System.out.println("-----------------------------------------------------------------");
			System.out.println();
		}
	}

	private static String parseJavaFile() throws IOException {
		File file = new File("src/change/impact/graph/ast/parser/ASTExplorer.java");
		// File file = new File("E:\\Projects\\HelloWorld\\HelloWorld\\src\\HelloWorld.java");

		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		StringBuilder builder = new StringBuilder();
		String code;

		while ((code = bufferedReader.readLine()) != null) {
			builder.append(code + "\n ");
		}
		
		bufferedReader.close();

		return builder.toString();
	}
	
	private static CompilationUnit setupASTParser(String code) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		
		String unitName = "HelloWorld/HelloWorld.java";
		parser.setUnitName(unitName);
		
		// String[] sources = {"E:\\Projects\\HelloWorld\\HelloWorld"};
		// String[] sources = {"E:\\Projects\\CPSC 410\\coupling_visualizer"};
		// String[] classPaths = {"E:\\Projects\\CPSC 410\\coupling_visualizer\\libs\\eclipseAST\\org.eclipse.jdt.core-3.8.2.v20120814-155456.jar", "C:\\Program Files\\Java\\jre7\\lib\\resources.jar", "C:\\Program Files\\Java\\jre7\\lib\\rt.jar", "C:\\Program Files\\Java\\jre7\\lib\\jsse.jar", "C:\\Program Files\\Java\\jre7\\lib\\jce.jar", "C:\\Program Files\\Java\\jre7\\lib\\charsets.jar", "C:\\Program Files\\Java\\jre7\\lib\\jfr.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\access-bridge-64.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\dnsns.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\jaccess.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\localedata.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\sunec.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\sunjce_provide.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\sunmscapi.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\zipfs.jar"};
		
		parser.setSource(code.toCharArray());
		// parser.setEnvironment(classPaths, sources, new String[] { "UTF-8" }, true);
		parser.setEnvironment(null, null, null, true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setCompilerOptions(options);
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);


		return (CompilationUnit) parser.createAST(null);
	}
}
