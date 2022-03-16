package com.company.analyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SymbolTableGenerator {
    FileWriter outsymboltables = null;
    public SymbolTableGenerator(FileWriter outsymboltables) {
        this.outsymboltables = outsymboltables;
    }
    public void PrintSymbolTable(Node pNode) throws IOException {
        List<Node> listNodes = Find(pNode.Children, "STRUCTORIMPLORFUNCLIST").Children;
        for (Node node : listNodes) {

            if(node.label.equals("structDecl")) {
                WriteClassTable(node, listNodes);
            }
            else if(node.label.equals("funcDef")) {
                WriteFunctionTable (node, null, "", "", "");
            }
        }
    }

    private void WriteClassTable(Node node, List<Node> listNodes) throws IOException {
//        Node defnitionNode = node.Children.Find(x => x.label == "id");
        Node defnitionNode = Find(node.Children, "id");
        outsymboltables.write("| class     | " + defnitionNode.value + "\n");
        outsymboltables.write("|    ==============================================================================\n");
        outsymboltables.write("|     | table: " + defnitionNode.value + "\n");
        outsymboltables.write("|    ==============================================================================\n");


//        Node inheritNode = node.Children.Find(x => x.label == "inheritList");
        Node inheritNode = Find(node.Children, "inheritList");
        if (inheritNode.Children != null && inheritNode.Children.size () > 0)
        {
            String inheritText = "|     | inherit     | ";
            for (Node inheritChildNode : inheritNode.Children)
            {
                inheritText = inheritText + inheritChildNode.value;
            }
            outsymboltables.write(inheritText + "\n");
        }
        else
            outsymboltables.write("|     | inherit     | none \n");
        WriteClassVariable(node);
        WriteClassMethods(node, listNodes, defnitionNode.value);
    }

    private Node Find(List<Node> children, String strEq) {
        for(Node node: children) {
            if(node.label.equals(strEq)) {
                return node;
            }
        }
        return null;
    }

    private void WriteClassMethods(Node node, List<Node> rootNodes, String className) throws IOException {
        for (Node childNode : node.Children)
        {
            if (childNode.label.equals("funcDecl"))
            {
                String visibility = Find(node.Children, "visibility").value;
                String funcName = Find(childNode.Children, "id").value;
                WriteFunctionTable(childNode, rootNodes, className, visibility, funcName);
            }
            WriteClassMethods(childNode, rootNodes, className);
        }
    }

    private void WriteFunctionTable(Node node, List<Node> rootNodes, String className, String visibility, String funcName) throws IOException {
        if (className.equals("")) {
            MethodWriting(node, "");
        }
        else {
            if (rootNodes != null) {
                List<Node> implNodes = FindAll(rootNodes, "implDecl");
                for (Node childImplNode : implNodes)
                {
                    Node idNode = Find(childImplNode.Children, "id");
                    if (idNode.value.equals(className))
                    {
                        Node funcDefListNode = Find(childImplNode.Children,"funcDefList");
                        List<Node> listOfFuncDef = FindAll(funcDefListNode.Children,"funcDef");
                        for (Node funcDefNode : listOfFuncDef) {
                            if (Find(funcDefNode.Children, "id").value.equals(funcName))
                                MethodWriting(funcDefNode, visibility);
                        }
                    }
                }
            }
        }
    }

    private List<Node> FindAll(List<Node> rootNodes, String strEq) {
        List<Node> ll = new ArrayList<> ();
        for(Node node: rootNodes) {
            if(node.label.equals(strEq)) {
                ll.add(node);
            }
        }
        return ll;
    }

    private void MethodWriting(Node node, String visibility) throws IOException {
        Node defnitionNode = Find(node.Children, "id");
        Node defnitionType = Find(node.Children, "type");
        Node defnitionParam = Find(node.Children, "fparamList");
        if (defnitionNode != null)
        {
            outsymboltables.write("| function   | " + defnitionNode.value);
            if (defnitionParam != null && defnitionParam.Children.size() > 0)
            {
                String fparamString = "     | (";
                for (Node paramNode : defnitionParam.Children)
                {
                    if (paramNode.Children != null && paramNode.Children.size () > 0)
                    {
                        fparamString = fparamString + Find(paramNode.Children, "type").value;
                        if (Find(paramNode.Children, "dimList").Children.size () > 0)
                            fparamString = fparamString + "[], ";
                        else
                            fparamString = fparamString + ", ";
                    }
                }
                fparamString = fparamString.trim();
                fparamString = fparamString + ")";
                outsymboltables.write(fparamString);
            }
            else
                outsymboltables.write("     | ()");

            if (defnitionType != null)
                outsymboltables.write(": " + defnitionType.value );

            if (!visibility.equals(""))
                outsymboltables.write("  | " + visibility + "\n");
            else
                outsymboltables.write("\n");

            outsymboltables.write("|    ==============================================================================\n");
            outsymboltables.write("|     | table: " + defnitionNode.value + "\n");
            outsymboltables.write("|    ==============================================================================\n");
        }
        for (Node childNode : node.Children) {
            if (childNode.label.equals("statementBlock"))
            {
                for (Node variableNode : childNode.Children)
                {
                    if (variableNode.label == "id")
                    {
                        int variableNodeIndex = IndexOf(childNode.Children, variableNode);
                        String type = childNode.Children.get(variableNodeIndex - 1).value;
                        String dimListNode = (childNode.Children.get(variableNodeIndex - 2).Children.size() > 0) ? "[]" : "";
                        outsymboltables.write( "|     | local      | " + variableNode.value + "      | " + type + dimListNode + "\n");
                    }
                }
            }
        }
        outsymboltables.write("===================================================================================\n");
    }

    private int IndexOf(List<Node> children, Node variableNode) {
        int i=0;
        for(Node node:children) {
            if(node == variableNode) return i;
            i++;
        }
        return -1;
    }

    private void WriteClassVariable(Node node) throws IOException {
        for (Node childNode : node.Children)
        {
            if (childNode.label.equals("varDecl"))
            {
                String visibility = Find(node.Children, "visibility").value;
                WriteVariableTable(childNode, visibility);
            }
            WriteClassVariable(childNode);
        }
    }

    private void WriteVariableTable(Node childNode, String visibility) throws IOException {

        Node idNode = Find(childNode.Children, "id");
        Node typeNode = Find(childNode.Children, "type");
        if (idNode != null && typeNode != null)
        {
            outsymboltables.write("|     | data      | " + idNode.value + "      | " + typeNode.value + "  | " + visibility + "\n");
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        FileWriter outsyntaxerrors = null;
        FileWriter outderivation = null;
        FileWriter outast = null;
        FileWriter outsymboltables = null;
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

                    }
                }
                try {
                    outderivation.close();
                    outsyntaxerrors.close();
                    outast.close ();
                    outsymboltables.close();
                } catch (IOException e) {
                    e.printStackTrace ();
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


