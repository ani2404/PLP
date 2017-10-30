/**
 * /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
 */

package cop5556fa17;

import static cop5556fa17.Scanner.Kind.BOOLEAN_LITERAL;
import static cop5556fa17.Scanner.Kind.IDENTIFIER;
import static cop5556fa17.Scanner.Kind.INTEGER_LITERAL;
import static cop5556fa17.Scanner.Kind.KW_a;
import static cop5556fa17.Scanner.Kind.OP_DIV;
import static cop5556fa17.Scanner.Kind.SEMI;
import static cop5556fa17.Scanner.Kind.STRING_LITERAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(scanner.new Token(kind, pos, length, line, pos_in_line), t);
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token check(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}

	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, as we will want to do 
	 * later, the end of line character would be inserted by the text editor.
	 * Showing the input will let you check your input is what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n ;; ";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNext(scanner, SEMI, 5, 1, 2, 3);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it a String literal
	 * that is missing the closing ".  
	 * 
	 * Note that the outer pair of quotation marks delineate the String literal
	 * in this test program that provides the input to our Scanner.  The quotation
	 * mark that is actually included in the input must be escaped, \".
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failUnclosedStringLiteral() throws LexicalException {
		String input = "\" greetings  ";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(13,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void earlyTermination() throws LexicalException {
		String input = "\0";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(0, e.getPos());
			throw e;
		}
	}

	@Test
	public void SingleForwardSlash() throws LexicalException {
		String input = "/";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_DIV, 0, 1, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void DoubleForwardSlash() throws LexicalException {
		String input = "//anirudh\r\nsarma";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 11, 5, 2, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void IdentifierTest() throws LexicalException {
		String input = "truef _a1$ x1 $1 2a";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 5, 1, 1);
		checkNext(scanner, IDENTIFIER, 6, 4, 1, 7);
		checkNext(scanner, IDENTIFIER, 11, 2, 1, 12);
		checkNext(scanner, IDENTIFIER, 14, 2, 1, 15);
		checkNext(scanner, INTEGER_LITERAL, 17, 1, 1, 18);
		checkNext(scanner, KW_a, 18, 1, 1, 19);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void BooleanLiteralTest() throws LexicalException {
		String input = "true false";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, BOOLEAN_LITERAL, 0, 4, 1, 1);
		checkNext(scanner, BOOLEAN_LITERAL, 5, 5, 1, 6);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void KeywordTest() throws LexicalException {
		String input = "x X y Y r R a A Z DEF_X DEF_Y SCREEN cart_x cart_y polar_a polar_r abs sin cos atan log image int boolean url file";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		int pos = 0;
		int posInLine = 1;
		int line = 1;
		String[] keyword = input.split(" ");
		for(int index=4,i=0;index < 30;index+=1,i+=1){

			Scanner.Kind kind = Scanner.Kind.values()[index];
			checkNext(scanner, kind, pos, keyword[i].length(), line, posInLine);
			pos += (keyword[i].length()+1);
			posInLine = pos +1;
		}
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void SeparatorTest() throws LexicalException {
		String input = "()[];,";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		int pos = 0;
		int posInLine = 1;
		int line = 1;
		for(int index=51;index < 57;index+=1){

			Scanner.Kind kind = Scanner.Kind.values()[index];
			checkNext(scanner, kind, pos, 1, line, posInLine);
			pos += 1;
			posInLine = pos +1;
		}
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void OperatorAndWhiteSpaceTest() throws LexicalException {
		String input = "= >\t<\f! ? : == != >= <= & | + - * / % ** @ -> <-";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		int pos = 0;
		int posInLine = 1;
		int line = 1;
		String[] keyword = input.split("[ \t\f]");
		for(int index=30,i=0;index < 51;index+=1,i+=1){

			Scanner.Kind kind = Scanner.Kind.values()[index];
			checkNext(scanner, kind, pos, keyword[i].length(), line, posInLine);
			pos += (keyword[i].length()+1);
			posInLine = pos +1;
		}
		checkNextIsEOF(scanner);
	}

	@Test
	public void DigitTest() throws LexicalException {
		String input = "01230";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 4, 1, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void OverflowTest() throws LexicalException {
		String input = "2147483648";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(0, e.getPos());
			throw e;
		}
	}
	
	@Test
	public void stringLiteralTest() throws LexicalException {
		String input = "\"anirudh\\b\\t\\n\\f\\r\\\"\\'\\\\\"";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, STRING_LITERAL,0,25,1,1);
		checkNextIsEOF(scanner);
		
	}
	
	@Test
	public void stringLiteralInvalidEscapeTest() throws LexicalException {
		String input = "\"anirudh\\b\\t\\n\\f\\r\\\"\\'\\\\\\w\"";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(25, e.getPos());
			throw e;
		}
		
	}
	
	@Test
	public void stringLiteralInvalidEscapeTerminationTest() throws LexicalException {
		String input = "\"anirudh\\b\\t\\n\\f\\r\\\"\\'\\\\\\";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(25, e.getPos());
			throw e;
		}
		
	}
	
	@Test
	public void stringLiteralLineTerminatorTest() throws LexicalException {
		String input = "\"anirudh\n\"";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(8, e.getPos());
			throw e;
		}
		
	}
	
	@Test
	public void stringLiteralLineTerminatorTest1() throws LexicalException {
		String input = "\"anirudh\r\"";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(8, e.getPos());
			throw e;
		}
		
	}
}
