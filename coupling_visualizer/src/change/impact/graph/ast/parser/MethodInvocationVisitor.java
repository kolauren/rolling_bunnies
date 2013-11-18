package change.impact.graph.ast.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.javatuples.Triplet;

public class MethodInvocationVisitor extends ASTVisitor {
	private List<Triplet<String, String, Integer>> methodInvocationPairs = new ArrayList<Triplet<String, String, Integer>>();
	
	public boolean visit(MethodInvocation node) {
		String methodInvokedName = node.getName().toString();
		String objectInvokedName;
		if (node.getExpression() != null) {
			objectInvokedName = node.getExpression().toString();
		} else {
			objectInvokedName = null;
		}
		
		int position = node.getStartPosition();
		
		methodInvocationPairs.add(new Triplet<String, String, Integer>(methodInvokedName, objectInvokedName, position));
		
		return super.visit(node);
	}
	
	public List<Triplet<String, String, Integer>> getMethodInvocations() {
		return methodInvocationPairs;
	}
}
