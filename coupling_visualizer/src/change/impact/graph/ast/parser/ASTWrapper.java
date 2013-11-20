package change.impact.graph.ast.parser;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import change.impact.graph.Method;

public class ASTWrapper {
	//this class is a wrapper for Eclipse AST objects
	private ASTParser parser;
	private CompilationUnit cUnit;
	private String className;
	private String sourceLoc;
	
	public ASTWrapper(ASTParser parser, CompilationUnit cUnit, String sourceLoc) throws IOException {
		this.parser = parser;
		this.cUnit = cUnit;
		
		TypeDeclarationVisitor typeVisitor = new TypeDeclarationVisitor();
		cUnit.accept(typeVisitor);
		
		this.className = typeVisitor.getClassName();
		this.setSourceLoc(sourceLoc);
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

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getSourceLoc() {
		return sourceLoc;
	}

	public void setSourceLoc(String sourceLoc) {
		this.sourceLoc = sourceLoc;
	}
	
	public ASTWrapper clone() {
		ASTWrapper ast = null;
		try {
			ast = new ASTWrapper(parser, cUnit, sourceLoc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ast;
	}
}
