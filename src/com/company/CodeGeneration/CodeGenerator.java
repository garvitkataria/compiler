package com.company.CodeGeneration;

import com.company.analyzer.Node;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CodeGenerator {
    FileWriter moon = null;
    StringBuilder moonsb = null;
    public CodeGenerator(FileWriter moon, StringBuilder moonsb) {
        this.moon = moon;
        this.moonsb = moonsb;
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
        String type = null;
        List<String> fparamList = null;
        Collections.reverse (node.Children);
        for(Node child: node.Children) {
            if(child.label.equals("statementBlock")) {
                moonsb.append("\n"+funcName+" "+fparamList + " : " + type + "\n");
                generateStatementBlockCode(child);
            }
            else if(child.label.equals("fparamList")) {
                fparamList = getFparamList(child);
            }
            else if(child.label.equals("id")) {
                funcName = child.value;
            }
            else if(child.label.equals("type")) {
                type = child.value;
            }
        }
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
        moonsb.append("start generateIfStatementCode\n");
        Collections.reverse (node.Children);
        int block = 1;
        for(Node child: node.Children) {
            if(child.label.equals("statementBlock")) {
                moonsb.append("start block " + block +"\n");
                generateStatementBlockCode(child);
                moonsb.append("end block " + block +"\n");
                block++;
            }
            else if(child.label.equals("relExpr")) {
                generateRelExprCode(child);
            }
        }
        moonsb.append("end generateIfStatementCode\n");
    }

    private void generateRelExprCode(Node node) {
        moonsb.append("start generateRelExprCode\n");
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
        moonsb.append( "condition# "+ var2+" "+relOp+" "+var1 +"\n");
        moonsb.append("end generateRelExprCode \n");
    }

    private void generateStatementBlockCode(Node pnode) {
        moonsb.append( "start generateStatementBlockCode\n");
        Collections.reverse (pnode.Children);
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
            } else if (node.label.equals ("returnStatement")) {
                generateReturnStatement (node);
            }
        }
        moonsb.append("end generateStatementBlockCode\n");
    }

    private void generateReturnStatement(Node node) {
        String var = null;
        for(Node child: node.Children) {
            if(child.label.equals("intlit")) {
                var = child.value;
            }
            else if(child.label.equals("functionCall/DataMember")) {
                var = getDataMember(child);
            }
        }
        moonsb.append("return "+var+"\n");
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
        moonsb.append( fcallName+" "+aParams + "\n");
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
        moonsb.append( "generateWhileStatementCode\n");
        Collections.reverse(node.Children);
        for(Node child: node.Children) {
            if(child.label.equals("relExpr")) {
                generateRelExprCode(child);
            }
            else if(child.label.equals("statementBlock")) {
                generateStatementBlockCode(child);
            }

        }
        moonsb.append("====generateWhileStatementCode====\n");
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
        moonsb.append ( var+" = "+tempVar + "\n");
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
            moonsb.append( tempvar+" = "+var2 + " " + opr + " " + var1 + "\n");
        }
        else if(node.label.equals("addOp")) {
            moonsb.append ( tempvar+" = "+var2 + " " + opr + " " + var1 + "\n");
        }
        return tempvar;
    }
    private static long idCounter = 0;
    private String generateTempVar() {
        String tempVar = "temp" + idCounter++;
        moonsb.append ("integer " + tempVar+"\n");
        return tempVar;
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
        moonsb.append( type+" "+id+dimList + "\n");
    }

    private String getDimList(Node node) {
        StringBuilder sb = new StringBuilder();
        for(Node child: node.Children) {
            sb.insert(0,"["+child.value+"]");
        }
        return sb.toString();
    }

    private void generateReadStatementCode(Node node) {
        moonsb.append("read "+ getVariable(node.Children.get(0)) + "\n");
    }

    private void generateWriteStatementCode(Node node) {
        //todo expr
        moonsb.append("write "+ getDataMember(node.Children.get(0)) + "\n");
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
