package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.List;

public interface ILexer {

	int current = 0;
	int start = 0;
	int line = 1;
	String source = null;
	List<IToken> tokens = new ArrayList<>();
	IToken next() throws LexicalException;
	IToken peek() throws LexicalException;
	char advance();
	void addToken(IToken type);
	void addToken(IToken type, Object literal);
	boolean isAtEnd();
	void scanToken();

	List<IToken> Scanner(String source);
}

  class Lexer implements ILexer {

	Lexer() {

	}
	  int current = 0;
	  int start = 0;
	  int line = 1;

	 @Override
	 public IToken next() throws LexicalException {
		 return null;
	 }

	 @Override
	 public IToken peek() throws LexicalException {
		 return null;
	 }

	 @Override
	 public char advance() {
		 return 0;
	 }

	 @Override
	 public void addToken(IToken type) {

	 }

	 @Override
	 public void addToken(IToken type, Object literal) {

	 }

	  @Override
	  public boolean isAtEnd() {
		return false;
	  }
	  @Override
	  public void  scanToken() {

	  }


	  @Override
	 public List<IToken> Scanner(String source) {
		 while (!isAtEnd()) {
			 // We are at the beginning of the next lexeme.
			 start = current;
			 scanToken();
		 }

//		 tokens.add(new Token("", null, line));
		 return tokens;
	 }


 }
