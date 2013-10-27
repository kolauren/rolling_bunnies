import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import com.google.gson.Gson;

public class CommitTxtToJSON {

	public enum Status {
		MODIFIED('M'), DELETED('D'), ADDED('A');
		private final char value;

		Status(char value) {
			this.value = value;
		}

		public static Status getByValue(char v) {
			for (Status status : Status.values()) {
				if (status.value == v)
					return status;
			}
			return null;
		}
	}

	// Given commit log using git command this method will parse
	// and add commit objects to the listOfCommits; returns listOfCommits
	private static List<Commit> parseCommitLog(File f) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));

		List<Commit> listOfCommits = new ArrayList<Commit>();
		int numCommits = 0;
		Commit c;
		List<String> added;
		List<String> modified;
		List<String> deleted;

		String line = br.readLine();
		while (line != null) {
			//System.out.println(line);
			if (line.contains("commit,")) {
				// this is the start of a new commit
				c = new Commit();
				added = new ArrayList<String>();
				modified = new ArrayList<String>();
				deleted = new ArrayList<String>();

				// this is the current commit's data
				// "commit: 875bc22, iambchan, 1376893547"
				// get the commitHash and add it to the commit Object
				String hash = getCommitHash(line);
				c.setCommitNumber(hash);
				line = br.readLine();

				while (line.length() != 0) {
					line = br.readLine();
					if (line == null) {
						// testing purposes
						//System.out.println("ermergered");
						//System.out.println(numCommits);
						break;
					}
					if (line.contains(".java")) {
						String className = getClassString(line);
						char status = line.charAt(0);
						Status s = Status.getByValue(status);
						switch (s) {
						case DELETED:
							deleted.add(className);
						case ADDED:
							added.add(className);
						case MODIFIED:
							modified.add(className);
						}
					}
				}
				c.setAddedJavaFiles(added);
				c.setModifiedJavaFiles(modified);
				c.setRemovedJavaFiles(deleted);
				listOfCommits.add(c);
				numCommits++;
			} else
				line = br.readLine();
		}
		System.out.println(numCommits);
		br.close();
		fis.close();
		return listOfCommits;
	}

	// String should look like this
	// "A	src/com/example/helloworld/DatePickerFragment.java"
	// returns the .java filename.
	private static String getClassString(String commitString) {
		String className = commitString;
		String[] splitString = className.split("/");
		for (String s : splitString) {
			if (s.contains(".java")) {
				return s;
			}
		}
		return className;
	}

	// Sample input: "commit, d38d801, iambchan, 1378270356"
	// output: commit hash
	private static String getCommitHash(String commitString) {
		String s = commitString;
		String[] splitString = s.split(",");
		return splitString[1].trim();
	}

	private static void writeCommitsToJSONFile(List<Commit> listOfCommits)
			throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(locationToWrite + "/commits.json");
		writer.println("[");

		Gson gson = new Gson();
		int numCommits = listOfCommits.size();
		for (int i = 0; i < numCommits - 1; i++) {
			writer.println(gson.toJson(listOfCommits.get(i)));
			writer.print(",");
		}
		writer.println(gson.toJson(listOfCommits.get(numCommits - 1)));
		
		writer.println("]");
		writer.close();
	}

	// given log file, outputs JSON file
	public static void convertToJSONFile(File file) throws IOException {
		List<Commit> listOfCommits = parseCommitLog(file);
		writeCommitsToJSONFile(listOfCommits);
	}

	private static String locationToWrite;
	public static void main(String[] args) throws IOException {
		// first arg is log filepath "/home/bessie/workspace/HelloWorld/changes5.txt"
		// to get the log, run this command with git
		// git whatchanged --name-status --pretty=format:"commit, %h, %an, %at" > changes5.txt
		File file = new File(args[0]);
		locationToWrite = file.getParent();
		convertToJSONFile(file);
	}

}
