package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.List;

public interface ILexer {

	int current = 0;
	int start = 0;
	int line = 1;
	int column = 1;

	String source = null;
	List<IToken> tokens = new ArrayList<>();

	IToken next() throws LexicalException;

	IToken peek() throws LexicalException;

	char char_peek();
	char char_peekNext();

	char advance();

	boolean match(char expected);

	void addToken(IToken.Kind type);

	void addToken(IToken.Kind type, Object literal);

	void numberToLexeme();

	void identifier();

	boolean isAtEnd();

	void scanToken();

	boolean isAlpha(char c);

	boolean isAlphaNumeric(char c);

	boolean isDigit(char c);

	void stringToLexeme();


	List<IToken.Token> Scanner();


}

