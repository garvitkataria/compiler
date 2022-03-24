package com.company.analyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TypeChecking {
    FileWriter outsemanticerrors = null;
    public TypeChecking(FileWriter outsemanticerrors)
    {
        this.outsemanticerrors = outsemanticerrors;
    }

    public Node PrintSemanticErrors(Node p_Node) throws IOException {
        List<Node> listImplementationNode = Find(p_Node.Children, "STRUCTORIMPLORFUNCLIST").Children;

        for (Node node : listImplementationNode)
        {
            switch (node.label)
            {
                case "structDecl":
                {
                    FindClassErrors(node, listImplementationNode, node);
                    break;
                }
                case "funcDef":
                {
                    FindFunctionErrors(node, listImplementationNode, node);
                    break;
                }
            }
        }
        return p_Node;

    }

    private Node Find(List<Node> children, String strEq) {
        for(Node node: children) {
            if(node.label.equals(strEq)) {
                return node;
            }
        }
        return null;
    }

    private void CheckForAssignmentErrors(Node p_Node, List<Node> allNodes, Node rootNode) throws IOException {
        Table dt = new Table();
        String rightSideOutputType = null;
        String leftSideOutputType = null;
        Node isComplexVar = Find(Find(Find(p_Node.Children,"var").Children,"var0").Children,"dot");
        List<Node> isArrayVar = Find(Find(p_Node.Children, "var").Children, "indiceList").Children;
        Node variableNameNode = Find(Find(Find(p_Node.Children, "var").Children,"var0").Children, "id");
        String variableName = variableNameNode.value;
        dt = rootNode.m_symtab;
        List<String> parentClass = dt.getDataParentClass();
        String rightSideNode = p_Node.Children.get (0).label;
        switch (rightSideNode)
        {
            case "intlit":
                rightSideOutputType = "integer";
                break;
            case "functionCallDataMember":
                rightSideOutputType = GetOutputFCallDMemberType(p_Node, parentClass, allNodes, rootNode);
                break;

            case "floatnum":
                rightSideOutputType = "float";
                break;

        }
//        leftSideOutputType = GetArrayVarType(p_Node, parentClass, allNodes, rootNode);

        if (variableName != null && isComplexVar == null && isArrayVar.size() == 0)
            leftSideOutputType = GetVarType(variableNameNode, parentClass, allNodes, rootNode);
        if (leftSideOutputType!=null && leftSideOutputType.contains("[]"))
            leftSideOutputType = leftSideOutputType.split(String.valueOf ('['))[0];


        if (parentClass.size () == 0) {
            List<String> localorGlobal = dt.getDataParameterNameVariableName(variableName);
            if (localorGlobal.size()>0) {
                outsemanticerrors.write("Undeclared local variable: at line " + variableNameNode.line + "\n");
            }
        }

        if (!(leftSideOutputType==null) && !(rightSideOutputType==null) && leftSideOutputType != rightSideOutputType) {
            outsemanticerrors.write("Undeclared local variable: at line " + variableNameNode.line + "\n");
        }
    }

    private String GetVarType(Node variableName, List<String> parentClass, List<Node> allNodes, Node rootNode) throws IOException {
        if (parentClass.size() == 0)
        {
            List<String> paramList = rootNode.m_symtab.getDataParameterName(variableName.value);
            List<String> varList = rootNode.m_symtab.getDataVariableName(variableName.value);
            if (paramList.size() > 0 && varList.size() > 0) {
                outsemanticerrors.write("Multiple declaration of parameter: at line " + rootNode.line + "\n");
                return null;
            }
            else if (paramList.size() == 0 && varList.size() > 0) {
                return varList.get(0);
            }
            else if (paramList.size() > 0 && varList.size() == 0) {
                return paramList.get(0);
            }
            else if (paramList.size() == 0 && varList.size() == 0) {
                outsemanticerrors.write("Multiple declaration of parameter: at line " + variableName.line + "\n");
                return null;
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private String GetOutputFCallDMemberType(Node p_Node, List<String> parentClass, List<Node> allNodes, Node rootNode)
    {
        Node funcdatamemberNode = p_Node.Children.get(0);
        if (Find(Find(funcdatamemberNode.Children,"var0").Children,"dot") == null)
        {
            String variable = Find(Find(funcdatamemberNode.Children, "var0").Children, "id").value;
            if (parentClass.size() == 0)
            {
                List<String> row = rootNode.m_symtab.getDataParameterNameVariableName(variable);
                if (row.size() > 0)
                {
                    return row.get(0);
                }
            }
        }
        return null;
    }


    private void CheckForUndefinedErrors(Node p_Node, List<Node> allNodes, Node rootNode) throws IOException {
        Node functionCallName = Find(Find(p_Node.Children, "var0").Children, "id");
        if (Find(rootNode.Children, "id").value != functionCallName.value)
        {
            boolean nameMatch = false;
            List<Node> allFuncNodes = FindAll(allNodes, "funcDef");
            for (Node node : allNodes)
            {
                if (Find(node.Children,"id").value == functionCallName.value)
                nameMatch = true;
            }
            if (!nameMatch)
                outsemanticerrors.write("Undeclared free function: at line "+functionCallName.line+" \n");
        }


    }

    private void FindFunctionErrors(Node node, List<Node> allNodes, Node rootNode) throws IOException {
        Node StatementBlockNode = Find(node.Children,"statementBlock");
        CheckFunctionGlobalErrors(node);
        for (Node child : StatementBlockNode.Children)
        {

            if (child.label == "assignStatement")
            {
                CheckForAssignmentErrors(child, allNodes, rootNode);
            }
            if (child.label == "fcallStatement")
            {
                CheckForUndefinedErrors(child, allNodes, rootNode);
            }
        }
    }


    private void FindClassErrors(Node node, List<Node> allNodes, Node rootNode) throws IOException {
        List<Node> implNodes = FindAll(allNodes,"implDecl");
        for (Node childImplNode : implNodes)
        {
            if (Find(childImplNode.Children, "id").value == Find(node.Children, "id").value)
            {
                List<Node> functionDefList = FindAll(Find(childImplNode.Children,"funcDefList").Children, "funcDef");
                for (Node funcNode : functionDefList)
                {
                    Node StatementBlockNode = Find(funcNode.Children,"statementBlock");
                    for (Node child : StatementBlockNode.Children)
                    {

                        if (child.label == "assignStatement")
                        {
                            CheckForAssignmentErrors(child, allNodes, node);
                        }
                        if (child.label == "fcallStatement")
                        {
                            CheckForUndefinedErrors(child, allNodes, rootNode);
                        }
                    }

                }
            }
        }
    }

    private void CheckFunctionGlobalErrors(Node node) throws IOException {
        HashMap<String, Integer> GroupByParam = node.m_symtab.getGroupByParam();
        HashMap<String, Integer> GroupByVar = node.m_symtab.getGroupByVar();

        if (GroupByParam.size() > 0)
        {
            List<String> result = new ArrayList<> ();
            for(String key: GroupByParam.keySet ()) {
                if(GroupByParam.get(key) > 1) {
                    result.add(key);
                }
            }
            if (result.size () > 0 && !node.value.equals("$"))
                outsemanticerrors.write("Multiple same name parameters: at line " + node.line + "\n");
        }

        if (GroupByVar.size() > 0)
        {
            List<String> result = new ArrayList<> ();
            for(String key: GroupByVar.keySet ()) {
                if(GroupByVar.get(key) > 1) {
                    result.add(key);
                }
            }
            for (String item : result) {
                List<Node> nodell = FindAll(Find(node.Children, "statementBlock").Children, "id");
                if(nodell.size()>0) {
                    int lineNumber = nodell.get(0).line;
                    outsemanticerrors.write("Multiple same name local variables: at line " + lineNumber + "\n");
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

    public static void main(String[] args) throws FileNotFoundException {
        FileWriter outsyntaxerrors = null;
        FileWriter outderivation = null;
        FileWriter outast = null;
        FileWriter outsymboltables = null;
        FileWriter outsemanticerrors = null;
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
                            outsemanticerrors = new FileWriter("src/com/company/test/output_dir/"+filenameWithExt[0]+".outsemanticerrors");
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

                        TypeChecking typeChecking = new TypeChecking(outsemanticerrors);
                        typeChecking.PrintSemanticErrors(syntaxAnalysis.root);
                    }
                }
                try {
                    outderivation.close();
                    outsyntaxerrors.close();
                    outast.close ();
                    outsymboltables.close();
                    outsemanticerrors.close();
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
