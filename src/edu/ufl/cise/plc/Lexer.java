package edu.ufl.cise.plc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer implements ILexer {

    public static Map<String, IToken.Kind> keywords;
    int next = 0;
    int currentToken = 0;

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
        keywords.put("RED",    IToken.Kind.COLOR_CONST);
        keywords.put("WHITE",    IToken.Kind.COLOR_CONST);
        keywords.put("YELLOW",    IToken.Kind.COLOR_CONST);


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
    int column = -1;

    String source = "";
    List<IToken.Token> tokens = new ArrayList<>();

    Lexer(String source) {
        this.source = source;
    }

    @Override
    public boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_' || c == '$';
    }

    @Override
    public boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    @Override
    public boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

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

        if (token.getKind() == IToken.Kind.ERROR) {
            throw new LexicalException("test");
        }
        return token;
    }

    @Override
    public IToken peek() throws LexicalException {
        // convert into token and return
        //Changed arg from currentToken++ to currentToken
        IToken token = tokens.get(currentToken);
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


    }

    @Override
    public void identifier() {
        int tempColumn = column;
        while (isAlphaNumeric(char_peek())){
            advance();
            tempColumn++;
        }

        String text = source.substring(start, current);
        IToken.Kind type;
        if (keywords.containsKey(text)) {
            type = keywords.get(text);
            addToken(type, text);
        } else {
            addToken(IToken.Kind.IDENT, text);
        }
        column = tempColumn;
    }

    private void line_column_tracker() {
        this.line+= 1;
        this.column = -1;
    }

    @Override
    public boolean isAtEnd() {
        // check's if at end of lexeme
        return current >= source.length();
    }

    @Override
    public void scanToken() {
        char c = advance();
        column++;
        switch (c) {

            case '0' :
                if (char_peek() == '0'){
                    addToken(IToken.Kind.INT_LIT, '0');
                }
                else {
                    numberToLexeme();
                }
                break;
            case' ':  ; break;
            case'(': addToken(IToken.Kind.LPAREN); break;
            case ')': addToken(IToken.Kind.RPAREN); break;
            case '[' : addToken(IToken.Kind.LSQUARE); break;
            case ']' : addToken(IToken.Kind.RSQUARE); break;
            case '+' : addToken(IToken.Kind.PLUS); break;
            case '*' : addToken(IToken.Kind.TIMES); break;
            case '/' : addToken(IToken.Kind.DIV); break;
            case '^' : addToken(IToken.Kind.RETURN); break;
            case '%' : addToken(IToken.Kind.MOD); break;
            case ',' : addToken(IToken.Kind.COMMA); break;
            case ';' : addToken(IToken.Kind.SEMI); break;
            case '&' : addToken(IToken.Kind.AND); break;
            case '|' : addToken( IToken.Kind.OR); break;
            case '#' : commentSkip(); break;

            case '-' :
                if(match('>')) {
                    addToken(IToken.Kind.RARROW);
                    column++;
                } else {
                    addToken(IToken.Kind.MINUS);
                }
                break;

            case '!' :
                if(match('=')) {
                    addToken(IToken.Kind.NOT_EQUALS);
                    column++;
                } else {
                    addToken(IToken.Kind.BANG);
                }
                break;

            case '=' :
                if(match('=')) {
                    addToken(IToken.Kind.EQUALS);
                    column++;
                } else {
                    addToken(IToken.Kind.ASSIGN);
                }
                break;

            case '<' :
                if (match('=') ){
                    addToken(IToken.Kind.LE);
                    column++;
                }
                else if (match('-')) {
                    addToken(IToken.Kind.LARROW);
                    column++;
                }
                else if (match('<')) {
                    addToken(IToken.Kind.LANGLE);
                    column++;
                }
                else {
                    addToken(IToken.Kind.LT);
                }
                break;

            case '>' :
                if (match('=') ){
                    addToken(IToken.Kind.GE);
                    column++;
                }
                else if (match('>')) {
                    addToken(IToken.Kind.RANGLE);
                    column++;
                }
                else {
                    addToken(IToken.Kind.GT);
                }
                break;

            // scanning source string for literals that are: strings, floats, ints, speacial keywords, and conditional statements
            case '"': stringToLexeme(); break;
            case '\n': line_column_tracker(); break;
            default:
                if (Character.isDigit(c)) {
                    numberToLexeme();
                }
                else if (Character.isAlphabetic(c) || c == '_' || c == '$'){
                    identifier();
                }
                else {
                    addToken(IToken.Kind.ERROR);
                }
                break;
        }
    }

    public void commentSkip() {
        while (char_peek() != '\n') {
            advance();
        }
    }

    @Override
    public void stringToLexeme() {
        boolean iterate = true;
        int tempColumn = column;
        int originalLine = line;
        while (char_peek() != '"' && !isAtEnd()) {
            if (char_peek() == '\n') {
                line++;
                tempColumn = -1;
                advance();
                continue;
            }
            else if(char_peek() == '\\'){
                if (char_peekNext() == 'b' ||
                char_peekNext() == 't' ||
                char_peekNext() == 'n' ||
                char_peekNext() == 'f' ||
                char_peekNext() == 'r' ||
                char_peekNext() == '"' ||
                char_peekNext() == '\'' ||
                char_peekNext() == '\\'){
                    advance();
                    tempColumn++;
                }
                else{
                    addToken(IToken.Kind.ERROR);
                    return;
                }
            }
            advance();
            tempColumn++;
        }
        if (isAtEnd()) {
            addToken(IToken.Kind.ERROR);
            return;
        }
        int tempLine = line;
        line = originalLine;

        advance();
        tempColumn++;
        String value = source.substring(start, current);
        addToken(IToken.Kind.STRING_LIT, value);
        column = tempColumn;
        line = tempLine;
    }

    @Override
    public LexicalException numberToLexeme() {
        boolean isFloat = false;
        int tempColumn = column;
        while (Character.isDigit(char_peek())){
            advance();
            tempColumn++;
        }
        if (char_peek() == '.' && Character.isDigit(char_peekNext())) {

            isFloat = true;
            advance();
            tempColumn++;
            while (Character.isDigit(char_peek())){
                advance();
                tempColumn++;
            }
        }

        if (char_peek() == '.') {
            if (!Character.isDigit(char_peekNext())) {
                addToken(IToken.Kind.ERROR);
                return null;
            }

            }



            if (isFloat) {
            try{
                Float.parseFloat(source.substring(start, current));
            }
            catch (Exception e){
                addToken(IToken.Kind.ERROR);
                return null;
            }
            addToken(IToken.Kind.FLOAT_LIT, Float.parseFloat(source.substring(start, current)));
        }
        else {
            try{
                Integer.parseInt(source.substring(start, current));
            }
            catch (Exception e){
                addToken(IToken.Kind.ERROR);
                return null;
            }
            addToken(IToken.Kind.INT_LIT, Integer.parseInt(source.substring(start, current)));
        }

        column = tempColumn;
        return null;
    }

    @Override
    public List<IToken.Token> Scanner() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }
        column += 1;
        tokens.add(new IToken.Token(IToken.Kind.EOF, "", null, line, column, false));

        return tokens;
    }

    @Override
    public void addToken(IToken.Kind type) {
        addToken(type, null);
    }

    @Override
    public void addToken(IToken.Kind type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new IToken.Token(type, text, literal, line, column, false));
    }

}

