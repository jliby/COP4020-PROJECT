package edu.ufl.cise.plc;
import edu.ufl.cise.plc.ast.*;
import java.util.List;
import static edu.ufl.cise.plc.IToken.Kind.*;

public class Parser implements IParser{

    /*=== CLASS VARIABLES ===*/
    private final List<Token> tokens;
    private int current = 0;
    public Token currentToken;
    public Token t;
    Lexer lexer = new Lexer("");
    ASTNode AST;

    //Constructor
    Parser(List<Token> tokens){
        this.tokens = tokens;
        this.lexer.tokens = tokens;
        currentToken = tokens.get(0);
        t = tokens.get(0);
    }

    /*=== HELPER FUNCTIONS ===*/
    private boolean check(Token.Kind type){
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    
    // Consume advances to the next token
    private Token consume() throws PLCException{
        if (!isAtEnd()){
            lexer.next();
            current++;
            currentToken = tokens.get(current);
        }
        return previous();
    }

    private boolean match(Token.Kind... types) throws PLCException{
        for (Token.Kind type : types){
            if (check(type)){
                consume();
                return true;
            }
        }
        return false;
    }

    private boolean isAtEnd(){
        return peek().type == EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    protected boolean isKind(Token.Kind kind){
        return currentToken.type == kind;
    }

    protected boolean isKind(Token.Kind... kinds){
        for (Token.Kind k: kinds){
            if (k== currentToken.type){
                return true;
            }
        }
        return false;
    }


    @Override
    public ASTNode parse() throws PLCException{
        AST = expr();
        return AST;
    }

    /*=== GRAMMAR RULE FUNCTIONS ===*/
    public Expr expr() throws PLCException{
        Expr e;
        if (isKind(KW_IF)){
            e = conditionExpr();
        }
        else{
            e = logicalOrExpr();
        }
        return e;
    }

    public Expr conditionExpr() throws PLCException{
        Token firstToken = currentToken;
        Expr e;
        consume();
        match(LPAREN);
        Expr condition = expr();
        match(RPAREN);
        Expr trueCase = expr();
        match(KW_ELSE);
        e = new ConditionalExpr(firstToken, condition, trueCase, expr());
        if(!match(KW_FI)){
            throw new SyntaxException("");
        }
        return e;
    }

    public Expr logicalOrExpr() throws PLCException{
        Token firstToken = currentToken;
        Expr left;
        Expr right;
        left = logicalAndExpr();
        while(isKind(OR)){
            Token op = currentToken;
            consume();
            right = logicalAndExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr logicalAndExpr() throws PLCException{
        Token firstToken = currentToken;
        Expr left;
        Expr right;
        left = comparisonExpr();
        while(isKind(AND)){
            Token op = currentToken;
            consume();
            right = comparisonExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr comparisonExpr() throws PLCException{
        Token firstToken = currentToken;
        Expr left;
        Expr right;
        left = additiveExpr();
        while(isKind(LT) || isKind(GT) || isKind(EQUALS) || isKind(NOT_EQUALS) || isKind(LE) || isKind(GE)){
            Token op = currentToken;
            consume();
            right = additiveExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr additiveExpr() throws PLCException{
        Token firstToken = currentToken;
        Expr left;
        Expr right;
        left = multiplicativeExpr();
        while(isKind(PLUS) || isKind(MINUS)){
            Token op = currentToken;
            consume();
            right = multiplicativeExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr multiplicativeExpr() throws PLCException{
        Token firstToken = currentToken;
        Expr left;
        Expr right;
        left = UnaryExpr();
        while(isKind(TIMES) || isKind(DIV) || isKind(MOD)){
            Token op = currentToken;
            consume();
            right = UnaryExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr UnaryExpr() throws PLCException{
        Token firstToken = currentToken;
        Expr e;
        if (isKind(BANG) || isKind(MINUS) || isKind(COLOR_OP) || isKind(IMAGE_OP)){
            Token op = currentToken;
            consume();
            Expr unaryExpr = expr();
            e = new UnaryExpr(firstToken, op, unaryExpr);
        }
        else{
            e = UnaryExprPostfix();
        }
        return e;
    }

    public Expr UnaryExprPostfix() throws PLCException{
        Token firstToken = currentToken;
        Expr e;
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
        else if (isKind(LPAREN)){
            consume();
            e = expr();
            match(RPAREN);
        }
        else{
            if(!isAtEnd()){
                consume();
            }
            throw new SyntaxException("");
        }
        if(isKind(LSQUARE)){
            consume();
            Expr e1 = expr();
            match(COMMA);
            Expr e2 = expr();
            match(RSQUARE);
            PixelSelector pixelSel = new PixelSelector(firstToken, e1, e2);
            e = new UnaryExprPostfix(firstToken, e, pixelSel);
        }
        return e;
    }
}