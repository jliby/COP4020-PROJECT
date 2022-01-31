package edu.ufl.cise.plc;


public class main {

    public static void main(String[] args) {
        Lexer scan = new Lexer("false true \n if fi \n 32423 433.23 \"hi\"  ");
        scan.Scanner("2");

        for (int i = 0; i < scan.tokens.size(); i++) {
            System.out.println(scan.tokens.get(i).literal);
        }
    }

}
