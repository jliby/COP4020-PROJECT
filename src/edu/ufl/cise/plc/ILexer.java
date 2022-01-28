package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.List;

import static edu.ufl.cise.plc.IToken.Kind.EOF;

public interface ILexer {

	int current = 0;
	int start = 0;
	int line = 1;
	String source = null;
	List<IToken> tokens = new ArrayList<>();
	IToken next() throws LexicalException;
	IToken peek() throws LexicalException;
	char advance();
	void addToken(IToken.Kind type);
	void addToken(IToken.Kind type, Object literal);
	boolean isAtEnd();
	void scanToken();

	List<Token> Scanner(String source);
}

  class Lexer implements ILexer {


	  int current = 0;
	  int start = 0;
	  int line = 1;
	  String source = "";
	  List<Token> tokens = new ArrayList<>();
	  Lexer(String source) {
		  this.source = source;
	  }
	 @Override
	 public IToken next() throws LexicalException {

//		 if (current + 1 >= source.length()) return '\0';
//		 return source.charAt(current + 1);

		 // convert into token and return
		 return null;
	 }

	 @Override
	 public IToken peek() throws LexicalException {
//		 if (isAtEnd()) return '\0';
//		     return source.charAt(current);

		 // convert into token and return

		 return null;
	 }

	 @Override
	 public char advance() {
		 return source.charAt(current++);

		 //next character in source string
	 }


	  @Override
	  public boolean isAtEnd() {
		  return current >= source.length();

		  // check's if at end of lexeme
	  }
	  @Override
	  public void  scanToken() {
		  char c = advance();
		  switch (c) {

//		  example
//			  case '(': addToken(IToken.Kind.LEFT_PAREN); break;
//			  case ')': addToken(RIGHT_PAREN); break;
//			  case '{': addToken(LEFT_BRACE); break;
//			  case '}': addToken(RIGHT_BRACE); break;
//			  case ',': addToken(COMMA); break;
//			  case '.': addToken(DOT); break;
//			  case '-': addToken(MINUS); break;
//			  case '+': addToken(PLUS); break;
//			  case ';': addToken(SEMICOLON); break;
//			  case '*': addToken(STAR); break; // [slash]
		  }
	  }


	  @Override
	 public List<Token> Scanner(String source) {
		  while (!isAtEnd()) {
			  // We are at the beginning of the next lexeme.
			  start = current;
			  scanToken();
		  }

		  tokens.add(new Token(EOF, "", null, line));
		  return tokens;
	 }
	  @Override
	  public void addToken(IToken.Kind type) {
		  addToken(type, null);
	  }

	  @Override
	  public void addToken(IToken.Kind type, Object literal) {
		  String text = source.substring(start, current);
		  tokens.add(new Token(type, text, literal, line));
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