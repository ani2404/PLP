package cop5556fa17;

import java.util.HashMap;
import java.util.Map;
import cop5556fa17.AST.*;

public class SymbolTable {
	// Singleton class
	
	private static SymbolTable singleobject;
	private Map<String,ASTNode> table;
	private SymbolTable(){
		table = new HashMap<>();
	};
	
	public static SymbolTable getSymbolTable(){
		if(singleobject == null) {
			singleobject = new SymbolTable();
		}
		return singleobject;
	}
	
	public boolean add(String name, ASTNode node){
		if(table.containsKey(name)) return false;
		table.put(name, node);
		return true;
	}
	
	public boolean contains(String name){
		return table.containsKey(name);
	}
	
	public ASTNode get(String name){
		return table.get(name);
	}
	

}
