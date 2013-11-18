package change.impact.graph.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.javatuples.Triplet;

import change.impact.graph.ast.parser.MethodDeclarationVisitor;
import change.impact.graph.ast.parser.MethodInvocationVisitor;
import change.impact.graph.ast.parser.TypeDeclarationVisitor;

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
		File file = new File("src/change/impact/graph/ast/parser/ASTExplorer.java");
		// File file = new File("E:\\Projects\\HelloWorld\\HelloWorld\\src\\HelloWorld.java");
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		FileInputStream inputStream = new FileInputStream(file);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		StringBuilder builder = new StringBuilder();
		String code;

		while ((code = bufferedReader.readLine()) != null) {
			builder.append(code + "\n ");
		}

		code = builder.toString();
		// System.out.println(code);

		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
		
		String unitName = "HelloWorld/HelloWorld.java";
		parser.setUnitName(unitName);
		
		// String[] sources = {"E:\\Projects\\HelloWorld\\HelloWorld"};
		String[] sources = {"E:\\Projects\\CPSC 410\\coupling_visualizer"};
		String[] classPaths = {"E:\\Projects\\CPSC 410\\coupling_visualizer\\libs\\eclipseAST\\org.eclipse.jdt.core-3.8.2.v20120814-155456.jar", "C:\\Program Files\\Java\\jre7\\lib\\resources.jar", "C:\\Program Files\\Java\\jre7\\lib\\rt.jar", "C:\\Program Files\\Java\\jre7\\lib\\jsse.jar", "C:\\Program Files\\Java\\jre7\\lib\\jce.jar", "C:\\Program Files\\Java\\jre7\\lib\\charsets.jar", "C:\\Program Files\\Java\\jre7\\lib\\jfr.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\access-bridge-64.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\dnsns.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\jaccess.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\localedata.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\sunec.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\sunjce_provide.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\sunmscapi.jar", "C:\\Program Files\\Java\\jre7\\lib\\ext\\zipfs.jar"};
		
		parser.setSource(code.toCharArray());
		// parser.setEnvironment(classPaths, sources, new String[] { "UTF-8" }, true);
		parser.setEnvironment(null, null, null, true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setCompilerOptions(options);
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);


		final CompilationUnit cUnit = (CompilationUnit) parser.createAST(null);
		//final CompilationUnit cUnit = PPACoreUtil.getCU(file, new PPAOptions());
		// testBindings(cUnit);

		MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
		TypeDeclarationVisitor visiter = new TypeDeclarationVisitor();

		cUnit.accept(visiter);
		System.out.println("Class Name : " + visiter.getClassName());
		System.out.println();

		cUnit.accept(visitor);

		for (MethodDeclaration method : visitor.getMethods()) {
			System.out.println("Method Name: " + method.getName().toString());
			if (method.getReturnType2() != null) {
				System.out.println("Return Type: " + method.getReturnType2().toString());
			}
			System.out.print("Parameter  : ");

			for (Object param : method.parameters()) {
				String p = param.toString();
				String[] splitString = p.split("\\s+");
				// System.out.print(splitString[0] + " ");
			}
			// System.out.println();

			int start = cUnit.getLineNumber(method.getStartPosition());
			int end = cUnit.getLineNumber(method.getStartPosition()
					+ method.getLength());

			// System.out.println("Starting   : " + start);
			// System.out.println("End        : " + end);
			// System.out.println();

			System.out.println("--- Inner Methods ---");
			Block block = method.getBody();
			MethodInvocationVisitor methodInvocationVisitor = new MethodInvocationVisitor();
			block.accept(methodInvocationVisitor);
			
			
			for (Triplet<String, String, Integer> triplet : methodInvocationVisitor.getMethodInvocations()) {
				// System.out.println("Triplets: ---------------------------------------------------------------");
				// System.out.println("Method: " + triplet.getValue0());
				// System.out.println("Object: " + triplet.getValue1());
				// System.out.println("Start : " + triplet.getValue2());
				// System.out.println("-------------------------------------------------------------------------");
			}
			
			block.accept(new ASTVisitor() {
				public boolean visit(SingleVariableDeclaration node) {
					System.out.println("Name: " + node.getType());
					return true;
				}
			});
			
			System.out.println("---------------------");
			System.out.println();
		}
	}

	private static void testBindings(CompilationUnit cUnit) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) cUnit.types().get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		System.out.println("TypeDeclaration : " + typeBinding);
		System.out.println("----------------------------");
		System.out.println();
		System.out.println("TypeDeclaration : " + typeBinding.getJavaElement());
		System.out.println("----------------------------");
		System.out.println();
		System.out.println("TypeDeclaration : "
				+ typeBinding.getQualifiedName());
		System.out.println("----------------------------");
		System.out.println();
		System.out.println("TypeDeclaration : "
				+ typeBinding.getQualifiedName());
		System.out.println("----------------------------");
		System.out.println();

		MethodDeclaration methodDeclaration = (MethodDeclaration) typeDeclaration
				.bodyDeclarations().get(1);
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		System.out.println("MethodDeclaration : " + methodBinding);
		System.out.println("----------------------------");
		System.out.println();
		System.out.println("MethodDeclaration : "
				+ methodBinding.getJavaElement());
		System.out.println("----------------------------");
		System.out.println();

		Block body = methodDeclaration.getBody();
		ExpressionStatement expression = (ExpressionStatement) body
				.statements().get(0);
		Expression e1 = expression.getExpression();
		System.out.println("Expression : " + e1.resolveTypeBinding());
		System.out.println("----------------------------");
		System.out.println();
	}
}
