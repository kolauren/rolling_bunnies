package change.impact.graph.ast.parser;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ClassInstanceCreationVisitor extends ASTVisitor {
	
	public boolean visit(ClassInstanceCreation node) {
		return super.visit(node);
	}
}
