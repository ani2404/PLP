/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF,WHITESPACE,COMMENT;
	}

	
	public static final List<String> keywords = new ArrayList<> (Arrays.asList(
		"x","X","y","Y","r","R","a","A","Z","DEF_X","DEF_Y","SCREEN","cart_x","cart_y",
		"polar_a","polar_r","abs","sin","cos","atan","log","image","int","boolean","url",
		"file","=",">","<","!","?",":","==","!=",">=","<=","&","|","+","-","*","/","%","**",
		"@","->","<-","(",")","[","]",";",","
	));
	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}
		
		public boolean booleanVal() {
			assert kind == Kind.BOOLEAN_LITERAL;
			return String.copyValueOf(chars, pos, length).equals("true");
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;

	private static final char CR = 0x0D;

	private static final char LF = 0x0A;

	private static final char SP = 0x20;

	private static final char HT = 0x09;

	private static final char FF = 0x0C;

	private static final char DoubleQuote = 0x22;

	private static final char BackSlash = 0x5C;

	private static final char ForwardSlash = '/';

	private static final char ZERO = '0';
	
	private static final char SingleQuote = 0x27;
	
	
	
	static final Set<Character> digitSet = new HashSet<>(Arrays.asList('0','1','2','3','4','5',
			'6','7','8','9'));
	static final Set<Character> identifierStartSet = new HashSet<>(Arrays.asList('_','$',
			'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s',
			't','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L',
			'M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'));

	
	static final Set<Character> escapeCharacterSet = new HashSet<>(Arrays.asList('b','t','n','f','r',
			DoubleQuote,SingleQuote,BackSlash));
	static final Set<Character> whitespaceSet = new HashSet<>(Arrays.asList(CR,LF,SP,HT,FF));
	
	static final Map<String,Kind> kindMap = new HashMap<>();
	static{
		//Dirty Hack
		int index = 4;
		for(String s : keywords){
			kindMap.put(s, Kind.values()[index]);
			index+=1;
		}
	}
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  

	private Integer line = new Integer(1);
	private Integer posInLine = new Integer(1);
	private Integer tokenlength = new Integer(1); // default token length

	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
	}


	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		int pos = 0;

		Kind tokentype = Kind.EOF; // default token type
		
		while (pos < chars.length){
			tokenlength = 1;
			tokentype = findInputType(pos);
			
			if (tokentype == Kind.EOF){
		       if(pos < chars.length-1) throw new LexicalException("Error: Terminating character literal"
		       		+ "\"\\0\" at non-terminating position: "+ pos, pos);
		       else {
		    	   tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		       }
			}
			else if(tokentype == Kind.COMMENT){
				tokenlength=processComment(pos);
				
				
			}
			else if(tokentype == Kind.WHITESPACE){
				tokenlength=processWhitespace(pos);
			}
			else if(tokentype == Kind.STRING_LITERAL){
				tokenlength=processStringLiteral(pos);
				
			}
			else if(tokentype == Kind.IDENTIFIER){
				tokenlength = processIdentifier(pos);
				
			}
			else if(tokentype == Kind.INTEGER_LITERAL){
				tokenlength = processIntegerLiteral(pos);
				
			}
			else{
				// Separator or Operator
				tokens.add(new Token(tokentype, pos, tokenlength, line, posInLine));
			}
			
			if(tokentype != Kind.WHITESPACE){
				// posInLine is already updated when the type is whitespace
				posInLine += tokenlength;
				
			}
			pos += tokenlength;
		}
		
		
		return this;

	}
	
	int processComment(final int charpos){
		int pos = charpos+2; // first two characters are "//"
		//Last character is EOF and not part of the comment
		//Any ASCII character except CR | LF are valid
		while(pos < chars.length-1 && chars[pos] != CR && chars[pos] != LF){			
			pos+=1;
		}
		
		return pos - charpos;
	}
	
	int processWhitespace(final int charpos){
		int pos = charpos;
		char prevChar = '\0';
		// SP | HT |FF | CR | LF | CR LF
		
		while(pos < chars.length-1){
			if(chars[pos] == CR){				
				line+=1;
				posInLine=0;
			}
			else if(chars[pos] == LF){
				if(prevChar != CR){
					line+=1;					
				}
				posInLine=0;
			}
			else if(chars[pos] != SP && chars[pos] != HT && chars[pos] != FF){
				break;
			}
			prevChar = chars[pos];
			pos+=1;
			posInLine+=1;
		}
		
		return pos - charpos;
	}
	
	
	int processStringLiteral(final int charpos) throws LexicalException{
		int pos = charpos+1; // the first char is "

		while(pos < chars.length){
			
			if(chars[pos] == DoubleQuote){
				pos+=1;
				break;
			}
			if(chars[pos] == CR || chars[pos] == LF){
				throw new LexicalException("Error: Line Terminator character in a string Literal at position: "+ pos, pos);
			}
			if(chars[pos] == BackSlash){
				pos+=1;
				if(!IsEsacpeCharacter(chars[pos])){
					throw new LexicalException("Not a valid escape sequence at position: " + pos, pos);
				}
				
				
			}
			pos+=1;
		}
		// Last character is guaranteed to be EOF
		if(pos == chars.length) throw new LexicalException("Error: String Literal not terminated", chars.length-1);
		tokens.add(new Token(Kind.STRING_LITERAL, charpos, pos - charpos, line, posInLine));
		return pos - charpos;
	}
	
	int processIdentifier(final int charpos){
		int pos = charpos +1; // first Character is identifier start
		StringBuilder build = new StringBuilder();
		build.append(chars[charpos]);
		while(pos < chars.length-1 && isIdentifierPart(chars[pos])){
			build.append(chars[pos]);
			pos+=1;
		}
		
		//Need to determine the token kind
		Kind kind = Kind.IDENTIFIER;
		String ident = build.toString();
		//Q: Case Sensitive?
        if(ident.equals("true") || ident.equals("false")) kind = Kind.BOOLEAN_LITERAL;
        else kind = kindMap.getOrDefault(ident,kind);
		
		
		
		tokens.add(new Token(kind, charpos, pos - charpos, line, posInLine));
		return pos - charpos;
	}

	int processIntegerLiteral(final int charpos) throws LexicalException{
		int pos = charpos+1;
		StringBuilder num = new StringBuilder();
		num.append(chars[charpos]);
		if(chars[charpos] != ZERO){
			while(pos < chars.length-1 && isDigit(chars[pos])){
				num.append(chars[pos]);
				pos+=1;
			}		
			
		}
		try{
			Integer.parseInt(num.toString());
		}catch(NumberFormatException e){
			throw new LexicalException("Integer Literal is out of range at position: "+ charpos, charpos);
		}
		tokens.add(new Token(Kind.INTEGER_LITERAL, charpos, pos - charpos, line, posInLine));
		return pos - charpos;
	}

	Kind findInputType(int charpos) throws LexicalException{
		
		if(chars[charpos]== EOFchar) return Kind.EOF;
	    else if(chars[charpos]== ForwardSlash){
	    	// Comment or operator
			if(charpos+1 < chars.length-1 && chars[charpos+1] == ForwardSlash){
				return Kind.COMMENT;
			}
			else{
				// It is an operator
				return Kind.OP_DIV;
			}			
		}
		else if(isWhitespace(chars[charpos])) return Kind.WHITESPACE;
		else if(isIdentifierStart(chars[charpos])) return Kind.IDENTIFIER;
		else if(isDigit(chars[charpos])) return Kind.INTEGER_LITERAL;
		else if(isStringLiteral(chars[charpos])) return Kind.STRING_LITERAL;
		else{
			// Could be an operator or separator or invalid character literal
			String token = new String(chars,charpos,Math.min(2, chars.length-charpos-1));// Last character is EOF
			String token2 = new String(chars,charpos,1);
			if(kindMap.containsKey(token)){
				tokenlength = token.length();//could be length of two or one
				return kindMap.get(token);
			}
			if(kindMap.containsKey(token2)) return kindMap.get(token2);
			
			throw new LexicalException("Error: Character literal is not a valid literal at position: "+ charpos, charpos);
		}
	}

	private boolean IsEsacpeCharacter(char c) {
		// TODO Auto-generated method stub
		return escapeCharacterSet.contains(c);
	}
	
	
	private boolean isStringLiteral(char c) {
		// TODO Auto-generated method stub
		return c == DoubleQuote;
	}


	private boolean isDigit(char c) {
		// TODO Auto-generated method stub
		return digitSet.contains(c);
	}


	private boolean isIdentifierStart(char c) {
		// TODO Auto-generated method stub
		return identifierStartSet.contains(c);
	}

	private boolean isIdentifierPart(char c) {
		// TODO Auto-generated method stub
		return isIdentifierStart(c) || isDigit(c);
	}

	private boolean isWhitespace(char c) {
		// TODO Auto-generated method stub
		return whitespaceSet.contains(c);
	}


	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
