package change.impact.graph;

public class Method {	
	private String id;
	private ChangeStatus status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String name;
	private String clazz;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public ChangeStatus getStatus() {
		return status;
	}

	public void setStatus(ChangeStatus status) {
		this.status = status;
	}

	

}
