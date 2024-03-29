package com.company.analyzer;

import java.io.*;
import java.util.*;

public class SyntaxAnalysis
{
    private String lookahead = null;
    private int currentTokenIndex = 0;
    private boolean error = false;
    HashSet<String> tokenTypesToIgnore = new HashSet<>();
    Lexer lex;
    Token prevtoken = null;
    Token token = null;
    FileWriter outderivation = null;
    FileWriter outsyntaxerrors = null;
    public Node root = null;
    public static Stack<Node> st;

    public boolean pushInStack(String label) {
        if(prevtoken != null) st.push(new Node(label, prevtoken.value, token.line));
        else st.push(new Node(label));
        return true;
    }

    public boolean popFromStack(String label, int noOfChildren) {
        Node parent = new Node(label, token.value, token.line);
        System.out.print ("popFromStack: "+ label+": ");
        while(noOfChildren>0 && st.size ()>0) {
            parent.Children.add(st.peek());
            System.out.print (st.peek().label+" ");
            st.pop();
            noOfChildren--;
        }
        System.out.println ();
        st.push(parent);
        return true;
    }

    public boolean popFromStackUntilEpsilon(String label) {
        Node parent = new Node(label, token.value, token.line);
        System.out.print ("popFromStackUntilEpsilon: "+ label+": ");
        while(!st.peek().label.equals("epsilon")) {
            parent.Children.add(st.peek());
            System.out.print (st.peek().label+" ");
            st.pop();
        }
        st.pop();
        System.out.println ();
        st.push(parent);
        return true;
    }

    public static void printAST(Node node, int depth, FileWriter outast) throws IOException {
        for(int i=0; i<depth; i++)
            outast.write("|\t");
        outast.write(node.label+"(value: "+node.value+", line:"+node.line+")\n");
        for(Node child: node.Children) {
            printAST(child, depth+1, outast);
        }
    }

    public SyntaxAnalysis(Lexer lexer) {
        st = new Stack<>();
        this.lex = lexer;
        tokenTypesToIgnore.add("inlinecmt");
        tokenTypesToIgnore.add("blockcmt");

    }
    
    public boolean Parse(FileWriter outderivation, FileWriter outsyntaxerrors)
    {
        this.outderivation = outderivation;
        this.outsyntaxerrors = outsyntaxerrors;
        try {
            GetNextToken();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        if (START())
            return true;
        else
            return false;
    }
    
    public void writeOutDerivation(String str) {

        try {
            this.outderivation.write(str+"\n");
            //System.out.print(str+"\n");
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
    private void writeOutDerivationNoLineBreak(String str) {
        try {
            this.outderivation.write(str);
            //System.out.print (str);
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void writeOutSyntaxErrors(String str) {
        try {
            this.outsyntaxerrors.write(str+"\n");
            //System.out.print (str+"\n");
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
    
    private boolean Match(String v)
    {
        if (lookahead.equals(v))
        {
            writeOutDerivationNoLineBreak(lookahead+" ");
            try {
                GetNextToken();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            return true;
        }
        else
        {
            writeOutSyntaxErrors("Syntax error at line " + token.line + ", expected token: "+ v );
            try {
                GetNextToken();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            return false;
        }
    }



    private boolean START()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func", "impl", "struct"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (firstSet.contains(lookahead))
        {
            if (PROG() && popFromStack("prog", 1)) {
                writeOutDerivation ("START -> PROG");
            }
            else
                error = true;
        }
        else
            error = true;
        return !error;
    }

    private boolean skipErrors(HashSet<String> firstSet, HashSet<String> followSet, boolean epsilon) {
        if(firstSet.contains(lookahead) || (epsilon && followSet.contains(lookahead))) return true;
        else {
            writeOutSyntaxErrors ("Syntax error at: "+ token.line +" "+token.value);
            while(!firstSet.contains(lookahead) && !followSet.contains(lookahead)) {
                try {
                    GetNextToken();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
                if(epsilon && followSet.contains(lookahead)) return false;
            }
        }
    return true;
    }

    private void GetNextToken() throws IOException {
        prevtoken = token;
        token = lex.scan();
        System.out.println("prevtoken"+prevtoken);
        System.out.println("token"+token);
        if(token == null) return;
        lookahead = token.type;

        while(tokenTypesToIgnore.contains(lookahead)) {
            token = lex.scan();
            if(token == null) return;
            lookahead = token.type;
        }
        currentTokenIndex = currentTokenIndex + 1;
    }

    private boolean APARAMS()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"intnum", "floatnum", "openpar", "not", "id", "plus", "minus"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closepar"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (firstSet.contains(lookahead))
        {
            if (EXPR() && REPTAPARAMS1())
                writeOutDerivation("APARAMS -> EXPR REPTAPARAMS1");
            else
                error = true;
        }
        else if (followSet.contains(lookahead))
            writeOutDerivation("APARAMS -> epsilon");

        else
            error = true;

        return !error;
    }

    private boolean REPTAPARAMS1()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"comma"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closepar"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (firstSet.contains(lookahead))
        {
            if (APARAMSTAIL() && REPTAPARAMS1())
                writeOutDerivation("REPTAPARAMS1-> APARAMSTAIL REPTAPARAMS1");
            else
                error = true;
        }
        else if (followSet.contains(lookahead))
            writeOutDerivation("REPTAPARAMS1 -> epsilon");

        else
            error = true;

        return !error;
    }

    private boolean APARAMSTAIL()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"comma", "closepar"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closepar"}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("comma"))
        {
            if (Match("comma") && EXPR())
                writeOutDerivation("APARAMSTAIL -> comma EXPR");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean ADDOP()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"plus", "minus", "or"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"id", "intlit", "floatlit", "lpar", "not", "plus", "minus"}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("plus"))
        {
            if (Match("plus") && pushInStack("opr"))
                writeOutDerivation("ADDOP -> plus");
            else
                error = true;
        }
        else if (lookahead.equals("minus"))
        {
            if (Match("minus") && pushInStack("opr"))
                writeOutDerivation("ADDOP -> minus");
            else
                error = true;
        }
        else if (lookahead.equals("or"))
        {
            if (Match("or") && pushInStack("opr"))
                writeOutDerivation("ADDOP -> or");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean ARITHEXPR()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"intnum", "floatnum", "openpar", "not", "id", "plus", "minus"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{""}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("intnum") || lookahead.equals("floatnum") || lookahead.equals("openpar") || lookahead.equals("not") || lookahead.equals("id") || lookahead.equals("plus") || lookahead.equals("minus"))
        {
            if (TERM() && RIGHTRECARITHEXPR())
                writeOutDerivation("ARITHEXPR -> TERM RIGHTRECARITHEXPR");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean RIGHTRECARITHEXPR()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"plus", "minus", "or"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closesqbr", "eq", "neq", "lt", "gt", "leq", "geq", "comma", "closepar", "semi"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("plus") || lookahead.equals("minus") || lookahead.equals("or"))
        {
            if (ADDOP() && TERM() && popFromStack("addOp", 3) && RIGHTRECARITHEXPR())
                writeOutDerivation("RIGHTRECARITHEXPR -> ADDOP TERM RIGHTRECARITHEXPR");
            else
                error = true;
        }
        else if (lookahead.equals("closesqbr") || lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq") || lookahead.equals("comma") || lookahead.equals("closepar") || lookahead.equals("semi"))
            writeOutDerivation("RIGHTRECARITHEXPR -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean RIGHTRECTERM()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"mult", "div", "and"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closesqbr", "eq", "neq", "lt", "gt", "leq", "geq", "plus", "minus", "or", "comma", "closepar", "semi"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("mult") || lookahead.equals("div") || lookahead.equals("and"))
        {
            if (MULTOP() && FACTOR() && popFromStack("multOp", 3) && RIGHTRECTERM())
                writeOutDerivation("RIGHTRECTERM -> MULTOP FACTOR RIGHTRECTERM");
            else
                error = true;
        }
        else if (lookahead.equals("closesqbr") || lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq") || lookahead.equals("plus") || lookahead.equals("minus") || lookahead.equals("or") || lookahead.equals("comma") || lookahead.equals("closepar") || lookahead.equals("semi"))
            writeOutDerivation("RIGHTRECTERM -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean REPTSTATBLOCK1()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"if", "while", "read", "write", "return", "id"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closecubr"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("if") || lookahead.equals("while") || lookahead.equals("read") || lookahead.equals("write") || lookahead.equals("return") || lookahead.equals("id"))
        {
            if (STATEMENT() && REPTSTATBLOCK1())
                writeOutDerivation("REPTSTATBLOCK1 -> STATEMENT REPTSTATBLOCK1 ");
            else
                error = true;
        }
        else if (lookahead.equals("closecubr"))
            writeOutDerivation("REPTSTATBLOCK1 -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean REPTSTRUCTDECL4()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"public", "private"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closecubr"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("public") || lookahead.equals("private"))
        {
            if (VISIBILITY() && pushInStack("visibility") && MEMBERDECL() && popFromStack ("membDecl", 2) && REPTSTRUCTDECL4())
                writeOutDerivation("REPTSTRUCTDECL4 -> VISIBILITY MEMBERDECL REPTSTRUCTDECL4");
            else
                error = true;
        }
        else if (lookahead.equals("closecubr")) {
            writeOutDerivation("REPTSTRUCTDECL4 -> epsilon");
        }
        else
            error = true;

        return !error;
    }

    private boolean REPTVARDECL4()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"semi"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("opensqbr"))
        {
            if (ARRAYSIZE() && REPTVARDECL4())
                writeOutDerivation("REPTVARDECL4 -> ARRAYSIZE REPTVARDECL4");
            else
                error = true;
        }
        else if (lookahead.equals("semi"))
            writeOutDerivation("REPTVARDECL4 -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean ARRAYSIZE()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("opensqbr"))
        {
            if (Match("opensqbr") && ARRAYDASH())
                writeOutDerivation("ARRAYSIZE -> lsqbr ARRAYDASH");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean ARRAYDASH()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"intnum", "closesqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("intnum"))
        {
            if (Match("intnum") && pushInStack("num") && Match("closesqbr"))
                writeOutDerivation("ARRAYDASH -> intnum rsqbr");
            else
                error = true;
        }
        else if (lookahead.equals("closesqbr"))
        {
            if (Match("closesqbr"))
                writeOutDerivation("ARRAYDASH -> rsqbr ");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean ASSIGNOP()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"assign"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("assign"))
        {
            if (Match("assign"))
                writeOutDerivation("ASSIGNOP -> equal");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean EXPR()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"intnum", "floatnum", "openpar", "not", "id", "plus", "minus"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("intnum") || lookahead.equals("floatnum") || lookahead.equals("openpar") || lookahead.equals("not") || lookahead.equals("id") || lookahead.equals("plus") || lookahead.equals("minus"))
        {
            if (ARITHEXPR() && EXPRDASH())
                writeOutDerivation("EXPR -> ARITHEXPR EXPRDASH");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean EXPRDASH()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"eq", "neq", "lt", "gt", "leq", "geq"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"semi", "comma", "closepar"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq"))
        {
            if (RELOP() && ARITHEXPR() && popFromStack("relExp", 3))
                writeOutDerivation("EXPRDASH -> RELOP ARITHEXPR");
            else
                error = true;
        }
        else if (lookahead.equals("semi") || lookahead.equals("comma") || lookahead.equals("closepar"))
            writeOutDerivation("EXPRDASH ->  epsilon");

        else
            error = true;

        return !error;
    }

    private boolean FPARAMS()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"id"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closepar"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("id"))
        {
            if (Match("id") && pushInStack("id") && Match("colon") && TYPE() && pushInStack("epsilon") && REPTFPARAMS3() && popFromStackUntilEpsilon("dimList") && popFromStack ("fparam", 3) && REPTFPARAMS4())
                writeOutDerivation("FPARAMS -> id colon TYPE REPTFPARAMS3 REPTFPARAMS4");
            else
                error = true;
        }
        else if (lookahead.equals("closepar"))
            writeOutDerivation("FPARAMS ->  epsilon");

        else
            error = true;

        return !error;
    }

    private boolean REPTFPARAMS4()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"comma"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closepar"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("comma"))
        {
            if (FPARAMSTAIL() && popFromStack ("fparam", 3) && REPTFPARAMS4())
                writeOutDerivation("REPTFPARAMS4 -> FPARAMSTAIL REPTFPARAMS4");
            else
                error = true;
        }
        else if (lookahead.equals("closepar"))
            writeOutDerivation("REPTFPARAMS4 -> epsilon");

        else
            error = true;

        return !error;
    }

    private boolean REPTFPARAMS3()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closepar", "comma"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("opensqbr"))
        {
            if (ARRAYSIZE() && REPTFPARAMS3())
                writeOutDerivation("REPTFPARAMS3 -> ARRAYSIZE REPTFPARAMS3");
            else
                error = true;
        }
        else if (lookahead.equals("closepar") || lookahead.equals("comma"))
            writeOutDerivation("REPTFPARAMS3 -> epsilon");

        else
            error = true;

        return !error;
    }

    private boolean FPARAMSTAIL()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"comma"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("comma"))
        {
            if (Match("comma") && Match("id") && pushInStack("id") && Match("colon") && TYPE() && pushInStack("epsilon") && REPTFPARAMSTAIL4() && popFromStackUntilEpsilon("dimList"))
                writeOutDerivation("FPARAMSTAIL -> comma id colon TYPE REPTFPARAMSTAIL4");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean REPTFPARAMSTAIL4()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"comma", "closepar"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("opensqbr"))
        {
            if (ARRAYSIZE() && REPTFPARAMSTAIL4())
                writeOutDerivation("REPTFPARAMSTAIL4 -> ARRAYSIZE REPTFPARAMSTAIL4");
            else
                error = true;
        }
        else if (lookahead.equals("comma") || lookahead.equals("closepar"))
            writeOutDerivation("REPTFPARAMSTAIL4 -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean FACTOR()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"intnum", "floatnum", "id", "openpar", "minus", "plus", "not"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("intnum"))
        {
            if (Match("intnum") && pushInStack("intlit"))
                writeOutDerivation("FACTOR -> intnum");
            else
                error = true;
        }

        else if (lookahead.equals("floatnum"))
        {
            if (Match("floatnum") && pushInStack("floatnum"))
                writeOutDerivation("FACTOR -> floatnum");

            else
                error = true;
        }

        else if (lookahead.equals("id"))
        {
            if (pushInStack("epsilon") && pushInStack("epsilon") && REPTVARIABLE0() && pushInStack("id") && popFromStackUntilEpsilon("var0") && FACTORDASH() && popFromStackUntilEpsilon("functionCall/DataMember"))
                writeOutDerivation("FACTOR -> REPTVARIABLE0 id FACTORDASH");
            else
                error = true;
        }

        else if (lookahead.equals("openpar"))
        {
            if (Match("openpar") && ARITHEXPR() && Match("closepar"))
                writeOutDerivation("FACTOR -> lpar ARITHEXPR rpar");
            else
                error = true;
        }

        else if (lookahead.equals("minus"))
        {
            if (SIGN() && FACTOR() && popFromStack("sign", 1))
                writeOutDerivation("FACTOR -> SIGN FACTOR");
            else
                error = true;
        }

        else if (lookahead.equals("plus"))
        {
            if (SIGN() && FACTOR() && popFromStack("sign", 1))
                writeOutDerivation("FACTOR -> SIGN FACTOR");
            else
                error = true;
        }

        else if (lookahead.equals("not"))
        {
            if (Match("not") && FACTOR() && popFromStack("not", 1))
                writeOutDerivation("FACTOR -> not FACTOR");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean REPTVARIABLE0()
    {
//        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{}));
//        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"id"}));
//        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("id"))
        {
            if (!error)
                Match("id");
            if (lookahead.equals("dot"))
            {
                if (IDNEST() && REPTVARIABLE0())
                {
                    writeOutDerivation("REPTVARIABLE0 -> IDNEST REPTVARIABLE0");
                }
                else
                    error = true;
            }
            else
                writeOutDerivation("REPTVARIABLE0 -> epsilon");
        }

        else
            error = true;

        return !error;
    }

    private boolean FACTORDASH()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"openpar", "opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"mult","div","and","closesqbr","eq","neq","lt","gt","leq","geq","plus","minus","or","comma","closepar","semi"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("openpar"))
        {
            if (Match("openpar") && pushInStack("epsilon") && APARAMS() && popFromStackUntilEpsilon("aParams") && Match("closepar"))
                writeOutDerivation("FACTORDASH -> lpar APARAMS rpar");
            else
                error = true;
        }
        else if (lookahead.equals("opensqbr"))
        {
            if (pushInStack("epsilon") && REPTVARIABLE2() && popFromStackUntilEpsilon("indiceList"))
                writeOutDerivation("FACTORDASH -> REPTVARIABLE2");
            else
                error = true;
        }
        else if (lookahead.equals("mult") || lookahead.equals("div") || lookahead.equals("and") || lookahead.equals("closesqbr") || lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq") || lookahead.equals("plus") || lookahead.equals("minus") || lookahead.equals("or") || lookahead.equals("comma") || lookahead.equals("closepar") || lookahead.equals("semi"))
            writeOutDerivation("FACTORDASH -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean REPTVARIABLE2()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"openpar", "opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"mult","div","and","closesqbr","eq","neq","lt","gt","leq","geq","plus","minus","or","comma","closepar","semi", "assign"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("opensqbr"))
        {
            if (INDICE() && REPTVARIABLE2())
                writeOutDerivation("REPTVARIABLE2 -> INDICE REPTVARIABLE2");
            else
                error = true;
        }
        else if (lookahead.equals("mult") || lookahead.equals("div") || lookahead.equals("and") || lookahead.equals("closesqbr") || lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq") || lookahead.equals("assign") || lookahead.equals("plus") || lookahead.equals("minus") || lookahead.equals("or") || lookahead.equals("comma") || lookahead.equals("closepar") || lookahead.equals("semi"))
            writeOutDerivation("REPTVARIABLE2 -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean FUNCBODY()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"opencubr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("opencubr"))
        {
            if (Match("opencubr") && pushInStack("epsilon") && REPTFUNCBODY1() && popFromStackUntilEpsilon("statementBlock") && Match("closecubr"))
                writeOutDerivation("FUNCBODY -> lcurbr REPTFUNCBODY1 rcurbr ");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean REPTFUNCBODY1()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"let","if","while","read","write","return","id"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closecubr"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("let") || lookahead.equals("if") || lookahead.equals("while") || lookahead.equals("read") || lookahead.equals("write") || lookahead.equals("return") || lookahead.equals("id"))
        {
            if (VARDECLORSTAT() && REPTFUNCBODY1())
                writeOutDerivation("REPTFUNCBODY1 -> VARDECLORSTAT REPTFUNCBODY1");
            else
                error = true;
        }
        else if (lookahead.equals("closecubr"))
            writeOutDerivation("REPTFUNCBODY1 -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean FUNCDECL()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("func"))
        {
            if (FUNCHEAD() && Match("semi"))
                writeOutDerivation("FUNCDECL -> FUNCHEAD semi");

            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean FUNCDEF()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("func"))
        {
            if (FUNCHEAD() && FUNCBODY())
                writeOutDerivation("FUNCDEF -> FUNCHEAD FUNCBODY");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean FUNCHEAD()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("func"))
        {
            if (Match("func") && Match("id") && pushInStack("id") && Match("openpar") && pushInStack("epsilon") && FPARAMS() && popFromStackUntilEpsilon("fparamList") && Match("closepar") && Match("arrow") && RETURNTYPE())
                writeOutDerivation("FUNCHEAD -> func id lpar FPARAMS rpar arrow RETURNTYPE");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean IDNEST()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"dot"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("dot"))
        {
            if (pushInStack("id") && IDNESTDASH())
                writeOutDerivation("IDNEST-> id IDNESTDASH");

            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean IDNESTDASH()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"dot", "openpar", "opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("openpar"))
        {
            if (Match("openpar") && pushInStack("epsilon") && APARAMS() && popFromStackUntilEpsilon("aParams") && Match("closepar") && Match("dot") && popFromStack("dot", 2))
                writeOutDerivation("IDNESTDASH -> lpar APARAMS rpar dot");
            else
                error = true;
        }
        else if (lookahead.equals("opensqbr"))
        {
            if (pushInStack("epsilon") && REPTIDNEST1() && popFromStackUntilEpsilon("indiceList") && Match("dot") && popFromStack("dot", 2))
                writeOutDerivation("IDNESTDASH -> REPTIDNEST1 dot");
            else
                error = true;
        }
        else if (lookahead.equals("dot"))
        {
            if (pushInStack("epsilon") && REPTIDNEST1() && popFromStackUntilEpsilon("indiceList") && Match("dot") && popFromStack("dot", 2))
                writeOutDerivation("IDNESTDASH -> REPTIDNEST1 dot");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean REPTIDNEST1()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"dot"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("opensqbr"))
        {
            if (INDICE() && REPTIDNEST1())
                writeOutDerivation("REPTIDNEST1 -> INDICE REPTIDNEST1 ");
            else
                error = true;
        }
        else if (lookahead.equals("dot"))
            writeOutDerivation("REPTIDNEST1 -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean IMPLDEF()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"impl"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("impl"))
        {
            if (Match("impl") && Match("id") && pushInStack("id") && Match("opencubr") && pushInStack("epsilon") && REPTIMPLDEF3() && popFromStackUntilEpsilon("funcDefList") && Match("closecubr"))
                writeOutDerivation("IMPLDEF -> impl id lcurbr REPTIMPLDEF3 rcurbr");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean REPTIMPLDEF3()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closecubr"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("func"))
        {
            if (FUNCDEF() && popFromStack("funcDef",4) && REPTIMPLDEF3())
                writeOutDerivation("REPTIMPLDEF3 -> FUNCDEF REPTIMPLDEF3 ");
            else
                error = true;
        }
        else if (lookahead.equals("closecubr"))
            writeOutDerivation("REPTIMPLDEF3 -> epsilon");

        else
            error = true;

        return !error;
    }

    private boolean INDICE()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"opensqbr"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{""}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("opensqbr"))
        {
            if (Match("opensqbr") && ARITHEXPR() && Match("closesqbr"))
                writeOutDerivation("INDICE -> lsqbr ARITHEXPR rsqbr ");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean MEMBERDECL()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func", "let"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{""}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("func"))
        {
            if (FUNCDECL() && popFromStack("funcDecl", 3))
                writeOutDerivation("MEMBERDECL -> FUNCDECL");
            else
                error = true;
        }
        else if (lookahead.equals("let"))
        {
            if (VARDECL() && popFromStack ("varDecl", 3))
                writeOutDerivation("MEMBERDECL -> VARDECL");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean MULTOP()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"mult", "div", "and"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{""}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("mult"))
        {
            if (Match("mult") && pushInStack("opr"))
                writeOutDerivation("MULTOP -> mult");
            else
                error = true;
        }
        else if (lookahead.equals("div"))
        {
            if (Match("div") && pushInStack("opr"))
                writeOutDerivation("MULTOP -> div");
            else
                error = true;
        }
        else if (lookahead.equals("and"))
        {
            if (Match("and") && pushInStack("opr"))
                writeOutDerivation("MULTOP -> and");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean optstructDecl2()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"inherits"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"opencubr"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("inherits"))
        {
            if (Match("inherits") && Match("id") && pushInStack("id") && REPTOPTSTRUCTDECL22())
                writeOutDerivation("OPTSTRUCTDECL2 -> inherits id REPTOPTSTRUCTDECL22");
            else
                error = true;
        }
        else if (lookahead.equals("opencubr")) {
            writeOutDerivation("OPTSTRUCTDECL2 -> epsilon");
        }
        else
            error = true;

        return !error;
    }

    private boolean REPTOPTSTRUCTDECL22()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"comma"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"opencubr"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("comma"))
        {
            if (Match("comma") && Match("id") && pushInStack("id") && REPTOPTSTRUCTDECL22())
                writeOutDerivation("REPTOPTSTRUCTDECL22 -> comma id REPTOPTSTRUCTDECL22");
            else
                error = true;
        }
        else if (lookahead.equals("opencubr")) {
            writeOutDerivation ("REPTOPTSTRUCTDECL22 -> epsilon");
        }

        else
            error = true;

        return !error;
    }

    private boolean PROG()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func", "impl", "struct"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("func") || lookahead.equals("impl") || lookahead.equals("struct"))
        {
            if (pushInStack("epsilon") && REPTPROG0() && popFromStackUntilEpsilon("STRUCTORIMPLORFUNCLIST"))
                writeOutDerivation("PROG -> REPTPROG0");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean REPTPROG0()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func", "impl", "struct"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"$"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("func") || lookahead.equals("impl") || lookahead.equals("struct"))
        {
            if (STRUCTORIMPLORFUNC() && REPTPROG0())
                writeOutDerivation("REPTPROG0 -> STRUCTORIMPLORFUNC REPTPROG0");
            else
                error = true;
        }
        else if (lookahead.equals("$")) {
            writeOutDerivation ("REPTPROG0 -> epsilon");
        }

        else
            error = true;

        return !error;
    }

    private boolean RELEXPR()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"intnum", "floatnum", "openpar", "not", "id", "plus", "minus"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("intnum") || lookahead.equals("floatnum") || lookahead.equals("openpar") || lookahead.equals("not") || lookahead.equals("id") || lookahead.equals("plus") || lookahead.equals("minus"))
        {
            if (ARITHEXPR() && RELOP() && ARITHEXPR() && popFromStack("relExpr", 3))
                writeOutDerivation("RELEXPR -> ARITHEXPR RELOP ARITHEXPR");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean RELOP()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"eq", "neq", "lt", "gt", "leq", "geq"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("eq"))
        {
            if (Match("eq") && pushInStack("relOp"))
                writeOutDerivation("RELOP -> eq");

            else
                error = true;
        }
        else if (lookahead.equals("neq"))
        {
            if (Match("neq") && pushInStack("relOp"))
                writeOutDerivation("RELOP -> neq");

            else
                error = true;
        }
        else if (lookahead.equals("lt"))
        {
            if (Match("lt") && pushInStack("relOp"))
                writeOutDerivation("RELOP -> lt");

            else
                error = true;
        }
        else if (lookahead.equals("gt"))
        {
            if (Match("gt") && pushInStack("relOp"))
                writeOutDerivation("RELOP -> gt");

            else
                error = true;
        }
        else if (lookahead.equals("leq"))
        {
            if (Match("leq") && pushInStack("relOp"))
                writeOutDerivation("RELOP -> leq");

            else
                error = true;
        }
        else if (lookahead.equals("geq"))
        {
            if (Match("geq") && pushInStack("relOp"))
                writeOutDerivation("RELOP -> geq");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean RETURNTYPE()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"void", "integer", "float", "id"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("void"))
        {
            if (Match("void") && pushInStack("type"))
                writeOutDerivation("RETURNTYPE -> void");
            else
                error = true;
        }
        else if (lookahead.equals("integer") || lookahead.equals("float") || lookahead.equals("id"))
        {
            if (TYPE())
                writeOutDerivation("RETURNTYPE -> TYPE");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean SIGN()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"plus", "minus"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("plus"))
        {
            if (Match("plus"))
                writeOutDerivation("SIGN -> plus");
            else
                error = true;
        }
        else if (lookahead.equals("minus"))
        {
            if (Match("minus"))
                writeOutDerivation("SIGN -> minus");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean STATBLOCK()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"id", "opencubr", "return", "while", "read", "write", "if"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"else", "semi"}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("id"))
        {
            if (STATEMENT())
                writeOutDerivation("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("opencubr"))
        {
            if (Match("opencubr") && REPTSTATBLOCK1() && Match("closecubr"))
                writeOutDerivation("STATBLOCK -> lcurbr REPTSTATBLOCK1 rcurbr");
            else
                error = true;
        }
        else if (lookahead.equals("return"))
        {
            if (STATEMENT())
                writeOutDerivation("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("while"))
        {
            if (STATEMENT())
                writeOutDerivation("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("read"))
        {
            if (STATEMENT())
                writeOutDerivation("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("write"))
        {
            if (STATEMENT())
                writeOutDerivation("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("if"))
        {
            if (STATEMENT())
                writeOutDerivation("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("else") || lookahead.equals("semi"))
            writeOutDerivation("STATBLOCK -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean STATEMENT()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"id", "return","write", "read", "while", "if"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("id"))
        {
            if (pushInStack("epsilon") && REPTVARIABLE0() && pushInStack ("id") && popFromStackUntilEpsilon("var0") && STATEMENTDASH())
                writeOutDerivation("STATEMENT -> REPTVARIABLE0 id STATEMENTDASH");
            else
                error = true;
        }

        else if (lookahead.equals("return"))
        {
            if (Match("return") && Match("openpar") && EXPR() && Match("closepar") && Match("semi") && popFromStack("returnStatement", 1))
                writeOutDerivation("STATEMENT -> return lpar EXPR rpar semi");
            else
                error = true;
        }

        else if (lookahead.equals("write"))
        {
            if (Match("write") && Match("openpar") && EXPR() && Match("closepar") && Match("semi") && popFromStack("writeStatement", 1))
                writeOutDerivation("STATEMENT -> write lpar EXPR rpar semi");
            else
                error = true;
        }

        else if (lookahead.equals("read"))
        {
            if (Match("read") && Match("openpar") && VARIABLE() && Match("closepar") && Match("semi") && popFromStack("readStatement", 1))
                writeOutDerivation("STATEMENT -> read lpar VARIABLE rpar semi");
            else
                error = true;
        }

        else if (lookahead.equals("while"))
        {
            if (Match("while") && Match("openpar") && RELEXPR() && Match("closepar") && pushInStack("epsilon") && STATBLOCK() && popFromStackUntilEpsilon("statementBlock") && Match("semi") && popFromStack("whileStatement", 2))
                writeOutDerivation("STATEMENT -> while lpar RELEXPR rpar STATBLOCK semi");
            else
                error = true;
        }

        else if (lookahead.equals("if"))
        {
            if (Match("if") && Match("openpar") && RELEXPR() && Match("closepar") && Match("then") && pushInStack("epsilon") && STATBLOCK() && popFromStackUntilEpsilon("statementBlock") && Match("else") && pushInStack("epsilon") && STATBLOCK() && popFromStackUntilEpsilon("statementBlock") && Match("semi") && popFromStack("ifStatement", 3))
                writeOutDerivation("STATEMENT -> if lpar RELEXPR rpar then STATBLOCK else STATBLOCK semi");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean STATEMENTDASH()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"openpar", "opensqbr","assign"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("openpar"))
        {
            if (Match("openpar") && pushInStack("epsilon") && APARAMS() && popFromStackUntilEpsilon ("aParams") && Match("closepar") && Match("semi") && popFromStack("fcallStatement", 2))
                writeOutDerivation("STATEMENTDASH -> lpar APARAMS rpar semi");
            else
                error = true;
        }
        else if (lookahead.equals("opensqbr"))
        {
            if ( pushInStack("epsilon") && REPTVARIABLE2() && popFromStackUntilEpsilon("indiceList") && popFromStack("var", 2) && ASSIGNOP() && EXPR() && Match("semi") && popFromStack("assignStatement", 2))
                writeOutDerivation("STATEMENTDASH -> REPTVARIABLE2 ASSIGNOP EXPR semi");
            else
                error = true;
        }
        else if (lookahead.equals("assign"))
        {
            if ( pushInStack("epsilon") && REPTVARIABLE2() && popFromStackUntilEpsilon("indiceList") && popFromStack("var", 2) && ASSIGNOP() && EXPR() && Match("semi") && popFromStack("assignStatement", 2))
                writeOutDerivation("STATEMENTDASH -> REPTVARIABLE2 ASSIGNOP EXPR semi");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean STRUCTDECL()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"struct"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("struct"))
        {
            if (Match("struct") && Match("id") && pushInStack("id") && pushInStack("epsilon") && optstructDecl2() && popFromStackUntilEpsilon("inheritList") && Match("opencubr") && pushInStack("epsilon") && REPTSTRUCTDECL4() && popFromStackUntilEpsilon("memberList") && Match("closecubr") && Match("semi"))
                writeOutDerivation("STRUCTDECL -> struct id OPTSTRUCTDECL2 lcurbr REPTSTRUCTDECL4 rcurbr semi");

            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean STRUCTORIMPLORFUNC()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"struct", "impl", "func"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("func"))
        {
            if (FUNCDEF() && popFromStack("funcDef", 4)) {
                writeOutDerivation ("STRUCTORIMPLORFUNC -> FUNCDEF");
            }
            else
                error = true;
        }
        else if (lookahead.equals("impl"))
        {
            if (IMPLDEF() && popFromStack("implDecl", 2)) {
                writeOutDerivation ("STRUCTORIMPLORFUNC -> IMPLDEF");
            }
            else
                error = true;
        }
        else if (lookahead.equals("struct"))
        {
            if (STRUCTDECL() && popFromStack("structDecl", 3)) {
                writeOutDerivation ("STRUCTORIMPLORFUNC -> STRUCTDECL");
            }
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean TERM()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"intnum", "floatnum", "openpar", "not", "id", "plus", "minus"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("intnum") || lookahead.equals("floatnum") || lookahead.equals("openpar") || lookahead.equals("not") || lookahead.equals("id") || lookahead.equals("plus") || lookahead.equals("minus"))
        {
            if (FACTOR() && RIGHTRECTERM())
                writeOutDerivation("TERM -> FACTOR RIGHTRECTERM");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean TYPE()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"integer", "float", "id"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("integer"))
        {
            if (Match("integer") && pushInStack("type"))
                writeOutDerivation("TYPE -> integer");
            else
                error = true;
        }
        else if (lookahead.equals("float"))
        {
            if (Match("float") && pushInStack("type"))
                writeOutDerivation("TYPE -> float");
            else
                error = true;
        }
        else if (lookahead.equals("id"))
        {
            if (Match("id") && pushInStack("type"))
                writeOutDerivation("TYPE -> id");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean VARDECL()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"let"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("let"))
        {
            if (Match("let") && Match("id") && pushInStack("id") && Match("colon") && TYPE() && pushInStack("epsilon") && REPTVARDECL4() && popFromStackUntilEpsilon("dimList") && Match("semi"))
                writeOutDerivation("VARDECL -> let id colon TYPE REPTVARDECL4 semi");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean VARDECLORSTAT()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"let", "id", "return", "write", "read", "while", "if"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("id"))
        {
            if (STATEMENT())
                writeOutDerivation("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("let"))
        {
            if (VARDECL() && popFromStack ("varDecl", 3))
                writeOutDerivation("VARDECLORSTAT -> VARDECL");
            else
                error = true;
        }
        else if (lookahead.equals("return"))
        {
            if (STATEMENT())
                writeOutDerivation("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("write"))
        {
            if (STATEMENT())
                writeOutDerivation("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("read"))
        {
            if (STATEMENT())
                writeOutDerivation("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("while"))
        {
            if (STATEMENT())
                writeOutDerivation("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("if"))
        {
            if (STATEMENT())
                writeOutDerivation("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean VARIABLE()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"id"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("id"))
        {
            if (pushInStack("epsilon") && pushInStack("epsilon") && REPTVARIABLE0() && pushInStack("id") && popFromStackUntilEpsilon("var0") && pushInStack("epsilon") && REPTVARIABLE2() && popFromStackUntilEpsilon("indiceList") && popFromStackUntilEpsilon("variable"))
                writeOutDerivation("VARIABLE -> REPTVARIABLE0 id REPTVARIABLE2");
            else
                error = true;
        }

        else
            error = true;

        return !error;
    }

    private boolean VISIBILITY()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"public", "private"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("public"))
        {
            if (Match("public"))
                writeOutDerivation("VISIBILITY -> public");
            else
                error = true;
        }
        else if (lookahead.equals("private"))
        {
            if (Match("private"))
                writeOutDerivation("VISIBILITY -> private");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        FileWriter outsyntaxerrors = null;
        FileWriter outderivation = null;
        FileWriter outast = null;
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
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                        Lexer lex = new Lexer();
                        SyntaxAnalysis sa = new SyntaxAnalysis(lex);
                        System.out.println (sa.Parse(outderivation, outsyntaxerrors));
                        System.out.println (sa.st.size());
                        try {
                            printAST(sa.st.peek(), 0, outast);
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                        System.out.println ("=====================\n\n");
                    }
                }
                try {
                    outderivation.close();
                    outsyntaxerrors.close();
                    outast.close ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }
        catch (FileNotFoundException err) {
            System.out.println(err.getMessage());
        }
    }
}
