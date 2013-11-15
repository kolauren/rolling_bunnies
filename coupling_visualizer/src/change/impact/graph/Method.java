package change.impact.graph;

public class Method {	
	private String id;
	private ChangeStatus status;


	private String name;
	private String clazz;
	private String packageName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	

}
