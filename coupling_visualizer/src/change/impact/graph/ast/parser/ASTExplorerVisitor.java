package change.impact.graph.ast.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class ASTExplorerVisitor extends ASTVisitor {
	private List<MethodDeclaration> methodDeclarations = new ArrayList<MethodDeclaration>();
	private List<MethodInvocationDetails> methodInvocations = new ArrayList<MethodInvocationDetails>();
	private List<VariableDetails> singleVariableDeclarations = new ArrayList<VariableDetails>();
	private String className;
	private List<VariableDetails> variableDeclarations = new ArrayList<VariableDetails>();
	
	/**
	 * Used to visit the FieldDeclaration nodes of the AST.
	 */
	public boolean visit(FieldDeclaration node) {
		return super.visit(node);
	}

	/**
	 * Used to visit the MethodDeclaration nodes of the AST.
	 */
	public boolean visit(MethodDeclaration node) {
		methodDeclarations.add(node);

		return super.visit(node);
	}

	public List<MethodDeclaration> getMethodDeclarations() {
		return methodDeclarations;
	}

	/**
	 * Used to visit the MethodInvocation nodes of the AST.
	 */
	public boolean visit(MethodInvocation node) {
		String methodInvokedName = node.getName().toString();
		String objectInvokedName;
		int startLine = node.getStartPosition();

		if (node.getExpression() != null) {
			objectInvokedName = node.getExpression().toString();
		} else {
			objectInvokedName = null;
		}
		
		MethodInvocationDetails details = new MethodInvocationDetails(node, methodInvokedName, objectInvokedName, startLine);
		methodInvocations.add(details);
		
		return super.visit(node);
	}
	
	public List<MethodInvocationDetails> getMethodInvocations() {
		return methodInvocations;
	}

	/**
	 * Used to visit the SingleVariableDeclaration nodes of the AST.
	 */
	public boolean visit(SingleVariableDeclaration node) {
		String variableType = node.getType().toString();
		String variableName = node.getName().toString();
		int startLine = node.getStartPosition();

		VariableDetails details = new VariableDetails(variableType, variableName, startLine);
		singleVariableDeclarations.add(details);
		
		return super.visit(node);
	}
	
	public List<VariableDetails> getSingleVariableDeclarations() {
		return singleVariableDeclarations;
	}

	/**
	 * Used to visit the TypeDeclaration nodes of the AST.
	 */
	public boolean visit(TypeDeclaration node) {
		className = node.getName().toString();
		
		return super.visit(node);
	}
	
	public String getClassName() {
		return className;
	}

	/**
	 * Used to visit the VariableDeclaration nodes of the AST.
	 */
	public boolean visit(VariableDeclarationStatement node) {
		String variableType = node.getType().toString();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
		String variableName = fragment.getName().toString();
		int startLine = node.getStartPosition();

		VariableDetails details = new VariableDetails(variableType, variableName, startLine);
		variableDeclarations.add(details);
		
		return super.visit(node);
	}

	public List<VariableDetails> getVariableDeclarations() {
		return variableDeclarations;
	}
}
