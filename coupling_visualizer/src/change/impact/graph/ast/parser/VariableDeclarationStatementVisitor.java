package change.impact.graph.ast.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.javatuples.Pair;
import org.javatuples.Triplet;

public class VariableDeclarationStatementVisitor extends ASTVisitor {
	private List<Triplet<String, String, Integer>> variableDeclarationTriplet = new ArrayList<Triplet<String, String, Integer>>();
	
	public boolean visit(VariableDeclarationStatement node) {
		String varType = node.getType().toString();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
		String varName = fragment.getName().toString();
		int position = node.getStartPosition();
		
		variableDeclarationTriplet.add(new Triplet<String, String, Integer>(varType, varName, position));
		return super.visit(node);
	}
	
	public List<Triplet<String, String, Integer>> getVariableTriplets() {
		return variableDeclarationTriplet;
	}
}
