package com.company.CodeGeneration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Common {
    public static String fileContent = "";
    public static List<String> lstDigits = Arrays.asList (new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
    public static List<String> lstAlphabets = Arrays.asList (new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"});
    public static List<String> lstReserverdKeyWords = Arrays.asList (new String[]{"if", "public", "read", "then", "private", "write", "else", "func", "return", "integer", "var", "self", "float", "struct", "inherits", "void", "while", "let", "func", "impl"});

    public static boolean continueRead = true;
    public static String tokenContent;
    public static String[] arrString;
    public static List<String> lstIgnoreToken = Arrays.asList (new String[]{"inlinecmt", "blockcmt"});

    public static HashMap<String, Integer> dictMemSize = new HashMap<String, Integer> () {{ put("integer", 4); put("float", 8); }};
    public static HashMap<String, String> dictOprName = new HashMap<String, String>() {{  put("+", "add"); put("-", "sub"); put("*", "mul"); put("/", "div"); }};
    public static HashMap<String, String> dictCompareOpr = new HashMap<String, String>() {{ put(" and ", "and"); put(" or ", "or"); put(" not ", "not"); put(" == ", "ceq"); put(" != ", "cne"); put(" <= ", "cle"); put(" < ", "clt"); put(" >= ", "cge"); put(" > ", "cgt");  }};

}
