package edu.ufl.cise.plc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ufl.cise.plc.IToken.Kind;
import edu.ufl.cise.plc.ast.ASTNode;
import edu.ufl.cise.plc.ast.ASTVisitor;
import edu.ufl.cise.plc.ast.AssignmentStatement;
import edu.ufl.cise.plc.ast.BinaryExpr;
import edu.ufl.cise.plc.ast.BooleanLitExpr;
import edu.ufl.cise.plc.ast.ColorConstExpr;
import edu.ufl.cise.plc.ast.ColorExpr;
import edu.ufl.cise.plc.ast.ConditionalExpr;
import edu.ufl.cise.plc.ast.ConsoleExpr;
import edu.ufl.cise.plc.ast.Declaration;
import edu.ufl.cise.plc.ast.Dimension;
import edu.ufl.cise.plc.ast.Expr;
import edu.ufl.cise.plc.ast.FloatLitExpr;
import edu.ufl.cise.plc.ast.IdentExpr;
import edu.ufl.cise.plc.ast.IntLitExpr;
import edu.ufl.cise.plc.ast.NameDef;
import edu.ufl.cise.plc.ast.NameDefWithDim;
import edu.ufl.cise.plc.ast.PixelSelector;
import edu.ufl.cise.plc.ast.Program;
import edu.ufl.cise.plc.ast.ReadStatement;
import edu.ufl.cise.plc.ast.ReturnStatement;
import edu.ufl.cise.plc.ast.StringLitExpr;
import edu.ufl.cise.plc.ast.Types.Type;
import edu.ufl.cise.plc.ast.UnaryExpr;
import edu.ufl.cise.plc.ast.UnaryExprPostfix;
import edu.ufl.cise.plc.ast.VarDeclaration;
import edu.ufl.cise.plc.ast.WriteStatement;

import static edu.ufl.cise.plc.ast.Types.Type.*;

public class TypeCheckVisitor implements ASTVisitor {

	SymbolTable symbolTable = new SymbolTable();
	Program root;

	record Pair<T0,T1>(T0 t0, T1 t1){};  //may be useful for constructing lookup tables.

	private void check(boolean condition, ASTNode node, String message) throws TypeCheckException {
		if (!condition) {
			throw new TypeCheckException(message, node.getSourceLoc());
		}
	}

	//The type of a BooleanLitExpr is always BOOLEAN.
	//Set the type in AST Node for later passes (code generation)
	//Return the type for convenience in this visitor.
	@Override
	public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws Exception {
		booleanLitExpr.setType(Type.BOOLEAN);
		return Type.BOOLEAN;
	}

	@Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws Exception {
		stringLitExpr.setType(Type.STRING);
		return STRING;
	}

	@Override
	public Object visitIntLitExpr(IntLitExpr intLitExpr, Object arg) throws Exception {
		intLitExpr.setType(Type.INT);
		return INT;
	}

	@Override
	public Object visitFloatLitExpr(FloatLitExpr floatLitExpr, Object arg) throws Exception {
		floatLitExpr.setType(Type.FLOAT);
		return Type.FLOAT;
	}

	@Override
	public Object visitColorConstExpr(ColorConstExpr colorConstExpr, Object arg) throws Exception {
		colorConstExpr.setType(Type.COLOR);
		return COLOR;
	}

	@Override
	public Object visitConsoleExpr(ConsoleExpr consoleExpr, Object arg) throws Exception {
		consoleExpr.setType(Type.CONSOLE);
		return Type.CONSOLE;
	}

	//Visits the child expressions to get their type (and ensure they are correctly typed)
	//then checks the given conditions.
	@Override
	public Object visitColorExpr(ColorExpr colorExpr, Object arg) throws Exception {
		Type redType = (Type) colorExpr.getRed().visit(this, arg);
		Type greenType = (Type) colorExpr.getGreen().visit(this, arg);
		Type blueType = (Type) colorExpr.getBlue().visit(this, arg);
		check(redType == greenType && redType == blueType, colorExpr, "color components must have same type");
		check(redType == Type.INT || redType == Type.FLOAT, colorExpr, "color component type must be int or float");
		Type exprType = (redType == Type.INT) ? Type.COLOR : Type.COLORFLOAT;
		colorExpr.setType(exprType);
		return exprType;
	}



	//Maps forms a lookup table that maps an operator expression pair into result type.
	//This more convenient than a long chain of if-else statements.
	//Given combinations are legal; if the operator expression pair is not in the map, it is an error.
	Map<Pair<Kind,Type>, Type> unaryExprs = Map.of(
			new Pair<Kind,Type>(Kind.BANG,BOOLEAN), BOOLEAN,
			new Pair<Kind,Type>(Kind.MINUS, FLOAT), FLOAT,
			new Pair<Kind,Type>(Kind.MINUS, INT),INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,INT), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,COLOR), INT,
			new Pair<Kind,Type>(Kind.COLOR_OP,IMAGE), IMAGE,
			new Pair<Kind,Type>(Kind.IMAGE_OP,IMAGE), INT
	);

	//Visits the child expression to get the type, then uses the above table to determine the result type
	//and check that this node represents a legal combination of operator and expression type.
	@Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws Exception {
		// !, -, getRed, getGreen, getBlue
		Kind op = unaryExpr.getOp().getKind();
		Type exprType = (Type) unaryExpr.getExpr().visit(this, arg);
		//Use the lookup table above to both check for a legal combination of operator and expression, and to get result type.
		Type resultType = unaryExprs.get(new Pair<Kind,Type>(op,exprType));
		check(resultType != null, unaryExpr, "incompatible types for unaryExpr");
		//Save the type of the unary expression in the AST node for use in code generation later.
		unaryExpr.setType(resultType);
		//return the type for convenience in this visitor.
		return resultType;
	}

	//This method has several cases. Work incrementally and test as you go.
	@Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws Exception {
		Kind op = binaryExpr.getOp().getKind();
		Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
		Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
		Type returnType = null;

		//Now for a copious number of branches
		if((op == Kind.AND || op == Kind.OR) && leftType == Type.BOOLEAN && rightType == Type.BOOLEAN) {
			returnType = Type.BOOLEAN;
		}
		else if((op == Kind.EQUALS || op == Kind.NOT_EQUALS) && leftType == rightType) {
			returnType = Type.BOOLEAN;
		}
		else if(op == Kind.PLUS || op == Kind.MINUS) {
			if(leftType == Type.INT && rightType == Type.INT) {
				returnType = Type.INT;
			}
			else if(leftType == Type.FLOAT && rightType == Type.FLOAT) {
				returnType = Type.FLOAT;
			}
			else if(leftType == Type.INT && rightType == Type.FLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				returnType = Type.FLOAT;
			}
			else if(leftType == Type.FLOAT && rightType == Type.INT) {
				binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				returnType = Type.FLOAT;
			}
			else if(leftType == Type.COLOR && rightType == Type.COLOR) {
				returnType = Type.COLOR;
			}
			else if(leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) {
				returnType = Type.COLORFLOAT;
			}
			else if(leftType == Type.COLORFLOAT && rightType == Type.COLOR) {
				binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				returnType = Type.COLORFLOAT;
			}
			else if(leftType == Type.COLOR && rightType == Type.COLORFLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				returnType = Type.COLORFLOAT;
			}
			else if(leftType == Type.IMAGE && rightType == Type.IMAGE) {
				returnType = Type.IMAGE;
			}
		}
		else if(op == Kind.TIMES || op == Kind.DIV || op == Kind.MOD) {
			if(leftType == Type.INT && rightType == Type.INT) {
				returnType = Type.INT;
			}
			else if(leftType == Type.FLOAT && rightType == Type.FLOAT) {
				returnType = Type.FLOAT;
			}
			else if(leftType == Type.INT && rightType == Type.FLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				returnType = Type.FLOAT;
			}
			else if(leftType == Type.FLOAT && rightType == Type.INT) {
				binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				returnType = Type.FLOAT;
			}
			else if(leftType == Type.COLOR && rightType == Type.COLOR) {
				returnType = Type.COLOR;
			}
			else if(leftType == Type.COLORFLOAT && rightType == Type.COLORFLOAT) {
				returnType = Type.COLORFLOAT;
			}
			else if(leftType == Type.COLORFLOAT && rightType == Type.COLOR) {
				binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				returnType = Type.COLORFLOAT;
			}
			else if(leftType == Type.COLOR && rightType == Type.COLORFLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				returnType = Type.COLORFLOAT;
			}
			else if(leftType == Type.IMAGE && rightType == Type.IMAGE) {
				returnType = Type.IMAGE;
			}
			else if(leftType == Type.IMAGE && rightType == Type.INT) {
				returnType = Type.IMAGE;
			}
			else if(leftType == Type.IMAGE && rightType == Type.FLOAT) {
				returnType = Type.IMAGE;
			}
			else if(leftType == Type.INT && rightType == Type.COLOR) {
				binaryExpr.getLeft().setCoerceTo(Type.COLOR);
				returnType = Type.COLOR;
			}
			else if(leftType == Type.COLOR && rightType == Type.INT) {
				binaryExpr.getRight().setCoerceTo(Type.COLOR);
				returnType = Type.COLOR;
			}
			else if(leftType == Type.FLOAT && rightType == Type.COLOR) {
				binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				returnType = Type.COLORFLOAT;
			}
			else if(leftType == Type.COLOR && rightType == Type.FLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.COLORFLOAT);
				binaryExpr.getRight().setCoerceTo(Type.COLORFLOAT);
				returnType = Type.COLORFLOAT;
			}
		}
		else if(op == Kind.LT || op == Kind.LE || op == Kind.GT || op == Kind.GE) {
			if(leftType == Type.INT && rightType == Type.INT) {
				returnType = Type.BOOLEAN;
			}
			else if(leftType == Type.FLOAT && rightType == Type.FLOAT) {
				returnType = Type.BOOLEAN;
			}
			else if(leftType == Type.INT && rightType == Type.FLOAT) {
				binaryExpr.getLeft().setCoerceTo(Type.FLOAT);
				returnType = Type.BOOLEAN;
			}
			else if(leftType == Type.FLOAT && rightType == Type.INT) {
				binaryExpr.getRight().setCoerceTo(Type.FLOAT);
				returnType = Type.BOOLEAN;
			}
		}
		else {
			check(false, binaryExpr, "invalid types for binary expression");
		}

		binaryExpr.setType(returnType);
		return returnType;
	}

	@Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws Exception {
		//TODO:  implement this method
		//throw new UnsupportedOperationException("Unimplemented visit method.");

		String name = identExpr.getFirstToken().getText();
		Declaration dec = symbolTable.lookup(name);
		check(dec != null, identExpr, "undefined identifier " + name);
		check(dec.isInitialized(), identExpr, "using uninitialized variable " + name);
		identExpr.setDec(dec);  //save declaration--will be useful later.
		Type type = dec.getType();
		identExpr.setType(type);
		return type;

	}

	@Override
	public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws Exception {
		//TODO  implement this method
//		Type condType = (Type) conditionalExpr.getCondition().visit(this, arg);
//
//		check(condType == BOOLEAN, conditionalExpr, "condition must be boolean");
//		check(conditionalExpr.getTrueCase().getType() == conditionalExpr.getFalseCase().getType(), conditionalExpr, "trueCase must be equal to false case");
//		return conditionalExpr.getTrueCase().getType();

		Type conditionType = (Type) conditionalExpr.getCondition().visit(this, arg);
		Type trueType = (Type) conditionalExpr.getTrueCase().visit(this, arg);
		Type falseType = (Type) conditionalExpr.getFalseCase().visit(this, arg);
		check(conditionType == Type.BOOLEAN, conditionalExpr, "condition must be type boolean");
		check(trueType == falseType, conditionalExpr, "type of true case must match type of false case");
		conditionalExpr.setType(trueType);
		return trueType;
	}


	@Override
	public Object visitDimension(Dimension dimension, Object arg) throws Exception {
		Type leftType = (Type) dimension.getHeight().visit(this, arg);
		check(leftType == Type.INT, dimension.getHeight(), "only ints as dimension components");
		Type rightType = (Type) dimension.getWidth().visit(this, arg);
		check(rightType == Type.INT, dimension.getWidth(), "only ints as dimension components");
		return null;
	}

	@Override
	//This method can only be used to check PixelSelector objects on the right hand side of an assignment.
	//Either modify to pass in context info and add code to handle both cases, or when on left side
	//of assignment, check fields from parent assignment statement.
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		Type xType = (Type) pixelSelector.getX().visit(this, arg);
		check(xType == Type.INT, pixelSelector.getX(), "only ints as pixel selector components");
		Type yType = (Type) pixelSelector.getY().visit(this, arg);
		check(yType == Type.INT, pixelSelector.getY(), "only ints as pixel selector components");
		return null;
	}

	@Override
	//This method several cases--you don't have to implement them all at once.
	//Work incrementally and systematically, testing as you go.
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws Exception {
		boolean isCompatible = false;

		Declaration targetDecleration = symbolTable.lookup(assignmentStatement.getName());
		check(targetDecleration !=null, assignmentStatement, "var is not declared: " + assignmentStatement.getName());

		Type expressionType = null;
		Type targetType= targetDecleration.getType();
		if(assignmentStatement.getSelector()==null) expressionType =(Type) assignmentStatement.getExpr().visit(this, arg);

		assignmentStatement.setTargetDec(targetDecleration);
		if(targetType != IMAGE){
			check(assignmentStatement.getSelector()==null, assignmentStatement, "not image data type cannnot have pixel selector");
			if(targetType==expressionType) isCompatible = true;
			else if (((targetType==INT && expressionType==COLOR) || (targetType==COLOR && expressionType ==INT) || (targetType==INT && expressionType ==FLOAT) || (targetType==FLOAT && expressionType==INT) )){
				isCompatible=true;
				//set coercion
				assignmentStatement.getExpr().setCoerceTo(targetType);
			}
		}
		else{
			if(assignmentStatement.getSelector() == null){
				if(expressionType ==COLOR || expressionType ==COLORFLOAT || expressionType ==INT || expressionType ==FLOAT){
					isCompatible  =true;
					if(expressionType==INT) assignmentStatement.getExpr().setCoerceTo(COLOR);
					else if(expressionType==FLOAT) assignmentStatement.getExpr().setCoerceTo(COLORFLOAT);
				}
			}
			else{
				String X=assignmentStatement.getSelector().getX().getText();
				String Y =assignmentStatement.getSelector().getY().getText();
				check(symbolTable.lookup(X)==null && symbolTable.lookup(Y)==null, assignmentStatement,
						"variables in pixel selector ARE NOT COMPATIBLE WITH GLOBAL SCOPE");
				// CAST TYPE

				assignmentStatement.getSelector().getY().setType(INT);


				assignmentStatement.getSelector().getX().setType(INT);

				check(assignmentStatement.getSelector().getX() instanceof IdentExpr &&
								assignmentStatement.getSelector().getY() instanceof IdentExpr , assignmentStatement,
						" left side has no identity");

				NameDef xND = new NameDef(assignmentStatement.getFirstToken(), "int", "x");
				NameDef yND = new NameDef(assignmentStatement.getFirstToken(), "int", "y");

				VarDeclaration xDec = new VarDeclaration(assignmentStatement.getFirstToken(), xND, null, null);
				VarDeclaration yDec = new VarDeclaration(assignmentStatement.getFirstToken(), yND, null, null);

				symbolTable.insert(X, xDec);
				symbolTable.insert(Y, yDec);

				xDec.setInitialized(true);
				yDec.setInitialized(true);

				expressionType =(Type) assignmentStatement.getExpr().visit(this, arg);
				if (expressionType == COLORFLOAT || expressionType == FLOAT || expressionType == INT || expressionType == COLOR) {
					isCompatible = true;
					if (expressionType == INT || expressionType == COLORFLOAT || expressionType == FLOAT) {
						assignmentStatement.getExpr().setCoerceTo(COLOR);
					}
				}

				symbolTable.remove(X);
				symbolTable.remove(Y);

				xDec.setInitialized(false);
				yDec.setInitialized(false);
			}
		}

		check(isCompatible, assignmentStatement, "not compatible: "
				+ assignmentStatement.getName());

		// initialize
		targetDecleration.setInitialized(true);
		return null;
	}


	@Override
	public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws Exception {
		Type sourceType = (Type) writeStatement.getSource().visit(this, arg);
		Type destType = (Type) writeStatement.getDest().visit(this, arg);
		check(destType == Type.STRING || destType == Type.CONSOLE, writeStatement,
				"illegal destination type for write");
		check(sourceType != Type.CONSOLE, writeStatement, "illegal source type for write");
		return null;
	}

	@Override
	public Object visitReadStatement(ReadStatement readStatement, Object arg) throws Exception {
		String lhsName = readStatement.getName();
		Declaration lhsDec = symbolTable.lookup(lhsName);
		readStatement.setTargetDec(lhsDec);
		Type targetType = lhsDec.getType();

		//A read statement can't have a pixel selector (newly added constraint)
		check(readStatement.getSelector() == null, readStatement, "read statement cannot have pixel selector");

		Type rhsType = (Type) readStatement.getSource().visit(this, arg);
		check(rhsType == Type.CONSOLE || rhsType == Type.STRING, readStatement, "source (rhs) must be console or string");

		readStatement.getTargetDec().setInitialized(true);
		return null;
	}
	@Override
	public Object visitVarDeclaration(VarDeclaration declaration, Object arg) throws Exception {
		Type declarativeType =(Type)declaration.getNameDef().visit(this, arg);
		Type expressionType=null;
		boolean isCompatible=false;

		if(declaration.getOp()!=null) {
			expressionType = (Type) declaration.getExpr().visit(this, arg);
		}
		if(declarativeType==IMAGE){
			boolean isIMGorDIM = (expressionType==IMAGE || declaration.getNameDef().getDim()!=null);
			check(isIMGorDIM, declaration, " must be assigned either as an Image or have a Dimension");
			isCompatible = true;
		}
		else if(declaration.getOp()==null){
			return null;
		}
		else if(declaration.getOp().getKind()==Kind.ASSIGN){
			if(declarativeType==expressionType) isCompatible=true;
			else if ((declarativeType==INT && expressionType==FLOAT) || (declarativeType==FLOAT && expressionType==INT) ||
					(declarativeType==INT && expressionType==COLOR) || (declarativeType==COLOR && expressionType==INT)){
				declaration.getExpr().setCoerceTo(declarativeType);
				isCompatible=true;
			}
		}
		else if(declaration.getOp().getKind()==Kind.LARROW){
			boolean isExpressionConsoleOrString = (expressionType==CONSOLE || expressionType==STRING);
			check(isExpressionConsoleOrString, declaration, "Right side must be CONSOLE or STRING");
		}
		declaration.getNameDef().setInitialized(true);
		return null;
	}


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		//Save root of AST so return type can be accessed in return statements
		root = program;

		List<NameDef> params=program.getParams();
		for (NameDef node : params) {
			node.visit(this, arg);
			node.setInitialized(true);
		}
		//Check declarations and statements
		List<ASTNode> decsAndStatements = program.getDecsAndStatements();
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}
		return program;
	}

	@Override
	public Object visitNameDef(NameDef nameDef, Object arg) throws Exception {
		if (!symbolTable.insert(nameDef.getName(), nameDef)){
			throw new TypeCheckException(nameDef.getName() + " already exists");
		}
		return nameDef.getType();
	}


	@Override
	public Object visitNameDefWithDim(NameDefWithDim nameDefWithDim, Object arg) throws Exception {
		//TODO:  implement this method


		String name = nameDefWithDim.getName();
		boolean inserted = symbolTable.insert(name, nameDefWithDim);
		check(inserted, nameDefWithDim, "variable " + name + " already declared");

		check(symbolTable.lookup(nameDefWithDim.getDim().getWidth().getText()) != null, nameDefWithDim.getDim().getWidth(), "not declared width");
		check(symbolTable.lookup(nameDefWithDim.getDim().getHeight().getText()) != null, nameDefWithDim.getDim().getHeight(), "not declared height");

		Type width = (Type) nameDefWithDim.getDim().getWidth().visit(this, arg);
		Type height = (Type) nameDefWithDim.getDim().getHeight().visit(this, arg);

		check( height == INT, nameDefWithDim.getDim().getHeight(), "bad type supposed to be int");
		check(width == INT , nameDefWithDim.getDim().getWidth(), "bad type supposed to be int");


		return null;
	}
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws Exception {
		Type returnType = root.getReturnType();  //This is why we save program in visitProgram.
		Type expressionType = (Type) returnStatement.getExpr().visit(this, arg);
		check(returnType == expressionType, returnStatement, "return statement with invalid type");
		return null;
	}

	@Override
	public Object visitUnaryExprPostfix(UnaryExprPostfix unaryExprPostfix, Object arg) throws Exception {
		Type expType = (Type) unaryExprPostfix.getExpr().visit(this, arg);
		check(expType == Type.IMAGE, unaryExprPostfix, "pixel selector can only be applied to image");
		unaryExprPostfix.getSelector().visit(this, arg);
		unaryExprPostfix.setType(Type.INT);
		unaryExprPostfix.setCoerceTo(COLOR);
		return Type.COLOR;
	}

}