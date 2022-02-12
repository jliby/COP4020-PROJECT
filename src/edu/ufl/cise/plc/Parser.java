package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.Expr;

import java.util.List;

import static edu.ufl.cise.plc.IToken.Kind.EOF;

public class Parser implements IParser {


    private final List<IToken.Token> tokens;
    private int current = 0;



    Parser(List<IToken.Token> tokens) {
        this.tokens = tokens;
    }

    // helper functions for tokens list
    private boolean check(IToken.Token.Kind type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    private IToken.Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private IToken.Token peek() {
        return tokens.get(current);
    }

    private IToken.Token previous() {
        return tokens.get(current - 1);
    }

    private boolean match(IToken.Token.Kind... types) {
        for (IToken.Token.Kind type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }
    @Override
    public ASTNode parse() throws PLCException {
        ASTNode AST;
        try {
         AST = expression();
        } catch (Exception e) {
            return null;
        }
        return AST;
    }
    private Expr expression() {

        return
    }





}