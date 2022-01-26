package com.company;

import com.company.analyzer.Lexer;
import com.company.analyzer.Token;

import java.io.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        FileWriter outlexerrors = null;
        FileWriter outlextokens = null;
        try {
            File folder = new File("src/com/company/test/input_dir");
            File[] listOfFiles = folder.listFiles();

            for (File file: listOfFiles) {
                if (file.isFile()) {
                    String filename = file.getName();
                    String[] filenameWithExt = filename.split("\\.");
                    if(filenameWithExt.length > 1 && filenameWithExt[1].equals("src")) {
                        System.out.println (filename);
                        FileInputStream inFile = new FileInputStream("src/com/company/test/input_dir/"+filename);
                        System.setIn(inFile);
                        try {
                            outlexerrors = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".outlexerrors");
                            outlextokens = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".outlextokens");
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }

                        Lexer lex = new Lexer();
                        try {
                            while(true) {
                                Token token = lex.scan();
                                if(token == null) break;
                                outlextokens.write(token +"\n");
                                if(token.type.equals("invalidchar")) {
                                    outlexerrors.write("Lexical error: Invalid character: \"" + token.value + "\": line " + token.line + ".\n");
                                }
                                if(token.type.equals("invalidnum")) {
                                    outlexerrors.write("Lexical error: Invalid number: \"" + token.value + "\": line " + token.line + ".\n");
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace ();
                        }

                        try {
                            outlextokens.close();
                            outlexerrors.close();
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                    }
                }
            }
        }
        catch (FileNotFoundException err) {
            System.out.println(err.getMessage());
        }


    }
}
