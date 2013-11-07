package commit.parser;

import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Maps;

public enum CommitFileStatus {
	MODIFIED("modified"),
	ADDED("added"),
	REMOVED("removed"),
	RENAMED("renamed");
	
	private static final Map<String, CommitFileStatus> statuses = Maps.newHashMap();
	
	static {
		for(CommitFileStatus status : CommitFileStatus.values()) {
			statuses.put(status.statusName, status);
		}
	}
	
	private String statusName;
	
	private CommitFileStatus(String statusName) {
		this.statusName = statusName;
	}
	
	public static CommitFileStatus fromString(String status) {
		if(statuses.containsKey(status))
			return statuses.get(status);
		else
			throw new NoSuchElementException("unexpected file status: "+status);
	}
	
	public String getName() {
		return statusName;
	}
}
