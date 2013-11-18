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
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import change.impact.graph.ast.parser.MethodDeclarationVisitor;
import change.impact.graph.ast.parser.TypeDeclarationVisitor;

public class JavaParserTest {
	public static void main(String[] args) throws ExecutionException,
			IOException {

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
		File file = new File(
				"src/change/impact/graph/ast/parser/ASTExplorer.java");
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

		parser.setSource(code.toCharArray());
		String[] sourcePathEntries = { "src/change/impact/graph/ast/parser/ASTExplorer.java" };
		String[] encoding = { "UTF-8" };
		parser.setEnvironment(null, sourcePathEntries, encoding, true);
		parser.setResolveBindings(true);
		//parser.setStatementsRecovery(true);
		//parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		String unitName = "change.impact.graph.ast.parser.ASTExplorer";
		parser.setUnitName(unitName);

		final CompilationUnit cUnit = (CompilationUnit) parser.createAST(null);

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
				System.out.println("Return Type: "
						+ method.getReturnType2().toString());
			}
			System.out.print("Parameter  : ");

			for (Object param : method.parameters()) {
				String p = param.toString();
				String[] splitString = p.split("\\s+");
				System.out.print(splitString[0] + " ");
			}
			System.out.println();

			int start = cUnit.getLineNumber(method.getStartPosition());
			int end = cUnit.getLineNumber(method.getStartPosition()
					+ method.getLength());

			System.out.println("Starting   : " + start);
			System.out.println("End        : " + end);
			System.out.println();

			System.out.println("--- Inner Methods ---");
			Block block = method.getBody();
			block.accept(new ASTVisitor() {
				public boolean visit(MethodInvocation node) {
					// System.out.println("Expression Binding: " +
					// node.resolveTypeBinding());
					// System.out.println("Expression: " +
					// node.getExpression());
					// System.out.println(node.getExpression());
					System.out.println("Name: " + node.getName());
					
					if (node.getName().resolveBinding() != null) {
						System.out.println(node.resolveMethodBinding());
					}
					System.out.println();

					return true;
				}
			});
			
			block.accept(new ASTVisitor() {
				public boolean visit(ClassInstanceCreation node) {
					System.out.println(node.toString());
					System.out.println();
					
					return true;
				}
			});
			System.out.println("---------------------");
		}
	}

	private static void testBindings(CompilationUnit cUnit) {
		TypeDeclaration typeDeclaration = (TypeDeclaration) cUnit.types()
				.get(0);
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
