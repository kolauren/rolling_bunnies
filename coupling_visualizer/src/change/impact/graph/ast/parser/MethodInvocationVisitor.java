package change.impact.graph.ast.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationVisitor extends ASTVisitor {
	List<String> methods = new ArrayList<String>();
	
	public boolean visit(MethodInvocation node) {
		methods.add(node.getName().toString());
		
		return super.visit(node);
	}
	
	public List<String> getMethods() {
		return methods;
	}
}
