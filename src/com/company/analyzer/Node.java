package com.company.analyzer;

import java.util.ArrayList;
import java.util.List;

public class Node {
    String label;
    String value;
    List<Node> Children;
    int line = -1;
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
}
