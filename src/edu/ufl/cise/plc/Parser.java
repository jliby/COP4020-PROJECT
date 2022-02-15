package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.beans.Expression;
import java.util.List;

import static edu.ufl.cise.plc.IToken.Kind.*;

public class Parser implements IParser {

// tokens list
    private final List<Token> tokens;
    private int current = 0;
    public Token currentToken;
    public Token t;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
        currentToken = tokens.get(0);
        t = tokens.get(0);
    }

    // helper functions for tokens list
    private boolean check(Token.Kind type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    private Token consume() {
        if (!isAtEnd()) current++;
        return previous();
    }
    // Consume advances to the next token

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


    protected boolean isKind(Token.Kind kind) {
        return currentToken.type == kind;
    }

    protected boolean isKind(Token.Kind... kinds) {
        for (Token.Kind k: kinds) {
            if (k== currentToken.type) {
                return true;
            }
        }
        return false;
    }

    private boolean match(Token.Kind... types) {
        for (Token.Kind type : types) {
            if (check(type)) {
                consume();
                return true;
            }
        }

        return false;
    }
    @Override
    public ASTNode parse() throws PLCException {
        ASTNode AST;
        try {
         AST = Expression();
        }
        catch (Exception e) {
            return null;
        }
        return AST;
    }

    public Expr Expression() throws LexicalException{
        Token firstToken = t;
        Expr left = null;
        Expr right = null;
        left = term();
        while (isKind(PLUS) || isKind(MINUS)){
            IToken op = t;
            consume();
            right = term();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;

//        if ( isKind(KW_IF)) {
//            ConditionalExpression();
//        } else if (isKind(BANG, MINUS, COLOR_OP, IMAGE_OP)) {
//            LogicalOrExpression();
//        }

    }
    public Expr term() throws LexicalException {
        Expr left = null;
        Expr right = null;
        left = factor();
        if(isKind(TIMES) || isKind(DIV)){
            consume();
            factor();
        }
        return factor();
    }

    public Expr factor() throws LexicalException {
        IToken firstToken = t;
        Expr e = null;
        if(isKind(INT_LIT)){
            e = new IntLitExpr(firstToken);
            consume();
        }
        else if (isKind(LPAREN)){
            consume();
            e = Expression();
            match(RPAREN);
        }
        else {
            throw new LexicalException("");
        }
        return e;
    }


    public void ConditionalExpression() {
        if (isKind(KW_IF)) {
            consume();
        } else {
            // return an error
        }
        if (isKind(LPAREN)) {
            consume();
            //
        } else {
            // return an err
        }
        Expression();
        if (isKind(RPAREN)) {
            consume();
        }

        Expression();

        if (isKind(KW_ELSE)) {
            consume();
        }
        else {
            // return an error
        }

        Expression();

        if(isKind(KW_FI)) {
            consume();
        }

        return;


    }

    public Expr LogicalOrExpression() {
        LogicalAndExpression();
        if(isKind(OR)) {
            while (isKind(OR)) {
                consume();
                LogicalAndExpression();
            }
        }
    }

//
//    ComparisonExpr ::=
//    AdditiveExpr ( ('<' | '>' | '==' | '!=' | '<=' | '>=') AdditiveExpr)*

    public Expr ComparisonExpression() {

        AdditiveExpression();
        if(isKind(LANGLE, RANGLE, EQUALS, NOT_EQUALS, GE, LE)){
            while(isKind(LANGLE, RANGLE, EQUALS, NOT_EQUALS, GE, LE)) {
                consume();
                AdditiveExpression();
            }
        } else {
            // err
            return  null;
        }
        return null;
    }

     public Expr LogicalAndExpression() {
        ComparisonExpression();
         if(isKind(AND)) {
             while (isKind(AND)) {
                 consume();
                 ComparisonExpression();
             }
         } else {

         }

     }
    public Expr AdditiveExpression() {

        }
    public Expr MultiplicativeExpression() {

        }
    public Expr UnaryExpression() {

        }
    public Expr PrimaryExpression() {

        }
    public Expr PixelSelector() {

        }
}