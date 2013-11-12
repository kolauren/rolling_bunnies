package commit.retriever;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnifiedDiffParser {
	// probably doesn't exist in github diff cause file data is included in json outside diff
	private static final String PATH_TIME = "(?<filepath>.*)\\s+''(?<time>.*)''";
	private static final String OLD_FILE = "--- ";
	private static final String NEW_FILE = "\\+\\+\\+ ";
	// does not deal with the case where the range is omitted (which means 1) for some versions of GNU diff. 
	// i'm sure which version of unified (har har?) diff github uses.
	private static final String RANGE_HEADER = "@@ -(?<oldStart>\\d+),(?<oldRange>\\d+) \\+(?<newStart>\\d+),(?<newRange>\\d+) @@.*";
	
	private static final String ADDED_LINE = "\\+(?<added>.*)";
	private static final String REMOVED_LINE = "-(?<removed>.*)";
	private static final String UNCHANGED_LINE = " (?<unchanged>.*)";
	//github diff specific; indicates that file terminates without a newline
	private static final String NO_NEWLINE = "\\\\ No newline at end of file";
	
	//prepare for ugly hard-coded if-else statements of doom
	public static Diff parse(String unifiedDiff) throws IOException {
		Diff diff = new Diff();
		//must be bigger than largest set of captured groups
		String[] captured = new String[8];
		int oldStart = -1;
		int oldRange = -1;
		int newStart = -1;
		int newRange = -1;
		int oldLineNumber = -2;
		int newLineNumber = -2;
		for(String line : unifiedDiff.split("\n")) {
			if(getCapturedGroups(OLD_FILE+PATH_TIME, line, captured, "filepath", "time")) {
				diff.setOldPath(captured[0]);
				diff.setOldTime(captured[1]);
			} else if(getCapturedGroups(NEW_FILE+PATH_TIME, line, captured, "filepath", "time")) {
				diff.setNewPath(captured[0]);
				diff.setNewTime(captured[1]);
			} else if(getCapturedGroups(RANGE_HEADER, line, captured, "oldStart", "oldRange", "newStart", "newRange")) {
				//make sure lines add up to the range
				assert(oldStart+oldRange==oldLineNumber);
				assert(newStart+newRange==newLineNumber);
				
				oldStart = Integer.parseInt(captured[0]);
				oldRange = Integer.parseInt(captured[1]);
				newStart = Integer.parseInt(captured[2]);
				newRange = Integer.parseInt(captured[3]);
				oldLineNumber = oldStart;
				newLineNumber = newStart;
			} else if(getCapturedGroups(ADDED_LINE, line, captured, "added")) {
				diff.putAddedLine(newLineNumber, captured[0]);
				newLineNumber++;
			} else if(getCapturedGroups(REMOVED_LINE, line, captured, "removed")) {
				diff.putRemovedLine(oldLineNumber, captured[0]);
				oldLineNumber++;
			} else if(getCapturedGroups(UNCHANGED_LINE, line, captured, "unchanged")) {
				newLineNumber++;
				oldLineNumber++;
			} else if (line.matches(NO_NEWLINE)) {
				//end of diff
				continue;
			}
			else {
				throw new IOException("Couldn't parse diff line: "+line);
			}
		}
		
		return diff;	
	}
	
	/**
	 * if regex matches:
	 *	Returns true and an output[] with captured groups in order of the group names
	 * else:
	 * 	Returns false and unchanged (empty) output[]
	 * 
	 * @param regex
	 * @param input
	 * @param groupNames
	 * @return
	 */
	private static boolean getCapturedGroups(String regex, String input, String[] output, String ... groupNames) {
		//output array initialized by caller; needs to be big enough to hold output
		assert(groupNames.length <= output.length);
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		if(m.matches()) {
			for(int i=0; i<groupNames.length; i++) {
				output[i] = m.group(groupNames[i]);
			}
			return true;
		}
		else
			return false;
	}
	

}
