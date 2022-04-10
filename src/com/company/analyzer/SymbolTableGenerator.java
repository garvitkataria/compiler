package com.company.analyzer;

import com.company.CodeGeneration.Common;

import java.io.*;
import java.util.*;

public class SymbolTableGenerator {
    FileWriter outsymboltables = null;
    private String functionName;
    HashMap<String, Integer> classSizeMap;
    public SymbolTableGenerator(FileWriter outsymboltables) {
        this.outsymboltables = outsymboltables;
        classSizeMap = new HashMap<> ();
    }

    public Node PrintSymbolTable(Node pNode) throws IOException {
        Node temp = new Node();
        temp = pNode;
        List<Node> listNodes = Find(pNode.Children, "STRUCTORIMPLORFUNCLIST").Children;
        Collections.reverse (listNodes);
        for (Node node : listNodes) {
            node.m_symtab = InitializeDataTable();
            if(node.label.equals("structDecl")) {
                Node result = WriteClassTable(node, listNodes);
            }
            else if(node.label.equals("funcDef")) {
                Node result = WriteFunctionTable (node, null, null, "", "", "");
            }
        }
        return pNode;
    }

    private Node WriteClassTable(Node node, List<Node> listNodes) throws IOException {
        node.entry = new SymbolEntry();
        Node defnitionNode = Find(node.Children, "id");
        List<Node> classNodes = FindAll(node.Children, "memberList").get (0).Children;
        int classMemory = 0;
        for (Node memNode : classNodes)
        {
            for (Node classvar : memNode.Children)
            {
                if (classvar.label == "varDecl")
                    classMemory = classMemory + Common.dictMemSize.get(Find(classvar.Children,"type").value);
            }
        }
        outsymboltables.write("| class     | " + defnitionNode.value + "|     | " + classMemory + "\n");
        outsymboltables.write("|    ==============================================================================\n");
        outsymboltables.write("|     | table: " + defnitionNode.value + "\n");
        outsymboltables.write("|    ==============================================================================\n");

        Node inheritNode = Find(node.Children, "inheritList");
        if (inheritNode.Children != null && inheritNode.Children.size () > 0)
        {
            String inheritText = "|     | inherit     | ";
            for (Node inheritChildNode : inheritNode.Children)
            {
                inheritText = inheritText + inheritChildNode.value;
                node.entry.NodeType = "Class";
                node.entry.Name = defnitionNode.value;
                node.entry.Inherits = inheritChildNode.value;
                node.entry.size = classMemory;
                System.out.println ("classMemory: "+ classMemory+" "+defnitionNode);
                classSizeMap.put (defnitionNode.value, classMemory);
            }
            outsymboltables.write(inheritText + "\n");
        }
        else
            outsymboltables.write("|     | inherit     | none \n");
        node = WriteClassVariable(node, node);
        node = WriteClassMethods(node, listNodes, node, defnitionNode.value);

        return node;
    }

    private Node Find(List<Node> children, String strEq) {
        for(Node node: children) {
            if(node.label.equals(strEq)) {
                return node;
            }
        }
        return null;
    }

    private Node WriteClassMethods(Node node, List<Node> rootNodes, Node rootClassNode, String className) throws IOException {
        Node memListNode = Find(node.Children, "memberList");
        for (Node childNode : memListNode.Children) {
            for (Node child : childNode.Children) {
                if (child.label.equals("funcDecl"))
                {
                    String visibility = Find(childNode.Children, "visibility").value;
                    String funcName = Find(child.Children, "id").value;
                    rootClassNode = WriteFunctionTable(child, rootNodes, rootClassNode, className, visibility, funcName);
                }
            }
        }
        return rootClassNode;
    }

    private Node WriteFunctionTable(Node node, List<Node> rootNodes, Node rootClassNode, String className, String visibility, String funcName) throws IOException {
        functionName = "";
        if (className.equals("")) {

            return MethodWriting(node, null, "", "", rootNodes);
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
                                rootClassNode = MethodWriting(funcDefNode, rootClassNode, className, visibility, rootNodes);
                        }
                    }
                }
            }
            return rootClassNode;
        }
    }

    private List<Node> FindAll(List<Node> rootNodes, String strEq) {

        List<Node> ll = new ArrayList<> ();
        if(rootNodes == null) return ll;
        for(Node node: rootNodes) {
            if(node.label.equals(strEq)) {
                ll.add(node);
            }
        }
        return ll;
    }

    private Node MethodWriting(Node node, Node rootClassNode, String ClassName, String visibility, List<Node> allNodes) throws IOException {
        Node defnitionNode = Find(node.Children, "id");
        Node defnitionType = Find(node.Children, "type");
        Node defnitionParam = Find(node.Children, "fparamList");
        functionName = defnitionNode.value;
        Node tempNode = new Node();
        if (defnitionNode != null)
        {
            tempNode.entry = new SymbolEntry();
            tempNode.entry.NodeType = "Function";
            tempNode.entry.Name = defnitionNode.value;
            tempNode.entry.NodeVisibility = visibility;
            tempNode.entry.ReturnType = defnitionType.value;

            outsymboltables.write("| function   | " + defnitionNode.value);
            if (defnitionParam != null && defnitionParam.Children.size() > 0)
            {
                String fparamString = "     | (";
                for (Node paramNode : defnitionParam.Children)
                {
                    if (paramNode.Children != null && paramNode.Children.size() > 0)
                    {
                        fparamString = fparamString + Find(paramNode.Children, "type").value;
                        int DimListNodeCount = Find(paramNode.Children, "dimList").Children.size();
                        if (DimListNodeCount > 0)
                        {
                            for (int i = 0; i < DimListNodeCount; i++)
                            {
                                fparamString = fparamString + "[]";
                            }
                            fparamString = fparamString + " ,";
                        }
                        else
                            fparamString = fparamString + ", ";
                        tempNode.entry.ParameterName = Find(paramNode.Children, "id").value;
                        tempNode.entry.ParameterType = Find(paramNode.Children, "type").value;
                        for (int i = 0; i < DimListNodeCount; i++)
                        {
                            tempNode.entry.ParameterType = tempNode.entry.ParameterType + "[]";
                        }


                        if (rootClassNode == null)
                        {
                            node.entry = tempNode.entry;
                            node.m_symtab = AddNewRow(node.m_symtab, node.entry);
                        }
                        else
                        {
                            rootClassNode.entry = tempNode.entry;
                            rootClassNode.entry.ParentClass = ClassName;
                            rootClassNode.m_symtab = AddNewRow(rootClassNode.m_symtab, rootClassNode.entry);
                        }


                    }
                }
                fparamString = fparamString.trim();
                fparamString = fparamString + ")";
                outsymboltables.write(fparamString);
            }
            else
                outsymboltables.write("     | ()");
            if (defnitionType != null)
                outsymboltables.write(": " + defnitionType.value);

            if (!visibility.equals(""))
                outsymboltables.write("  | " + visibility + "\n");
            List<Node> nodesForMemory = FindAll(FindAll(node.Children, "statementBlock").get(0).Children, "varDecl");
            int memorySize = 0;
            for (Node nodeMem : nodesForMemory)
            {
                if (nodeMem.Children.get (0).Children.size () > 0)
                {
                    int arraySize = 1;
                    for (Node nodeArr : nodeMem.Children.get (0).Children)
                    {
                        arraySize = arraySize * Integer.parseInt (nodeArr.value);
                    }
                    memorySize = memorySize + (arraySize * Common.dictMemSize.get(nodeMem.Children.get (1).value));
                }
                else
                {
                    if (Common.dictMemSize.containsKey(nodeMem.Children.get (1).value))
                        memorySize = memorySize + Common.dictMemSize.get(nodeMem.Children.get (1).value);
                    else
                    {
                        List<Node> classNodes = FindAll(allNodes, "structDecl");
                        for (Node classNode : classNodes)
                        {
                            if (FindAll(classNode.Children, "id").get(0).value.equals(nodeMem.Children.get (1).value))
                            {
                                for (SymbolEntry row : classNode.m_symtab.content)
                                {
                                    if (row.NodeType.equals("Class") && row.Name.equals (nodeMem.Children.get (1).value))
                                    {
                                        memorySize = memorySize + row.size;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            outsymboltables.write("      | " + memorySize + "\n");

            outsymboltables.write("|    ==============================================================================\n");
            outsymboltables.write("|     | table: " + defnitionNode.value + "\n");
            outsymboltables.write("|    ==============================================================================\n");




        }
        for (Node childNode: node.Children)
        {
            if (childNode.label.equals ("statementBlock"))
            {
                for (Node variableNode: childNode.Children)
                {
                    System.out.println ("variableNode.value "+variableNode.value+" "+variableNode.label);
                    if (variableNode.label.equals ("varDecl"))
                    {
                        int variableNodeIndex = IndexOf(childNode.Children, variableNode);
                        String idVal = variableNode.Children.get (2).value;
                        String type = variableNode.Children.get (1).value;
                        System.out.println ("idVal"+idVal);
                        System.out.println ("variableNodetype"+variableNode.Children.get (1).value);
                        System.out.println ("variableNodedimListNodeCount"+variableNode.Children.get (0).label);
                        int dimListNodeCount = variableNode.Children.get (0).Children.size();
                        String dimListNode = "";
                        int arraySize = 1;
                        for (int i = 0; i < dimListNodeCount; i++)
                        {
                            arraySize = arraySize * Integer.parseInt (variableNode.Children.get (0).Children.get (i).value);
                            dimListNode = dimListNode + "[" + variableNode.Children.get (0).Children.get (i).value + "]";
                        }
                        int memorySize = 0;
                        if (Common.dictMemSize.containsKey(type.toLowerCase ().trim()))
                        {
                            memorySize = Common.dictMemSize.get(type.toLowerCase().trim());
                        }
                        else
                        {
                            List<Node> classNodes = FindAll(allNodes, "structDecl");
                            for (Node classNode : classNodes)
                            {
                                if (FindAll(classNode.Children, "id").get(0).value.equals (type))
                                {
                                    for (SymbolEntry row : classNode.m_symtab.content)
                                    {
                                        if (row.NodeType.equals ("Class") && row.Name.equals (type) )
                                        {
                                            memorySize = memorySize + row.size;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if(classSizeMap.containsKey (type)) {
                            outsymboltables.write("|     | local      | " + idVal + "      | " + type + dimListNode + "      | " + (classSizeMap.get (type) * arraySize) + "\n");
                        }
                        else {
                            outsymboltables.write("|     | local      | " + idVal + "      | " + type + dimListNode + "      | " + (memorySize * arraySize) + "\n");
                        }


                        if (rootClassNode == null)
                        {
                            if (node.entry == null)
                                node.entry = tempNode.entry;
                            node.entry.ParameterType = "";
                            node.entry.ParameterName = "";
                            node.entry.VariableName = idVal;
                            node.entry.VariableType = type + dimListNode;
                            node.m_symtab = AddNewRow(node.m_symtab, node.entry);
                        }
                        else
                        {
                            if (rootClassNode.entry == null)
                                rootClassNode.entry = tempNode.entry;
                            rootClassNode.entry.ParentClass = ClassName;
                            rootClassNode.entry.ParameterType = "";
                            rootClassNode.entry.ParameterName = "";
                            rootClassNode.entry.VariableName = variableNode.value;
                            rootClassNode.entry.VariableType = type + dimListNode;
                            rootClassNode.m_symtab = AddNewRow(rootClassNode.m_symtab, rootClassNode.entry);
                        }
                    }
                }
            }
        }
        outsymboltables.write("===================================================================================\n");
        if (rootClassNode != null)
            return rootClassNode;
        else
            return node;
    }


    private int IndexOf(List<Node> children, Node variableNode) {
        int i=0;
        for(Node node:children) {
            if(node.equals(variableNode) ) return i;
            i++;
        }
        return -1;
    }

    private Node WriteClassVariable(Node node, Node rootClassNode) throws IOException {
        Node memListNode = Find(node.Children, "memberList");
        for (Node childNode : memListNode.Children) {
            for (Node child: childNode.Children) {
                if (child.label.equals ("varDecl") ) {
                    String visibility = Find(childNode.Children, "visibility").value;
                    rootClassNode = WriteVariableTable(child, visibility, rootClassNode);
                }
            }
        }
        return rootClassNode;
    }

    private Node WriteVariableTable(Node childNode, String visibility, Node rootClassNode) throws IOException {

        Node idNode = Find(childNode.Children, "id");
        Node typeNode = Find(childNode.Children, "type");
        if (idNode != null && typeNode != null)
        {
            rootClassNode.entry.VariableName = idNode.value;
            rootClassNode.entry.VariableType = typeNode.value;
            rootClassNode.entry.VarVisibility = visibility;
            rootClassNode.m_symtab = AddNewRow(rootClassNode.m_symtab, rootClassNode.entry);
            int arraySize = 1;
            outsymboltables.write("|     | data      | " + idNode.value + "      | " + typeNode.value + "  | " + visibility + "      | " + (Common.dictMemSize.get(typeNode.value.toLowerCase().trim()) * arraySize) + "\n");
        }
        return rootClassNode;
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

                        printAST2(syntaxAnalysis.root, 0);
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

    public Table InitializeDataTable()
    {
        Table table = new Table();
        return table;
    }

    public Table AddNewRow(Table table, SymbolEntry entryFields)
    {
        table.addNewRow(entryFields);
        return table;
    }

    public static void printAST2(Node node, int depth) throws IOException {
        for(int i=0; i<depth; i++)
            System.out.print("|\t");
        System.out.println(node.label + " - "+node.m_symtab);
        for(Node child: node.Children) {
            printAST2(child, depth+1);
        }
    }
}


