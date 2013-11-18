package change.impact.graph.ast.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.javatuples.Pair;

public class VariableDeclarationStatementVisitor extends ASTVisitor {
	private List<Pair<String, String>> variableDeclarationPair = new ArrayList<Pair<String, String>>();
	
	public boolean visit(VariableDeclarationStatement node) {
		String varType = node.getType().toString();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0);
		String varName = fragment.getName().toString();
		
		variableDeclarationPair.add(new Pair<String, String>(varType, varName));
		
		return super.visit(node);
	}
	
	public List<Pair<String, String>> getVariablePairs() {
		return variableDeclarationPair;
	}
}
