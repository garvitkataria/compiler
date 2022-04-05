package com.company.analyzer;

public class SymbolEntry {


    public String NodeType;
    public String Name;
    public String ParameterName;
    public String ParameterType;
    public String ReturnType;
    public String VariableName;
    public String VariableType;
    public String VarVisibility;
    public String NodeVisibility;
    public String Inherits;
    public String ParentClass;
    public int size;

    public SymbolEntry()
    {
    }

    public SymbolEntry(String nodeType, String name, String parameterName, String parameterType, String returnType, String variableName, String variableType, String varVisibility, String nodeVisibility, String inherits, String parentClass) {
        NodeType = nodeType;
        Name = name;
        ParameterName = parameterName;
        ParameterType = parameterType;
        ReturnType = returnType;
        VariableName = variableName;
        VariableType = variableType;
        VarVisibility = varVisibility;
        NodeVisibility = nodeVisibility;
        Inherits = inherits;
        ParentClass = parentClass;
    }

    @Override
    public String toString() {
        return "SymbolEntry{" +
                "NodeType='" + NodeType + '\'' +
                ", Name='" + Name + '\'' +
                ", ParameterName='" + ParameterName + '\'' +
                ", ParameterType='" + ParameterType + '\'' +
                ", ReturnType='" + ReturnType + '\'' +
                ", VariableName='" + VariableName + '\'' +
                ", VariableType='" + VariableType + '\'' +
                ", VarVisibility='" + VarVisibility + '\'' +
                ", NodeVisibility='" + NodeVisibility + '\'' +
                ", Inherits='" + Inherits + '\'' +
                ", ParentClass='" + ParentClass + '\'' +
                ", size=" + size +
                '}';
    }
}
