package change.impact.graph;

public class CommitMethod {
	private Method method;
	private ChangeStatus status;
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public ChangeStatus getStatus() {
		return status;
	}
	public void setStatus(ChangeStatus status) {
		this.status = status;
	}
}
