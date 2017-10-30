package cop5556fa17;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

import cop5556fa17.TypeUtils.Type;
import static cop5556fa17.Parser.operators;

public class TypeCheckVisitor implements ASTVisitor {
	
		SymbolTable table;
		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		

	public TypeCheckVisitor() {
		// TODO Auto-generated constructor stub
		table = SymbolTable.getSymbolTable();
	}
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_Variable.name;
		if(isDuplicate(name)) {
			throw new SemanticException(declaration_Variable.firstToken, "Variable name is already defined");
		}
		Expression e = declaration_Variable.e;
		e.visit(this, null);
		Type expectedType = TypeUtils.getType(declaration_Variable.type);
		if(e.nodetype != expectedType) {
			throw new SemanticException(declaration_Variable.firstToken, "Variable type and expression type mismatch");
		}
		declaration_Variable.nodetype = expectedType;
		table.add(name,declaration_Variable);
		return expectedType;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = expression_Binary.e0;
		Expression e1 = expression_Binary.e1;
		e0.visit(this,null);
		e1.visit(this, null);
		if(e0.nodetype != e1.nodetype){
			throw new SemanticException(expression_Binary.firstToken, "Expressions are not compatible with "
					+ "the binary operator");
		}
		Kind operator = expression_Binary.op;
		if(operators.get(0).contains(operator) || operators.get(1).contains(operator)){
			//AND,OR
			if(e0.nodetype != Type.INTEGER){
				throw new SemanticException(expression_Binary.firstToken, "Expressions must evaluate to"
						+ "type Integer");
			}
			expression_Binary.nodetype = e0.nodetype;
		}
		else if(operators.get(2).contains(operator)){
			// EQ,NEQ
			expression_Binary.nodetype = Type.BOOLEAN;
		}
		else if(operators.get(3).contains(operator)){
			//GE,GT,LT,LE
			if(e0.nodetype != Type.INTEGER){
				throw new SemanticException(expression_Binary.firstToken, "Expressions must evaluate to"
						+ "type Integer");
			}
			expression_Binary.nodetype = Type.BOOLEAN;
		}
		else {
			//DIV,MINUS,MOD,PLUS,POWER,TIMES
			if(e0.nodetype != Type.INTEGER){
				throw new SemanticException(expression_Binary.firstToken, "Expressions must evaluate to"
						+ "type Integer");
			}
			expression_Binary.nodetype = Type.INTEGER;
		}
		
		return expression_Binary.nodetype;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type expressiontype = (Type)expression_Unary.e.visit(this, null);
		if(expression_Unary.op == Kind.OP_EXCL){
			if(expressiontype != Type.INTEGER && expressiontype != Type.BOOLEAN){
				throw new SemanticException(expression_Unary.firstToken, "Expression must evaluate to"
						+ "either Integer or Boolean");
			}
			expression_Unary.nodetype = expressiontype;
		}
		else {
			if(expressiontype != Type.INTEGER){
				throw new SemanticException(expression_Unary.firstToken, "Expression must evaluate to"
						+ "Integer");
			}
			expression_Unary.nodetype = Type.INTEGER;
		}
		return expression_Unary.nodetype;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type firsttype = (Type)index.e0.visit(this, null);
		Type secondtype = (Type)index.e1.visit(this, null);
		if(firsttype != Type.INTEGER || secondtype != Type.INTEGER){
			throw new SemanticException(index.firstToken, "Expression must evaluate to"
					+ "Integer");
		}
		index.nodetype = Type.INTEGER;
		boolean kwr = index.e0.getClass() == Expression_PredefinedName.class && 
				index.e0.firstToken.kind == Kind.KW_R;
		boolean kwa = index.e1.getClass() == Expression_PredefinedName.class && 
				index.e1.firstToken.kind == Kind.KW_A;
		index.setCartesian(kwr && kwa);
		return index.nodetype;
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(!table.contains(expression_PixelSelector.name)){
			throw new SemanticException(expression_PixelSelector.firstToken, expression_PixelSelector.name + "is not defined");
		}
		Type identifiertype = table.get(expression_PixelSelector.name).nodetype;
		expression_PixelSelector.index.visit(this, null);
		if(identifiertype != Type.IMAGE){
			throw new SemanticException(expression_PixelSelector.firstToken, "Identifier should be"
					+ "of type Image");
		}
		
		expression_PixelSelector.nodetype = Type.INTEGER;
		return expression_PixelSelector.nodetype;
		
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Type conditionaltype  = (Type)expression_Conditional.condition.visit(this, null);
		Type trueconditiontype = (Type)expression_Conditional.trueExpression.visit(this, null);
		Type falseconditiontype = (Type)expression_Conditional.falseExpression.visit(this, null);
		if(conditionaltype!= Type.BOOLEAN){
			throw new SemanticException(expression_Conditional.firstToken, "Conditional Expression must"
					+ "evaluate to boolean");
		}
		else if(trueconditiontype != falseconditiontype){
			throw new SemanticException(expression_Conditional.firstToken, "True Expression type must"
					+ "same as False expression");
		}
		expression_Conditional.nodetype = falseconditiontype;
		
		return expression_Conditional.nodetype;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_Image.name;
		if(isDuplicate(name)){
			throw new SemanticException(declaration_Image.firstToken, "Image name is already defined");
		}
		Expression xSize  = declaration_Image.xSize;
		Expression ySize  = declaration_Image.ySize;
		xSize.visit(this, null);
		ySize.visit(this, null);
		if(xSize.nodetype != ySize.nodetype || xSize.nodetype != Type.INTEGER){
			throw new SemanticException(declaration_Image.firstToken, "xSize and ySize expressions are not correct");
		}
		declaration_Image.source.visit(this, null);
		
		declaration_Image.nodetype = Type.IMAGE;
		table.add(name, declaration_Image);
		return declaration_Image.nodetype;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(isvalidURL(source_StringLiteral.fileOrUrl)){
			source_StringLiteral.nodetype = Type.URL;
		}
		else{
			source_StringLiteral.nodetype = Type.FILE;
		}
		return source_StringLiteral.nodetype;
	}

	private boolean isvalidURL(String fileOrUrl) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Type expressiontype = (Type) source_CommandLineParam.paramNum.visit(this, null);
		if(expressiontype != Type.INTEGER){
			throw new SemanticException(source_CommandLineParam.firstToken, ""
					+ "Expression must evaluate to Integer");
		}
		source_CommandLineParam.nodetype = Type.INTEGER;
		return source_CommandLineParam.nodetype;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(!table.contains(source_Ident.name)){
			throw new SemanticException(source_Ident.firstToken, source_Ident.name +"not defined");
		}
		Type sourcetype = table.get(source_Ident.name).nodetype;
		if(sourcetype != Type.FILE && sourcetype != Type.URL){
			throw new SemanticException(source_Ident.firstToken, "Source type must be a file or url");
		}
		source_Ident.nodetype = sourcetype;
		return source_Ident.nodetype;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = declaration_SourceSink.name;
		if(isDuplicate(name)){
			throw new SemanticException(declaration_SourceSink.firstToken,
				"Sourcesink name is already defined");
		}
		declaration_SourceSink.source.visit(this, null);
		Type expectedType = TypeUtils.getType(declaration_SourceSink.type);
		if(declaration_SourceSink.source.nodetype != expectedType){
			throw new SemanticException(declaration_SourceSink.firstToken,
					"Expected Type and source mismatch");
		}
		declaration_SourceSink.nodetype = expectedType;
		table.add(name, declaration_SourceSink);
		return declaration_SourceSink.nodetype;
	}

	private boolean isDuplicate(String name) {
		// TODO Auto-generated method stub
		return table.contains(name);
	}
	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_IntLit.nodetype = Type.INTEGER;
		return expression_IntLit.nodetype;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type expressiontype = (Type) expression_FunctionAppWithExprArg.arg.visit(this, null);
		if(expressiontype != Type.INTEGER){
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, ""
					+ "Expression must evaluate to Integer");
		}
		expression_FunctionAppWithExprArg.nodetype = Type.INTEGER;
		return expression_FunctionAppWithExprArg.nodetype;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		expression_FunctionAppWithIndexArg.arg.visit(this, null);
		expression_FunctionAppWithIndexArg.nodetype = Type.INTEGER;
		return expression_FunctionAppWithIndexArg.nodetype;
		
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_PredefinedName.nodetype = Type.INTEGER;
		return expression_PredefinedName;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = statement_Out.name;
		if(!table.contains(name)){
			throw new SemanticException(statement_Out.firstToken, name + "is not defined");
		}
		statement_Out.sink.visit(this, null);
		Type identifierType = table.get(name).nodetype;
		Type sinkType = statement_Out.sink.nodetype;
		if(!(
		(identifierType == Type.IMAGE && (sinkType == Type.FILE || sinkType == Type.SCREEN)) ||
	    ((identifierType == Type.INTEGER || identifierType == Type.BOOLEAN)&& sinkType == Type.SCREEN)
	    )){
			throw new SemanticException(statement_Out.firstToken, "sink type and identifer type mismatch");
		}
		statement_Out.nodetype = identifierType;
		return identifierType;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		String name = statement_In.name;
		if(!table.contains(name)){
			throw new SemanticException(statement_In.firstToken, name + "is not defined");
		}
		statement_In.source.visit(this, null);
		Type identifierType = table.get(name).nodetype;
		Type sourceType = statement_In.source.nodetype;
		if(sourceType != identifierType){
			throw new SemanticException(statement_In.firstToken, "Source type and identifer type mismatch");
		}
		statement_In.nodetype = identifierType;
		return identifierType;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type lhstype = (Type)statement_Assign.lhs.visit(this, null);
		Type expressionType = (Type) statement_Assign.e.visit(this, null);
		if(lhstype != expressionType){
			throw new SemanticException(statement_Assign.firstToken, "LHS type and Expression type mismatch");
		}
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		statement_Assign.nodetype = lhstype;
		
		return lhstype;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(!table.contains(lhs.name)){
			throw new SemanticException(lhs.firstToken, lhs.name +"not defined");
		}
		lhs.nodetype = table.get(lhs.name).nodetype;
		lhs.index.visit(this, null);
		lhs.setCartesian(lhs.index.isCartesian());
		return lhs.nodetype;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		sink_SCREEN.nodetype = Type.SCREEN;
		return sink_SCREEN.nodetype;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		if(!table.contains(sink_Ident.name)){
			throw new SemanticException(sink_Ident.firstToken, sink_Ident.name +"not defined");
		}
		Type sinktype = table.get(sink_Ident.name).nodetype;
		if(sinktype != Type.FILE){
			throw new SemanticException(sink_Ident.firstToken, "Sink type must be a file");
		}
		sink_Ident.nodetype = sinktype;
		return sink_Ident.nodetype;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expression_BooleanLit.nodetype = Type.BOOLEAN;
		return expression_BooleanLit.nodetype;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		if(!table.contains(expression_Ident.name)){
			throw new SemanticException(expression_Ident.firstToken, expression_Ident.name +"not defined");
		}
		
		expression_Ident.nodetype = Type.INTEGER;
		return expression_Ident.nodetype;
	}

}
