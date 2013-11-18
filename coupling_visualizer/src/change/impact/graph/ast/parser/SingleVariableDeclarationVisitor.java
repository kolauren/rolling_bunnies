package change.impact.graph.ast.parser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.javatuples.Pair;

public class SingleVariableDeclarationVisitor extends ASTVisitor {
	private List<Pair<String, String>> variableDeclarationPair = new ArrayList<Pair<String, String>>();
	
	public boolean visit(SingleVariableDeclaration node) {
		String varType = node.getType().toString();
		String varName = node.getName().toString();

		variableDeclarationPair.add(new Pair<String, String>(varType, varName));
		
		return super.visit(node);
	}
	
	public List<Pair<String, String>> getVariablePairs() {
		return variableDeclarationPair;
	}
}
