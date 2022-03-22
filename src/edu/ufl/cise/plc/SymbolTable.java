package edu.ufl.cise.plc;
import edu.ufl.cise.plc.ast.Declaration;
import java.util.HashMap;

public class SymbolTable {
    HashMap<String, Declaration> entries = new HashMap<>();

    //returns true if name successfully inserted in symbol table, false if already present
    public boolean insert(String name, Declaration declaration) {
        return (entries.putIfAbsent(name,declaration) == null);
    }

    //returns Declaration if present, or null if name not declared.
    public Declaration lookup(String name) {
        return entries.get(name);
    }

}
