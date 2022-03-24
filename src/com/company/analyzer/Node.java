package com.company.analyzer;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Node {
    public String label;
    public String value;
    public List<Node> Children;
    int line = -1;
    public Table m_symtab;
    public SymbolEntry entry;
    public Node() {
    }

    public Node(String label) {
        this.label = label;
        this.value = null;
        Children = new ArrayList<>();
    }
    public Node(String label, String value, int line) {
        this.line = line;
        this.label = label;
        this.value = value;
        Children = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Node{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", line=" + line +
                '}';
    }
}
