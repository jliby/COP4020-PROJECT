package edu.ufl.cise.plc;
import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Types.*;
import java.util.Collections;
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

    private Token match(Token.Kind... types) throws PLCException{
        for (Token.Kind type : types){
            if (check(type)){
                return consume();
            }
        }
        return null;
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
    boolean reachedEndOfFunction = true;

    @Override
    public ASTNode parse() throws PLCException{
        //AST = expr();
        AST = program();
        return AST;
    }

    /*=== GRAMMAR RULE FUNCTIONS ===*/
    public Program program() throws PLCException {

        Token firstToken = currentToken;
        Type returnType = null;
        String name = null;
        List<NameDef> params = new java.util.ArrayList<>(Collections.emptyList());
        List<ASTNode> decsAndStatements = new java.util.ArrayList<>(Collections.emptyList());

        if (isKind(TYPE) || isKind(KW_VOID)){
            returnType = Type.toType(firstToken.getText());
            consume();
            Token identToken = match(IDENT);
            if (identToken != null){
                name = identToken.getText();

                Token lParen = match(LPAREN);
                if (lParen == null){
                    throw new SyntaxException("");
                }

                NameDef nameDef = nameDef();

                if(nameDef != null){
                    params.add(nameDef);
                    while(isKind(COMMA)){
                        consume();
                        nameDef = nameDef();
                        if(nameDef !=  null){
                            params.add(nameDef);
                        }
                        else if (isKind(RPAREN)){
                            throw new SyntaxException("");
                        }
                    }
                }

                Token rParen = match(RPAREN);
                if (rParen == null){
                    throw new SyntaxException("");
                }

                Declaration dec = declaration();
                Statement state = statement();

                //Try changing this to try catch and change return null to throw except in state and dec functs.
                while (dec != null || state != null){
                    if(dec != null){
                        decsAndStatements.add(dec);
                    }
                    else if (state != null){
                        decsAndStatements.add(state);
                    }
                    if(!isKind(SEMI)){
                        throw new SyntaxException("");
                    }
                    consume();
                    dec = declaration();
                    state = statement();

                }

                if (dec == null && state == null && !isKind(EOF)){
                    throw new SyntaxException("");
                }

                return new Program(firstToken, returnType,name,params,decsAndStatements);
            }
        }
        throw new SyntaxException("");
    }


    public NameDef nameDef() throws PLCException{
        Token firstToken = currentToken;
        if(isKind(TYPE)){

            consume();
            Token name = match(IDENT);

            if(name != null){
                return new NameDef(firstToken, firstToken.getText(), name.getText());
            }
            else{
                Dimension dim = dimension();
                if (dim != null){
                    name = match(IDENT);
                    if(name != null){
                        return new NameDefWithDim(firstToken, firstToken.getText(), name.getText(), dim);
                    }
                }
                else{
                    throw new SyntaxException("");
                }
            }
        }
        return null;
    }

    public VarDeclaration declaration() throws PLCException{
        Token firstToken = currentToken;
        NameDef nameDef = nameDef();

        if (nameDef != null){
            if(isKind(ASSIGN) || isKind(LARROW)){
                Token op = currentToken;
                consume();
                Expr e = expr();
                return new VarDeclaration(firstToken, nameDef, op, e);

            }
            return new VarDeclaration(firstToken, nameDef, null, null);
        }

        return null;

    }

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
        if(match(KW_FI) == null){
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
        left = unaryExpr();
        while(isKind(TIMES) || isKind(DIV) || isKind(MOD)){
            Token op = currentToken;
            consume();
            right = unaryExpr();
            left = new BinaryExpr(firstToken, left, op, right);
        }
        return left;
    }

    public Expr unaryExpr() throws PLCException{
        Token firstToken = currentToken;
        Expr e;
        if (isKind(BANG) || isKind(MINUS) || isKind(COLOR_OP) || isKind(IMAGE_OP)){
            Token op = currentToken;
            consume();
            Expr unaryExpr = unaryExpr();
            e = new UnaryExpr(firstToken, op, unaryExpr);
        }
        else{
            e = UnaryExprPostfix();
        }
        return e;
    }

    public Expr UnaryExprPostfix() throws PLCException{
        //PrimaryExpr is called first and PixelSelector is called within it
        Token firstToken = currentToken;
        Expr e = primaryExpr();
        PixelSelector pixelSelector = pixelSelector();
        if(pixelSelector != null){
            e = new UnaryExprPostfix(firstToken, e, pixelSelector);
        }
        return e;
    }

    public Expr primaryExpr() throws PLCException{
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
        else if (isKind(COLOR_CONST)){
            e = new ColorConstExpr(firstToken);
            consume();
        }
        else if (isKind(LANGLE)){
            consume();
            Expr red = expr();
            match(COMMA);
            Expr green = expr();
            match(COMMA);
            Expr blue = expr();
            match(RANGLE);
            e = new ColorExpr(firstToken, red, green, blue);
        }
        else if (isKind(KW_CONSOLE)){
            e = new ConsoleExpr(firstToken);
            consume();
        }
        else{
            if(!isAtEnd()){
                consume();
            }
            throw new SyntaxException("");
        }
        return e;
    }

    public PixelSelector pixelSelector() throws PLCException{
        if(isKind(LSQUARE)){
            Token firstToken = currentToken;
            consume();
            Expr x = expr();
            match(COMMA);
            Expr y = expr();
            match(RSQUARE);
            return new PixelSelector(firstToken, x, y);
        }
        return null;
    }

    public Dimension dimension() throws PLCException{
        if(isKind(LSQUARE)){
            Token firstToken = currentToken;
            consume();
            Expr width = expr();
            match(COMMA);
            Expr height = expr();
            match(RSQUARE);
            return new Dimension(firstToken, width, height);
        }
        throw new SyntaxException("");
    }

    public Statement statement() throws PLCException{
        Token firstToken = currentToken;
        Expr e;


        Token name = match(IDENT);
        if(name != null){
            PixelSelector pixelSelector = pixelSelector();
            if(match(ASSIGN) != null){
                e = expr();
                return new AssignmentStatement(firstToken, name.getText(), pixelSelector, e);
            }
            else if (match(LARROW) != null){
                e = expr();
                return new ReadStatement(firstToken, name.getText(), pixelSelector, e);
            }
            else{
                return null;
            }
        }
        else{
            if(match(KW_WRITE) != null){
                Expr source = expr();
                if(match(RARROW) != null){
                    Expr dest = expr();
                    return new WriteStatement(firstToken, source, dest);
                }
                else{
                    return null;
                }
            }
            else{
                if(match(RETURN) != null){
                    e = expr();
                    return new ReturnStatement(firstToken, e);
                }
                else{
                    if(reachedEndOfFunction == false) {
                        reachedEndOfFunction = true;
                        throw new SyntaxException("");
                    }
                    return null;
                }

            }
        }
    }
}