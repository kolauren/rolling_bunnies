package change.impact.graph.ast.parser;

public class VariableDetails {
	private String variableType;
	private String variableName;
	private int startLine;
	
	public VariableDetails(String variableType, String variableName, int startLine) {
		this.variableType = variableType;
		this.variableName = variableName;
		this.startLine = startLine;
	}

	public String getVariableType() {
		return variableType;
	}

	public void setVariableType(String variableType) {
		this.variableType = variableType;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}
}
