package change.impact.graph.ast.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodDeclarationVisitor extends ASTVisitor {
	List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
	List<String> parameters = new ArrayList<String>();

	public boolean visit(MethodDeclaration node) {
		methods.add(node);

		return super.visit(node);
	}

	public List<MethodDeclaration> getMethods() {
		return methods;
	}

	public List<String> getParameters() {
		return parameters;
	}
}
