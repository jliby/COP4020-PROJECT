package edu.ufl.cise.plc;
import edu.ufl.cise.plc.ast.ASTVisitor;

public class CompilerComponentFactory {

	public static Lexer getLexer(String input) {
		Lexer test = new Lexer(input);
		test.Scanner();
		return test;
	}

	public static IParser getParser(String input) {
		Lexer lexer = getLexer(input);
		return new Parser(lexer.getTokens());
	}

	public static ASTVisitor getTypeChecker(){
		return new TypeCheckVisitor();
	}
}