package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.runtime.ImageOps;

import java.awt.*;
import java.util.Locale;

import static edu.ufl.cise.plc.ast.Types.Type.*;


public class backup implements ASTVisitor {
    Types.Type global_type = VOID;
    boolean fileUrlIOEnd = false;

    private String pkg;

    public backup(String pkg_name) {
        this.pkg = pkg_name;
    }


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

            str.append(type);
            str.append(")");
        }
        void readConsole() {
            str.append("ConsoleIO.readValueFromConsole(\"");

        }

        void ternaryConditionalOperator() {
            str.append("?");
        }
        void ternaryResult() {
            str.append(":");
        }

        StringBuilder getString() {
            return str;
        }

        void setAssignment(Object obj) {
            // name
            str.append(obj);
            // assignment operator
            str.append("=");
        }

        void print(Object obj) {
            str.append("ConsoleIO.console.println(");
            str.append(")");
        }
        void readName(Object name, Object targetType) {
            str.append(name);
            str.append("=");
            str.append(" (");
            str.append(targetType);
            str.append(") ");

        }

        void readConsoleExpr(Object type) {
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

    // data type

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

    public String StringToLowercase(Types.Type type){
        if (type == Types.Type.STRING )
            return "String";
        else
            return type.toString().toLowerCase();
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        // add true or false literal expressions
        res.add(booleanLitExpr.getValue());
        return res.str;
    }

    // “””<stringLitExpr.getValue>”””
    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        boolean start = true;
        res.add("\"\"\"\n");
        res.add(stringLitExpr.getValue());
        res.add("\"\"\"");;

        return res.str;
    }

    //    Java int literal corresponding to value
//
//If coerceTo != null and coerceTo != INT, add cast to coerced type.
    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type;
        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Types.Type.INT){
            type = intLitExpr.getType();
            res.coerceType((StringToLowercase(type)));

        } else {

            if (global_type != VOID) {
                intLitExpr.setCoerceTo(global_type);
                type = intLitExpr.getCoerceTo();
                res.coerceType((StringToLowercase(type)));
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
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type;
        if (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Types.Type.FLOAT){



            floatLitExpr.setCoerceTo(global_type);
            type = floatLitExpr.getCoerceTo();
            res.coerceType((StringToLowercase(type)));

        } else {


            if (global_type != VOID) {
                type = floatLitExpr.getType();
                res.coerceType((StringToLowercase(type)));
            }

        }
        res.add(floatLitExpr.getValue());
        res.add("f");
        return res.str;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        res.add("ColorTuple.unpack(Color." + colorConstExpr.getText() + ".getRGB()");
        return res.str;
    }

    //    ( <boxed(coerceTo)> ConsoleIO.readValueFromConsole( “coerceType”,
    @Override
    public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        res.readConsole();
        // continues to visitReadStatement method :)
        return res.str;
    }

    @Override
    public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        res.add("new ColorTuple("+colorExpr.getRed().getText() + "," + colorExpr.getGreen().getText() + "," + colorExpr.getBlue().getText() + ")");
        return res.str;
    }

//    ( <op> <expr> )

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        //  op
        res.add(" ");

        res.add(unaryExpression.getOp().getText());
        //  expr
        unaryExpression.getExpr().visit(this, res.str);
        return res.str;
    }

    //    ( <left> <op> <right> )
    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type = binaryExpr.getType();
        Types.Type leftType = binaryExpr.getLeft().getType();
        Types.Type rightType = binaryExpr.getRight().getType();

        if(leftType == COLOR && rightType == COLOR){
            //throw new UnsupportedOperationException("N/A");
            res.add("(ImageOps.binaryTupleOp(");
            res.add("ImageOps.OP." + binaryExpr.getOp().getKind().name() + ",");
            res.add(binaryExpr.getLeft().getText() + ",");
            res.add(binaryExpr.getRight().getText() + "))");
        }
        else if (leftType == IMAGE && rightType == IMAGE){
            //throw new UnsupportedOperationException("N/A");
            System.out.println("BOTH ARE IMAGES");
        }
        else {
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

        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type = identExpr.getCoerceTo() != null ? identExpr.getCoerceTo() : identExpr.getType();
        //add cast type if applicable
        if (identExpr.getCoerceTo() != null && identExpr.getCoerceTo() != type) {

            res.coerceType(StringToLowercase(identExpr.getCoerceTo()));
        }
//        if (identExpr.getType() == COLOR){
//            res.add("ColorTuple");
//        }
//        else{
//            res.add(identExpr.getText());
//        }
        res.add(identExpr.getText());

        return res.str;
    }

    //    ( <condition> ) ? <trueCase> : <falseCase>
    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
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
        System.out.println("workings");

        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        System.out.println("workings");
        dimension.getWidth().visit(this, res.getString());
        res.add(",");
        dimension.getHeight().visit(this, res.getString());

        return res.getString();
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
        throw new UnsupportedOperationException("n/a");
    }

    //    <name> = <expr> ;
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        // add name =
        res.setAssignment(assignmentStatement.getName());
        res.add("(" + StringToLowercase(assignmentStatement.getTargetDec().getType())  +") (");
//        res.add("(");
//        if(assignmentStatement.getTargetDec().getType() == COLOR){
//            res.add("ColorTuple");
//        }
//        else{
//            res.add(StringToLowercase(assignmentStatement.getTargetDec().getType()));
//        }
//        res.add(") (");

        // add  expr
        assignmentStatement.getExpr().visit(this, res.getString());

        res.add(")");
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
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        res.print(writeStatement.getSource().getText());
        return res.getString();
    }

    @Override
    public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        //if reading from console then append (object version of type)
        res.readName(readStatement.getName(), boxed(readStatement.getTargetDec().getType()));
        readStatement.getSource().visit(this, res.getString());
        //if reading from console
        Types.Type targetType = readStatement.getTargetDec().getType();
        res.readConsoleExpr((targetType));
        return res.getString();
    }

//    <package declaration>
//<imports>
//    public class <name> {
//        public static <returnType> apply( <params> ){
//        <decsAndStatements>
//        }
//    }


    @Override
    public Object visitProgram(Program program, Object arg) throws Exception {
        StringBuilder str = new StringBuilder();
        str.append("package ").append(pkg).append(";\n");
        str.append("import edu.ufl.cise.plc.runtime.*; \n");
        str.append("import edu.ufl.cise.plc.runtime.FileURLIO; \n");
        str.append("import java.awt.image.BufferedImage; \n");
        str.append("import edu.ufl.cise.plc.runtime.ColorTuple; \n");
        str.append("import edu.ufl.cise.plc.runtime.ImageOps; \n");

        str.append("public class ").append(program.getName()).append("{\n");


        String typeLowerCase = StringToLowercase(program.getReturnType());

        str.append("public static ");
        if (typeLowerCase.equals("image")) {
            str.append("BufferedImage");
        }
        else if (typeLowerCase.equals("color")){
            str.append("ColorTuple");
        }
        else {
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
        StringBuilder sb = (StringBuilder) arg;
        if(nameDefintion.getType() == COLOR){
            global_type = nameDefintion.getType();
            sb.append("ColorTuple").append(" ").append(nameDefintion.getName());
        }
        else{
            String typeLowerCase = StringToLowercase(nameDefintion.getType());
            global_type = nameDefintion.getType();
            sb.append(typeLowerCase).append(" ").append(nameDefintion.getName());
        }

        return sb;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
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

        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Expr expr = returnStatement.getExpr();
        res.returnStatement();
        expr.visit(this, res.getString());
        return res.getString();
    }

    @Override
    public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {

        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        declaration.getNameDef().visit(this, res.getString());

        if (declaration.getExpr() != null) {
            if (global_type == IMAGE) {
                res.add(" = ");
                res.add("FileURLIO.readImage(");

                declaration.getExpr().visit(this, res.getString());
                res.add(",");
                declaration.getDim().visit(this, arg);
                res.add(")");
            }
            else if (global_type == COLOR) {
                res.add(" = ");
                res.add("(ColorTuple)(");
                declaration.getExpr().visit(this, res.getString());
                res.add(")");
            }
            else {
                global_type = declaration.getType();
                res.add("=");
                res.add("(" + StringToLowercase(global_type) + ")");
                res.add("(");

                declaration.getExpr().visit(this, res.getString());
                res.add(")");
            }
        }

        return res.getString();
    }


    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        throw new UnsupportedOperationException("N/A");
    }
}