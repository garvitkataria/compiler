package com.company.CodeGeneration;

import com.company.analyzer.Lexer;
import com.company.analyzer.SymbolTableGenerator;
import com.company.analyzer.SyntaxAnalysis;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        FileWriter outsyntaxerrors = null;
        FileWriter outderivation = null;
        FileWriter outast = null;
        FileWriter outsymboltables = null;
        FileWriter intermediateasm = null;
        FileWriter moon = null;
        try {
            File folder = new File("src/com/company/test/input_dir");
            File[] listOfFiles = folder.listFiles();

            for (File file: listOfFiles) {
                if (file.isFile()) {
                    String filename = file.getName();
                    String[] filenameWithExt = filename.split("\\.");
                    if(filenameWithExt.length > 1 && filenameWithExt[1].equals("src")) {
                        System.out.println ("Processing File: " + filename);
                        FileInputStream inFile = new FileInputStream("src/com/company/test/input_dir/"+filename);
                        System.setIn(inFile);
                        try {
                            outsyntaxerrors = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".outsyntaxerrors");
                            outderivation = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".outderivation");
                            outast = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".outast");
                            outsymboltables = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".outsymboltables");
                            intermediateasm = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".imt");
                            moon = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".moon");
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                        Lexer lex = new Lexer();
                        SyntaxAnalysis syntaxAnalysis = new SyntaxAnalysis(lex);
                        System.out.println (syntaxAnalysis.Parse(outderivation, outsyntaxerrors));
                        System.out.println (syntaxAnalysis.st.size());
                        try {
                            syntaxAnalysis.root = syntaxAnalysis.st.peek();
                            syntaxAnalysis.printAST(syntaxAnalysis.st.peek(), 0, outast);
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                        System.out.println ("=====================\n\n");

                        SymbolTableGenerator symbolTableGenerator = new SymbolTableGenerator(outsymboltables);
                        outsymboltables.write("| table: global\n");
                        outsymboltables.write("===================================================================================\n");
                        symbolTableGenerator.PrintSymbolTable(syntaxAnalysis.root);

                        StringBuilder moonsb = new StringBuilder ();
                        CodeGenerator codeGenerator = new CodeGenerator(intermediateasm, moonsb);
                        codeGenerator.generate(syntaxAnalysis.root);
                        intermediateasm.write (moonsb.toString ());
                    }
                    try {
                        outderivation.close();
                        outsyntaxerrors.close();
                        outast.close ();
                        outsymboltables.close();
                        intermediateasm.close();
                    } catch (IOException e) {
                        e.printStackTrace ();
                    }

                    File imtfile = new File("src/com/company/test/output_dir/"+filenameWithExt[0]+".imt");
                    CodeGeneratorAsm cda = new CodeGeneratorAsm(moon);
                    Scanner scan = new Scanner (imtfile);
                    while(scan.hasNextLine()){
                        String temp = scan.nextLine ();
                        cda.arrFileContent.add(temp);

                    }
                    scan.close ();
                    moon.write("entry\n");
                    cda.GenerateAssemblyCode();
                    moon.close ();
                }
            }
        }
        catch (FileNotFoundException err) {
            System.out.println(err.getMessage());
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}
