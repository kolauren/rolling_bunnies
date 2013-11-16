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
	private ASTParser parser;
	private CompilationUnit cUnit;
	
	public ASTWrapper(ASTParser parser, CompilationUnit cUnit) throws IOException {
		this.parser = parser;
		this.cUnit = cUnit;
	}
	
	public ASTParser getParser() {
		return parser;
	}

	public void setParser(ASTParser parser) {
		this.parser = parser;
	}

	public CompilationUnit getCompilationUnit() {
		return cUnit;
	}
	
	public void setCompilationUnit(CompilationUnit cUnit) {
		this.cUnit = cUnit;
	}
}
