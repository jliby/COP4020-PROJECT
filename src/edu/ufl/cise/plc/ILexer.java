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

	boolean match(char expected);

	void addToken(IToken.Kind type);

	void addToken(IToken.Kind type, Object literal);

	void numberToLexeme();


	boolean isAtEnd();

	void scanToken();

	void stringToLexeme();

	List<IToken.Token> Scanner(String source);

	class Lexer implements ILexer {


		int current = 0;
		int start = 0;
		int line = 1;
		String source = "";
		List<IToken.Token> tokens = new ArrayList<>();

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
		public boolean match(char expected) {
			if (isAtEnd()) return false;
			if (source.charAt(current) != expected) return false;

			current++;
			return true;
		}

		@Override
		public char advance() {
			return source.charAt(current++);

			//next character in source string
		}


		private void string() {
			while (peek() != '"' && !isAtEnd()) {
				if (peek() == '\n') line++;
				advance();
			}

			if (isAtEnd()) {
//				Lox.error(line, "Unterminated string.");
				return;
			}

			// The closing ".
			advance();

			// Trim the surrounding quotes.
			String value = source.substring(start + 1, current - 1);
			addToken(IToken.Kind.STRING_LIT, value);
		}



		@Override
		public boolean isAtEnd() {
			return current >= source.length();

			// check's if at end of lexeme
		}

		@Override
		public void scanToken() {
			char c = advance();
			switch (c) {

				// cases for single and double lexemes.
				case'(': addToken(IToken.Kind.RPAREN); break;
				case ')': addToken(IToken.Kind.LPAREN); break;
				case '[' : addToken(IToken.Kind.RSQUARE); break;
				case ']' : addToken(IToken.Kind.LSQUARE); break;
				case '+' : addToken(IToken.Kind.PLUS); break;
				case '*' : addToken(IToken.Kind.TIMES); break;
				case '-' : addToken(match('>') ? IToken.Kind.RARROW : IToken.Kind.MINUS); break;
				case '/' : addToken(IToken.Kind.DIV); break;
				case '^' : addToken(IToken.Kind.RETURN); break;
				case '%' : addToken(IToken.Kind.MOD); break;
				case ',' : addToken(IToken.Kind.COMMA); break;
				case ';' : addToken(IToken.Kind.SEMI); break;
				case '&' : addToken(IToken.Kind.AND); break;
				case '|' : addToken( IToken.Kind.OR); break;
				case '!' : addToken(match('=') ? IToken.Kind.NOT_EQUALS : IToken.Kind.BANG); break;
				case '=' : addToken(match('=') ? IToken.Kind.EQUALS : IToken.Kind.ASSIGN); break;
				case '<' :
						if (match('=') ){
							addToken(IToken.Kind.LE);
						}
						else if (match('-')) {
							addToken(IToken.Kind.LARROW);
						}

						else if (match('<')) {
							addToken(IToken.Kind.LANGLE);
						}

						else {
							addToken(IToken.Kind.LT);
						}
						break;
				case '>' :
						if (match('=') ){
							addToken(IToken.Kind.GE);
						}


						else if (match('>')) {
							addToken(IToken.Kind.RANGLE);
						}

						else {
							addToken(IToken.Kind.GT);
						}
						break;
				// scanning source string for literals that are: strings, floats, ints, speacial keywords, and conditional statements
				case '"': stringToLexeme(); break;

				default:
					if (Character.isDigit(c)) {
						numberToLexeme();
					} else {
//						Lox.error(line, "Unexpected character.");
					}
					break;
//new
			}
		}

		@Override
		public void stringToLexeme() {

		}
		public void numberToLexeme() {

		}


		@Override
		public List<IToken.Token> Scanner(String source) {
			while (!isAtEnd()) {
				// We are at the beginning of the next lexeme.
				start = current;
				scanToken();
			}

			tokens.add(new IToken.Token(EOF, "", null, line));
			return tokens;
		}

		@Override
		public void addToken(IToken.Kind type) {
			addToken(type, null);
		}

		@Override
		public void addToken(IToken.Kind type, Object literal) {
			String text = source.substring(start, current);
			tokens.add(new IToken.Token(type, text, literal, line));
		}

	}
}
