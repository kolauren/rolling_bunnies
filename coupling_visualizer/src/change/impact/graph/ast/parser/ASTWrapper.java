package change.impact.graph.ast.parser;

import java.io.IOException;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTWrapper {
	//this class is a wrapper for Eclipse AST objects
	private ASTParser parser;
	private CompilationUnit cUnit;
	private String className;
	
	public ASTWrapper(ASTParser parser, CompilationUnit cUnit) throws IOException {
		this.parser = parser;
		this.cUnit = cUnit;
		
		TypeDeclarationVisitor typeVisitor = new TypeDeclarationVisitor();
		typeVisitor.visit(cUnit);
		
		this.className = typeVisitor.getClassName();
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
}
