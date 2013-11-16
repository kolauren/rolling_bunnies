package change.impact.graph.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.eval.IEvaluationContext;

import change.impact.graph.ast.parser.MethodVisitor;
import change.impact.graph.ast.parser.TypeVisitor;

public class JavaParserTest {
	public static void main(String[] args) throws IOException, ExecutionException {
		File file = new File("C:\\Users\\Richard\\Desktop\\MethodVisitor.java");
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		
		FileInputStream inputStream = new FileInputStream(file);
		BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
		StringBuilder builder = new StringBuilder();
		String code;	
		
		while ((code = bufferedReader.readLine()) != null) {
			builder.append(code + "\\n ");
		}
		
		code = builder.toString();
		System.out.println("----------------------");
		System.out.println(code);
		System.out.println("----------------------");
		
		/**code = "package p;\n" + 
				"public class X {\n" + 
				"	public int i;\n" + 
				"	public static void main(String[] args) {\n" + 
				"		int length = args.length;\n" + 
				"		System.out.println(length);\n" + 
				"	}\n" + 
				"}";
		*/
		parser.setSource(code.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		String unitName = "change.impact.graph.ast.parser/MethodVisitor.java";
		parser.setUnitName(unitName);

		final CompilationUnit cUnit = (CompilationUnit) parser.createAST(null);
		
		// testBindings(cUnit);
		
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
			System.out.println("cUnit Length: " + cUnit.getLength());
			System.out.println("method Start: " + method.getStartPosition());
			System.out.println("No Idea HURR: " + cUnit.getLineNumber(method.getStartPosition()));
			
			int start = cUnit.getLength() - method.getStartPosition();
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
	
	private static void testBindings(CompilationUnit cUnit) {
		TypeDeclaration typeDeclaration  = (TypeDeclaration) cUnit.types().get(0);
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		System.out.println("TypeDeclaration : " + typeBinding);
		System.out.println("----------------------------");
		System.out.println();
		System.out.println("TypeDeclaration : " + typeBinding.getJavaElement());
		System.out.println("----------------------------");
		System.out.println();
		System.out.println("TypeDeclaration : " + typeBinding.getQualifiedName());
		System.out.println("----------------------------");
		System.out.println();
		System.out.println("TypeDeclaration : " + typeBinding.getQualifiedName());
		System.out.println("----------------------------");
		System.out.println();
		
		MethodDeclaration methodDeclaration = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(1);
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		System.out.println("MethodDeclaration : " + methodBinding);
		System.out.println("----------------------------");
		System.out.println();
		System.out.println("MethodDeclaration : " + methodBinding.getJavaElement());
		System.out.println("----------------------------");
		System.out.println();
		
		Block body = methodDeclaration.getBody();
		ExpressionStatement expression = (ExpressionStatement) body.statements().get(0);
		Expression e1 = expression.getExpression();
		System.out.println("Expression : " + e1.resolveTypeBinding());
		System.out.println("----------------------------");
		System.out.println();
	}
}
