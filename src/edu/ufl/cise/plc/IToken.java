package edu.ufl.cise.plc;

public interface IToken {

	public record SourceLocation(int line, int column) {}

	public static enum Kind {
		IDENT,
		INT_LIT,
		FLOAT_LIT,
		STRING_LIT,
		BOOLEAN_LIT, // 'true','false'
		LPAREN, // '('
		RPAREN, // ')'
		LSQUARE, // '['
		RSQUARE, // ']'
		LANGLE, // '<<'
		RANGLE, // '>>'
		PLUS, // '+'
		MINUS, // '-'
		TIMES, // '*'
		DIV, //  '/'
		MOD, // '%'
		COLOR_CONST, // 'BLACK','BLUE','CYAN','DARK_GRAY','GRAY','GREEN','LIGHT_GRAY','MAGENTA','ORANGE','PINK',
		// 'RED','WHITE','YELLOW'
		KW_IF, // 'if'
		KW_FI, //'fi'
		KW_ELSE, //'else'
		KW_WRITE, // 'write'
		KW_CONSOLE, // 'console'
		AND, // '&'
		OR, // '|'
		BANG, // '!'
		LT, // '<'
		GT, // '>'
		EQUALS, // '=='
		NOT_EQUALS, // '!='
		LE, //  '<='
		GE, //  '>='
		TYPE, //int, float, string, boolean, color, image
		COLOR_OP, //getRed, getGreen, getBlue
		IMAGE_OP, //getWidth, getHeight
		SEMI, // ';'
		COMMA, // ','
		ASSIGN, // '='
		RARROW, // '->'
		LARROW, // '<-'
		KW_VOID, // 'void'
		RETURN, // '^'
		EOF, // used as sentinal, does not correspond to input
		ERROR, // use to avoid exceptions if scanning all input at once
	}

	//returns the token kind
	public Kind getKind();

	//returns the characters in the source code that correspond to this token
	//if the token is a STRING_LIT, this returns the raw characters, including delimiting "s and unhandled escape sequences.
	public String getText();

	//returns the location in the source code of the first character of the token.
	public SourceLocation getSourceLocation();

	//returns the int value represented by the characters of this token if kind is INT_LIT
	public int getIntValue();

	public boolean getIsException();

	//returns the float value represented by the characters of this token if kind is FLOAT_LIT
	public float getFloatValue();

	//returns the boolean value represented by the characters of this token if kind is BOOLEAN_LIT
	public boolean getBooleanValue();

	//returns the String represented by the characters of this token if kind is STRING_LIT
	//The delimiters should be removed and escape sequences replaced by the characters they represent.
	public String getStringValue();

	class Token implements IToken {

		public Kind type;
		public Object literal;
		int line;
		int column;
		String lexeme;

		boolean isException;
		public Token(Kind type, String lexeme, Object literal, int line, int column, boolean isException) {
			this.type = type;
			this.lexeme = lexeme;
			this.literal = literal;
			this.line = line;
			this.column = column;
			this.isException = isException;
		}

		@Override
		public boolean getIsException() {
			return isException;
		}

		@Override
		public Kind getKind() {
			return type;
		}

		@Override
		public String getText() {
			return literal.toString();
		}

		@Override
		public SourceLocation getSourceLocation() {
			return new SourceLocation(line, column);
		}

		@Override
		public int getIntValue() {
			int lit = Integer.parseInt(literal.toString());
			return lit;
		}

		@Override
		public float getFloatValue() {
			float lit = Float.parseFloat(literal.toString());
			return lit;
		}

		@Override
		public boolean getBooleanValue() {
			if (literal == "false") {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public String getStringValue() {
			String rawStr = literal.toString();
			String returnStr = "";
			for (int i = 1; i < rawStr.length() - 1; i++) {
				if (rawStr.charAt(i) == '\\') {
					if (rawStr.charAt(i + 1) == 'b') {
						returnStr += '\b';
					} else if (rawStr.charAt(i + 1) == 't') {
						returnStr += '\t';
					} else if (rawStr.charAt(i + 1) == 'n') {
						returnStr += '\n';
					} else if (rawStr.charAt(i + 1) == 'f') {
						returnStr += '\f';
					} else if (rawStr.charAt(i + 1) == 'r') {
						returnStr += '\r';
					} else if (rawStr.charAt(i + 1) == '"') {
						returnStr += '\"';
					} else if (rawStr.charAt(i + 1) == '\'') {
						returnStr += '\'';
					} else if (rawStr.charAt(i + 1) == '\\') {
						returnStr += '\\';
					}
					i++;
				} else {
					returnStr += rawStr.charAt(i);
				}
			}
			return returnStr;
		}
	}
}