package commit.coupling.analyzer;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import commit.retriever.Commit;

public class CommitCouplingAnalyzer {
	public static List<String> importList;
	public static List<String> variableTypeList;
	
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

	// Getting the type one coupling for the analysis.
	public static void getTypeOneCoupling(File file) throws IOException, ParseException {
		importList = new ArrayList<String>();
		variableTypeList = new ArrayList<String>();
		
		File tempFile = new File("C:\\Users\\rchon_000\\Desktop\\Code.java");
		CompilationUnit cUnit;
		FileInputStream inputStream = new FileInputStream(tempFile);
		
		try {
			// Parse the file.
			cUnit = JavaParser.parse(inputStream);
		} finally {
			inputStream.close();
		}
		
		
		// Get all the variables.
		getAllVariables(cUnit);

		System.out.println(variableTypeList);
		
        new ImportVisitor().visit(cUnit, null);
        
        System.out.println(importList);
    }
	
	public static void getAllVariables(CompilationUnit cUnit) {
		// Get the global variables.
		getGlobalVariables(cUnit);
		
		// Visit the method's variables.
        new MethodVariableVisitor().visit(cUnit, null);
	}
	
	/**
	 * Get the global variables in the code.
	 * 
	 * @param cUnit
	 */
	public static void getGlobalVariables(CompilationUnit cUnit) {
        //List all global variables.
        List<TypeDeclaration> globalVars = cUnit.getTypes();
        
        for (TypeDeclaration type : globalVars) {
            List<BodyDeclaration> members = type.getMembers();

            for (BodyDeclaration member : members) {
                if (member instanceof FieldDeclaration) {
                    FieldDeclaration memberType = (FieldDeclaration) member;
                    List <VariableDeclarator> fields = memberType.getVariables();
                    System.out.println(memberType.getType() + " : " + fields.get(0).getId().getName());
                    variableTypeList.add(memberType.getType().toString());
                }
            }
        }
	}

    /**
     * Simple visitor implementation for visiting VariableDeclarationExpr nodes. 
     */
    private static class MethodVariableVisitor extends VoidVisitorAdapter {
    	public void visit(VariableDeclarationExpr n, Object arg) {
    		List <VariableDeclarator> variables = n.getVars();
    		
    		for (VariableDeclarator variable: variables) {
    			System.out.println(n.getType() + " : " + variable.getId().getName());
    			variableTypeList.add(n.getType().toString());
    		}
        }
    }
    
    /**
     * Visitor implementation for visiting ImportDeclaration nodes.
     */
    private static class ImportVisitor extends VoidVisitorAdapter {
    	public void visit(ImportDeclaration n, Object arg) {
    		System.out.println(n.getName().toString());
    		importList.add(n.getName().toString());
    	}
    }
}
