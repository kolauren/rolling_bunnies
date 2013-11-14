package change.impact.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import change.impact.graph.commit.Commit;

public class CommitCouplingAnalyzer {
	//TODO: do coupling analysis using AST from java parser
	//returns map of class -> coupled classes
	public Map<String, Collection<String>> getCoupledClasses(Commit commit) {
		return null;
	}

	//creates json file  coupling data based on codePro Analytix xml output
	public void getCoupledClasses(String xmlFilePath) {
		File file = new File(xmlFilePath);

		try {
			InputStream inputStream = new FileInputStream(file);
			StringBuilder builder = new StringBuilder();

			int pointer = 0;

			while ((pointer = inputStream.read()) != -1) {
				builder.append((char) pointer);
			}

			String xml = builder.toString();

			inputStream.close();

			JSONObject json = XML.toJSONObject(xml);
			JSONObject customJSON = new JSONObject();

			// String indentedJSON = json.toString(4);
			// System.out.println(indentedJSON);

			// Getting the Project name to be the root.
			customJSON.put("name", json.getJSONObject("dependencyAnalysis").getJSONObject("project").get("name"));

			// Getting the children with type as the key.
			JSONArray projectClassArray = json.getJSONObject("dependencyAnalysis").getJSONArray("type");
			JSONObject temp = new JSONObject();
			JSONArray children = new JSONArray();

			for (int i = 0; i < projectClassArray.length(); i++) {
				temp = new JSONObject();

				// Getting the name of the current child.
				temp.put("name", ((JSONObject) projectClassArray.get(i)).get("name"));

				// System.out.println(((JSONObject) projectClassArray.get(i)).get("name"));

				if (((JSONObject) projectClassArray.get(i)).has("reference")) {
					if (((JSONObject) projectClassArray.get(i)).get("reference").getClass().equals(JSONArray.class)) {
						JSONArray childImports = (JSONArray) ((JSONObject) projectClassArray.get(i)).get("reference");

						ArrayList<String> imports = new ArrayList<String>();

						// Getting the imports for each children.
						for (int j = 0; j < childImports.length(); j++) {
							imports.add((String) ((JSONObject) childImports.get(j)).get("type"));
						}

						temp.put("import", imports);
					} else {
						JSONObject childImport = (JSONObject) ((JSONObject) projectClassArray.get(i)).get("reference");
						JSONArray importArray = new JSONArray();
						importArray.put(childImport.get("type"));

						temp.put("import", importArray);
					}
				}

				String indentedJSON = temp.toString(4);
				System.out.println(indentedJSON);

				children.put(temp);
			}

			customJSON.put("children", children);

			String indentedJSON = customJSON.toString(4);
			// System.out.println(indentedJSON);

			PrintStream output = new PrintStream(new FileOutputStream("output/coupling.json"));
			output.println(indentedJSON);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
