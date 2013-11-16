package change.impact.graph.ast.parser;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeVisitor extends ASTVisitor {
	private String className;
	
	public boolean visit(TypeDeclaration node) {
		className = node.getName().toString();
		
		return super.visit(node);
	}
	
	public String getClassName() {
		return className;
	}
}
