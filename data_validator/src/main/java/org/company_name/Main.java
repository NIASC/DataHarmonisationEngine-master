package org.company_name;


import java.io.IOException;

public class Main {
    public static void main(String[] args)  {
        Cli cli = new Cli(args);
        cli.parse();
    }
}
