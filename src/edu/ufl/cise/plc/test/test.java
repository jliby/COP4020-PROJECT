package edu.ufl.cise.plc.test;

import edu.ufl.cise.plc.runtime.ConsoleIO;

public class test {
    public static void main(String[] args) {
            int x0=(int)((int)34);
            int x1=(int)((int)56);
            ConsoleIO.console.println(x0);
            int x3;
            x3= (Integer) ConsoleIO.readValueFromConsole("INT","Enter INT:");
            ConsoleIO.console.println("x3=");
            ConsoleIO.console.println(x3);
            ConsoleIO.console.println(x1);
            int x4=(int)(x0);
            x4=(int) (x4+x1/3);
            ConsoleIO.console.println("  x4=");
            ConsoleIO.console.println(x4);


    }
}


