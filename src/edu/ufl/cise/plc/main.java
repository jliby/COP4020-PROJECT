package edu.ufl.cise.plc;


public class main {

    public static void main(String[] args) throws LexicalException {
		String input = """
			0.32
			00.15
			10.030.32
			""";
		Lexer scan = new Lexer(input);
        scan.Scanner();
        System.out.println(input);
        for (int i = 0; i < scan.tokens.size(); i++) {
            System.out.println(scan.tokens.get(i).type + ": " + scan.tokens.get(i).literal  + " ln " + scan.tokens.get(i).line  + " col " + scan.tokens.get(i).column  );
        }
    }
}
