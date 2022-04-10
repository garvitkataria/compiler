package com.company.analyzer;

import java.io.*;
import java.util.*;

import static com.company.CodeGeneration.Common.*;

public class TypeChecking {
    FileWriter outsemanticerrors = null;
    public TypeChecking(FileWriter outsemanticerrors)
    {
        this.outsemanticerrors = outsemanticerrors;
    }

    public Node PrintSemanticErrors(Node p_Node) throws IOException {
        List<Node> listImplementationNode = Find(p_Node.Children, "STRUCTORIMPLORFUNCLIST").Children;
        GetRootCodeErrors(listImplementationNode);
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

    private void GetRootCodeErrors(List<Node> listImplementationNode)
    {
        List<Node> classNodes = FindAll(listImplementationNode, "structDecl");
        String rootClass = null;
        String rootInheritClass = null;
        for (Node node : classNodes) {
            Node inheritNode = node;
            boolean isInhetitsFlag = true;
            rootClass = Find(node.Children, "id").value;
            while (isInhetitsFlag) {
                rootInheritClass = GetInheritClass(inheritNode, classNodes);
                if (rootInheritClass == null || rootInheritClass.equals(rootClass))
                    isInhetitsFlag = false;
                else
                {
                    for(Node in: classNodes) {
                        if(Find(in.Children, "id").value.equals (rootInheritClass)) {
                            inheritNode = in;
                            break;
                        }
                    }
                }
            }
            if (rootInheritClass == null)
                continue;
            else if (rootClass.equals(rootInheritClass))
                semanticErrors.add("[error] Circular class dependency (inheritance cycles): at line " + Find(node.Children,"id").line);
        }

    }

    private String GetInheritClass(Node node, List<Node> classNodes)
    {
        List<String> inheritsNode = new ArrayList<> ();
        for(SymbolEntry se: node.m_symtab.content) {
            if(se.Inherits != null && se.Inherits != "") {
                inheritsNode.add (se.Inherits);
            }
        }
        if (inheritsNode.size() > 0)
            return inheritsNode.get (0);
        else
            return null;
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
        dt = rootNode.m_symtab;
        String rightSideOutputType = null;
        String leftSideOutputType = null;
        Node isComplexVar = Find(Find(Find(p_Node.Children,"var").Children,"var0").Children,"dot");
        List<Node> isArrayVar = Find(Find(p_Node.Children, "var").Children, "indiceList").Children;
        Node variableName = Find(Find(Find(p_Node.Children, "var").Children,"var0").Children, "id");
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
        if (isComplexVar != null)
        {
            Node varName = Find(isComplexVar.Children, "id");
            String varNameType = null;
            for(SymbolEntry se: rootNode.m_symtab.content) {
                if(se.VariableName!=null && se.VariableName.equals (varName.value)) {
                    varNameType = se.VariableName;
                    break;
                }
            }
            String paramType = null;
            for(SymbolEntry se: rootNode.m_symtab.content) {
                if(se.ParameterName!=null && se.ParameterName.equals(varName.value)) {
                    paramType = se.ParameterName;
                    break;
                }
            }
            if (varNameType!=null && dictMemSize.containsKey(varNameType))
                semanticErrors.add("[error] '.' operator used on non-class type: at line " + varName.line);
            else if (paramType!=null && dictMemSize.containsKey(paramType))
                semanticErrors.add("[error] '.' operator used on non-class type: at line " + varName.line);

            if (varNameType!=null)
            {
                List<Node> classType = new ArrayList<> ();
                List<Node> structDeclNodeList = FindAll(allNodes, "structDecl");
                for(Node childn: structDeclNodeList) {
                    if(Find(childn.Children, "id").value.equals(varNameType)) {
                        classType.add(childn);
                    }
                }
                if (classType != null && classType.size () > 0)
                {
                    List<String> lstClassVariables = new ArrayList<> ();
                    Node cnode = classType.get(0);
                    for(SymbolEntry se: cnode.m_symtab.content) {
                        if(se.VariableName!=null && !se.VariableName.isEmpty() && se.NodeType.equals("Class")) {
                            lstClassVariables.add(se.VariableName);
                        }
                    }
                    if (lstClassVariables.size() > 0 && !lstClassVariables.contains(variableName.value))
                    {
                        semanticErrors.add("[error] Undeclared data member (search in class table): at line " + varName.line);
                    }
                }
            }
            else if (paramType!=null)
            {
                List<Node> classType = new ArrayList<> ();
                List<Node> structDeclNodeList = FindAll(allNodes, "structDecl");
                for(Node childn: structDeclNodeList) {
                    if(Find(childn.Children, "id").value.equals(paramType)) {
                        classType.add(childn);
                    }
                }
                if (classType != null && classType.size () > 0)
                {
                    List<String> lstClassVariables = new ArrayList<> ();
                    Node cnode = classType.get(0);
                    for(SymbolEntry se: cnode.m_symtab.content) {
                        if(se.VariableName!=null && !se.VariableName.isEmpty() && se.NodeType.equals("Class")) {
                            lstClassVariables.add(se.VariableName);
                        }
                    }
                    if (lstClassVariables.size () > 0 && !lstClassVariables.contains(variableName.value))
                    {
                        semanticErrors.add("[error] Undeclared data member (search in class table): at line " + varName.line);
                    }
                }
            }
        }

        if (isArrayVar.size () > 0)
        {
            Node nonIntIndexes = Find(isArrayVar, "intlit");
            if (nonIntIndexes != null)
                semanticErrors.add("[error] Array index is not an integer: at line " + nonIntIndexes.line);

            String varNameType = null;
            for(SymbolEntry se: rootNode.m_symtab.content) {
                if(se.VariableName.equals (variableName)) {
                    varNameType = se.VariableName;
                    break;
                }
            }

            String paramType = null;
            for(SymbolEntry se: rootNode.m_symtab.content) {
                if(se.ParameterName.equals (variableName)) {
                    paramType = se.ParameterName;
                    break;
                }
            }

            if (varNameType != null)
            {
                nonIntIndexes = Find(isArrayVar, "intlit");
                varNameType = varNameType.trim();
                if ((varNameType.split("\\[").length - 1) != isArrayVar.size ())
                {
                    semanticErrors.add("[error] Use of array with wrong number of dimensions: at line " + nonIntIndexes.line);
                }
            }
            else if (paramType != null)
            {
                nonIntIndexes = Find(isArrayVar, "intlit");
                paramType = paramType.trim();
                if ((paramType.split("\\[").length - 1) != isArrayVar.size ())
                {
                    semanticErrors.add("[error] Use of array with wrong number of dimensions: at line " + nonIntIndexes.line);
                }
            }

        }

        if (variableName != null && isComplexVar == null && isArrayVar.size () == 0)
            leftSideOutputType = GetVarType(variableName, parentClass, allNodes, rootNode);
        if (leftSideOutputType!=null && leftSideOutputType.contains("[]"))
            leftSideOutputType = leftSideOutputType.split("\\[")[0];
        if (parentClass == null)
        {
            String localorGlobal = null;
            for(SymbolEntry se: rootNode.m_symtab.content) {
                if(se.ParameterName.equals (variableName.value) || se.VariableName.equals (variableName.value)) {
                    localorGlobal = se.ParameterName;
                    break;
                }
            }
            if (localorGlobal == null)
            {
                outsemanticerrors.write("Undeclared local variable: at line " + variableName.line + "\n");
                semanticErrors.add("[error]1 Undeclared variable (check for existence of local variable): at line " + variableName.line);
            }
        }

        if (leftSideOutputType!=null && rightSideOutputType!=null && leftSideOutputType != rightSideOutputType)
        {
            outsemanticerrors.write("Undeclared local variable: at line " + variableName.line + "\n");
            semanticErrors.add("[error]2 Undeclared variable (check for existence of local variable): at line " + variableName.line);
        }
    }

    private String GetVarType(Node variableName, List<String> parentClass, List<Node> allNodes, Node rootNode) throws IOException {
        if (parentClass.size() == 0)
        {
            List<String> paramList = rootNode.m_symtab.getDataParameterName(rootNode,variableName.value);
            List<String> varList = rootNode.m_symtab.getDataVariableName(rootNode,variableName.value);
            if (paramList.size() > 0 && varList.size() > 0) {
                semanticErrors.add("[error] multiply declaration of parameter: at line " + rootNode.line);
                return null;
            }
            else if (paramList.size() == 0 && varList.size() > 0) {
                return varList.get(0);
            }
            else if (paramList.size() > 0 && varList.size() == 0) {
                return paramList.get(0);
            }
            else if (paramList.size() == 0 && varList.size() == 0) {
                semanticErrors.add("[error]3 Undeclared variable (check for existence of local variable): at line " + variableName.line);
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

    private void CheckForUndefinedErrors(Node p_Node, List<Node> allNodes, Node rootNode)
    {
        Node functionCallName = Find(Find(p_Node.Children, "var0").Children, "id");

        if (!Find(rootNode.Children, "id").value.equals (functionCallName.value) )
        {
            List<Node> funcCallParam = Find(p_Node.Children, "aParams").Children;
            List<String> lstFuncCallTypes = GetFunctionCallTypes(funcCallParam, rootNode);
            List<String> lstFuncDefTypes = new ArrayList<> ();
            boolean nameMatch = false;
            boolean paramCountMatch = false;
            int funcDefParamCount = 0;
            List<Node> allFuncNodes = FindAll(allNodes, "funcDef");
            for (Node node : allFuncNodes)
            {
                if (Find(node.Children, "id").value.equals(functionCallName.value))
                {
                    nameMatch = true;
                    for(SymbolEntry se: rootNode.m_symtab.content) {
                        if(se.ParameterName!=null && !se.ParameterName.isEmpty()) {
                            funcDefParamCount ++;
                        }
                    }
                    for(SymbolEntry se: rootNode.m_symtab.content) {
                        if(se.ParameterName!=null && !se.ParameterName.isEmpty()) {
                            lstFuncDefTypes.add (se.ParameterType);
                        }
                    }
                    if (funcCallParam.size() != funcDefParamCount && !paramCountMatch)
                    {
                        paramCountMatch = false;
                    }
                    else if (funcCallParam.size () == funcDefParamCount)
                    {
                        paramCountMatch = true;
                    }

                    if (lstFuncCallTypes.size () == lstFuncDefTypes.size ())
                    {
                        int idx=0;
                        for (String item : lstFuncCallTypes) {
                            if (lstFuncDefTypes.get (idx).contains("["))
                            {
                                if (!item.trim().split("\\[")[0].equals(lstFuncDefTypes.get (idx).trim().split("\\[")[0]))
                                {
                                    semanticErrors.add("[error] Function call with wrong type of parameters: at line " + functionCallName.line);
                                    break;
                                }
                                else if ((item.trim().split("\\[").length - 1) != (lstFuncDefTypes.get(idx).trim().split("\\[").length - 1))
                                {
                                    semanticErrors.add("[error] Array parameter using wrong number of dimensions: at line " + functionCallName.line);
                                    break;
                                }
                            }
                            else
                            {
                                if (!item.trim().equals (lstFuncDefTypes.get(idx).trim()))
                                {
                                    semanticErrors.add("[error] Function call with wrong type of parameters: at line " + functionCallName.line);
                                    break;
                                }
                            }
                            idx++;
                        }
                    }
                }
            }
            if (!nameMatch)
                semanticErrors.add("[error] Undeclared free function: at line " + functionCallName.line);
            if (!paramCountMatch && nameMatch)
                semanticErrors.add("[error] Function call with wrong number of parameters: at line " + functionCallName.line);
        }


    }

    private List<String> GetFunctionCallTypes(List<Node> funcCallParam, Node rootNode)
    {
        List<String> lstFuncCallTypes = new ArrayList<> ();
        if (funcCallParam.size () > 0)
        {
            for(Node node : funcCallParam)
            {
                if (node.label.equals("functionCallDataMember"))
                {
                    String paramName = Find(Find(node.Children, "var0").Children,"id").value;
                    if (!paramName.isEmpty () && paramName!=null)
                    {
                        List<String> paramType = new ArrayList<>();
                        for(SymbolEntry se: rootNode.m_symtab.content) {
                            if(se.ParameterName.equals(paramName)) {
                                paramType.add(se.ParameterType);
                            }
                        }
                        List<String> varType = new ArrayList<>();
                        for(SymbolEntry se: rootNode.m_symtab.content) {
                            if(se.VariableName.equals(paramName)) {
                                varType.add(se.VariableType);
                            }
                        }
                        if (paramType.size () > 0)
                        {
                            lstFuncCallTypes.add(paramType.get (0));
                        }
                        else if (varType.size () > 0)
                        {
                            lstFuncCallTypes.add(varType.get (0));
                        }
                    }
                }
                else
                    lstFuncCallTypes.add(dictTypes.get(node.label));
            }
        }
        return lstFuncCallTypes;
    }


    private void FindFunctionErrors(Node node, List<Node> allNodes, Node rootNode) throws IOException {
        Node StatementBlockNode = Find(node.Children,"statementBlock");
        CheckFunctionGlobalErrors(node, allNodes);
        for (Node child : StatementBlockNode.Children)
        {

            if (child.label.equals ("assignStatement"))
            {
                CheckForAssignmentErrors(child, allNodes, rootNode);
            }
            if (child.label.equals ("fcallStatement"))
            {
                CheckForUndefinedErrors(child, allNodes, rootNode);
            }
            if (child.label.equals ("varDecl"))
            {
                if (!dictMemSize.containsKey(Find(child.Children, "type").value))
                {
                    List<Node> getClassName = new ArrayList<> ();
                    for(Node nd: allNodes) {
                        if(nd.label.equals("structDecl")) {
                            for(Node ndchild: nd.Children) {
                                if(ndchild.label.equals ("id") && Find(child.Children, "type").value.equals (ndchild.value)) {
                                    getClassName.add(ndchild);
                                }
                            }
                        }
                    }
                    if (getClassName.size () == 0)
                        semanticErrors.add("[error] undeclared class: at line " + Find(child.Children, "id").line);
                }
            }
        }
        Node paramsNode = Find(node.Children, "fparamList");
        if (paramsNode.Children.size () > 0)
        {
            for (Node child : paramsNode.Children)
            {
                if (!dictMemSize.containsKey(Find(child.Children, "type").value))
                {
                    Node getClassName = null;
                    for(Node nd: allNodes) {
                        if(nd.label.equals("structDecl")) {
                            for(Node ndchild: nd.Children) {
                                if(ndchild.label.equals ("id") && Find(child.Children, "type").value.equals (ndchild.value)) {
                                    getClassName = ndchild;
                                }
                            }
                        }
                    }
                    if (getClassName == null)
                        semanticErrors.add("[error] undeclared class: at line " + Find(child.Children,"id").line);
                }
            }
        }
    }


    private void FindClassErrors(Node node, List<Node> allNodes, Node rootNode) throws IOException {
        List<Node> implNodes = FindAll(allNodes,"implDecl");
        for (Node childImplNode : implNodes)
        {
            if (Find(childImplNode.Children, "id").value.equals(Find(node.Children, "id").value))
            {
                List<Node> functionDefList = FindAll(Find(childImplNode.Children,"funcDefList").Children, "funcDef");
                CheckforGlobalClassErrors(node, functionDefList, allNodes);
                for (Node funcNode : functionDefList)
                {
                    Node StatementBlockNode = Find(funcNode.Children,"statementBlock");
                    for (Node child : StatementBlockNode.Children)
                    {

                        if (child.label.equals("assignStatement"))
                        {
                            CheckForAssignmentErrors(child, allNodes, node);
                        }
                        if (child.label.equals ("fcallStatement"))
                        {
                            CheckForUndefinedErrors(child, allNodes, rootNode);
                        }
                    }

                }
            }
        }
    }
    private void CheckforGlobalClassErrors(Node node, List<Node> functionDefList, List<Node> allNodes)
    {
        List<String> implementedFunction = new ArrayList<> ();
        List<String> declaredFunction = new ArrayList<> ();
        List<String> varDecl = new ArrayList<> ();
        Node classNameNode = Find(node.Children, "id");

        HashSet<String> seSet = new HashSet<>();
        for(SymbolEntry se: node.m_symtab.content) {
            if(se.NodeType.equals("Class") && se.Name.equals(classNameNode.value) && !se.Inherits.equals("")) {
                seSet.add(se.Inherits);
            }
        }


        String inheritsClass = null;
        if(seSet.size ()>0) {
            inheritsClass = seSet.stream().findFirst().get();
        }

        for (Node implNode : functionDefList) {
            implementedFunction.add(Find(implNode.Children, "id").value);
        }
        List<Node> memDeclNodes = FindAll(Find(node.Children, "memberList").Children, "membDecl");
        if (memDeclNodes != null)
        {
            for (Node memNode: memDeclNodes)
            {
                Node funcNode = Find(memNode.Children, "funcDecl");
                Node varNode = Find(memNode.Children, "varDecl");
                if (varNode != null)
                {
                    if (varDecl.size() > 0 && varDecl.contains(Find(varNode.Children, "id").value))
                        semanticErrors.add("[error] multiply declared data member in class: at line " + Find(varNode.Children, "id").line);
                    else
                        varDecl.add(Find(varNode.Children, "id").value);
                }
                if (funcNode != null)
                {
                    if (declaredFunction.size() > 0 & declaredFunction.contains(Find(funcNode.Children, "id").value))
                        semanticErrors.add("[warning] overloaded member function: at line " + Find(funcNode.Children, "id").line);
                    else
                        declaredFunction.add(Find(funcNode.Children, "id").value);
                    if (!implementedFunction.contains(Find(funcNode.Children, "id").value))
                        semanticErrors.add("[error] undefined member function declaration: at line " + Find(funcNode.Children, "id").line);

                    if (inheritsClass!=null && !inheritsClass.equals(""))
                    {
                        List<Node> inheritClassNode = new ArrayList<> ();
                        for(Node nd: allNodes) {
                            if(nd.label.equals ("structDecl")) {
                                inheritClassNode.add(nd);
                            }
                        }
                        for (Node inheritClass : inheritClassNode)
                        {
                            String inheritNode = Find(inheritClass.Children, "id").value;
                            if (inheritNode.equals(inheritsClass) )
                            {
                                HashSet<String> seset = new HashSet<> ();
                                for(SymbolEntry se: inheritClass.m_symtab.content) {
                                    if(se.NodeType.equals("Function")) {
                                        seset.add(se.Name);
                                    }
                                }
                                List<String> inheritFunctNames = new ArrayList<> ();
                                for(String fn: seset) inheritFunctNames.add (fn);
                                if (inheritFunctNames != null && inheritFunctNames.size () > 0)
                                {
                                    if (inheritFunctNames.contains(Find(funcNode.Children, "id").value))
                                        semanticErrors.add("[warning] Overridden member function: at line " + Find(funcNode.Children, "id").line);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String item : implementedFunction)
        {
            if (!declaredFunction.contains(item))
            {
                List<Node> listUndeclFunc = new ArrayList<> ();
                for(Node fd: functionDefList) {
                    if(Find(fd.Children, "id").equals (item)){
                        listUndeclFunc.add(fd);
                    }
                }
                for (Node undeclFunc : listUndeclFunc)
                {
                    semanticErrors.add("[error] undeclared member function definition: at line " + Find(undeclFunc.Children, "id").line);
                }
            }
        }

        List<Node> classDecl = new ArrayList<> ();
        for(Node sdnode: allNodes) {
            if(sdnode.label.equals("structDecl") && Find(sdnode.Children, "id").value.equals(Find(node.Children, "id").value)) {
                classDecl.add(sdnode);
            }
        }
        if (classDecl.size() > 1)
        {
            int i = 1;
            while (i < classDecl.size())
            {
                semanticErrors.add("[error] multiply declared class: at line " + Find(classDecl.get (i).Children, "id").line);
                i++;
            }

        }
        List<String> varNames = new ArrayList<> ();
        for(SymbolEntry se: node.m_symtab.content) {
            if(se.NodeType.equals("Class") && se.Name.equals(classNameNode.value)) {
                varNames.add(se.VariableName);
            }
        }

        List<Node> inheritVarNames = new ArrayList ();
        for(Node nd: allNodes) {
            if(nd.label.equals("structDecl") && Find(nd.Children, "id").value.equals(inheritsClass)) {
                inheritVarNames.add(nd);
            }
        }
        for (Node classNode : inheritVarNames)
        {
            HashSet<String> tempVI = new HashSet<> ();
            for(SymbolEntry se: node.m_symtab.content) {
                if(se.NodeType.equals("Class")) {
                    tempVI.add(se.VariableName);
                }
            }
            List<String> variableInherits = new ArrayList<> ();
            for(String t: tempVI) variableInherits.add(t);

            List<String> CommonVar = new ArrayList<> ();
            for(String x: variableInherits) {
                for(String y: varNames) {
                    if(x.equals (y)) {
                        CommonVar.add (x);
                    }
                }
            }
            if (CommonVar != null && CommonVar.size () > 0)
            {
                for (String varName : CommonVar)
                {
                    if (memDeclNodes != null)
                    {
                        for (Node memNode : memDeclNodes)
                        {
                            Node varNode = Find(memNode.Children, "varDecl");
                            if (varNode != null)
                            {
                                if (Find(varNode.Children, "id").value.equals(varName))
                                semanticErrors.add("[warning] shadowed inherited data member: at line " + Find(varNode.Children, "id").line);

                            }
                        }
                    }
                }
            }
        }
    }

    private void CheckFunctionGlobalErrors(Node node, List<Node> allNodes) throws IOException {
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
                semanticErrors.add("[error] multiply same name parameters: at line " + node.line);
        }

        if (GroupByVar.size () > 0)
        {
            List<String> result = new ArrayList<> ();
            for(String key: GroupByVar.keySet ()) {
                if(key!=null && !key.isEmpty() && GroupByVar.getOrDefault(key, -1) != -1) {
                    result.add (key);
                }
            }
            for (String item : result)
            {
                int lineNumber = 0;
                List<Node> varNodes = FindAll(Find(node.Children, "statementBlock").Children, "varDecl");
                for (Node varNode : varNodes)
                {
                    if (Find(varNode.Children, "id").value.equals(item))
                    lineNumber = Find(varNode.Children, "id").line;
                }
                semanticErrors.add("[error] multiply declared identifier in function : at line " + lineNumber);
            }
        }

        List<Node> funcDecl = new ArrayList<> ();
        for(Node nodek: allNodes) {
            if(nodek.label.equals("funcDef") && Find(nodek.Children, "id").value.equals(Find(nodek.Children, "id").value)) {
                funcDecl.add (nodek);
            }
        }
        if (funcDecl.size() > 1)
        {
            int i = 1;
            while (i < funcDecl.size())
            {
                if ((Find(funcDecl.get(i).Children, "fparamList").Children.size() == Find(funcDecl.get(0).Children, "fparamList").Children.size())
                        && (Find(funcDecl.get(i).Children, "type").value.equals(Find(funcDecl.get(0).Children, "type").value)))
                {
                    semanticErrors.add("[error] multiply declared free function: at line " + Find(funcDecl.get(i).Children, "id").line);
                }
                else
                    semanticErrors.add("[warning] overloaded free function: at line " + Find(funcDecl.get(i).Children, "id").line);
                i++;
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
                        for(String semanticError: semanticErrors)
                            outsemanticerrors.write(semanticError + "\n");
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
