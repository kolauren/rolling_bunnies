package change.impact.graph.ast.parser;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationDetails {
	private MethodInvocation methodInvocation;
	private String methodName;
	private String objectName;
	private int startLine;
	
	public MethodInvocationDetails(MethodInvocation methodInvocation, String methodName, String objectName, int startLine) {
		this.methodInvocation = methodInvocation;
		this.methodName = methodName;
		this.objectName = objectName;
		this.startLine = startLine;
	}

	public MethodInvocation getMethodInvocation() {
		return methodInvocation;
	}

	public void setMethodInvocation(MethodInvocation methodInvocation) {
		this.methodInvocation = methodInvocation;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}
}
