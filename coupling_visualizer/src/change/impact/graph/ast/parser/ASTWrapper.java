package change.impact.graph.ast.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTWrapper {
	//this class is a wrapper for Eclipse AST objects
	private CompilationUnit cUnit;
	private ASTParser parser;
	
	public ASTWrapper(URL url) throws IOException {
		createParser(url);
	}
	
	public void createParser(URL url) throws IOException {
		parser = ASTParser.newParser(AST.JLS3);
		InputStreamReader inputReader = new InputStreamReader(url.openStream());
        BufferedReader bufferedReader = new BufferedReader(inputReader);
		StringBuilder builder = new StringBuilder();
        String code;
        
		while ((code = bufferedReader.readLine()) != null) {
			builder.append(code.trim() + " \\n ");
		}
		
		bufferedReader.close();
		inputReader.close();

		code = builder.toString();
		
		parser.setSource(code.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
	}
	
	public CompilationUnit getCompilationUnit() {
		return cUnit;
	}
	
	public void setCompilationUnit(CompilationUnit cUnit) {
		this.cUnit = cUnit;
	}
}
