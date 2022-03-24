package com.company.CodeGeneration;

import com.company.analyzer.Node;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {
    FileWriter moon = null;
    public CodeGenerator(FileWriter moon) {
        this.moon = moon;
    }

    public void generate(Node root) {
        Node structOrImplOrFuncListNode = root.Children.get(0);
        traverseTree(structOrImplOrFuncListNode);
    }

    private void traverseTree(Node node) {
        for(Node child: node.Children) {
            if(child.label.equals("funcDef")) {
                generateFuncDefCode(child);
            }
        }
    }

    private void generateFuncDefCode(Node node) {
        String funcName = null;
        List<String> fparamList = null;
        for(Node child: node.Children) {
            if(child.label.equals("statementBlock")) {
                generateStatementBlockCode(child);
            }
            else if(child.label.equals("fparamList")) {
                fparamList = getFparamList(child);
            }
            else if(child.label.equals("id")) {
                funcName = child.value;
            }
        }
        System.out.println(funcName+" "+fparamList);
    }

    private List<String> getFparamList(Node node) {
        List<String> fparamList = new ArrayList<> ();
        for(Node child: node.Children) {
            fparamList.add(getFparam(child));
        }
        return fparamList;
    }

    private String getFparam(Node node) {
        String id = null;
        String type = null;
        String dimList = null;

        for(Node child: node.Children) {
            if(child.label.equals("dimList")) {
                dimList = getDimList(child);
            }
            else if(child.label.equals("type")) {
                type = child.value;
            }
            else if(child.label.equals("id")) {
                id = child.value;
            }
        }
        return type+" "+id+dimList;
    }

    private void generateIfStatementCode(Node node) {
        System.out.println("====generateIfStatementCode====");
        for(Node child: node.Children) {
            if(child.label.equals("statementBlock")) {
                generateStatementBlockCode(child);
            }
            else if(child.label.equals("relExpr")) {
                generateRelExprCode(child);
            }
        }
        System.out.println("generateIfStatementCode");
    }

    private void generateRelExprCode(Node node) {
        System.out.println ("====generateRelExprCode====");
        String var1 = null;
        String var2 = null;
        String relOp = null;
        for(Node child: node.Children) {
            if(child.label.equals("intlit")) {
                if(var1 == null) {
                    var1 = child.value;
                }
                else {
                    var2 = child.value;
                }
            }
            else if(child.label.equals("functionCall/DataMember")) {
                if(var1 == null) {
                    var1 = getDataMember(child);
                }
                else {
                    var2 = getDataMember(child);
                }
            }
            else if(child.label.equals("relOp")) {
                relOp = child.value;
            }
            else if(child.label.equals("multOp")  || child.label.equals("addOp")) {
                if(var1 == null) {
                    var1 = generateMultAddOp(child);
                }
                else {
                    var2 = generateMultAddOp(child);
                }
            }
        }
        System.out.println (var2+" "+relOp+" "+var1);
        System.out.println("generateRelExprCode");
    }

    private void generateStatementBlockCode(Node pnode) {
        System.out.println(" ====generateStatementBlockCode==== ");
        for(Node node: pnode.Children) {
            if (node.label.equals ("writeStatement")) {
                generateWriteStatementCode (node);
            } else if (node.label.equals ("readStatement")) {
                generateReadStatementCode (node);
            } else if (node.label.equals ("varDecl")) {
                generateVarDeclCode (node);
            } else if (node.label.equals ("assignStatement")) {
                generateAssignStatementCode (node);
            } else if (node.label.equals ("ifStatement")) {
                generateIfStatementCode (node);
            } else if (node.label.equals ("whileStatement")) {
                generateWhileStatementCode (node);
            } else if (node.label.equals ("fcallStatement")) {
                generateFcallStatementCode (node);
            }
        }
        System.out.println("generateStatementBlockCode");
    }

    private void generateFcallStatementCode(Node node) {
        String fcallName = null;
        List<String> aParams = null;
        for(Node child: node.Children) {
            if(child.label.equals("aParams")) {
                aParams = getAParams(child);
            }
            else if(child.label.equals("var0")) {
                fcallName = getVar0 (child);
            }
        }
        System.out.println (fcallName+" "+aParams);
    }

    private List<String> getAParams(Node node) {
        List<String> aparams = new ArrayList<> ();
        for(Node child: node.Children) {
            if(child.label.equals("intlit")) {
                aparams.add(child.value);
            }
            else if(child.label.equals("functionCall/DataMember")) {
                aparams.add(getDataMember(child));
            }
        }
        return aparams;
    }

    private void generateWhileStatementCode(Node node) {
        System.out.println("====generateWhileStatementCode====");

        for(Node child: node.Children) {
            if(child.label.equals("relExpr")) {
                generateRelExprCode(child);
            }
            else if(child.label.equals("statementBlock")) {
                generateStatementBlockCode(child);
            }

        }
        System.out.println("generateWhileStatementCode");
    }

    private void generateAssignStatementCode(Node node) {
        String var = null;
        String tempVar = null;
        for(Node child: node.Children) {
            if(child.label.equals("intlit")) {
                tempVar = child.value;
            }
            else if(child.label.equals("var")) {
                var = getVariable(child);
            }
            else if(child.label.equals("functionCall/DataMember")) {
                tempVar = getDataMember(child);
            }
            else if(child.label.equals("multOp") || child.label.equals("addOp")) {
                tempVar = tempVar = generateMultAddOp(child);
            }
        }
        System.out.println (var+" = "+tempVar);
    }

    private String generateMultAddOp(Node node) {
        String var1 = null;
        String var2 = null;
        String opr = null;
        for(Node child: node.Children) {
            if(child.label.equals("functionCall/DataMember")) {
                if(var1 == null) {
                    var1 = getDataMember(child);
                }
                else {
                    var2 = getDataMember(child);
                }
            }
            else if(child.label.equals("intlit")) {
                if(var1 == null) {
                    var1 = child.value;
                }
                else {
                    var2 = child.value;
                }
            }
            else if(child.label.equals("opr")) {
                opr = child.value;
            }
            else if(child.label.equals("multOp") || child.label.equals("addOp")) {
                if(var1 == null) {
                    var1 = generateMultAddOp(child);
                }
                else {
                    var2 = generateMultAddOp(child);
                }
            }
        }
        String tempvar = generateTempVar();
        if(node.label.equals("multOp")) {
            System.out.println (tempvar+" = "+var2 + " " + opr + " " + var1);
        }
        else if(node.label.equals("addOp")) {
            System.out.println (tempvar+" = "+var2 + " " + opr + " " + var1);
        }
        return tempvar;
    }
    private static long idCounter = 0;
    private String generateTempVar() {
        return String.valueOf("temp"+idCounter++);
    }

    private void generateVarDeclCode(Node node) {
        String type = "";
        String id = "";
        String dimList = "";
        for(Node child: node.Children) {
            if(child.label.equals("dimList")) {
                dimList = getDimList(child);
            }
            else if(child.label.equals("type")) {
                type = child.value;
            }
            else if(child.label.equals("id")) {
                id = child.value;
            }
        }
        System.out.println (type+" "+id+dimList);
    }

    private String getDimList(Node node) {
        StringBuilder sb = new StringBuilder();
        for(Node child: node.Children) {
            sb.insert(0,"["+child.value+"]");
        }
        return sb.toString();
    }

    private void generateReadStatementCode(Node node) {
        System.out.println ("read "+ getVariable(node.Children.get(0)));
    }

    private void generateWriteStatementCode(Node node) {
        //todo expr
        System.out.println ("write "+ getDataMember(node.Children.get(0)));
    }

    private String getDataMember(Node node) {
        // todo
        return getVariable(node);
    }

    private String getVariable(Node node) {
        String var0 = "";
        String indiceList = "";
        for(Node child: node.Children) {
            if(child.label.equals("var0")) {
                var0 = getVar0(child);
            }
            else if(child.label.equals("indiceList")) {
                indiceList = getIndiceList(child);
            }
        }
        return var0+indiceList;
    }

    private String getVar0(Node node) {
        // todo
        return node.Children.get(0).value;
    }

    private String getIndiceList(Node node) {
        StringBuilder sb = new StringBuilder();
        for(Node child: node.Children) {
            if(child.label.equals("intlit")) {
                sb.insert(0,"["+child.value+"]");
            }
            else if(child.label.equals("functionCall/DataMember")) {
                sb.insert(0,"["+getDataMember(child)+"]");
            }
        }
        return sb.toString();
    }
}
