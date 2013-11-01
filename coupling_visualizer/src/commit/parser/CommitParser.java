package commit.parser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.egit.github.core.RepositoryCommit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.gson.*;

/**
 * A parser for the github commit json.
 * 
 * This class includes workarounds for egit's inability to fetch files from a commit
 * @author p
 *
 */
public class CommitParser {
	//commit json objects
	public static final String COMMIT_FILES = "files";
	public static final String COMMIT_FILES_FILENAME = "filename";
	public static final String COMMIT_FILES_STATUS = "status";
	
	GitHubDao githubDao;
	
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
	
	public CommitParser() {
		//TODO: move args to properties file
		String user = "pammil";
		String password = "5cd8f20e47dfc2ffc846e82c652450c61f0a41a9";
		
		//basic authentication
		githubDao = new GitHubDao(user, password);
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
	public Map<CommitFileStatus, Collection<String>> getJavaFileNames(String jsonCommit) throws IOException {
		Collection<String> modified = Lists.newArrayList();
		Collection<String> added = Lists.newArrayList();
		Collection<String> removed = Lists.newArrayList();
		Collection<String> renamed = Lists.newArrayList();
		
		Map<CommitFileStatus, Collection<String>> files = Maps.newHashMap();
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
	
	//returns numCommits oldest commits
	public Collection<Commit> getCommits(String owner, String repoName, int numCommits) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Deque<Commit> commits = Queues.newArrayDeque();
		List<RepositoryCommit> githubCommits = githubDao.queryCommits(owner, repoName);
		
		int max;
		if((numCommits < 0) || (numCommits > githubCommits.size()))
			max = githubCommits.size();
		else
			max = numCommits;
		
		for(int i=0; i<max; i++) {
			RepositoryCommit githubCommit = githubCommits.get(i);
			System.out.println(githubCommit.getUrl());
			String jsonCommit = githubDao.queryCommitJson(owner, repoName, githubCommit.getSha());
			Map<CommitFileStatus, Collection<String>> javaFiles = getJavaFileNames(jsonCommit);
			if(javaFiles != null) {
				Commit commit = new Commit();	
				for(CommitFileStatus status : CommitFileStatus.values()) {
					Commit.getSetMethodByStatus(status).invoke(commit, javaFiles.get(status));
				}
				commits.push(commit);
			}
		}
		
		//id the commits by order (oldest -> new)
		int i=0;
		for(Commit commit : commits) {
			commit.setCommitNumber(i);
			i++;
		}
		return commits;
	}
	
	//get all commits
	public Collection<Commit> getCommits(String owner, String repoName) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return getCommits(owner, repoName, -1);
	}
	
	
	//TODO
	public void parseDiff(String diff) {
		
	}
}