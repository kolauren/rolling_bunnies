package core;

import java.io.IOException;
import java.util.Deque;
import java.util.Map;
import java.util.NoSuchElementException;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.gson.*;

/**
 * A parser for the github commit json.
 * 
 * This class is a workaround for egit's inability to fetch files from a commit
 * @author p
 *
 */
public class ParseGitHubCommit {
	//commit json objects
	public static final String COMMIT_FILES = "files";
	public static final String COMMIT_FILES_FILENAME = "filename";
	public static final String COMMIT_FILES_STATUS = "status";
	
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
	
	/**
	 * Parses github commit json for java files only. Files are listed in order of
	 * most recent -> old.
	 * 
	 * Returns null if the map is empty since we are not interested in commits with no java file changes.
	 * 
	 * @param jsonCommit
	 * @return
	 * @throws IOException
	 */
	public static Map<CommitFileStatus, Deque<String>> getJavaFileNames(String jsonCommit) throws IOException {
		Deque<String> modified = Queues.newArrayDeque();
		Deque<String> added = Queues.newArrayDeque();
		Deque<String> removed = Queues.newArrayDeque();
		Deque<String> renamed = Queues.newArrayDeque();
		
		Map<CommitFileStatus, Deque<String>> files = Maps.newHashMap();
		files.put(CommitFileStatus.ADDED, added);
		files.put(CommitFileStatus.MODIFIED, modified);
		files.put(CommitFileStatus.REMOVED, removed);
		files.put(CommitFileStatus.RENAMED, renamed);
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObj = jsonParser.parse(jsonCommit).getAsJsonObject();
		JsonArray fileArray = jsonObj.get(COMMIT_FILES).getAsJsonArray();
		
		for(int i=0; i<fileArray.size(); i++) {
			JsonObject file = fileArray.get(i).getAsJsonObject();
			String filename = file.get(COMMIT_FILES_FILENAME).getAsString();
			if(filename.endsWith(".java")) {
				String status = file.get(COMMIT_FILES_STATUS).getAsString();
				
				switch(CommitFileStatus.fromString(status)){
					case MODIFIED:
						modified.push(filename); 
						break;
					case ADDED:
						added.push(filename); 
						break;
					case REMOVED:
						removed.push(filename); 
						break;
					case RENAMED:
						renamed.push(filename);
						break;
				}
				
			}
		}
		
		if(modified.isEmpty() && added.isEmpty() && removed.isEmpty() && renamed.isEmpty())
			files = null;
		
		return files;
	}
}