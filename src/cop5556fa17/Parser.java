package cop5556fa17;

import static cop5556fa17.Scanner.Kind.*;


import cop5556fa17.AST.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;

public class Parser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;
	private static final Set<Kind> unaryset = new HashSet<>(Arrays.asList(
			KW_x,KW_y,KW_r,KW_a,KW_X,KW_Y,KW_Z,KW_A,KW_R,
			KW_DEF_X,KW_DEF_Y,INTEGER_LITERAL,BOOLEAN_LITERAL));
	
	private static final Set<Kind> functionnameset = new HashSet<>(Arrays.asList(
			KW_sin,KW_cos,KW_atan,KW_abs,KW_cart_x,KW_cart_y,KW_polar_a,KW_polar_r));
	public static final List<HashSet<Kind>> operators = new ArrayList<>(Arrays.asList(
			new HashSet<>(Arrays.asList(Kind.OP_OR)),
			new HashSet<>(Arrays.asList(Kind.OP_AND)),
			new HashSet<>(Arrays.asList(Kind.OP_EQ,Kind.OP_NEQ)),
			new HashSet<>(Arrays.asList(Kind.OP_LT,Kind.OP_GT,Kind.OP_LE,Kind.OP_GE)),
			new HashSet<>(Arrays.asList(Kind.OP_PLUS,Kind.OP_MINUS)),
			new HashSet<>(Arrays.asList(Kind.OP_TIMES,Kind.OP_DIV,Kind.OP_MOD))
			));

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	Program program() throws SyntaxException {
		Token firstToken = null;
		Token name = null;
		ArrayList<ASTNode> decsAndStatements = new ArrayList<>();
		// Starts with an identifier
		// store the identifier
		firstToken = t; // first token is the identifier
		name = t; // program name is in the identifier
		if(!match(t.kind,Kind.IDENTIFIER)){
			throwerror(Kind.IDENTIFIER.toString() + "at the start of the program");	
		}

		while (true) {
			Token type = t;
			if (match(t.kind, Kind.KW_int) || match(t.kind, Kind.KW_boolean)) {
				// variable declaration
			    decsAndStatements.add(variabledeclaration(type));
			} else if (match(t.kind, Kind.KW_image)) {
				// image declaration
				decsAndStatements.add(imagedeclaration(type));
			} else if (match(t.kind, Kind.KW_url) || match(t.kind, Kind.KW_file)) {
				// source sink declaration
				decsAndStatements.add(sourcesinkdeclaration(type));
			} else if (match(t.kind, Kind.IDENTIFIER)) {
				// statement
				decsAndStatements.add(statement(type));
			} else {
				// end of program break
				break;
			}

			if (!match(t.kind, Kind.SEMI)) {
				throwerror(Kind.SEMI.toString());
			}
		}
		return new Program(firstToken, name, decsAndStatements);
		
	}

	
	private Statement statement(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		//statement first token is the identifier
		if(match(t.kind,Kind.OP_ASSIGN)){
			return assignmentstatement(firstToken);
		}
		else if(match(t.kind,Kind.LSQUARE)){
			return arrayassignmentstatement(firstToken);
		}
		else if(match(t.kind,Kind.OP_RARROW)){
			return imageoutstatement(firstToken);
		}
		else if(match(t.kind,Kind.OP_LARROW)){
			return imageinstatement(firstToken);
		}
		else{
			
			throwerror("a valid assignment operator");
		}
		return null; // unreachable code
	}

	private Statement imageinstatement(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		Source source = source();
		//firstToken and name are same
		return new Statement_In(firstToken, firstToken, source);
	}

	private Statement imageoutstatement(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		Sink sink =sink();
		//firstToken and name are same
		return new Statement_Out(firstToken, firstToken, sink);
	}

	private Sink sink() throws SyntaxException {
		// TODO Auto-generated method stub
		Token token = t;
		if(match(t.kind,Kind.IDENTIFIER)){
			//firstToken and name are same
			return new Sink_Ident(token, token);
		}
		else if(match(t.kind,Kind.KW_SCREEN)){
			return new Sink_SCREEN(token);
		}
		else{
			throwerror(" valid sink");
		}
		return null; // unreachable code
	}

	private Statement arrayassignmentstatement(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		Index index = lhsselector();
		if(!match(t.kind,Kind.RSQUARE)){
			throwerror("closing square bracket in lhs of "
					+ "assignment statement");
		}
		if(!match(t.kind,Kind.OP_ASSIGN)){
			throwerror("assignment operator");
		}
		//firstToken is the identifer, name is also indentifier
		return new Statement_Assign(firstToken, new LHS(firstToken, firstToken, index), expression());
	}

	private Index lhsselector() throws SyntaxException {
		// TODO Auto-generated method stub
		Index index  = null;

		if(!match(t.kind,Kind.LSQUARE)){
			throwerror("opening square bracket in lhs of "
					+ "assignment statement");
		}
		Token firstToken  = t;
		Token secondToken = null;
		if(match(t.kind,Kind.KW_x)){
			if(!match(t.kind,Kind.COMMA)){
				throwerror("comma");
			}
			secondToken = t;
			if(!match(t.kind,Kind.KW_y)){
				throwerror(Kind.KW_y.toString());
			}
		}
		else if(match(t.kind,Kind.KW_r)){
			if(!match(t.kind,Kind.COMMA)){
				throwerror("comma");
			}
			secondToken = t;
			if(!match(t.kind,Kind.KW_A)){
				throwerror(Kind.KW_A.toString());
			}
		}
		else {
			throwerror("selector type in lhs of "
					+ "assignment statement");
		}
		
		if(!match(t.kind,Kind.RSQUARE)){
			throwerror("closing square bracket in lhs of "
					+ "assignment statement");
		}
		//firsttoken is either KW_x or KW_r
		index = new Index(firstToken, new Expression_PredefinedName(firstToken, firstToken.kind), 
				 new Expression_PredefinedName(secondToken, secondToken.kind));
		return index;
	}

	private Statement assignmentstatement(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		//firsttoken is identifier
		return new Statement_Assign(firstToken, new LHS(firstToken, firstToken, null), expression());
	}

	private Declaration_SourceSink sourcesinkdeclaration(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		Token name = t;
		if(!match(t.kind,Kind.IDENTIFIER)){
			throwerror(Kind.IDENTIFIER.toString()+ " in sourcesink declaration");
		}
		if(!match(t.kind,Kind.OP_ASSIGN)){
			throwerror(Kind.OP_ASSIGN.toString()+ " in sourcesink declaration");
		}
		Source source = source();
		//firsttoken is either KW_url or KW_file which is also the type
		return new Declaration_SourceSink(firstToken, firstToken, name, source);
	}

	private Declaration_Image imagedeclaration(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		Expression xSize = null,ySize = null;
		Source source = null;
		Token name = null;
		//if it does not match LSQUARE, need not throw error 
		// as it is not mandatory
		if(match(t.kind,Kind.LSQUARE)){
			Index index = selector();
			if(!match(t.kind,Kind.RSQUARE)){
				throwerror(Kind.RSQUARE.toString()+ "in image declaration");
			}
			xSize = index.e0;
			ySize = index.e1;
		}
		name = t;
		if(!match(t.kind,Kind.IDENTIFIER)){
			throwerror(Kind.IDENTIFIER.toString()+ "in image declaration");
		}
		
		// if it does not match, then the expected is semi
		if(match(t.kind,Kind.OP_LARROW)){
			source = source();
		}
		//firsttoken is KW_IMAGE
		return new Declaration_Image(firstToken, xSize, ySize, name, source);
	}

	private Source source() throws SyntaxException {
		// TODO Auto-generated method stub
		Token token = t;
		if(match(t.kind,Kind.STRING_LITERAL)){
			//good to go
			return new Source_StringLiteral(token, token.getText());
		}
		else if(match(t.kind,Kind.IDENTIFIER)){
			return new Source_Ident(token, token);
		}
		else if(match(t.kind,Kind.OP_AT)){
			return new Source_CommandLineParam(token, expression());
		}
		else{
			throwerror("valid source type");
		}
		return null;
		
	}

	private Declaration_Variable variabledeclaration(Token type) throws SyntaxException{
		// TODO Auto-generated method stub
		Token name = t;
		if(!match(t.kind,Kind.IDENTIFIER)){
			throwerror(Kind.IDENTIFIER.toString()+"in variable declaration");			
		}
		Expression e = null;
		// the next token should be assign or could be end of variable declaration 
		if(match(t.kind,Kind.OP_ASSIGN)){
			//handle expression
			e = expression();
		}
		// Q: what is the firsttoken?
		return new Declaration_Variable(type, type, name, e);
	}

	private void nexttoken() {
		// TODO Auto-generated method stub
		t = scanner.nextToken();
	}

	private boolean match(Kind actual, Kind expected) {
		// TODO Auto-generated method stub
		if(actual == expected)nexttoken();
		return actual == expected;
	}

	void throwerror(String type) throws SyntaxException{
		String message =  "Expected " + type + " at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message); 
	}
	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * @return 
	 * 
	 * @throws SyntaxException
	 */
	public Expression expression() throws SyntaxException {
		//TODO implement this.
		Expression e = evalexpression(0);

		if(match(t.kind,Kind.OP_Q)){
			Expression trueExpression = expression();
			if(!match(t.kind,Kind.OP_COLON)){
				throwerror(Kind.OP_COLON.toString());
			}
			Expression falseExpression = expression();
			//Q: what is the first token
			e = new Expression_Conditional(e.firstToken, e, trueExpression, falseExpression);
		}
		return e;
	}



	private Expression evalexpression(int count) throws SyntaxException {
		// TODO Auto-generated method stub
		if(count == operators.size()) return unaryexpression();
		Expression e0 = evalexpression(count+1);
		Expression e1 = null;
		Token op = t;
		while(match(t.kind,operators.get(count))){
			e1 = evalexpression(count+1);
			e0 = new Expression_Binary(e0.firstToken, e0, op, e1);
			op = t;
		}
		return e0;
	}

	/*private Expression andexpression() throws SyntaxException {
		// TODO Auto-generated method stub
		eqexpression();
		while(match(t.kind,Kind.OP_AND)){
			eqexpression();
		}
	}

	private void eqexpression() throws SyntaxException {
		// TODO Auto-generated method stub
		relexpression();
		while(match(t.kind,Kind.OP_EQ)|| match(t.kind,Kind.OP_NEQ)){
			relexpression();
		}
	}
	
	private void relexpression() throws SyntaxException {
		// TODO Auto-generated method stub
		addexpression();
		while(match(t.kind,Kind.OP_LT)|| match(t.kind,Kind.OP_GT)
				|| match(t.kind,Kind.OP_LE) || match(t.kind,Kind.OP_GE)){
			addexpression();
		}
	}

	private void addexpression() throws SyntaxException {
		// TODO Auto-generated method stub
		mulexpression();
		while(match(t.kind,Kind.OP_PLUS)|| match(t.kind,Kind.OP_MINUS)){
			mulexpression();
		}
	}
	
	private void mulexpression() throws SyntaxException {
		// TODO Auto-generated method stub
		unaryexpression();
		while(match(t.kind,Kind.OP_TIMES)|| match(t.kind,Kind.OP_DIV)
				|| match(t.kind,Kind.OP_MOD)){
			unaryexpression();
		}
	}
*/
	private Expression unaryexpression() throws SyntaxException {
		// TODO Auto-generated method stub
		Token token= t;
		if(match(t.kind, Kind.OP_PLUS) || match(t.kind, Kind.OP_MINUS) || 
				match(t.kind, Kind.OP_EXCL)){
			return new Expression_Unary(token, token, unaryexpression());
		}
		else if(match(t.kind,unaryset)){
			// end of the expression
			if(token.kind == Kind.INTEGER_LITERAL){
				return new Expression_IntLit(token, token.intVal());
			}
			else if(token.kind == Kind.BOOLEAN_LITERAL){
				return new Expression_BooleanLit(token, token.booleanVal());
			}
			else {
				return new Expression_PredefinedName(token, token.kind);
			}
		}
		else if(match(t.kind,Kind.LPAREN)){
			Expression e= expression();
			if(!match(t.kind,Kind.RPAREN)){
				throwerror(Kind.RPAREN.toString());
			}
			return e;
			// end of the expression
		}
		else if(match(t.kind,functionnameset)){
			//function application
			return function(token);
		}
		else if(match(t.kind,Kind.IDENTIFIER)){
			return identorpixelexpression(token);
			// end of the expression
		}
		else{
			throwerror("unary expression");
		}
		return null;
	}

	private Expression identorpixelexpression(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		//Potential issue
		if(match(t.kind,Kind.LSQUARE)){
			Index index = selector();
			if(!match(t.kind,Kind.RSQUARE)){
				throwerror(Kind.RSQUARE.toString());
			}
			return new Expression_PixelSelector(firstToken, firstToken, index);
		}
		else{
			return new Expression_Ident(firstToken, firstToken);
		}
		
	}

	private Expression function(Token firstToken) throws SyntaxException {
		// TODO Auto-generated method stub
		if(match(t.kind,Kind.LPAREN)){
			Expression e = expression();
			if(!match(t.kind,Kind.RPAREN)){
				throwerror(Kind.RPAREN.toString());
			}
			return new Expression_FunctionAppWithExprArg(firstToken, firstToken.kind, e);
		}
		else if(match(t.kind,Kind.LSQUARE)){
			Index i = selector();
			if(!match(t.kind,Kind.RSQUARE)){
				throwerror(Kind.RSQUARE.toString());
			}
			return new Expression_FunctionAppWithIndexArg(firstToken, firstToken.kind, i);
		}
		else{
			throwerror("opening square bracket or parentheses");
		}
		return null;
	}

	private Index selector() throws SyntaxException {
		// TODO Auto-generated method stub
		Expression e0 = expression();
		if(!match(t.kind,Kind.COMMA)){
			throwerror(Kind.COMMA.toString());
		}
		Expression e1 = expression();
		return new Index(e0.firstToken, e0, e1);
	}

	private boolean match(Kind kind, Set<Kind> set) {
		// TODO Auto-generated method stub
		if(set.contains(kind)) nexttoken();
		return set.contains(kind);
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
