package edu.ufl.cise.plc;


public class main {

    public static void main(String[] args) {
        Lexer scan = new Lexer("false true BLACK \n if fi \n 32423 433.23 \"hi\" cap ");
        scan.Scanner("2");

        for (int i = 0; i < scan.tokens.size(); i++) {
            System.out.println(scan.tokens.get(i).type + ": " + scan.tokens.get(i).literal  + " ln " + scan.tokens.get(i).line  + " col " + scan.tokens.get(i).column  );
        }
    }

}
