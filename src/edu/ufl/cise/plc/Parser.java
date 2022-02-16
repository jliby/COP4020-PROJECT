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
        if (!isAtEnd()){
            current++;
            currentToken = tokens.get(current);
        }
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
//        try {
//         AST = expr();
//        }
//        catch (Exception e) {
//            return null;
//        }
        //AST = expr();
        AST = exprNew();
        return AST;
    }

    public Expr expr() throws SyntaxException, LexicalException {
        Token firstToken = currentToken;
        Expr left = null;
        Expr right = null;
        left = term();
        while (isKind(PLUS) || isKind(MINUS)){
            IToken op = currentToken;
            firstToken = consume();
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
    public Expr term() throws SyntaxException, LexicalException {
        Token firstToken = currentToken;
        Expr left = null;
        Expr right = null;
        left = factor();
        while(isKind(TIMES) || isKind(DIV)){
            IToken op = currentToken;
            firstToken = consume();
            right = factor();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr factor() throws SyntaxException, LexicalException {
        IToken firstToken = currentToken;
        Expr e = null;
        if(isKind(INT_LIT)){
            e = new IntLitExpr(firstToken);
            consume();
        }
        else if (isKind(LPAREN)){
            consume();
            e = expr();
            match(RPAREN);
        }
        else if (isKind(BOOLEAN_LIT)){
            e = new BooleanLitExpr(firstToken);
            consume();
        }
        else if (isKind(STRING_LIT)){
            e = new StringLitExpr(firstToken);
            consume();
        }
        else if (isKind(FLOAT_LIT)){
            e = new FloatLitExpr(firstToken);
            consume();
        }
        else if (isKind(IDENT)){
            e = new IdentExpr(firstToken);
            consume();
        }
        else if (isKind(KW_IF)){
            IToken current = firstToken;
            consume();
            match(LPAREN);
            Expr condition = expr();
            match(RPAREN);
            Expr trueCase = expr();
            match(KW_ELSE);
            e = new ConditionalExpr(current, condition, trueCase, expr());
            match(KW_FI);
        }
        else if (isKind(BANG)|| isKind(MINUS) || isKind(COLOR_OP) || isKind(IMAGE_OP))
        {
            IToken op = firstToken;
            consume();
            Expr right = expr();
            e = new UnaryExpr(op, op, right);
        }
        else {
            if (isKind(ERROR)){
                throw new LexicalException("");
            }
            else{
                throw new SyntaxException("");
            }
        }
        return e;
    }


    /*=== Restructure ===*/

    public Expr exprNew() throws SyntaxException, LexicalException {
        Token firstToken = currentToken;
        Expr left = null;
        Expr right = null;
        Expr e = null;
        if (isKind(KW_IF)){
            left = conditionExpr();
        }
        else{
            left = logicalOrExpr();
        }
        return left;
    }
    public Expr conditionExpr() throws SyntaxException, LexicalException{
        Token firstToken = currentToken;
        Expr e = null;
        consume();
        Expr condition = exprNew();
        Expr trueCase = exprNew();
        match(KW_ELSE);
        e = new ConditionalExpr(firstToken, condition, trueCase, expr());
        match(KW_FI);
        return e;
    }
    public Expr logicalOrExpr() throws SyntaxException, LexicalException{
        Token firstToken = currentToken;
        Expr left = null;
        Expr right = null;
        left = logicalAndExpr();
        while(isKind(OR)){
            Token op = currentToken;
            consume();
            right = logicalAndExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }
    public Expr logicalAndExpr() throws SyntaxException, LexicalException{
        Token firstToken = currentToken;
        Expr left = null;
        Expr right = null;
        left = comparisonExpr();
        while(isKind(AND)){
            Token op = currentToken;
            consume();
            right = comparisonExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }
    public Expr comparisonExpr() throws SyntaxException, LexicalException{
        Token firstToken = currentToken;
        Expr left = null;
        Expr right = null;
        left = additiveExpr();
        while(isKind(RARROW) || isKind(LARROW) || isKind(EQUALS) || isKind(NOT_EQUALS) || isKind(LE) || isKind(GE)){
            Token op = currentToken;
            consume();
            right = additiveExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }
    public Expr additiveExpr() throws SyntaxException, LexicalException{
        Token firstToken = currentToken;
        Expr left = null;
        Expr right = null;
        left = multiplicativeExpr();
        while(isKind(PLUS) || isKind(MINUS)){
            Token op = currentToken;
            consume();
            right = multiplicativeExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }
    public Expr multiplicativeExpr() throws SyntaxException, LexicalException{
        Token firstToken = currentToken;
        Expr left = null;
        Expr right = null;
        left = UnaryExpr();
        while(isKind(TIMES) || isKind(DIV) || isKind(MOD)){
            Token op = currentToken;
            consume();
            right = UnaryExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }
    public Expr UnaryExpr() throws SyntaxException, LexicalException{
        Token firstToken = currentToken;
        Expr e = null;
        if (isKind(BANG) || isKind(MINUS) || isKind(COLOR_OP) || isKind(IMAGE_OP)){
            Token op = currentToken;
            e = new UnaryExpr(firstToken, op, null);
        }
        else{
            e = UnaryExprPostfix();
        }
        return e;
    }
    public Expr UnaryExprPostfix() throws SyntaxException, LexicalException{
        Token firstToken = currentToken;
        Expr e = null;
        if (isKind(BOOLEAN_LIT)){
            e = new BooleanLitExpr(firstToken);
            consume();
        }
        else if (isKind(STRING_LIT)){
            e = new StringLitExpr(firstToken);
            consume();
        }
        else if (isKind(INT_LIT)){
            e = new IntLitExpr(firstToken);
        }
        else if (isKind(FLOAT_LIT)){
            e = new FloatLitExpr(firstToken);
            consume();
        }
        else if (isKind(IDENT)){
            e = new IdentExpr(firstToken);
            consume();
        }
        else if (isKind(LPAREN)){
            e = exprNew();
            match(RPAREN);
        }
        return e;
    }


//    public void ConditionalExpression() {
//        if (isKind(KW_IF)) {
//            consume();
//        } else {
//            // return an error
//        }
//        if (isKind(LPAREN)) {
//            consume();
//            //
//        } else {
//            // return an err
//        }
//        Expression();
//        if (isKind(RPAREN)) {
//            consume();
//        }
//
//        Expression();
//
//        if (isKind(KW_ELSE)) {
//            consume();
//        }
//        else {
//            // return an error
//        }
//
//        Expression();
//
//        if(isKind(KW_FI)) {
//            consume();
//        }
//
//        return;
//
//
//    }
//
//    public Expr LogicalOrExpression() {
//        LogicalAndExpression();
//        if(isKind(OR)) {
//            while (isKind(OR)) {
//                consume();
//                LogicalAndExpression();
//            }
//        }
//    }
//
////
////    ComparisonExpr ::=
////    AdditiveExpr ( ('<' | '>' | '==' | '!=' | '<=' | '>=') AdditiveExpr)*
//
//    public Expr ComparisonExpression() {
//
//        AdditiveExpression();
//        if(isKind(LANGLE, RANGLE, EQUALS, NOT_EQUALS, GE, LE)){
//            while(isKind(LANGLE, RANGLE, EQUALS, NOT_EQUALS, GE, LE)) {
//                consume();
//                AdditiveExpression();
//            }
//        } else {
//            // err
//            return  null;
//        }
//        return null;
//    }
//
//     public Expr LogicalAndExpression() {
//        ComparisonExpression();
//         if(isKind(AND)) {
//             while (isKind(AND)) {
//                 consume();
//                 ComparisonExpression();
//             }
//         } else {
//
//         }
//
//     }
//    public Expr AdditiveExpression() {
//
//        }
//    public Expr MultiplicativeExpression() {
//
//        }
//    public Expr UnaryExpression() {
//
//        }
//    public Expr PrimaryExpression() {
//
//        }
//    public Expr PixelSelector() {
//
//        }
}