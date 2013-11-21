package change.impact.graph.commit;

import java.util.Map;

import com.google.common.collect.Maps;

public class Diff {
	//not provided in github diffs
	private String oldPath;
	private String newPath;
	private String oldTime;
	private String newTime;

	//stuff needed for change impact graph generation
	private String rawCodeURL;
	private Map<Integer, String> addedLines;
	private Map<Integer, String> removedLines;
	//uses line number from the old file
	private Map<Integer, String> unchangedLines;
	
	public Diff() {
		addedLines = Maps.newLinkedHashMap();
		removedLines = Maps.newLinkedHashMap();
		unchangedLines = Maps.newLinkedHashMap();
	}
	
	public String constructOldFile() {
		Map<Integer, String> lines = Maps.newLinkedHashMap();
		lines.putAll(removedLines);
		lines.putAll(unchangedLines);
		StringBuilder sb = new StringBuilder();
		
		for(Integer number : lines.keySet()) {
			sb.append(lines.get(number));
		}

		return sb.toString();
	}

	public void putAddedLine(int number, String line) {
		addedLines.put(number, line);
	}
	
	public void putRemovedLine(int number, String line) {
		removedLines.put(number, line);
	}
	
	public void putUnchangedLine(int number, String line) {
		unchangedLines.put(number, line);
	}
	
	public String getAddedLine(int number) {
		return addedLines.get(number);
	}
	
	public String getRemovedLine(int number) {
		return removedLines.get(number);
	}
	
	public String getUnchangedLine(int number) {
		return unchangedLines.get(number);
	}

	public String getOldTime() {
		return oldTime;
	}
	public void setOldTime(String oldTime) {
		this.oldTime = oldTime;
	}
	public String getNewTime() {
		return newTime;
	}
	public void setNewTime(String newTime) {
		this.newTime = newTime;
	}
	public String getOldPath() {
		return oldPath;
	}
	public void setOldPath(String oldPath) {
		this.oldPath = oldPath;
	}
	public String getNewPath() {
		return newPath;
	}
	public void setNewPath(String newPath) {
		this.newPath = newPath;
	}
	public String getRawCodeURL() {
		return rawCodeURL;
	}
	public void setRawCodeURL(String newCode) {
		this.rawCodeURL = newCode;
	}
	public Map<Integer, String> getAddedLines() {
		return addedLines;
	}
	public void setAddedLines(Map<Integer, String> addedLines) {
		this.addedLines = addedLines;
	}
	public Map<Integer, String> getRemovedLines() {
		return removedLines;
	}
	public void setRemovedLines(Map<Integer, String> removedLines) {
		this.removedLines = removedLines;
	}
}

