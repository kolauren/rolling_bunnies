package change.impact.graph.ast.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.javatuples.Triplet;

public class SingleVariableDeclarationVisitor extends ASTVisitor {
	private List<Triplet<String, String, Integer>> variableDeclarationTriplet = new ArrayList<Triplet<String, String, Integer>>();
	
	public boolean visit(SingleVariableDeclaration node) {
		String varType = node.getType().toString();
		String varName = node.getName().toString();
		int position = node.getStartPosition();

		variableDeclarationTriplet.add(new Triplet<String, String, Integer>(varType, varName, position));
		
		return super.visit(node);
	}
	
	public List<Triplet<String, String, Integer>> getVariableTriplets() {
		return variableDeclarationTriplet;
	}
}
