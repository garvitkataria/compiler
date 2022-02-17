package com.company.analyzer;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;

public class SyntaxAnalysis
{
    private String lookahead = null;
    private int currentTokenIndex = 0;
    private boolean error = false;
    HashSet<String> tokenTypesToIgnore = new HashSet<>();
    Lexer lex;
    Token token = null;
    SyntaxAnalysis(Lexer lexer) {
        this.lex = lexer;
        tokenTypesToIgnore.add("inlinecmt");
        tokenTypesToIgnore.add("blockcmt");

    }

    public boolean Parse()
    {
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

    private boolean Match(String v)
    {
        if (lookahead.equals(v))
        {
            System.out.print(lookahead+" ");
            try {
                GetNextToken();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            return true;
        }
        else
        {
            System.out.println ("Syntax error at line " + token.line + ", expected token: "+ v);
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
            if (PROG())
                System.out.println("START -> PROG");
            else
                error = true;
        }
        else
            error = true;
        return !error;
    }

    private boolean skipErrors(HashSet<String> firstSet, HashSet<String> followSet, boolean epsilon) {
        if(firstSet.contains(lookahead)) return true;
        else if(epsilon && followSet.contains(lookahead)) return true;
        else {
            System.out.println ("Syntax error at: "+ token.line +" "+token.value);
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
        token = lex.scan();
        if(token == null) return;
        lookahead = token.type;

        while(tokenTypesToIgnore.contains(lookahead)) {
            token = lex.scan();
            if(token == null) return;
            lookahead = token.type;
        }
//        System.out.println("\nToken: "+token);
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
                System.out.println();
            else
                error = true;
        }
        else if (followSet.contains(lookahead))
            System.out.println();

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
                System.out.println("REPTAPARAMS1-> APARAMSTAIL REPTAPARAMS1");
            else
                error = true;
        }
        else if (followSet.contains(lookahead))
            System.out.println("REPTAPARAMS1 -> epsilon");

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
                System.out.println("APARAMSTAIL -> comma EXPR");
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
            if (Match("plus"))
                System.out.println("ADDOP -> plus");
            else
                error = true;
        }
        else if (lookahead.equals("minus"))
        {
            if (Match("minus"))
                System.out.println("ADDOP -> minus");
            else
                error = true;
        }
        else if (lookahead.equals("or"))
        {
            if (Match("or"))
                System.out.println("ADDOP -> or");
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
                System.out.println("ARITHEXPR -> TERM RIGHTRECARITHEXPR");
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
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{"closesqbr", "eq", "neq", "lt", "gt", "leq", "geq", "comma", "closepar", ""}));
        if(!skipErrors(firstSet, followSet, true)) return false;
        boolean error = false;
        if (lookahead.equals("plus") || lookahead.equals("minus") || lookahead.equals("or"))
        {
            if (ADDOP() && TERM() && RIGHTRECARITHEXPR())
                System.out.println("RIGHTRECARITHEXPR -> ADDOP TERM RIGHTRECARITHEXPR");
            else
                error = true;
        }
        else if (lookahead.equals("closesqbr") || lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq") || lookahead.equals("comma") || lookahead.equals("closepar") || lookahead.equals("semi"))
            System.out.println("RIGHTRECARITHEXPR -> epsilon");
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
            if (MULTOP() && FACTOR() && RIGHTRECTERM())
                System.out.println("RIGHTRECTERM -> MULTOP FACTOR RIGHTRECTERM");
            else
                error = true;
        }
        else if (lookahead.equals("closesqbr") || lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq") || lookahead.equals("plus") || lookahead.equals("minus") || lookahead.equals("or") || lookahead.equals("comma") || lookahead.equals("closepar") || lookahead.equals("semi"))
            System.out.println("RIGHTRECTERM -> epsilon");
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
                System.out.println("REPTSTATBLOCK1 -> STATEMENT REPTSTATBLOCK1 ");
            else
                error = true;
        }
        else if (lookahead.equals("closecubr"))
            System.out.println("REPTSTATBLOCK1 -> epsilon");
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
            if (VISIBILITY() && MEMBERDECL() && REPTSTRUCTDECL4())
                System.out.println("REPTSTRUCTDECL4 -> VISIBILITY MEMBERDECL REPTSTRUCTDECL4");
            else
                error = true;
        }
        else if (lookahead.equals("closecubr"))
            System.out.println("REPTSTRUCTDECL4 -> epsilon");
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
                System.out.println("REPTVARDECL4 -> ARRAYSIZE REPTVARDECL4");
            else
                error = true;
        }
        else if (lookahead.equals("semi"))
            System.out.println("REPTVARDECL4 -> epsilon");
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
                System.out.println("ARRAYSIZE -> lsqbr ARRAYDASH");
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
            if (Match("intnum") && Match("closesqbr"))
                System.out.println("ARRAYDASH -> intnum rsqbr");
            else
                error = true;
        }
        else if (lookahead.equals("closesqbr"))
        {
            if (Match("closesqbr"))
                System.out.println("ARRAYDASH -> rsqbr ");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }

    private boolean ASSIGNOP()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"equal"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        boolean error = false;
        if (lookahead.equals("assign"))
        {
            if (Match("assign"))
                System.out.println("ASSIGNOP -> equal");
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
                System.out.println("EXPR -> ARITHEXPR EXPRDASH");
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
            if (RELOP() && ARITHEXPR())
                System.out.println("EXPRDASH -> RELOP ARITHEXPR");
            else
                error = true;
        }
        else if (lookahead.equals("semi") || lookahead.equals("comma") || lookahead.equals("closepar"))
            System.out.println("EXPRDASH ->  epsilon");

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
            if (Match("id") && Match("colon") && TYPE() && REPTFPARAMS3() && REPTFPARAMS4())
                System.out.println("FPARAMS -> id colon TYPE REPTFPARAMS3 REPTFPARAMS4");
            else
                error = true;
        }
        else if (lookahead.equals("closepar"))
            System.out.println("FPARAMS ->  epsilon");

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
            if (FPARAMSTAIL() && REPTFPARAMS4())
                System.out.println("REPTFPARAMS4 -> FPARAMSTAIL REPTFPARAMS4");
            else
                error = true;
        }
        else if (lookahead.equals("closepar"))
            System.out.println("REPTFPARAMS4 -> epsilon");

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
                System.out.println("REPTFPARAMS3 -> ARRAYSIZE REPTFPARAMS3");
            else
                error = true;
        }
        else if (lookahead.equals("closepar") || lookahead.equals("comma"))
            System.out.println("REPTFPARAMS3 -> epsilon");

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
            if (Match("comma") && Match("id") && Match("colon") && TYPE() && REPTFPARAMSTAIL4())
                System.out.println("FPARAMSTAIL -> comma id colon TYPE REPTFPARAMSTAIL4");
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
                System.out.println("REPTFPARAMSTAIL4 -> ARRAYSIZE REPTFPARAMSTAIL4");
            else
                error = true;
        }
        else if (lookahead.equals("comma") || lookahead.equals("closepar"))
            System.out.println("REPTFPARAMSTAIL4 -> epsilon");
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
            if (Match("intnum"))
                System.out.println("FACTOR -> intnum");
            else
                error = true;
        }

        else if (lookahead.equals("floatnum"))
        {
            if (Match("floatnum"))
                System.out.println("FACTOR -> floatnum");

            else
                error = true;
        }

        else if (lookahead.equals("id"))
        {
            if (REPTVARIABLE0() && Match("id") && FACTORDASH())
                System.out.println("FACTOR -> REPTVARIABLE0 id FACTORDASH");
            else
                error = true;
        }

        else if (lookahead.equals("openpar"))
        {
            if (Match("openpar") && ARITHEXPR() && Match("closepar"))
                System.out.println("FACTOR -> lpar ARITHEXPR rpar");
            else
                error = true;
        }

        else if (lookahead.equals("minus"))
        {
            if (SIGN() && FACTOR())
                System.out.println("FACTOR -> SIGN FACTOR");
            else
                error = true;
        }

        else if (lookahead.equals("plus"))
        {
            if (SIGN() && FACTOR())
                System.out.println("FACTOR -> SIGN FACTOR");
            else
                error = true;
        }

        else if (lookahead.equals("not"))
        {
            if (Match("not") && FACTOR())
                System.out.println("FACTOR -> not FACTOR");
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
                    System.out.println("REPTVARIABLE0 -> IDNEST REPTVARIABLE0");
                }
                else
                    error = true;
            }
            else
                System.out.println("REPTVARIABLE0 -> epsilon");
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
            if (Match("openpar") && APARAMS() && Match("closepar"))
                System.out.println("FACTORDASH -> lpar APARAMS rpar");
            else
                error = true;
        }
        else if (lookahead.equals("opensqbr"))
        {
            if (REPTVARIABLE2())
                System.out.println("FACTORDASH -> REPTVARIABLE2");
            else
                error = true;
        }
        else if (lookahead.equals("mult") || lookahead.equals("div") || lookahead.equals("and") || lookahead.equals("closesqbr") || lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq") || lookahead.equals("plus") || lookahead.equals("minus") || lookahead.equals("or") || lookahead.equals("comma") || lookahead.equals("closepar") || lookahead.equals("semi"))
            System.out.println("FACTORDASH -> epsilon");
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
                System.out.println("REPTVARIABLE2 -> INDICE REPTVARIABLE2");
            else
                error = true;
        }
        else if (lookahead.equals("mult") || lookahead.equals("div") || lookahead.equals("and") || lookahead.equals("closesqbr") || lookahead.equals("eq") || lookahead.equals("neq") || lookahead.equals("lt") || lookahead.equals("gt") || lookahead.equals("leq") || lookahead.equals("geq") || lookahead.equals("assign") || lookahead.equals("plus") || lookahead.equals("minus") || lookahead.equals("or") || lookahead.equals("comma") || lookahead.equals("closepar") || lookahead.equals("semi"))
            System.out.println("REPTVARIABLE2 -> epsilon");
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
            if (Match("opencubr") && REPTFUNCBODY1() && Match("closecubr"))
                System.out.println("FUNCBODY -> lcurbr REPTFUNCBODY1 rcurbr ");
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
                System.out.println("REPTFUNCBODY1 -> VARDECLORSTAT REPTFUNCBODY1");
            else
                error = true;
        }
        else if (lookahead.equals("closecubr"))
            System.out.println("REPTFUNCBODY1 -> epsilon");
        else
            error = true;

        return !error;
    }

    private boolean FUNCDECL()
    {
        HashSet<String> firstSet = new HashSet<> (Arrays.asList(new String[]{"func"}));
        HashSet<String> followSet = new HashSet<> (Arrays.asList(new String[]{}));
        if(!skipErrors(firstSet, followSet, false)) return false;
        System.out.print("FUNCDECL ");
        boolean error = false;
        if (lookahead.equals("func"))
        {
            if (FUNCHEAD() && Match("semi"))
                System.out.println("FUNCDECL -> FUNCHEAD semi");

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
                System.out.println("FUNCDEF -> FUNCHEAD FUNCBODY");
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
            if (Match("func") && Match("id") && Match("openpar") && FPARAMS() && Match("closepar") && Match("arrow") && RETURNTYPE())
                System.out.println("FUNCHEAD -> func id lpar FPARAMS rpar arrow RETURNTYPE");
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
            if (IDNESTDASH())
                System.out.println("IDNEST-> id IDNESTDASH");

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
            if (Match("openpar") && APARAMS() && Match("closepar") && Match("dot"))
                System.out.println("IDNESTDASH -> lpar APARAMS rpar dot");
            else
                error = true;
        }
        else if (lookahead.equals("opensqbr"))
        {
            if (REPTIDNEST1() && Match("dot"))
                System.out.println("IDNESTDASH -> REPTIDNEST1 dot");
            else
                error = true;
        }
        else if (lookahead.equals("dot"))
        {
            if (REPTIDNEST1() && Match("dot"))
                System.out.println("IDNESTDASH -> REPTIDNEST1 dot");
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
                System.out.println("REPTIDNEST1 -> INDICE REPTIDNEST1 ");
            else
                error = true;
        }
        else if (lookahead.equals("dot"))
            System.out.println("REPTIDNEST1 -> epsilon");
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
            if (Match("impl") && Match("id") && Match("opencubr") && REPTIMPLDEF3() && Match("closecubr"))
                System.out.println("IMPLDEF -> impl id lcurbr REPTIMPLDEF3 rcurbr");
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
            if (FUNCDEF() && REPTIMPLDEF3())
                System.out.println("REPTIMPLDEF3 -> FUNCDEF REPTIMPLDEF3 ");
            else
                error = true;
        }
        else if (lookahead.equals("closecubr"))
            System.out.println("REPTIMPLDEF3 -> epsilon");

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
                System.out.println("INDICE -> lsqbr ARITHEXPR rsqbr ");
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
            if (FUNCDECL())
                System.out.println("MEMBERDECL -> FUNCDECL");
            else
                error = true;
        }
        else if (lookahead.equals("let"))
        {
            if (VARDECL())
                System.out.println("MEMBERDECL -> VARDECL");
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
            if (Match("mult"))
                System.out.println("MULTOP -> mult");
            else
                error = true;
        }
        else if (lookahead.equals("div"))
        {
            if (Match("div"))
                System.out.println("MULTOP -> div");
            else
                error = true;
        }
        else if (lookahead.equals("and"))
        {
            if (Match("and"))
                System.out.println("MULTOP -> and");
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
            if (Match("inherits") && Match("id") && REPTOPTSTRUCTDECL22())
                System.out.println("OPTSTRUCTDECL2 -> inherits id REPTOPTSTRUCTDECL22");
            else
                error = true;
        }
        else if (lookahead.equals("opencubr"))
            System.out.println("OPTSTRUCTDECL2 -> epsilon");

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
            if (Match("comma") && Match("id") && REPTOPTSTRUCTDECL22())
                System.out.println("REPTOPTSTRUCTDECL22 -> comma id REPTOPTSTRUCTDECL22");
            else
                error = true;
        }
        else if (lookahead.equals("opencubr"))
            System.out.println("REPTOPTSTRUCTDECL22 -> epsilon");

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
            if (REPTPROG0())
                System.out.println("PROG -> REPTPROG0");
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
                System.out.println("REPTPROG0 -> STRUCTORIMPLORFUNC REPTPROG0");
            else
                error = true;
        }
        else if (lookahead.equals("$"))
            System.out.println("REPTPROG0 -> epsilon");

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
            if (ARITHEXPR() && RELOP() && ARITHEXPR())
                System.out.println("RELEXPR -> ARITHEXPR RELOP ARITHEXPR");
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
        System.out.print("RELOP ");
        boolean error = false;
        if (lookahead.equals("eq"))
        {
            if (Match("eq"))
                System.out.println("RELOP -> eq");

            else
                error = true;
        }
        else if (lookahead.equals("neq"))
        {
            if (Match("neq"))
                System.out.println("RELOP -> neq");

            else
                error = true;
        }
        else if (lookahead.equals("lt"))
        {
            if (Match("lt"))
                System.out.println("RELOP -> lt");

            else
                error = true;
        }
        else if (lookahead.equals("gt"))
        {
            if (Match("gt"))
                System.out.println("RELOP -> gt");

            else
                error = true;
        }
        else if (lookahead.equals("leq"))
        {
            if (Match("leq"))
                System.out.println("RELOP -> leq");

            else
                error = true;
        }
        else if (lookahead.equals("geq"))
        {
            if (Match("geq"))
                System.out.println("RELOP -> geq");
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
            if (Match("void"))
                System.out.println("RETURNTYPE -> void");
            else
                error = true;
        }
        else if (lookahead.equals("integer") || lookahead.equals("float") || lookahead.equals("id"))
        {
            if (TYPE())
                System.out.println("RETURNTYPE -> TYPE");
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
                System.out.println("SIGN -> plus");
            else
                error = true;
        }
        else if (lookahead.equals("minus"))
        {
            if (Match("minus"))
                System.out.println("SIGN -> minus");
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
                System.out.println("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("opencubr"))
        {
            if (Match("opencubr") && REPTSTATBLOCK1() && Match("closecubr"))
                System.out.println("STATBLOCK -> lcurbr REPTSTATBLOCK1 rcurbr");
            else
                error = true;
        }
        else if (lookahead.equals("return"))
        {
            if (STATEMENT())
                System.out.println("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("while"))
        {
            if (STATEMENT())
                System.out.println("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("read"))
        {
            if (STATEMENT())
                System.out.println("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("write"))
        {
            if (STATEMENT())
                System.out.println("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("if"))
        {
            if (STATEMENT())
                System.out.println("STATBLOCK -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("else") || lookahead.equals("semi"))
            System.out.println("STATBLOCK -> epsilon");
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
            if (REPTVARIABLE0() && STATEMENTDASH())
                System.out.println("STATEMENT -> REPTVARIABLE0 id STATEMENTDASH");
            else
                error = true;
        }

        else if (lookahead.equals("return"))
        {
            if (Match("return") && Match("openpar") && EXPR() && Match("closepar") && Match("semi"))
                System.out.println("STATEMENT -> return lpar EXPR rpar semi");
            else
                error = true;
        }

        else if (lookahead.equals("write"))
        {
            if (Match("write") && Match("openpar") && EXPR() && Match("closepar") && Match("semi"))
                System.out.println("STATEMENT -> write lpar EXPR rpar semi");
            else
                error = true;
        }

        else if (lookahead.equals("read"))
        {
            if (Match("read") && Match("openpar") && VARIABLE() && Match("closepar") && Match("semi"))
                System.out.println("STATEMENT -> read lpar VARIABLE rpar semi");
            else
                error = true;
        }

        else if (lookahead.equals("while"))
        {
            if (Match("while") && Match("openpar") && RELEXPR() && Match("closepar") && STATBLOCK() && Match("semi"))
                System.out.println("STATEMENT -> while lpar RELEXPR rpar STATBLOCK semi");
            else
                error = true;
        }

        else if (lookahead.equals("if"))
        {
            if (Match("if") && Match("openpar") && RELEXPR() && Match("closepar") && Match("then") && STATBLOCK() && Match("else") && STATBLOCK() && Match("semi"))
                System.out.println("STATEMENT -> if lpar RELEXPR rpar then STATBLOCK else STATBLOCK semi");
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
            if (Match("openpar") && APARAMS() && Match("closepar") && Match("semi"))
                System.out.println("STATEMENTDASH -> lpar APARAMS rpar semi");
            else
                error = true;
        }
        else if (lookahead.equals("opensqbr"))
        {
            if (REPTVARIABLE2() && ASSIGNOP() && EXPR() && Match("semi"))
                System.out.println("STATEMENTDASH -> REPTVARIABLE2 ASSIGNOP EXPR semi");
            else
                error = true;
        }
        else if (lookahead.equals("assign"))
        {
            if (REPTVARIABLE2() && ASSIGNOP() && EXPR() && Match("semi"))
                System.out.println("STATEMENTDASH -> REPTVARIABLE2 ASSIGNOP EXPR semi");
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
            if (Match("struct") && Match("id") && optstructDecl2() && Match("opencubr") && REPTSTRUCTDECL4() && Match("closecubr") && Match("semi"))
                System.out.println("STRUCTDECL -> struct id OPTSTRUCTDECL2 lcurbr REPTSTRUCTDECL4 rcurbr semi");

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
            if (FUNCDEF())
                System.out.println();

            else
                error = true;
        }
        else if (lookahead.equals("impl"))
        {
            if (IMPLDEF())
                System.out.println();
            else
                error = true;
        }
        else if (lookahead.equals("struct"))
        {
            if (STRUCTDECL())
                System.out.println();
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
                System.out.println("TERM -> FACTOR RIGHTRECTERM");
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
            if (Match("integer"))
                System.out.println("TYPE -> integer");
            else
                error = true;
        }
        else if (lookahead.equals("float"))
        {
            if (Match("float"))
                System.out.println("TYPE -> float");
            else
                error = true;
        }
        else if (lookahead.equals("id"))
        {
            if (Match("id"))
                System.out.println("TYPE -> id");
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
            if (Match("let") && Match("id") && Match("colon") && TYPE() && REPTVARDECL4() && Match("semi"))
                System.out.println("VARDECL -> let id colon TYPE REPTVARDECL4 semi");
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
                System.out.println("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("let"))
        {
            if (VARDECL())
                System.out.println("VARDECLORSTAT -> VARDECL");
            else
                error = true;
        }
        else if (lookahead.equals("return"))
        {
            if (STATEMENT())
                System.out.println("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("write"))
        {
            if (STATEMENT())
                System.out.println("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("read"))
        {
            if (STATEMENT())
                System.out.println("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("while"))
        {
            if (STATEMENT())
                System.out.println("VARDECLORSTAT -> STATEMENT");
            else
                error = true;
        }
        else if (lookahead.equals("if"))
        {
            if (STATEMENT())
                System.out.println("VARDECLORSTAT -> STATEMENT");
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
            if (REPTVARIABLE0() && Match("id") && REPTVARIABLE2())
                System.out.println("VARIABLE -> REPTVARIABLE0 id REPTVARIABLE2");
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
                System.out.println("VISIBILITY -> public");
            else
                error = true;
        }
        else if (lookahead.equals("private"))
        {
            if (Match("private"))
                System.out.println("VISIBILITY -> private");
            else
                error = true;
        }
        else
            error = true;

        return !error;
    }
    public static void main(String[] args) throws FileNotFoundException {
        FileWriter outlexerrors = null;
        FileWriter outlextokens = null;
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

                        Lexer lex = new Lexer();
                        SyntaxAnalysis sa = new SyntaxAnalysis(lex);
                        System.out.println (sa.Parse());
                        System.out.println ("=====================\n\n");
                    }
                }
            }
        }
        catch (FileNotFoundException err) {
            System.out.println(err.getMessage());
        }
    }
}
