package edu.ufl.cise.plc;


public class main {

    public static void main(String[] args) {
        Lexer scan = new Lexer("false true if ");
        scan.Scanner("2");

        for (int i = 0; i < scan.tokens.size(); i++) {
            System.out.println(scan.tokens.get(i).type);
        }
    }

}
