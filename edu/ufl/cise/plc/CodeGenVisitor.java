package edu.ufl.cise.plc;
import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.runtime.ImageOps;
import java.awt.*;
import java.util.Locale;
import static edu.ufl.cise.plc.ast.Types.Type.*;

public class CodeGenVisitor implements ASTVisitor {
    Types.Type global_type = VOID;
    boolean fileUrlIOEnd = false;
    boolean unpack = false;
    boolean skipVisitAssignmnet = false;

    private String pkg;

    public CodeGenVisitor(String pkg_name) {
        this.pkg = pkg_name;
    }

    boolean isInitialized = false;
    class StringBuilderDelegate {
        StringBuilder  str;
        StringBuilderDelegate(Object arg ) {
            str = (StringBuilder) arg;
        }

        void add(Object obj) {
            str.append(obj);
        }

        void multiLine(Boolean start) {
            if (start) {
                str.append("\"\"\"\n");
            } else {
                str.append("\"\"\"");
            }
        }

        void coerceType(Object type) {

            str.append("(");
            if(type.equals("color")) {
                str.append("int");
            }else {
                str.append(type);
            }
            str.append(")");
        }

        void readConsole() {
            str.append("ConsoleIO.readValueFromConsole(");
        }

        void ternaryConditionalOperator() {
            str.append("?\n\t\t\t\t");
        }

        void ternaryResult() {
            str.append(":\n\t\t\t\t");
        }

        StringBuilder getString() {
            return str;
        }

        void setAssignment(Object obj) {
            str.append(obj);
            str.append("=");
        }

        void print(Object obj) {
            str.append("ConsoleIO.console.println(");
            if (obj.equals("<<") || obj.equals("RED")) {
                System.out.println(obj.toString());
            }else{
                str.append(obj);
            }
            str.append(")");
        }

        void readName(Object name, Object targetType) {
            if (targetType != null) {
                str.append(name);
                str.append("=");
                str.append(" (");
                str.append(targetType);
                str.append(") ");
            } else {
                str.append(name);
                str.append(" = ");
                str.append(" (");
                if (global_type == COLOR) {
                    str.append("ColorTuple");
                }
                else if (global_type == IMAGE) {
                    str.append("BufferedImage");
                }
                else {

                    str.append(global_type);
                }
                str.append(") ");
            }
        }

        void readConsoleExpr(Object type) {
            str.append("\"");
            str.append(type).append("\",");
            str.append("\"Enter ");
            str.append(type);
            str.append(":\")");
        }

        void importPackages(Object pkg) {
            str.append("package ");
            str.append(pkg);
            str.append(";\n");
        }

        void returnStatement() {
            if (fileUrlIOEnd) {
                str.append("FileURLIO.closeFiles();");
                str.append("\n");
            }
            str.append("return ");
        }
    }

    public String boxed(Types.Type type){
        switch(type) {
            case INT:
                return "Integer";
            case FLOAT:
                return "Float";
            case BOOLEAN:
                return "Boolean";
            case STRING:
                return "String";
            default:
                return null;
        }
    }

    public String StringToLowercase(Types.Type type) throws Exception {
        String str;
        if (type == Types.Type.STRING ) {
            return "String";
        }
        else {
            if (type == COLOR) {
                str = toJavaType(type);
                return str;
            }
            if (type == IMAGE) {
                str = toJavaType(type);
                return str;
            }
            return type.toString().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        System.out.println("visit Boolean Literal Expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        // add true or false literal expressions
        res.add(booleanLitExpr.getValue());
        return res.str;
    }

    // “””<stringLitExpr.getValue>”””
    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        System.out.println("visit string literal expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        boolean start = true;
        res.add("\"\"\"\n");
        res.add(stringLitExpr.getValue());
        res.add("\"\"\"");;

        return res.str;
    }

    //If coerceTo != null and coerceTo != INT, add cast to coerced type.
    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        System.out.println("visit int literal expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type;
        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Types.Type.INT){
            type = intLitExpr.getType();
            res.coerceType((StringToLowercase(type)));
        } else {
            if (global_type != VOID) {
                if (global_type == COLOR) {
                    intLitExpr.setCoerceTo(global_type);
                    type = intLitExpr.getType();
                    res.coerceType((StringToLowercase(type)));
                } else if (global_type == IMAGE) {
                    intLitExpr.setCoerceTo(global_type);
                    type = intLitExpr.getType();
                    res.coerceType((StringToLowercase(type)));
                } else {
                    intLitExpr.setCoerceTo(global_type);
                    type = intLitExpr.getCoerceTo();
                    res.coerceType((StringToLowercase(type)));
                }
            }
        }
        res.add(intLitExpr.getValue());
        return res.str;
    }

//    Java float literal corresponding to value.
//
//    If coerceTo != null and coerceTo != FLOAT, add cast to coerced type.
//
//    Recall Java float literals must have f appended.  E.g.  12.3 in source is 12.3f in Java.  (12.3 in Java is a double–if you do this your program will probably run, but fail test cases that check for equality)

    @Override
    public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
        System.out.println("visit float literal expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type;
        if (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Types.Type.FLOAT){
            floatLitExpr.setCoerceTo(global_type);
            type = floatLitExpr.getCoerceTo();
            res.coerceType((StringToLowercase(type)));
        } else {
            if (global_type != VOID) {
                if (global_type == COLOR) {
                    floatLitExpr.setCoerceTo(global_type);
                    type = floatLitExpr.getType();
                    res.coerceType((StringToLowercase(INT)));
                } else if (global_type == IMAGE) {
                    floatLitExpr.setCoerceTo(INT);
                    type = floatLitExpr.getType();
                    res.coerceType((StringToLowercase(INT)));
                } else {
                    type = floatLitExpr.getType();
                    res.coerceType((StringToLowercase(type)));
                }
            }
        }
        res.add(floatLitExpr.getValue());
        res.add("f");
        return res.str;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        System.out.println("visit color const expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        unpack = true;
        res.add("(ColorTuple.unpack(Color." + colorConstExpr.getText() + ".getRGB()))");
        return res.str;
    }

    //    ( <boxed(coerceTo)> ConsoleIO.readValueFromConsole( “coerceType”,
    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        System.out.println("visit console expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        System.out.println(consoleExpr.getText());
        if (global_type == IMAGE) {
            res.add("(String) ConsoleIO.readValueFromConsole(");
            res.add("\"STRING\"");
            res.add(",");
            res.add("\"Enter Image URL: \"");
            res.add(")");
        }
        if (global_type == COLOR) {
            res.add("(ColorTuple) ConsoleIO.readValueFromConsole(");
            res.add("\"COLOR\"");
            res.add(",");
            res.add("\"Enter COLOR: \"");
            res.add(")");
        }
        else if (global_type == INT) {
            res.add("(int) ConsoleIO.readValueFromConsole(");
            res.add("\"INT\"");
            res.add(",");
            res.add("\"Enter int: \"");
            res.add(")");
        }
        else if (global_type == FLOAT) {
            res.add("(float) ConsoleIO.readValueFromConsole(");
            res.add("\"FLOAT\"");
            res.add(",");
            res.add("\"Enter float: \"");
            res.add(")");
        }
        else if (global_type == BOOLEAN) {
            res.add("(boolean) ConsoleIO.readValueFromConsole(");
            res.add("\"BOOLEAN\"");
            res.add(",");
            res.add("\"Enter boolean: \"");
            res.add(")");
        }
        else if (global_type == STRING) {
            res.add("(String) ConsoleIO.readValueFromConsole(");
            res.add("\"STRING\"");
            res.add(",");
            res.add("\"Enter string: \"");
            res.add(")");
        }
        else {
            res.readConsole();
        }
        return res.str;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        System.out.println("visit color expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Expr red = colorExpr.getRed();
        Expr green = colorExpr.getGreen();
        Expr blue = colorExpr.getBlue();

        res.add("new ColorTuple(");
        red.visit(this, res.str);
        res.add(",");
        green.visit(this, res.str);
        res.add(",");

        blue.visit(this, res.str);
        res.add(")");

        return res.str;
    }

    //    ( <op> <expr> )

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        System.out.println("visit unary expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        System.out.println(unaryExpression.getType());
        if(unaryExpression.getOp().getKind() == IToken.Kind.COLOR_OP) {
            //getRed/Green/Blue return components of respective colors
            if(unaryExpression.getType() ==  COLOR) {
                res.add("ColorTuple.");
                res.add(unaryExpression.getOp().getText());
                res.add("(");
                unaryExpression.getExpr().visit(this, res.getString());
                res.add(")");
            }

            if(unaryExpression.getType() ==  INT) {
                res.add("ColorTuple.");
                res.add(unaryExpression.getOp().getText());
                res.add("(");
                unaryExpression.getExpr().visit(this, res.getString());
                res.add(")");
            }
            //Images will have their color components extracted and potentially assigned to another image
            else if(unaryExpression.getType() == IMAGE) {
                res.add("ImageOps.");
                res.add(switch(unaryExpression.getOp().getText()) {
                    case "getRed" -> "extractRed(";
                    case "getGreen" -> "extractGreen(";
                    case "getBlue" -> "extractBlue(";
                    default -> throw new IllegalArgumentException("Invalid unary operator");
                });
                unaryExpression.getExpr().visit(this, res.str);
                res.add(")");
            }
        }
        else if(unaryExpression.getOp().getKind() == IToken.Kind.IMAGE_OP) {
            res.add(" ");
            res.add("(");
            res.add(unaryExpression.getExpr().getText());
            res.add(")");
            res.add(".");
            res.add(unaryExpression.getOp().getText());
            res.add("()");
        }
        else {
            res.add(" ");
            res.add(unaryExpression.getOp().getText());
//            System.out.println(unaryExpression.getOp().getText());
//            System.out.println(unaryExpression.getOp().getKind());
//
//            System.out.println(unaryExpression.getExpr().getText());
            unaryExpression.getExpr().visit(this, res.str);
        }
        return res.str;
    }

    //    ( <left> <op> <right> )
    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        System.out.println("visit Binary Expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type = binaryExpr.getType();
        Types.Type leftType = binaryExpr.getLeft().getType();
        Types.Type rightType = binaryExpr.getRight().getType();

        Expr left = binaryExpr.getLeft();
        Expr right = binaryExpr.getRight();

        Types.Type binType = binaryExpr.getType();

        //These might already be handled by their visitors
        leftType = (left.getCoerceTo() != null) ? left.getCoerceTo() : left.getType();
        rightType = (right.getCoerceTo() != null) ? right.getCoerceTo() : right.getType();

        if(binaryExpr.getCoerceTo() != null && binType != binaryExpr.getCoerceTo()) {
            String coercedType = toJavaType(binaryExpr.getCoerceTo());
            if(!skipVisitAssignmnet) {
                res.add("(");
                res.add(coercedType);
                res.add(")");
            }
        }

        IToken.Kind opKind = binaryExpr.getOp().getKind();
        if((leftType == COLOR || rightType == COLOR) && (leftType == rightType)) {
            res.add("(");
            res.add("ImageOps.binaryTupleOp(");
            res.add(switch(opKind) {
                case PLUS -> "ImageOps.OP.PLUS";
                case MINUS -> "ImageOps.OP.MINUS";
                case TIMES -> "ImageOps.OP.TIMES";
                case DIV -> "ImageOps.OP.DIV";
                case MOD -> "ImageOps.OP.MOD";
                case EQUALS -> "ImageOps.BoolOP.EQUALS";
                case NOT_EQUALS -> "ImageOps.BoolOP.NOT_EQUALS";
                default -> throw new IllegalArgumentException("Invalid binary operand types for color");
            });
            res.add(",");
            left.visit(this, res.str);
            res.add(",");
            System.out.println("GOOD" + binaryExpr.getType());
            if (binaryExpr.getRight().getType() == INT && binaryExpr.getType() == COLOR && unpack == false) {
                res.add("new ColorTuple(");
                right.visit(this, res.str);
                res.add(")");
            } else {
                right.visit(this, res.str);
                unpack = true;

            }
            res.add(")");
            res.add(")");
        } else if((leftType == IMAGE || rightType == IMAGE) && (leftType == rightType)) {
            res.add("(");
            if(opKind == IToken.Kind.EQUALS || opKind == IToken.Kind.NOT_EQUALS) {
                if (opKind == IToken.Kind.NOT_EQUALS) {
                    res.add("!ImageEqualsOP.equals(");
                } else {
                    res.add("ImageEqualsOP.equals(");
                }
                left.visit(this, res.str);
                res.add(",");

                right.visit(this, res.str);
                res.add(")");
            } else {
                res.add("ImageOps.binaryImageImageOp(");
                res.add(switch(opKind) {
                    case PLUS -> "ImageOps.OP.PLUS";
                    case MINUS -> "ImageOps.OP.MINUS";
                    case TIMES -> "ImageOps.OP.TIMES";
                    case DIV -> "ImageOps.OP.DIV";
                    case MOD -> "ImageOps.OP.MOD";
                    default -> throw new IllegalArgumentException("Invalid binary operand types for image");
                });
                res.add(",");
                left.visit(this, res.str);
                res.add(",");
                right.visit(this, res.str);
                res.add(")");
            }
            res.add(")");
        }
        else if((leftType == IMAGE && rightType == COLOR) || (leftType == COLOR && rightType == IMAGE)) {

            //Determine which side has the image and which has the color
            Expr imageExpr = (leftType == IMAGE) ? left : right;
            Expr colorExpr = (leftType == COLOR) ? left : right;

            res.add("(");
            res.add("ImageOps.binaryImageScalarOp(");
            res.add(switch(opKind) {
                case PLUS -> "ImageOps.OP.PLUS";
                case MINUS -> "ImageOps.OP.MINUS";
                case TIMES -> "ImageOps.OP.TIMES";
                case DIV -> "ImageOps.OP.DIV";
                case MOD -> "ImageOps.OP.MOD";
                default -> throw new IllegalArgumentException("Invalid binary operand types for image");
            });
            res.add(",");
            imageExpr.visit(this, res.str);
            res.add(",");

            colorExpr.visit(this, res.str);
            res.add(")");
        } else if((leftType == IMAGE && rightType == INT) || (leftType == INT && rightType == IMAGE)) {

            //Determine which side has the image and which has the color
            Expr imageExpr = (leftType == IMAGE) ? left : right;
            Expr intExpr = (leftType == INT) ? left : right;

            res.add("ImageOps.binaryImageScalarOp(");
            res.add(switch(opKind) {
                case PLUS -> "ImageOps.OP.PLUS";
                case MINUS -> "ImageOps.OP.MINUS";
                case TIMES -> "ImageOps.OP.TIMES";
                case DIV -> "ImageOps.OP.DIV";
                case MOD -> "ImageOps.OP.MOD";
                default -> throw new IllegalArgumentException("Invalid binary operand types for image");
            });
            res.add(",");
            imageExpr.visit(this, res.str);
            res.add(",");
            intExpr.visit(this, res.str);
            res.add(")");
        } else {
            res.add("(");
            if (binaryExpr.getRight().getType() == Types.Type.STRING) {
                if (binaryExpr.getOp().getText() == "!=") {
                    res.add("!");
                }
            }
            binaryExpr.getLeft().visit(this, res.str);
            if ((binaryExpr.getOp().getText() == "!=" || binaryExpr.getOp().getText() == "==") && binaryExpr.getRight().getType() == Types.Type.STRING ) {
                res.add(".equals(");
                binaryExpr.getRight().visit(this, res.str);
                res.add(")");
            } else {
                res.add(binaryExpr.getOp().getText());
                binaryExpr.getRight().visit(this, res.str);
            }
            res.add(")");
        }
        return res.str;
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
        System.out.println("visit identity expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type = identExpr.getCoerceTo() != null ? identExpr.getCoerceTo() : identExpr.getType();
        //add cast type if applicable
        if (identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != type) {
            System.out.println("yes");
            res.coerceType(StringToLowercase(identExpr.getCoerceTo()));


            return res.str;
        } else {
            if (identExpr.getType() == COLOR && identExpr.getCoerceTo() == type) {
                res.add(identExpr.getText());
                res.add(".pack()");
            }
            else {
                res.add(identExpr.getText());
            }
            return res.str;
        }
    }

    //    ( <condition> ) ? <trueCase> : <falseCase>
    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        System.out.println("visit conditional expression");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        res.add("(");
        conditionalExpr.getCondition().visit(this, res.getString());
        // add ?
        res.ternaryConditionalOperator();
        conditionalExpr.getTrueCase().visit(this, res.getString());
        // add :
        res.ternaryResult();
        conditionalExpr.getFalseCase().visit(this, res.getString());
        res.add(")");
        return res.getString();
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        return "";
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        System.out.println("visit pixel selector");
        Object argObj = ((Object[]) arg)[0];
        StringBuilderDelegate res = new StringBuilderDelegate(argObj);
        String targetName = (String)((Object[]) arg)[1];

        Expr x = pixelSelector.getX();
        Expr y = pixelSelector.getY();

        res.add("for(int " + x.getText() + " = 0; ");
        res.add(x.getText() + " < " +targetName + ".getWidth(); ");
        res.add(x.getText() + "++)\n\t\t");
        res.add("for(int " + y.getText() + " = 0; ");
        res.add(y.getText() + " < " + targetName + ".getHeight(); ");
        res.add(y.getText() + "++)\n\t\t");

        return  res.str;
    }

    //    <name> = <expr> ;
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        System.out.println("visit assignment statement");
        //System.out.println("VISITED ASSIGN WITH " + assignmentStatement.getName() + " AND " + assignmentStatement.getExpr().getText());
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        if(assignmentStatement.getTargetDec().getType() == IMAGE && assignmentStatement.getExpr().getType() == IMAGE) {
            res.add(assignmentStatement.getName());
            res.add(" = ");
            //If the image previously had a dimension, it will always keep that size
            if(assignmentStatement.getTargetDec().getDim() != null) {
                res.add("ImageOps.resize(");

                assignmentStatement.getExpr().visit(this, res.str);
                if(assignmentStatement.getExpr().getType() == IMAGE) {
                    res.add(",");
                    System.out.println(assignmentStatement.getExpr().getType());
                    res.add(assignmentStatement.getName() + ".getWidth()");
                    res.add(",");
                    res.add(assignmentStatement.getName() + ".getHeight()");
                    res.add(")");
                }
                else {
                    res.add(",");
                    res.add(assignmentStatement.getTargetDec().getDim().getWidth().getText());
                    res.add(",");
                    res.add(assignmentStatement.getTargetDec().getDim().getHeight().getText());
                    res.add(")");
                }
            }
            else {
                res.add("ImageOps.clone(");
                assignmentStatement.getExpr().visit(this, res.str);
                res.add(")");

            }
        }
        else if(assignmentStatement.getExpr().getType() == COLOR) {
            Object[] args = {res.str, assignmentStatement.getName()};
            if(assignmentStatement.getSelector() != null) {
                assignmentStatement.getSelector().visit(this, args);

                res.add("ImageOps.setColor(");
                res.add(assignmentStatement.getName());
                res.add(",");
                res.add(assignmentStatement.getSelector().getX().getText());
                res.add(",");
                res.add(assignmentStatement.getSelector().getY().getText());
                res.add(",");

                //If the expression is an int coerced to a color, make a colortuple with it
                if (assignmentStatement.getExpr().getCoerceTo() == INT) {
                    res.add("new ColorTuple(");
                    assignmentStatement.getExpr().visit(this, res.str);
                    res.add(")");
                } else {
                    assignmentStatement.getExpr().visit(this, res.getString());
                }
                res.add(")");
            }
            else {
                if(assignmentStatement.getTargetDec().getDim() != null) {
                    res.add("for(int x = 0; x < " + assignmentStatement.getName() + ".getWidth(); ");
                    res.add("x++)\n\t\t");
                    res.add("for(int y = 0; y < " + assignmentStatement.getName() + ".getHeight(); ");
                    res.add("y++)\n\t\t\t");
                    res.add("ImageOps.setColor(" + assignmentStatement.getName() + ",x,y,");
                    assignmentStatement.getExpr().visit(this, res.str);
                    res.add(")");
                }
                else{
                    res.add(assignmentStatement.getName());
                    res.add(" = ");
                    assignmentStatement.getExpr().visit(this, res.str);
                }
            }
        }
        else if(assignmentStatement.getExpr().getCoerceTo() == INT) {
            res.add(assignmentStatement.getName());
            res.add(" = ");
            res.add("new ColorTuple(");
            assignmentStatement.getExpr().visit(this, res.str);
            res.add(")");
        }
        else if (assignmentStatement.getExpr().getCoerceTo() == COLOR) {
            System.out.println("new" + assignmentStatement.getExpr().getCoerceTo());
            res.add("for(int x = 0; x < " + assignmentStatement.getName() + ".getWidth(); ");
            res.add("x++)\n\t\t");
            res.add("for(int y = 0; y < " + assignmentStatement.getName() + ".getHeight(); ");
            res.add("y++)\n\t\t\t");
            res.add("ImageOps.setColor(" + assignmentStatement.getName() + ",x,y,");
            res.add("new ColorTuple(");
            // this is it
            skipVisitAssignmnet = true;
            assignmentStatement.getExpr().visit(this, res.str);
            skipVisitAssignmnet = false;
            res.add(")");
            res.add(")");        }
        else {
            res.setAssignment(assignmentStatement.getName());
            res.add("(" + StringToLowercase(assignmentStatement.getTargetDec().getType()) + ") (");
            assignmentStatement.getExpr().visit(this, res.getString());
            res.add(")");
        }
        return res.getString();
    }

//    ConsoleIO.console.println(<source>) ;
//
//println here is just the usual PrintStream method.
// Usually this is used with the PrintStream instance System.out.
// For this assignment, you should instead use the PrintStream object ConsoleIO.console.
// This will typically be assigned to System.out, but may be changed for grading or other purposes.

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
        System.out.println("visit write statement");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);

        Expr src = writeStatement.getSource();
        Types.Type srcType = src.getType();
        Expr dest = writeStatement.getDest();
        Types.Type destType = dest.getType();

        if(srcType == IMAGE) {
            if(destType == CONSOLE) {
                res.add("ConsoleIO.displayImageOnScreen(");
                src.visit(this, res.str);
            } else if(destType == STRING) {
                res.add("FileURLIO.writeImage(");
                src.visit(this, res.str);
                res.add(",");
                dest.visit(this, res.str);
            }
            res.add(")");
        } else if(destType == STRING) {
            res.add("FileURLIO.writeValue(");
            src.visit(this, res.str);
            res.add(",");
            dest.visit(this, res.str);
            res.add(")");
        }
        else {
            if (src.getType() == COLOR) {
                res.add("ConsoleIO.console.println(");
                src.visit(this, arg);
                res.add(")");

//                ColorTuple [red=1, green=2, blue=3]
//                ColorTuple [red=255, green=0, blue=0]
            }
            else {
                if(writeStatement.getSource().getType() == STRING){
                    res.add("ConsoleIO.console.println(");
                    writeStatement.getSource().visit(this, res.str);
                    res.add(")");
                }
                else{
                    res.print(writeStatement.getSource().getText());
                }
            }
        }
        return res.getString();
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        System.out.println("visit read statement");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        //if reading from console then append (object version of type)
        res.readName(readStatement.getName(), boxed(readStatement.getTargetDec().getType()));
        if(readStatement.getTargetDec().getType() == IMAGE) {
            //res.add(" = ");
            res.add("FileURLIO.readImage(");
            readStatement.getSource().visit(this, res.str);

            if (readStatement.getTargetDec() != null) {
                res.add(",");
                res.add(readStatement.getTargetDec().getDim().getWidth().getText());
                res.add(",");
                res.add(readStatement.getTargetDec().getDim().getHeight().getText());
            }

            res.add(")");
            return res.str;

        }
        else if (readStatement.getTargetDec().getType() == INT) {
            if(readStatement.getSource().getType() != CONSOLE) {
                res.add("FileURLIO.readValueFromFile(");
                readStatement.getSource().visit(this, res.str);
                res.add(")");
            }
            else {
                readStatement.getSource().visit(this, res.str);
            }
            return res.str;
        }
        else if (readStatement.getTargetDec().getType() == FLOAT) {
            if(readStatement.getSource().getType() != CONSOLE) {
                res.add("FileURLIO.readValueFromFile(");
                readStatement.getSource().visit(this, res.str);
                res.add(")");
            }
            else {
                readStatement.getSource().visit(this, res.str);
            }
            return res.str;
        }
        else if (readStatement.getTargetDec().getType() == STRING) {
            if(readStatement.getSource().getType() != CONSOLE) {
                res.add("FileURLIO.readValueFromFile(");
                readStatement.getSource().visit(this, res.str);
                res.add(")");
            }
            else {
                readStatement.getSource().visit(this, res.str);
            }
            return res.str;
        }
        else if(readStatement.getTargetDec().getType() == BOOLEAN) {
            if(readStatement.getSource().getType() != CONSOLE) {
                res.add("FileURLIO.readValueFromFile(");
                readStatement.getSource().visit(this, res.str);
                res.add(")");
            }
            else {
                readStatement.getSource().visit(this, res.str);
            }
            return res.str;
        }
        else if (readStatement.getTargetDec().getType() == COLOR){
            if(readStatement.getSource().getType() != CONSOLE) {
                res.add("FileURLIO.readValueFromFile(");
                readStatement.getSource().visit(this, res.str);
                res.add(")");
            }
            else {
                readStatement.getSource().visit(this, res.str);
//                res.add("FileURLIO.readValueFromFile(");
//                readStatement.getSource().visit(this, res.str);
//                res.add(")");
            }
            return res.str;
        }
//        else if (readStatement.getTargetDec().getType() == STRING){
//            res.add("FileURLIO.readValueFromFile(");
//            readStatement.getSource().visit(this, res.str);
//            res.add(")");
//            return res.str;
//        }
        else {
            readStatement.getSource().visit(this, res.getString());
            //if reading from console
            //Types.Type targetType = readStatement.getTargetDec().getType();
            //res.readConsoleExpr((targetType));
        }
        return res.getString();
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        StringBuilder str = new StringBuilder();
        str.append("package ").append(pkg).append(";\n");
        str.append("import edu.ufl.cise.plc.runtime.*; \n");
        str.append("import edu.ufl.cise.plc.runtime.FileURLIO; \n");
        str.append("import java.awt.image.BufferedImage; \n");
        str.append("import edu.ufl.cise.plc.runtime.ColorTuple; \n");
        str.append("import edu.ufl.cise.plc.runtime.ImageOps; \n");
        str.append("import edu.ufl.cise.plc.runtime.*; \n");
        str.append("import java.awt.Color; \n");

        str.append("public class ").append(program.getName()).append("{\n");

        String typeLowerCase = StringToLowercase(program.getReturnType());

        str.append("public static ");
        if (typeLowerCase.equals("image")) {
            str.append("BufferedImage");
        } else {
            str.append(typeLowerCase);
        }
        str.append(" apply(");
        for (int i = 0; i < program.getParams().size(); i++){
            program.getParams().get(i).visit(this, str);
            if(i != program.getParams().size() -1) str.append(", ");
        }
        str.append("){\n");
        for (int i = 0; i < program.getDecsAndStatements().size(); i++){
            str.append("\t");
            program.getDecsAndStatements().get(i).visit(this, str);
            str.append(";");
            if(i != program.getDecsAndStatements().size() -1) str.append("\n");
        }
        str.append("\n\t}\n}");
        return str.toString();
    }

    @Override
    public Object visitNameDef(NameDef nameDefintion, Object arg) throws Exception {
        System.out.println("visit name definition");
        StringBuilder sb = (StringBuilder) arg;
        if(nameDefintion.getType() == COLOR){
            global_type = nameDefintion.getType();
            sb.append("ColorTuple").append(" ").append(nameDefintion.getName());
        } else if(nameDefintion.getType() == IMAGE){
            global_type = nameDefintion.getType();
            sb.append("BufferedImage").append(" ").append(nameDefintion.getName());
        } else{
            String typeLowerCase = StringToLowercase(nameDefintion.getType());
            global_type = nameDefintion.getType();
            sb.append(typeLowerCase).append(" ").append(nameDefintion.getName());

        }
        return sb;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        System.out.println("visit name definition with dim");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        String typeLowerCase = StringToLowercase(nameDefWithDim.getType());
        global_type = nameDefWithDim.getType();

        res.add("BufferedImage");
        res.add(" ");
        res.add(nameDefWithDim.getName());

        return res.getString();
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
        System.out.println("visit return statement");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Expr expr = returnStatement.getExpr();
        res.returnStatement();
        expr.visit(this, res.getString());
        return res.getString();
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
        System.out.println("visit var declration");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        declaration.getNameDef().visit(this, res.str);
        if(declaration.getExpr() != null) {
            res.add(" = ");

            //Image with/without dimension case
            if(declaration.getType() == IMAGE) {

                if(declaration.getDim() != null) {
                    //Resize the RHS to the LHS
                    if(declaration.getExpr().getType() == IMAGE) {
                        res.add("ImageOps.resize(");
                        declaration.getExpr().visit(this, res.str);
                        if(declaration.getExpr().getType() == IMAGE) {
                            res.add(",");
                            declaration.getDim().getWidth().visit(this, res.str);
                            //res.add(declaration.getExpr().getText() + ".getWidth()");
                            res.add(",");
                            declaration.getDim().getHeight().visit(this, res.str);
                            //res.add(declaration.getExpr().getText() + ".getHeight()");
                            res.add(")");
                        }
                        else {
                            res.add(",");
                            res.add(declaration.getDim().getWidth().getText());
                            res.add(",");
                            res.add(declaration.getDim().getHeight().getText());
                            res.add(")");
                        }
                    }
                    //Assign this color to the image of size Dimension
                    //TODO: This doesn't work
                    else if(declaration.getExpr().getType() == COLOR || declaration.getExpr().getType() == INT) {
                        res.add("new BufferedImage(");
                        res.add(declaration.getDim().getWidth().getText());
                        res.add(",");
                        res.add(declaration.getDim().getHeight().getText());
                        res.add(",");
                        res.add("BufferedImage.TYPE_INT_RGB");
                        res.add(");");
                        res.add("\n");
                        res.add("\t");
                        res.add("for(int x = 0; x < " + declaration.getNameDef().getName() + ".getWidth(); ");
                        res.add("x++)\n\t\t");
                        res.add("for(int y = 0; y < " + declaration.getNameDef().getName() + ".getHeight(); ");
                        res.add("y++)\n\t\t\t");
                        res.add("ImageOps.setColor("+ declaration.getNameDef().getName() +",x,y,");
                        declaration.getExpr().visit(this, res.str);
                        res.add(")");
                    } else {
                        res.add("FileURLIO.readImage(");
                        declaration.getExpr().visit(this, res.str);
                        res.add(",");
                        res.add(declaration.getDim().getWidth().getText());
                        res.add(",");
                        res.add(declaration.getDim().getHeight().getText());
                        res.add(")");
                    }
                } else {
                    if(declaration.getExpr().getType() == IMAGE) {
                        res.add("ImageOps.clone(");
                        declaration.getExpr().visit(this, res.str);
                        res.add(")");
                        //declaration.getExpr().visit(this, res.str);
                    }
                    else {
                        res.add("FileURLIO.readImage(");
                        declaration.getExpr().visit(this, res.str);
                        res.add(")");
                    }
                }
            }
            //The type of the declaration is a color of some kind with initializer (single int)
            else if(declaration.getExpr() != null && declaration.getType() == COLOR) {
                //Only int and float initializations call a ColorTuple ctor
                if(declaration.getExpr().getType() == INT) {

                    res.add("new ColorTuple(");
                    declaration.getExpr().visit(this, res.str);
                    res.add(')');
                }
//                else if (declaration.getExpr().getType() == FLOAT){
//                    res.add("(float) FileURLIO.readValueFromFile(");
//                    declaration.getExpr().visit(this, res.str);
//                    res.add(")");
//                }
                else if (declaration.getExpr().getType() == STRING){
                    res.add("(ColorTuple) FileURLIO.readValueFromFile(");
                    declaration.getExpr().visit(this, res.str);
                    res.add(")");
                }
                else {
                    declaration.getExpr().visit(this, res.str);
                }
            }
            else if (declaration.getType() == FLOAT && declaration.getExpr().getType() == STRING){
                res.add("(float) FileURLIO.readValueFromFile(");
                declaration.getExpr().visit(this, res.str);
                res.add(")");
            }
            else if (declaration.getType() == INT && declaration.getExpr().getType() == STRING){
                res.add("(int) FileURLIO.readValueFromFile(");
                declaration.getExpr().visit(this, res.str);
                res.add(")");
            }
            else if (declaration.getType() == BOOLEAN && declaration.getExpr().getType() == STRING){
                res.add("(boolean) FileURLIO.readValueFromFile(");
                declaration.getExpr().visit(this, res.str);
                res.add(")");
            }
            //Check if it is a file: If the right side is a string and it is read statement
            else if (declaration.getType() == STRING && declaration.getExpr().getType() == STRING){
                if(declaration.getOp().getKind() == IToken.Kind.ASSIGN){
                    declaration.getExpr().visit(this, res.str);
                }
                else{
                    res.add("(String) FileURLIO.readValueFromFile(");
                    declaration.getExpr().visit(this, res.str);
                    res.add(")");
                }
                //res.add("(String) FileURLIO.readValueFromFile(");
                //declaration.getExpr().visit(this, res.str);
                //res.add(")");
            }
            //General case of a varDeclaration
            else {
                global_type = declaration.getType();
                res.add("(" + StringToLowercase(global_type) + ")");
                res.add("(");

                declaration.getExpr().visit(this, res.getString());
                res.add(")");
            }
        }
        //Image without expression cases
        else if(declaration.getExpr() == null && declaration.getType() == IMAGE) {

            if(declaration.getDim() != null) {
                res.add(" = new BufferedImage(");
                //res.add(declaration.getDim().getWidth().getText());
                declaration.getDim().getWidth().visit(this, res.str);
                res.add(",");
                //res.add(declaration.getDim().getHeight().getText());
                declaration.getDim().getHeight().visit(this, res.str);
                res.add(",");
                res.add("BufferedImage.TYPE_INT_RGB");
                res.add(")");
            }
        }
        return res.getString();
    }

    public String toJavaType(Types.Type type) throws Exception{
        return switch(type) {
            case BOOLEAN -> "boolean";
            case COLOR -> "ColorTuple";
            case FLOAT -> "float";
            case IMAGE -> "BufferedImage";
            case INT -> "int";
            case STRING -> "String";
            case VOID -> "void";
            default -> throw new IllegalArgumentException("Invalid conversion");
        };
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        System.out.println("visit unary expr post fix");
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Expr x = unaryExprPostfix.getSelector().getX();
        Expr y = unaryExprPostfix.getSelector().getY();

        res.add("ColorTuple.unpack(");
        res.add(unaryExprPostfix.getFirstToken().getText());
        res.add(".getRGB(");
        x.visit(this, res.str);
        res.add(",");
        y.visit(this, res.str);
        res.add("))");
        return res.str;
    }
}