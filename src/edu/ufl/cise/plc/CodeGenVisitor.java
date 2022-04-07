package edu.ufl.cise.plc;

import edu.ufl.cise.plc.ast.*;

import java.util.Locale;

public class CodeGenVisitor implements ASTVisitor {

    private String pkg;

    public CodeGenVisitor(String pkg_name) {
        this.pkg = pkg_name;
    }

    public static void main(String[] args){

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
            str.append(obj);
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
//        if (type == Types.Type.INT)
//            return "Integer";

    }

    public String StringToLowercase(Types.Type type){
        if (type == Types.Type.STRING )
            return "String";
        else
            return type.toString().toLowerCase();
    }

//    Java literal corresponding to value (i.e. true or false)
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
        res.multiLine(start);
        res.add(stringLitExpr.getValue());
        res.multiLine(!start);

        return res.str;
    }

//    Java int literal corresponding to value
//
//If coerceTo != null and coerceTo != INT, add cast to coerced type.
    @Override
    public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type = (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Types.Type.INT) ? intLitExpr.getCoerceTo() : intLitExpr.getType();
        if (intLitExpr.getCoerceTo() != null && intLitExpr.getCoerceTo() != Types.Type.INT)  res.coerceType((StringToLowercase(type)));
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
        Types.Type type = (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Types.Type.FLOAT) ? floatLitExpr.getCoerceTo() : floatLitExpr.getType();
        if (floatLitExpr.getCoerceTo() != null && floatLitExpr.getCoerceTo() != Types.Type.FLOAT) res.coerceType((StringToLowercase(type)));
        res.add(floatLitExpr.getValue());
        res.add("f");
        return res.str;
    }

    @Override
    public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
        throw new UnsupportedOperationException("N/A");
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

//    ( <op> <expr> )

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpression, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        //  op
        res.add(unaryExpression.getOp());
        //  expr
        unaryExpression.getExpr().visit(this, res.str);
        return res.str;
    }

//    ( <left> <op> <right> )
    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        Types.Type type = binaryExpr.getType();

        if(type == Types.Type.IMAGE) {
            throw new UnsupportedOperationException("N/A");
        }
        else {
            res.coerceType(StringToLowercase(type));
            res.add("(");
            binaryExpr.getLeft().visit(this, res.str);
            res.add(binaryExpr.getOp().getText());
            binaryExpr.getRight().visit(this, res.str);
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
        res.add(identExpr.getText());
        return res.str;
    }

//    ( <condition> ) ? <trueCase> : <falseCase>
    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
        StringBuilderDelegate res = new StringBuilderDelegate(arg);
        conditionalExpr.getCondition().visit(this, res.getString());
        // add ?
        res.ternaryConditionalOperator();
        conditionalExpr.getTrueCase().visit(this, res.getString());
        // add :
        res.ternaryResult();
        conditionalExpr.getFalseCase().visit(this, res.getString());
        return res.getString();
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws Exception {
        throw new UnsupportedOperationException("n/a");
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
        // add  expr
        assignmentStatement.getExpr().visit(this, res.getString());
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
        str.append("public class ").append(program.getName()).append("{\n");


        String typeLowerCase = StringToLowercase(program.getReturnType());

        str.append("public static ").append(typeLowerCase).append(" apply(");
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
        String typeLowerCase = StringToLowercase(nameDefintion.getType());
        sb.append(typeLowerCase).append(" ").append(nameDefintion.getName());
        return sb;
    }

    @Override
    public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
        throw new UnsupportedOperationException("N/A");
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
            res.add("=");
            declaration.getExpr().visit(this, res.getString());
        } else {
            return res.getString();

        }
        return res.getString();
    }

    @Override
    public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
        throw new UnsupportedOperationException("N/A");
    }
}