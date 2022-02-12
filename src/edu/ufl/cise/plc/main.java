package edu.ufl.cise.plc;


public class main {

    public static void main(String[] args) throws LexicalException {

        	String input = "09";

        Lexer scan = new Lexer(input);
        scan.Scanner();

        for (int i = 0; i < scan.tokens.size() - 1; i++) {
            System.out.println(scan.tokens.get(i).getText() + ": " + scan.tokens.get(i).getStringValue() );
        }
    }
}
