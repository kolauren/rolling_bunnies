package githubapiwhatdo;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class CommitParserTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testCommitParser()
		throws Exception {
		CommitSerializer commitParser = new CommitSerializer();
		
		Commit c = new Commit();
		c.setCommitNumber(1);
		c.setAddedJavaFiles(Arrays.asList("class1.java", "class2.java", "class6.java"));
		c.setModifiedJavaFiles(Arrays.asList("class2.java", "class6.java"));
		c.setRemovedJavaFiles(Arrays.asList("class0.java"));
		
		System.out.println(commitParser.commitToJSON(c,true));

	}

}
