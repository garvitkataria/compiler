package com.company;

import com.company.analyzer.Lexer;
import com.company.analyzer.Token;

import java.io.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        // for input stream : optional

            try {
                FileInputStream inFile = new FileInputStream("src/com/company/test/input");
                System.setIn(inFile);
            }
            catch (FileNotFoundException err) {
                System.out.println(err.getMessage());
            }

        System.out.println("start");
        // Initialize front-end syntax directed translator
        Lexer lex = new Lexer();
        try {
            while(true) {
                Token token = lex.scan();
                if(token == null) break;
                System.out.println("token: "+token);
            }

        } catch (IOException e) {
            e.printStackTrace ();
        }
        System.out.println("finish");
    }
}
