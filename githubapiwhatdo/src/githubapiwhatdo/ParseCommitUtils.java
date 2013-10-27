package githubapiwhatdo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;

public class ParseCommitUtils {
	//commit json objects
	public static String COMMIT_FILES = "files";
	public static String COMMIT_FILES_FILENAME = "filename";
	public static String COMMIT_FILES_STATUS = "status";
	
	public enum CommitFileStatus {
		MODIFIED("modified"),
		ADDED("added"),
		REMOVED("removed"),
		INVALID("otters");
		
		private String statusName;
		
		private CommitFileStatus(String statusName) {
			this.statusName = statusName;
		}
		
		public static CommitFileStatus match(String status) {
			if(status.equals(MODIFIED.statusName))
				return MODIFIED;
			else if(status.equals(ADDED.statusName))
				return ADDED;
			else if(status.equals(REMOVED.statusName))
				return REMOVED;
			else
				return INVALID;
		}
		
		public String getName() {
			return statusName;
		}
	}
	
	//parses json commit for java files
	public static Map<String, List<String>> getJavaFileNames(String jsonCommit) throws IOException {
		List<String> modified = Lists.newArrayList();
		List<String> added = Lists.newArrayList();
		List<String> removed = Lists.newArrayList();
		Map<String, List<String>> files = Maps.newHashMap();
		files.put(CommitFileStatus.ADDED.getName(), added);
		files.put(CommitFileStatus.MODIFIED.getName(), modified);
		files.put(CommitFileStatus.REMOVED.getName(), removed);
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObj = jsonParser.parse(jsonCommit).getAsJsonObject();
		JsonArray fileArray = jsonObj.get(COMMIT_FILES).getAsJsonArray();
		
		for(int i=0; i<fileArray.size(); i++) {
			JsonObject file = fileArray.get(i).getAsJsonObject();
			String filename = file.get(COMMIT_FILES_FILENAME).getAsString();
			if(filename.matches(".*\\.java")) {
				String status = file.get(COMMIT_FILES_STATUS).getAsString();
				
				switch(CommitFileStatus.match(status)){
					case MODIFIED:
						modified.add(filename); 
						break;
					case ADDED:
						added.add(filename); 
						break;
					case REMOVED:
						removed.add(filename); 
						break;
					case INVALID:
						throw new IOException("Commit file status is: "+status);
				}
				
			}
		}

		return files;
	}
}