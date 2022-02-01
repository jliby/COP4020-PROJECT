package edu.ufl.cise.plc;


public class main {

    public static void main(String[] args) {
        String input = """
				a123 456b
				""";
        Lexer scan = new Lexer(input);
        scan.Scanner();

        for (int i = 0; i < scan.tokens.size(); i++) {
            System.out.println(scan.tokens.get(i).type + ": " + scan.tokens.get(i).literal  + " ln " + scan.tokens.get(i).line  + " col " + scan.tokens.get(i).column  );
        }
    }

}
