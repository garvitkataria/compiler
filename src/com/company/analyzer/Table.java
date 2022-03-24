package com.company.analyzer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Table {
    List<SymbolEntry> content;

    public Table() {
        this.content = new ArrayList<> ();
    }

    public void addNewRow(SymbolEntry entryFields) {
        content.add(new SymbolEntry(entryFields.NodeType, entryFields.Name, entryFields.ParameterName, entryFields.ParameterType, entryFields.ReturnType, entryFields.VariableName, entryFields.VariableType, entryFields.VarVisibility, entryFields.NodeVisibility, entryFields.Inherits, entryFields.ParentClass));
    }

    @Override
    public String toString() {
        return "Table{" +
                "content=" + content +
                '}';
    }

    public List<String> getDataParentClass() {
        List<String> ll = new ArrayList<> ();
        for(SymbolEntry se: this.content) {
            if(se.ParentClass != null)
                ll.add(se.ParentClass);
        }
        return ll;
    }

    public List<String> getDataParameterNameVariableName(String variableName) {
        List<String> ll = new ArrayList<> ();
        for(SymbolEntry se: this.content) {
            if(se.ParameterName.equals(variableName) || se.VariableName.equals(variableName))
                ll.add(se.ParentClass);
        }
        return ll;
    }

    public List<String> getDataParameterName(String value) {
        List<String> ll = new ArrayList<> ();
        for(SymbolEntry se: this.content) {
            if(se.ParameterName == value)
                ll.add(se.ParameterName);
        }
        return ll;
    }

    public List<String> getDataVariableName(String value) {
        List<String> ll = new ArrayList<> ();
        for(SymbolEntry se: this.content) {
            if(se.VariableName == value)
                ll.add(se.VariableName);
        }
        return ll;
    }

    public HashMap<String, Integer> getGroupByParam() {
        HashMap<String, Integer> map = new HashMap<>();
        for(SymbolEntry se: this.content) {
            map.put(se.ParameterName, map.getOrDefault(se.ParameterName, 0) + 1);
        }
        return map;
    }

    public HashMap<String, Integer> getGroupByVar() {
        HashMap<String, Integer> map = new HashMap<>();
        for(SymbolEntry se: this.content) {
            map.put(se.VariableName, map.getOrDefault(se.VariableName, 0) + 1);
        }
        return map;
    }
}
