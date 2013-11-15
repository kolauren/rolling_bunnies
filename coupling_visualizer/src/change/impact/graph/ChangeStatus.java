package change.impact.graph;

public enum ChangeStatus {
	CHANGED("changed"),
	AFFECTED("affected"),
	UNAFFECTED("unaffected");
	
	private String statusName;
	
	ChangeStatus(String statusName) {
		this.statusName = statusName;
	}
	
	public String getStatusName() {
		return statusName;
	}
}
