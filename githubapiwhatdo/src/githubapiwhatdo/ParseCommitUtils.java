package githubapiwhatdo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;

/**
 * A parser for the github commit json.
 * 
 * This class is a workaround for egit's inability to fetch files from a commit
 * @author p
 *
 */
public class ParseCommitUtils {
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
	 * parses json commit for java files
	 * 
	 * returns null if the map is empty since we are not interested in commits with no java file changes
	 * 
	 * @param jsonCommit
	 * @return
	 * @throws IOException
	 */
	public static Map<CommitFileStatus, List<String>> getJavaFileNames(String jsonCommit) throws IOException {
		List<String> modified = Lists.newArrayList();
		List<String> added = Lists.newArrayList();
		List<String> removed = Lists.newArrayList();
		List<String> renamed = Lists.newArrayList();
		Map<CommitFileStatus, List<String>> files = Maps.newHashMap();
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
						modified.add(filename); 
						break;
					case ADDED:
						added.add(filename); 
						break;
					case REMOVED:
						removed.add(filename); 
						break;
					case RENAMED:
						renamed.add(filename);
						break;
				}
				
			}
		}
		
		if(modified.isEmpty() && added.isEmpty() && removed.isEmpty() && renamed.isEmpty())
			files = null;
		
		return files;
	}
}