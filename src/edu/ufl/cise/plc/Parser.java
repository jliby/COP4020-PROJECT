package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.Expr;

import java.beans.Expression;
import java.util.List;

import static edu.ufl.cise.plc.IToken.Kind.*;

public class Parser implements IParser {

// tokens list
    private final List<Token> tokens;
    private int current = 0;
    public Token currentToken;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
        currentToken = tokens.get(0);
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
        } catch (Exception e) {
            return null;
        }
        return AST;
    }

    public void Expression() {

        if ( isKind(KW_IF)) {
            ConditionalExpression();
        } else if (isKind(BANG, MINUS, COLOR_OP, IMAGE_OP)) {
            LogicalOrExpression();
        }

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

    public void factor() {

    }





}