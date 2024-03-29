package cop5556fa17;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.AST.*;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Program;
import cop5556fa17.Parser.SyntaxException;
import static cop5556fa17.Scanner.Kind.*;

public class ParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	public List<String> unaryexpressions = new ArrayList<>(Arrays.asList(
			"x","y","r","a","X","Y","Z","A","R","DEF_X","DEF_Y","2000","true","false",
			"sin(x)","cos(x)","atan(y)","abs(x)","cart_x[r,A]","cart_y[r,A]","polar_a[x,y]","polar_r[x,y]",
			"+-!x","var","var[first,second]","(var[first,second])"));
	
	

	public List<String> expressions = new ArrayList<>(unaryexpressions);
	
	public List<String> invalidunaryexpressions = new ArrayList<>(Arrays.asList(
			"@","sin x)","cos (x","atan (@)","cart_x r,A]","cart_y [r,A","polar_a [@,y]","polar_r [x,@]",
			"+-!@","var first,second]","var[first,second","(var[first,second]"));
	
	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. Parsing should fail
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the tokens
		Parser parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse(); //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}


	@Test
	public void testNameOnly() throws LexicalException, SyntaxException {
		String input = "prog";  //Legal program with only a name
		show(input);            //display input
		Scanner scanner = new Scanner(input).scan();   //Create scanner and create token list
		show(scanner);    //display the tokens
		Parser parser = new Parser(scanner);   //create parser
		Program ast = parser.parse();          //parse program and get AST
		show(ast);                             //Display the AST
		assertEquals(ast.name, "prog");        //Check the name field in the Program object
		assertTrue(ast.decsAndStatements.isEmpty());   //Check the decsAndStatements list in the Program object.  It should be empty.
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec = (Declaration_Variable) ast.decsAndStatements
				.get(0);  
		assertEquals(KW_int, dec.type.kind);
		assertEquals("k", dec.name);
		assertNull(dec.e);
	}
	
	@Test
	public void testcase1() throws SyntaxException, LexicalException {
		String input = "2";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	@Test
	public void testcase2() throws SyntaxException, LexicalException {
		String input = "a bcd";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void testcase3() throws SyntaxException, LexicalException {
		String input = "cart_x cart_y";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void testcase4() throws SyntaxException, LexicalException {
		String input = "prog int 2";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void testcase5() throws SyntaxException, LexicalException {
		String input = "prog image[filepng,png] imageName <- imagepng"; //Error as there is not semicolon for ending the statement
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		//Parser.program();
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void testcase6() throws SyntaxException, LexicalException {
		String input = "imageDeclaration image[\"abcd\"] "; //Should fail for image[""]
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void testcase7() throws SyntaxException, LexicalException {
		String input = "prog image[filepng,png] imageName <- imagepng; \n boolean ab=true;"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();
		show(ast);
		assertEquals("prog",ast.name);
		// First Declaration statement
		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements.get(0);  
		assertEquals(KW_image, dec.firstToken.kind);
		assertEquals("imageName", dec.name);
		Expression_Ident ei=(Expression_Ident)dec.xSize;
		assertEquals("filepng",ei.name);
		ei=(Expression_Ident)dec.ySize;
		assertEquals("png",ei.name);
		Source_Ident s=(Source_Ident) dec.source;
	    assertEquals("imagepng",s.name);
		// Second Declaration statement
	    Declaration_Variable dec2 = (Declaration_Variable) ast.decsAndStatements.get(1);  
		assertEquals("ab", dec2.name);
		assertEquals(KW_boolean, dec2.firstToken.kind);
		Expression_BooleanLit ebi=(Expression_BooleanLit)dec2.e;
		assertEquals(true,ebi.value);		
	}
	
	@Test
	public void testcase8() throws SyntaxException, LexicalException {
		String input = "prog image[filepng,jpg] imageName;"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();
		show(ast);
		assertEquals("prog",ast.name);
		Declaration_Image dec1 = (Declaration_Image) ast.decsAndStatements.get(0); 
		assertEquals(dec1.name,"imageName");
		Expression_Ident exi=(Expression_Ident)dec1.xSize;
		Expression_Ident eyi=(Expression_Ident)dec1.ySize;
		assertEquals(exi.name,"filepng");
		assertEquals(eyi.name,"jpg");
		assertNull(dec1.source);
	}
	
	@Test
	public void testcase10() throws SyntaxException, LexicalException {
		String input = "prog @expr k=12;"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();  //Parse the program
		show(ast);
		assertEquals(ast.name,"prog");
		assertEquals(ast.decsAndStatements.size(),0);
	}
	
	@Test
	public void testcase10parse() throws SyntaxException, LexicalException {
		String input = "prog @expr k=12;"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast=parser.parse();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void testcase11() throws SyntaxException, LexicalException {
		String input = "prog \"abcded\" boolean a=true;"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();  //Parse the program
		show(ast);
		assertEquals(ast.name,"prog");
		assertEquals(ast.decsAndStatements.size(),0);
	}
	
	@Test
	public void testcase11_parse() throws SyntaxException, LexicalException {
		String input = "prog \"abcded\" boolean a=true;"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast=parser.parse();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	

	@Test
	public void testcase12() throws SyntaxException, LexicalException {
		String input = "isBoolean boolean ab=true; boolean cd==true; abcd=true ? return true: return false;"; //Should fail for ==
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast=parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void testcase13() throws SyntaxException, LexicalException {
		String input = "isBoolean boolean ab=true; boolean cd==true; abcd=true ? return true: return false;"; //Should fail for =
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast=parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		} 
	}
	
	@Test
	public void testcase14() throws SyntaxException, LexicalException {
		String input = "isUrl url filepng=\"abcd\"; \n @expr=12; url awesome=@expr; \n url filepng=abcdefg"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();  //Parse the program
		show(ast);
		assertEquals(ast.name,"isUrl");
		assertEquals(ast.decsAndStatements.size(),1);
		Declaration_SourceSink dss=(Declaration_SourceSink)ast.decsAndStatements.get(0);
		assertEquals(dss.name,"filepng");
		assertEquals(dss.type,KW_url);
		Source_StringLiteral s=(Source_StringLiteral)dss.source;
		assertEquals(s.fileOrUrl,"abcd");
	}
	
	@Test
	public void testcase14_parse() throws SyntaxException, LexicalException {
		String input = "isUrl url filepng=\"abcd\"; \n @expr=12; url awesome=@expr; \n url filepng=abcdefg"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast=parser.parse();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}  
	}
	
	@Test
	public void testcase15() throws SyntaxException, LexicalException {
		String input = "isUrl url filepng=\"abcd\" \n @expr=12; url awesome=@expr; \n url filepng=abcdefg"; //Should fail for ; in line one
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast=parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}  
	}
	
	@Test
	public void testcase16() throws SyntaxException, LexicalException {
		String input = "isFile file filepng=\"abcd\"; \n @expr=12; url filepng=@expr; \n url filepng=abcdefg"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();  //Parse the program
		show(ast);
		assertEquals(ast.name,"isFile");
		assertEquals(ast.decsAndStatements.size(),1);
		assertEquals(ast.firstToken.kind,IDENTIFIER);
		
		// Declaration Statements
		Declaration_SourceSink ds=(Declaration_SourceSink)ast.decsAndStatements.get(0);
		assertEquals(ds.type,KW_file);
		assertEquals(ds.name,"filepng");
		Source_StringLiteral s=(Source_StringLiteral)ds.source;
		assertEquals(s.fileOrUrl,"abcd");
		//assertEquals(ast.)
	}
	
	@Test
	public void testcase16_parse() throws SyntaxException, LexicalException {
		String input = "isFile file filepng=\"abcd\"; \n @expr=12; url filepng=@expr; \n url filepng=abcdefg"; 
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast=parser.parse();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}  
	}
	
	@Test
	public void testcase17() throws SyntaxException, LexicalException {
		String input =  "isFile file filepng=\"abcd\" \n @expr=12; url filepng=@expr; \n url filepng=abcdefg";  //Should fail for ; in line one
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast=parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}  
	}
	
	@Test
	public void testcase18() throws SyntaxException, LexicalException {
		String input =  "isurl url urlname;";  //Should fail for url as url can only be initalised
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast=parser.program();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}  
	}
	
	@Test
	public void testcase19() throws SyntaxException, LexicalException {
		String input =  "declaration int xyz;\n boolean zya;\n image imagename;";  
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();  //Parse the program
		show(ast);
		assertEquals(ast.name,"declaration");
		assertEquals(ast.firstToken.kind,IDENTIFIER);
		
		//Declaration statements start
		Declaration_Variable dv1=(Declaration_Variable)ast.decsAndStatements.get(0);
		assertEquals(dv1.name,"xyz");
		assertEquals(dv1.type.kind,KW_int);
		assertNull(dv1.e);
		
		Declaration_Variable dv2=(Declaration_Variable)ast.decsAndStatements.get(1);
		assertEquals(dv2.name,"zya");
		assertEquals(dv2.type.kind,KW_boolean);
		assertNull(dv2.e);
		
		Declaration_Image dv3=(Declaration_Image)ast.decsAndStatements.get(2);	
		assertEquals(dv3.name,"imagename");
		assertNull(dv3.source);
		assertNull(dv3.xSize);
		assertNull(dv3.ySize);
		
		//Declaration statement end
	}
	
	@Test
	public void testcase20() throws SyntaxException, LexicalException {
		String input =  "imageProgram image imageName;"
				+ "\n imageName->abcdpng; "
				+ "\n imageName -> SCREEN; "
				+ "\n imageName <- \"awesome\";"
				+ "\n imageName <- @express; \n"
				+ "\n imageName <- abcdpng;";  // Image related Test cases
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		Parser parser = new Parser(scanner);
		Program ast=parser.program();  //Parse the program
		show(ast);
		assertEquals(ast.name,"imageProgram");
		
		//Declaration statement start
		Declaration_Image dv1=(Declaration_Image)ast.decsAndStatements.get(0);
		assertEquals(dv1.name,"imageName");
		assertNull(dv1.xSize);
		assertNull(dv1.ySize);
		assertNull(dv1.source);
		
		Statement_Out dv2=(Statement_Out)ast.decsAndStatements.get(1);
		assertEquals(dv2.name,"imageName");
		Sink_Ident si2=(Sink_Ident)dv2.sink;
		assertEquals(si2.name,"abcdpng");
	}
	

	
	}

