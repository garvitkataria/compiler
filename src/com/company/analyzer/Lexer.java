package com.company.analyzer;

import java.io.*;
import java.util.*;

public class Lexer {
    public static int line = 1;
    private char peek = ' ';
    private HashSet<String> reserveWords = new HashSet<>();

    public Lexer() {
        reserveWords.add("if");
        reserveWords.add("public");
        reserveWords.add("read");
        reserveWords.add("then");
        reserveWords.add("private");
        reserveWords.add("write");
        reserveWords.add("else");
        reserveWords.add("func");
        reserveWords.add("return");
        reserveWords.add("integer");
        reserveWords.add("var");
        reserveWords.add("self");
        reserveWords.add("float");
        reserveWords.add("struct");
        reserveWords.add("inherits");
        reserveWords.add("void");
        reserveWords.add("while");
        reserveWords.add("let");
        reserveWords.add("impl");
    }
    void readch() throws IOException {
        this.peek = (char) System.in.read();
    }

    boolean readch(char composite) throws IOException {
        readch();
        if (this.peek != composite) return false;
        this.peek = ' ';
        return true;
    }

    public Token scan() throws IOException {
        for ( ;; readch()) {
            if (peek == ' ' || peek == '\t') continue;
            else if (peek == '\n') line = line + 1;
            else if((int)peek == 65535) return null;
            else break;
        }

        switch(peek) {
            case '=':
                if (readch('=')) {
                    readch();
                    return new Token ("eq", "==", line);
                } else {
                    readch();
                    return new Token ("assign", "=", line);
                }
            case '<':
                readch();
                if (this.peek == '=') {
                    readch ();
                    return new Token ("leq", "<=", line);
                }
                else if(this.peek == '>') {
                    readch ();
                    return new Token ("noteq", "<>", line);
                }
                else return new Token("lt", "<", line);
            case '>':
                if (readch('=')) {
                    readch();
                    return new Token ("geq", ">=", line);
                } else {
                    readch();
                    return new Token ("gt", ">", line);
                }
            case ':':
                if (readch(':')) {
                    readch();
                    return new Token ("coloncolon", "::", line);
                } else {
                    readch();
                    return new Token ("colon", ":", line);
                }
            case '+':
                readch(); return new Token("plus", "+", line);
            case '-':
                if (readch('>')) {
                    readch();
                    return new Token ("arrow", "->", line);
                } else {
                    readch();
                    return new Token ("minus", "-", line);
                }
            case '*':
                readch(); return new Token("mult", "*", line);
            case '/':
                readch(); return new Token("div", "/", line);
            case '(':
                readch(); return new Token("openpar", "(", line);
            case ')':
                readch(); return new Token("closepar", ")", line);
            case '{':
                readch(); return new Token("opencubr", "{", line);
            case '}':
                readch(); return new Token("closecubr", "}", line);
            case '[':
                readch(); return new Token("opensqbr", "[", line);
            case ']':
                readch(); return new Token("closesqbr", "]", line);
            case '.':
                readch(); return new Token("dot", ".", line);
            case ',':
                readch(); return new Token("comma", ",", line);
            case ';':
                readch(); return new Token("semi", ";", line);
            case '|':
                readch(); return new Token("or", "|", line);
            case '&':
                readch(); return new Token("and", "&", line);
            case '!':
                readch(); return new Token("not", "!", line);
        }

        if (Character.isDigit(peek)) {
            StringBuffer num = new StringBuffer();
            if(this.peek == '0') {
                readch ();
                return new Token("intnum", "0", line);
            }
            do {
                num.append(peek);
                readch();
            } while (Character.isDigit(peek));

            if (this.peek != '.') {
                return new Token ("intnum", num.toString(), line);
            }
            else {
                do {
                    num.append(peek);
                    readch();
                } while (Character.isDigit(peek));
                if(peek == 'e' && (num.charAt(num.length ()-1) != '0' || (num.charAt(num.length ()-1) == '0' && num.charAt(num.length ()-2) == '.'))) {
                    num.append(peek);
                    readch();
                    if(peek == '+' || peek == '-' || Character.isDigit(peek)) {
                        num.append(peek);
                        if(this.peek == '0') {
                            readch ();
                            return new Token("floatnum", num.toString(), line);
                        }
                        readch();
                        if (Character.isDigit(peek)) {
                            if(this.peek == '0' && (num.charAt(num.length ()-1) == '+' || num.charAt(num.length ()-1) == '-') ) {
                                num.append(peek);
                                readch ();
                                return new Token("floatnum", num.toString(), line);
                            }
                            do {
                                num.append(peek);
                                readch();
                            } while (Character.isDigit(peek));
                            return new Token("floatnum", num.toString(), line);
                        }
                    }
                }
                if(num.charAt(num.length ()-1) != '0') {
                    return new Token("floatnum", num.toString(), line);
                }
                else if(num.length()>=2 && num.charAt(num.length ()-1) == '0' && num.charAt(num.length ()-2) == '.') {
                    return new Token("floatnum", num.toString(), line);
                }
            }
            return new Token("invalidnum", num.toString (), line);
        }

        if (Character.isLetter(peek) ) {
            StringBuffer b = new StringBuffer();
            do {
                b.append(peek);
                readch();
            } while(Character.isLetterOrDigit(peek) || peek=='_' || Character.isDigit(peek));
            String s = b.toString();
            if (reserveWords.contains(s)) {
                return new Token (s, s, line);
            }
            return new Token ("id", s, line);
        }

        Token t = new Token(peek+"", "invalidchar", line);
        peek = ' ';
        return t;
    }
}
