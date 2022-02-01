package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer implements ILexer {

    public static Map<String, IToken.Kind> keywords;
    int next = 0;
    int currentToken = 0;
    static boolean hadError = false;


    static {
        keywords = new HashMap<>();
        keywords.put("if",    IToken.Kind.KW_IF);
        keywords.put("fi",    IToken.Kind.KW_FI);
        keywords.put("else",    IToken.Kind.KW_ELSE);
        keywords.put("write",    IToken.Kind.KW_WRITE);
        keywords.put("console",    IToken.Kind.KW_CONSOLE);
        keywords.put("void",    IToken.Kind.KW_VOID);
        keywords.put("if",    IToken.Kind.KW_IF);

        keywords.put("BLACK",    IToken.Kind.COLOR_CONST);
        keywords.put("BLUE",    IToken.Kind.COLOR_CONST);
        keywords.put("CYAN",    IToken.Kind.COLOR_CONST);
        keywords.put("DARK_GRAY",    IToken.Kind.COLOR_CONST);
        keywords.put("GRAY",    IToken.Kind.COLOR_CONST);
        keywords.put("GREEN",    IToken.Kind.COLOR_CONST);
        keywords.put("LIGHT_GRAY",    IToken.Kind.COLOR_CONST);
        keywords.put("MAGENTA",    IToken.Kind.COLOR_CONST);
        keywords.put("ORANGE",    IToken.Kind.COLOR_CONST);
        keywords.put("PINK",    IToken.Kind.COLOR_CONST);

        keywords.put("getRed", IToken.Kind.COLOR_OP);
        keywords.put("getGreen", IToken.Kind.COLOR_OP);
        keywords.put("getBlue", IToken.Kind.COLOR_OP);

        keywords.put("int", IToken.Kind.TYPE);
        keywords.put("float", IToken.Kind.TYPE);
        keywords.put("string", IToken.Kind.TYPE);
        keywords.put("boolean", IToken.Kind.TYPE);
        keywords.put("color", IToken.Kind.TYPE);
        keywords.put("image", IToken.Kind.TYPE);


        keywords.put("getWidth", IToken.Kind.IMAGE_OP);
        keywords.put("getHeight", IToken.Kind.IMAGE_OP);

        keywords.put("true", IToken.Kind.BOOLEAN_LIT);
        keywords.put("false", IToken.Kind.BOOLEAN_LIT);
    }

    int current = 0;
    int start = 0;
    int line = 0;
    int column = 0;

    String source = "";
    List<IToken.Token> tokens = new ArrayList<>();

    Lexer(String source) {
        this.source = source;
    }

    @Override
    public boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    @Override
    public boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
    //< is-alpha
//> is-digit
    @Override
    public boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    } // [is-digit]

    @Override
    public char char_peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    @Override
    public char char_peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    @Override
    public IToken next() throws LexicalException {
        IToken token = tokens.get(currentToken);
        currentToken += 1;

        return token;
    }

    @Override
    public IToken peek() throws LexicalException {
        //		 if (isAtEnd()) return '\0';
        //		     return source.charAt(current);

        // convert into token and return
        IToken token = tokens.get(currentToken++);

        return token;
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
        current++;
        return source.charAt(current - 1);
        //next character in source string
    }

    @Override
    public void identifier() {
        while (isAlphaNumeric(char_peek())) advance();

/* Scanning identifier < Scanning keyword-type
    addToken(IDENTIFIER);
*/
//> keyword-type
        // See if the identifier is a reserved word.
        String text = source.substring(start, current);

        IToken.Kind type;
        if (keywords.containsKey(text)) {
            type = keywords.get(text);
            addToken(type, text);
        } else {

            addToken(IToken.Kind.IDENT, text);


        }


//< keyword-type
    }

    private void line_column_tracker() {
        this.line+= 1;
        this.column = 1;
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

            case '\n': line_column_tracker(); break;

            default:

                if (Character.isAlphabetic(c)) {
                    identifier();
                }

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
        while (char_peek() != '"' && !isAtEnd()) {
            if (char_peek() == '\n') {
                line++;
                column = 0;
            }
            advance();
        }

        // Unterminated string.
        if (isAtEnd()) {
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(IToken.Kind.STRING_LIT, value);

    }
    @Override
    public void numberToLexeme() {
        boolean isFloat = false;
        while (Character.isDigit(char_peek())) advance();

        // Look for a fractional part.
        if (char_peek() == '.' && Character.isDigit(char_peekNext())) {
            // Consume the "."
            isFloat = true;
            advance();

            while (Character.isDigit(char_peek())) advance();
        }

        if (isFloat) {
            addToken(IToken.Kind.FLOAT_LIT, Double.parseDouble(source.substring(start, current)));


        } else {
            addToken(IToken.Kind.INT_LIT, Integer.parseInt(source.substring(start, current)));

        }
    }


    @Override
    public List<IToken.Token> Scanner() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
            column += 1;
        }

        tokens.add(new IToken.Token(IToken.Kind.EOF, "", null, line, column));
        return tokens;
    }

    @Override
    public void addToken(IToken.Kind type) {
        addToken(type, null);
    }

    @Override
    public void addToken(IToken.Kind type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new IToken.Token(type, text, literal, line, column));
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
                               String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}