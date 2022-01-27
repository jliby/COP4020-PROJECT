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
	char addToken(IToken type);
	char addToken(IToken type, Object literal);
	boolean isAtEnd();
	void scanToken();

	List<Token> Scanner(String source);
}

  class Lexer implements ILexer {


	  int current = 0;
	  int start = 0;
	  int line = 1;
	  String source = "";

	  Lexer(String source) {
		  this.source = source;
	  }
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
	 public char addToken(IToken type) {
		return '0';
	 }

	 @Override
	 public char addToken(IToken type, Object literal) {
		 current++;
		 return source.charAt(current - 1);
	 }

	  @Override
	  public boolean isAtEnd() {
		  return current >= source.length();
	  }
	  @Override
	  public void  scanToken() {

	  }


	  @Override
	 public List<Token> Scanner(String source) {
		  List<Token> tokens = new ArrayList<>();
		  while (!isAtEnd()) {
			 // We are at the beginning of the next char.
			 start = current;
			 scanToken();
		 }

//		 tokens.add(new Token("", null, line));
		 return tokens;
	 }


 }

 /*
  * CLASS IMPLEMENTATAION TO MAINTAIN THE CHARACTERS IN THE SOURCE STRING
  * StringCharactersStream class is a helper class that performs the following:
  *
  */

// class StringCharactersStream {
//	// input is the source string
//	 String source_string = "";
//
//	 // current index of string
//	 int index =  0;
//
//	 // length of string
//	 int length = 0;
//
//	 // class constructor
//	public StringCharactersStream(String input_string) {
//		this.source_string = input_string;
//	}
//
// }