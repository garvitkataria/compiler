package com.company.CodeGeneration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodeGeneratorAsm {
    public static List<String> arrFileContent;
    FileWriter moon;
    public HashMap<String, Integer> VarOrParam;
    private boolean isread;
    private String functionName;

    public CodeGeneratorAsm(FileWriter moon)
    {
        arrFileContent = new ArrayList<> ();
        this.VarOrParam = new HashMap<>();
        this.moon = moon;
    }

    public void GenerateAssemblyCode() throws IOException {
        int nextContentToRead = 0;

        int funcDefCount = 0;
        for(String arrFileContentStr: arrFileContent) {
            if(arrFileContentStr.contains("start functionDef#")) {
                funcDefCount++;
            }
        }
        if (funcDefCount > 1)
        {
            moon.write("j main" + "\n");
        }
        int idx = 0;
        for (String item : arrFileContent)
        {
            nextContentToRead = ProcessingTags(item, idx, nextContentToRead, arrFileContent);
            idx++;
        }
        //moon.write ("hlt" + "\n");
        for (String key : VarOrParam.keySet ())
        {
            int size = 0;
            int memorySize = 1;
            if (key.contains(","))
            {
                size = Common.dictMemSize.get(key.split(",")[1].trim());
                memorySize = Integer.parseInt (key.split(",")[2].trim());
            }
            else
                size = VarOrParam.getOrDefault (key,0);
            if (memorySize > 1)
                moon.write (key.split (",")[0].trim() + "      res " + (size * memorySize) + "\n");
            else
                moon.write(key + "      res " + size + "\n");
        }


    }

    private int GetIfStatementAssembly(int index, List<String> rootBlockCode) throws IOException {
        HashMap<String, Integer> temp = new HashMap<String, Integer>();
        List<String> blockCode = new ArrayList<> ();
        int nextContentToRead = 0;
        int nextIndex = 0;
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            blockCode.add(rootBlockCode.get (i));
            if (rootBlockCode.get (i).equals ("end generateIfStatementCode#"))
            {
                index = i + 1;
                break;
            }
        }

        int idx = 0;
        for (String content : blockCode)
        {
            nextContentToRead = ProcessingTags(content, idx, nextContentToRead, blockCode);
            idx++;
        }
        return index;
    }

    private int GetConditionAssembly(String value, int index, List<String> rootBlockCode) throws IOException {
        String content = value.split("#")[1].trim();
        String compOpr = null;
        String[] operands = null;
        for (String key : Common.dictCompareOpr.keySet ())
        {
            if (content.contains(key))
            {
                compOpr = Common.dictCompareOpr.get (key);
                operands = content.split(key);
                break;
            }

        }
        if (operands != null && operands.length > 0 && compOpr!=null)
        {
            if (compOpr != "not")
            {
                if (Common.lstAlphabets.contains((operands[0].trim().charAt(0)+"").toLowerCase()) && Common.lstAlphabets.contains((operands[1].trim().charAt(0)+"").toLowerCase ()))
                {

                    if (rootBlockCode.get(rootBlockCode.size ()-1).equals("end generateWhileStatementCode") )
                    {
                        moon.write("gowhile1" + "\n");
                        moon.write("lw r1," + (VarOrParam.containsKey(functionName + operands[0].trim()) ? functionName + operands[0].trim() : operands[0].trim()) + "(r0)" + "\n");
                        moon.write("lw r2," + (VarOrParam.containsKey(functionName + operands[1].trim()) ? functionName + operands[1].trim() : operands[1].trim()) + "(r0)" + "\n");
                        moon.write(compOpr + " r3,r1,r2" + "\n");
                        moon.write("bz r3,endwhile1" + "\n");
                        index = WhileStatementBlock1(index, rootBlockCode);
                    }
                    if (rootBlockCode.get(rootBlockCode.size ()-1).equals ("end generateIfStatementCode#"))
                    {
                        moon.write("lw r1," + (functionName + operands[0].trim()) + "(r0)" + "\n");
                        moon.write("lw r2," + (functionName + operands[1].trim()) + "(r0)" + "\n");
                        moon.write(compOpr + " r3,r1,r2" + "\n");
                        moon.write("bz r3,block2" + "\n");
                        index = StatementBlock1(index, rootBlockCode);
                        index = StatementBlock2(index, rootBlockCode);
                    }

                }
                else
                {

                    if (rootBlockCode.get(rootBlockCode.size()-1).equals("end generateWhileStatementCode"))
                    {
                        moon.write("gowhile1" + "\n");
                        moon.write("lw r1," + (VarOrParam.containsKey(functionName + operands[0].trim()) ? functionName + operands[0].trim() : operands[0].trim()) + "(r0)" + "\n");
                        moon.write(compOpr + "i r2,r1," + operands[1].trim() + "\n");
                        moon.write("bz r2,endwhile1" + "\n");
                        index = WhileStatementBlock1(index, rootBlockCode);
                    }
                    if (rootBlockCode.get(rootBlockCode.size()-1).equals("end generateIfStatementCode#"))
                    {
                        moon.write("lw r1," + (functionName + operands[0].trim()) + "(r0)" + "\n");
                        moon.write(compOpr + "i r2,r1," + operands[1].trim() + "\n");
                        moon.write("bz r2,block2" + "\n");
                        index = StatementBlock1(index, rootBlockCode);
                        index = StatementBlock2(index, rootBlockCode);
                    }
                }
            }
            return index + 1;
        }
        return 0;
    }

    private int WhileStatementBlock1(int index, List<String> rootBlockCode) throws IOException {
        if (!VarOrParam.containsKey("buf"))
            VarOrParam.put("buf", 40);
        HashMap<String, Integer> temp = new HashMap<String, Integer>();
        List<String> blockCode = new ArrayList<> ();
        int nextContentToRead = 0;
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            if (rootBlockCode.get (i).trim().equals ("start generateStatementBlockCode"))
                break;
            else
                index = i;
        }
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            blockCode.add(rootBlockCode.get (i));
            if (rootBlockCode.get (rootBlockCode.size () - 1).equals  ("end generateStatementBlockCode"))
                break;
            else
                index = i;
        }
        int idx = 0;
        for (String lstItem : blockCode) {
            nextContentToRead = ProcessingTags(lstItem, idx++, nextContentToRead, blockCode);
        }
        moon.write("j gowhile1" + "\n");
        moon.write("endwhile1" + "\n");
        return index;
    }

    private int StatementBlock2(int index, List<String> rootBlockCode) throws IOException {
        if (!VarOrParam.containsKey("buf"))
            VarOrParam.put("buf", 40);
        HashMap<String, Integer> temp = new HashMap<>();
        List<String> blockCode = new ArrayList<> ();
        int nextContentToRead = 0;
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            if (rootBlockCode.get (i).trim().equals ("start block 2"))
                break;
            else
                index = i;
        }
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            blockCode.add(rootBlockCode.get (i));
            if (rootBlockCode.get (i).equals ("end block 2"))
                break;
            else
                index = i;
        }
        int idx = 0;
        for (String lstItem : blockCode) {
            nextContentToRead = ProcessingTags(lstItem, idx++, nextContentToRead, blockCode);
        }
        moon.write("endif1" + "\n");
        return index;
    }

    private int StatementBlock1(int index, List<String> rootBlockCode) throws IOException {
        if (!VarOrParam.containsKey("buf"))
            VarOrParam.put("buf", 40);
        HashMap<String, Integer> temp = new HashMap<>();
        List<String> blockCode = new ArrayList<> ();
        int nextContentToRead = 0;
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            if (rootBlockCode.get (i).trim().equals ("start block 1"))
                break;
            else
                index = i;
        }
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            blockCode.add(rootBlockCode.get (i));
            if (rootBlockCode.get (i) == "end block 1")
                break;
            else
                index = i;
        }
        int idx = 0;
        for (String lstItem : blockCode) {
            nextContentToRead = ProcessingTags(lstItem, idx++, nextContentToRead, blockCode);
        }
        moon.write("j endif1" + "\n");
        moon.write("block2" + "\n");
        return index;
    }

    private void GetReadStatementAssembly(String item) throws IOException {
        isread = true;
        String content = item.split("#")[1].trim();
        if (!VarOrParam.containsKey("buf"))
        {
            moon.write("getc r1" + "\n");
            if (content.split(" ")[1].contains("["))
            {
                boolean keyExist = false;
                int arrSize = 0;
                int[] indexes = GetArrayIndexes(content.split(" ")[1].trim());
                for (String key : VarOrParam.keySet ())
                {
                    if (key.split(",")[0].trim().equals(content.split(" ")[1].split("\\[")[0].trim()))
                    {
                        keyExist = true;
                        arrSize = VarOrParam.get(key);
                        break;
                    }
                }
                if (!keyExist)
                {
                    for (String key : VarOrParam.keySet ())
                    {
                        if (key.split(",")[0].trim().equals(content.split(" ")[1].split("\\[")[0].trim()))
                        {
                            keyExist = true;
                            arrSize = VarOrParam.get(key);
                            break;
                        }
                    }
                }
                int reg = WriteArrayAssemblyCode(indexes, arrSize);
                moon.write("sw " +  (functionName +  content.split(" ")[1].trim()).split("\\[")[0].trim() + "(r" + (reg - 1) + "),r1" + "\n");
            }
            else
            {
                moon.write("sw " +  (functionName + content.split(" ")[1].trim()) + "(r0),r1" + "\n");
            }
        }
    }

    private void GetWriteStatementAssembly(String item) throws IOException {
        String content = item.split("#")[1].trim();
        if (!VarOrParam.containsKey("buf")) {
            if (content.split (" ")[1].contains ("[")) {
                boolean keyExist = false;
                int arrSize = 0;
                int[] indexes = GetArrayIndexes (content.split (" ")[1].trim ());
                for (String key : VarOrParam.keySet ()) {
                    if (key.split (",")[0].trim ().equals (content.split (" ")[1].split ("\\[")[0].trim ()) ) {
                        keyExist = true;
                        arrSize = VarOrParam.get (key);
                        break;
                    }
                }
                if (!keyExist) {
                    for (String key : VarOrParam.keySet ())
                    {
                        if (key.split (",")[0].trim ().equals (content.split (" ")[1].split ("\\[")[0].trim ()) ) {
                            keyExist = true;
                            arrSize = VarOrParam.get(key);
                            break;
                        }
                    }
                }
                int reg = WriteArrayAssemblyCode (indexes, arrSize);
                moon.write("lw r1," + (functionName +  content.split (" ")[1].trim ()).split ("\\[")[0].trim () + "(r" + (reg - 1) + ")" + "\n");
            } else {
                moon.write("lw r1," + (functionName +  content.split (" ")[1].trim ()) + "(r0)" + "\n");
            }
            moon.write ("putc r1" + "\n");
        }
        else {
            moon.write("sw -8(r14),r1" + "\n");
            moon.write("addi r1, r0, buf" + "\n");
            moon.write("sw -12(r14),r1" + "\n");
            moon.write("jl     r15,intstr" + "\n");
            moon.write("sw -8(r14),r13" + "\n");
            moon.write("jl     r15,putstr" + "\n");
        }
    }

    private void GetVarDeclareAssembly(String item)
    {
        int memorySize = 1;
        String content = item.split("#")[1].trim();
        if (content.contains("["))
        {
            int[] arrIndex = GetArrayIndexes(content.split(" ")[1].trim());
            for (int index : arrIndex)
            {
                memorySize = memorySize * index;
            }
        }
        if (content.split(" ")[1].contains("["))
        {
            int[] indexes = GetArrayIndexes(content.split(" ")[1].trim());
            int maxIndex = indexes[0];
            for(int ind: indexes) maxIndex = Math.max (maxIndex, ind);
            VarOrParam.put(functionName + content.split(" ")[1].split("\\[")[0].trim() + "," + content.split(" ")[0].trim() + "," + memorySize, maxIndex);
        }
        else
        {
            VarOrParam.put(functionName + content.split(" ")[1].trim(), Common.dictMemSize.get(content.split(" ")[0].trim()));
        }
    }

    private void GetAssignStatementAssembly(String item) throws IOException {
        String content = item.split("#")[1].trim();
        String[] arrOperands = content.split("=");
        int[] arrayIndexes = null;
        int arraysize = 0;
        if (arrOperands[0].trim().contains("\\["))
        {
            arrayIndexes = GetArrayIndexes(arrOperands[0].trim());
            for (String key : VarOrParam.keySet ())
            {
                if (key.split(",")[0].trim().equals(arrOperands[0].split("\\[")[0].trim()))
                {
                    arraysize = VarOrParam.get(key);
                    break;
                }
            }
        }
        if (arrOperands[1].trim().contains("+"))
        {
            GenerateAirthmeticAssembly(arrOperands[0].trim(), arrOperands[1].trim(), arrayIndexes, "+");
        }
        else if (arrOperands[1].trim().contains("-"))
        {
            GenerateAirthmeticAssembly(arrOperands[0].trim(), arrOperands[1].trim(), arrayIndexes, "-");
        }
        else if (arrOperands[1].trim().contains("*"))
        {
            GenerateAirthmeticAssembly(arrOperands[0].trim(), arrOperands[1].trim(), arrayIndexes, "*");
        }
        else if (arrOperands[1].trim().contains("/"))
        {
            GenerateAirthmeticAssembly(arrOperands[0].trim(), arrOperands[1].trim(), arrayIndexes, "/");
        }
        else if (Common.lstAlphabets.contains((arrOperands[1].trim().charAt(0)+"").toLowerCase().trim()))
        {
            if (arrayIndexes != null && arrayIndexes.length > 0)
            {
                int reg = WriteArrayAssemblyCode(arrayIndexes, arraysize);
                moon.write ( "lw r" + reg + "," + (functionName + arrOperands[1].trim()) + "(r0)" + "\n");
                moon.write ("sw " + (functionName + arrOperands[0].split("\\[")[0].trim()) + "r" + (reg - 1) + ",r" + reg + "\n");
            }
            else
            {
                moon.write ("lw r1," + (functionName + arrOperands[1].trim()) + "(r0)" + "\n");
                moon.write ("sw " + (functionName + arrOperands[0].trim()) + "(r0),r1" + "\n");
            }
        }
        else
        {
            if (arrayIndexes != null && arrayIndexes.length > 0)
            {
                int reg = WriteArrayAssemblyCode(arrayIndexes, arraysize);
                moon.write ("addi r1,r0," + arrOperands[1] + "\n");
                moon.write ("sw " + (functionName + arrOperands[0].split("\\[")[0].trim()) + "(r" + (reg - 1) + "),r1" + "\n");
            }
            else
            {
                moon.write ("addi r1,r0," + arrOperands[1].trim() + "\n");
                moon.write ( "sw " + (functionName + arrOperands[0].trim()) + "(r0),r1" + "\n");
            }
        }
    }
    private int[] GetArrayIndexes(String v1)
    {
        String temp = v1.substring(v1.indexOf('[') + 1, v1.length ()-1);
//        System.out.println ("[][][]: "+temp);
        String[] arrTemp = temp.split("]\\[");
        List<String> result = new ArrayList<> ();
        for (String item : arrTemp)
        {
            if (Common.lstAlphabets.contains((item.charAt(0)+"").toLowerCase()))
                result.add("0");
            else
                result.add(item);
        }
        int[] res = new int[arrTemp.length];
        int idx = 0;
        for(String x: result) {
            res[idx++] = Integer.parseInt (x);
        }
        return res;
    }

    private void GenerateAirthmeticAssembly(String leftOperand, String rightOperands, int[] leftArrayIndexes, String opr) throws IOException {
//        System.out.println (rightOperands);
//        System.out.println (opr);
        String splitopr = "\\"+opr ;
//        System.out.println (splitopr);
        String[] arrOperands = rightOperands.split(splitopr);
        String oprName = Common.dictOprName.get(opr);
//        System.out.println (oprName);
        int[] firstOperandReg = null;
        int[] secondOperandReg = null;
        int leftArraySize = 0;
        int rightOpr1ArraySize = 0;
        int rightOpr2ArraySize = 0;
        if (leftArrayIndexes != null && leftArrayIndexes.length > 0)
        {
            for (String key : VarOrParam.keySet ())
            {
                if (key.split(",")[0].trim().equals(leftOperand.split("\\[")[0].trim()))
                {
                    leftArraySize = VarOrParam.get(key);
                    break;
                }
            }
        }
        if (arrOperands[0].trim().contains("["))
        {
            firstOperandReg = GetArrayIndexes(arrOperands[0].trim());
            for (String key : VarOrParam.keySet())
            {
                if (key.split(",")[0].trim().equals(arrOperands[0].split("\\[")[0].trim()))
                {
                    rightOpr1ArraySize = VarOrParam.get(key);
                    break;
                }
            }
        }
//        System.out.println (arrOperands.length);
        if (arrOperands[1].trim().contains("["))
        {
            secondOperandReg = GetArrayIndexes(arrOperands[1].trim());
            for (String key : VarOrParam.keySet ())
            {
                if (key.split(",")[0].trim() == arrOperands[1].split("\\[")[0].trim())
                {
                    rightOpr2ArraySize = VarOrParam.get(key);
                    break;
                }
            }
        }
        if (arrOperands.length == 2)
        {
            if (Common.lstAlphabets.contains((arrOperands[0].trim().charAt(0)+"").toLowerCase().trim()))
            {
                if (Common.lstAlphabets.contains((arrOperands[1].trim().charAt(0)+"").toLowerCase().trim()))
                {
                    if (firstOperandReg != null && firstOperandReg.length > 0)
                    {
                        int result = WriteArrayAssemblyCode(firstOperandReg, rightOpr1ArraySize);
                        moon.write("lw r1," + (functionName + arrOperands[0].trim().split("\\[")[0]) + "(r" + (result - 1) + ")" + "\n");
                    }
                    else
                        moon.write("lw r1," + (functionName + arrOperands[0].trim()) + "(r0)" + "\n");
                    if (secondOperandReg != null && secondOperandReg.length > 0)
                    {
                        int result = WriteArrayAssemblyCode(secondOperandReg, rightOpr2ArraySize);
                        moon.write("lw r2," + (functionName + arrOperands[1].trim().split("\\[")[0]) + "(r" + (result - 1) + ")" + "\n");
                    }
                    else
                        moon.write("lw r2," + (functionName + arrOperands[1].trim()) + "(r0)" + "\n");
                    moon.write(oprName + " r3,r1,r2" + "\n");
                    if (leftArrayIndexes != null && leftArrayIndexes.length > 0)
                    {
                        int result = WriteArrayAssemblyCode(leftArrayIndexes, leftArraySize);
                        moon.write("sw " + (functionName + leftOperand.split("\\[")[0]) + "(r" + (result - 1) + "),r3" + "\n");
                    }
                    else
                        moon.write("sw " + (functionName + leftOperand) + "(r0),r3" + "\n");
                }
                else
                {
                    if (firstOperandReg != null && firstOperandReg.length > 0)
                    {
                        int result = WriteArrayAssemblyCode(firstOperandReg, rightOpr1ArraySize);
                        moon.write("lw r1," + (functionName + arrOperands[0].trim().split("\\[")[0]) + "(r" + (result - 1) + ")" + "\n");
                    }
                    else
                        moon.write("lw r1," + (functionName + arrOperands[0].trim()) + "(r0)" + "\n");
                    moon.write(oprName + "i r2,r1," + arrOperands[1].trim() + "\n");

                    if (leftArrayIndexes != null && leftArrayIndexes.length > 0)
                    {
                        int result = WriteArrayAssemblyCode(leftArrayIndexes, leftArraySize);
                        moon.write("sw " + (functionName + leftOperand.split("\\[")[0]) + "(r" + (result - 1) + "),r3" + "\n");
                    }
                    else
                        moon.write("sw " + (functionName + leftOperand) + "(r0),r2" + "\n");
                }
            }
            else if (Common.lstAlphabets.contains((arrOperands[1].trim().charAt(0)+"").toLowerCase().trim()))
            {
                moon.write("lw r1," + (functionName + arrOperands[1].trim()) + "(r0)" + "\n");
                if (secondOperandReg != null && secondOperandReg.length > 0)
                {
                    int result = WriteArrayAssemblyCode(secondOperandReg, rightOpr2ArraySize);
                    moon.write("lw r2," + (functionName + arrOperands[1].trim().split("\\[")[0]) + "(r" + (result - 1) + ")" + "\n");
                }
                else
                    moon.write(oprName + "i r2,r1," + arrOperands[0].trim() + "\n");
                if (leftArrayIndexes != null && leftArrayIndexes.length > 0)
                {
                    int result = WriteArrayAssemblyCode(leftArrayIndexes, leftArraySize);
                    moon.write("sw " + (functionName + leftOperand.split("\\[")[0]) + "(r" + (result - 1) + "),r3" + "\n");
                }
                else
                    moon.write("sw " + (functionName + leftOperand) + "(r0),r2" + "\n");
            }
            else
            {
                moon.write("addi r1,r0," + arrOperands[0].trim() + "\n");
                moon.write(oprName + "i r2,r1," + arrOperands[1].trim() + "\n");
                moon.write("sw " + (functionName + leftOperand) + "(r0),r2" + "\n");
            }
        }
    }

    private int WriteArrayAssemblyCode(int[] arrayIndexes, int arraysize) throws IOException {
        int tmp = 0;
        if (isread)
            tmp = 1;

        int multiplyRegCount = arrayIndexes.length + 1 + tmp;
        List<Integer> multiplyIndexes = new ArrayList<> ();
        for (int i = 1; i <= arrayIndexes.length; i++)
        {
            moon.write("addi r" + (i + tmp) + ",r0," + arrayIndexes[i - 1] + "\n");
        }
        for (int i = 1; i <= arrayIndexes.length; i++)
        {
            moon.write("muli r" + multiplyRegCount + ",r" + i + "," + (arrayIndexes[i - 1] * Common.dictMemSize.get("integer") * arraysize) + "\n");
            arraysize = 1;
            multiplyIndexes.add(multiplyRegCount);
            multiplyRegCount = multiplyRegCount + 1;
        }
        int addRegCount = multiplyRegCount;
        for (int i = 1; i < multiplyIndexes.size (); i = i + 1)
        {
            if (i == 1)
            {
                moon.write("add r" + addRegCount + ",r" + multiplyIndexes.get (i - 1) + ",r" + multiplyIndexes.get (i) + "\n");
            }
            else
            {
                moon.write("add r" + addRegCount + ",r" + multiplyIndexes.get (i) + ",r" + (addRegCount - 1) + "\n");
            }
            addRegCount = addRegCount + 1;
        }
        return addRegCount;
    }

    private int ProcessingTags(String value, int index, int nextContentToRead, List<String> blockCode) throws IOException {
        String codeBlockType = value.split("#")[0].trim();
        if (nextContentToRead == 0 || nextContentToRead == index)
        {
            nextContentToRead = 0;
            switch (codeBlockType)
            {
                case "varDeclare":
                {
                    GetVarDeclareAssembly(value);
                    break;
                }

                case "assignStatement":
                    GetAssignStatementAssembly(value);
                    break;

                case "writeStatement":
                    GetWriteStatementAssembly(value);
                    break;

                case "readStatement":
                    GetReadStatementAssembly(value);
                    break;

                case "condition":
                {
                    nextContentToRead = GetConditionAssembly(value, index, blockCode);
                    break;
                }

                case "start generateIfStatementCode":
                {
                    nextContentToRead = GetIfStatementAssembly(index, blockCode);
                    break;
                }
                case "start generateWhileStatementCode":
                {
                    nextContentToRead = GetWhileStatementAssembly(index, blockCode);
                    break;
                }

                case "start functionDef":
                {
                    nextContentToRead = GetFunctionDeclAssembly(index, blockCode);
                    break;
                }

                case "funcCall":
                {
                    nextContentToRead = GetFunctionCallAssembly(index, blockCode);
                    break;
                }
            }
        }
        return nextContentToRead;
    }

    private int GetFunctionCallAssembly(int index, List<String> blockCode) throws IOException {
        String functionSignature = blockCode.get (index).split("#")[1].trim();
        if (blockCode.get (index).split("#")[0].trim().equals ("funcCall"))
        {
            String functionName = functionSignature.split(" ")[0].trim();
            String[] parameters = functionSignature.split(functionName)[1].trim().split(",");

            int idx = 0;
            for (String item : parameters)
            {
                if (item!=null && !item.isEmpty())
                {
                    if (Common.lstAlphabets.contains(item))
                    {
                        if (item.contains("["))
                        {
                            int[] arrIndexes = GetArrayIndexes(item);
                            int reg = WriteArrayAssemblyCode(arrIndexes, VarOrParam.get(item.split("\\[")[0].trim()) );
                            moon.write("lw r" + (idx + 1) + "," + item.split("\\[")[0].trim() + "(r" + (reg - 1) + ")" + "\n");
                            moon.write("sw " + functionName + "p" + (idx + 1) + "(r0),r" + (idx + 1) + "\n");
                        }
                        else
                        {
                            moon.write("lw r" + (idx + 1) + "," + (this.functionName + item) + "(r0)" + "\n");
                            String xkey = null;
                            for(String key: VarOrParam.keySet()) {
                                if(key.contains (functionName)) {
                                    xkey = key;
                                    break;
                                }
                            }
                            moon.write("sw " + xkey + "(r0),r" + (idx + 1) + "\n");
                        }
                    }
                    else
                    {
                        moon.write("lw r" + (idx + 1) + "," + item + "(r0)" + "\n");
                        String xkey = null;
                        for(String key: VarOrParam.keySet()) {
                            if(key.contains (functionName)) {
                                xkey = key;
                                break;
                            }
                        }
                        moon.write("sw " + xkey + "(r0),r" + (idx + 1) + "\n");
                    }
                }
                idx++;
            }
            moon.write("jl r15," + functionName + "\n");


        }
        return index;
    }

    private int GetWhileStatementAssembly(int index, List<String> rootBlockCode) throws IOException {
        List<String> blockCode = new ArrayList<> ();
        int nextContentToRead = 0;
        int nextIndex = 0;
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            blockCode.add(rootBlockCode.get (i));
            if (rootBlockCode.get (i).equals ("end generateWhileStatementCode"))
            {
                index = i + 1;
                break;
            }
        }

        int idx=0;
        for (String content : blockCode) {
            nextContentToRead = ProcessingTags(content, idx++, nextContentToRead, blockCode);
        }
        return index;
    }

    private int GetFunctionDeclAssembly(int index, List<String> rootBlockCode) throws IOException {
        String functionSignature = rootBlockCode.get (index).split("#")[1].trim();
        functionName = functionSignature.split(" ")[0].trim();
        String[] parameters = functionSignature.split(functionName)[1].trim().split(":")[0].trim().split(",");
        String returnType = functionSignature.split(functionName)[1].trim().split(":")[1].trim();
        moon.write(functionName + "\n");

        if (!returnType.equalsIgnoreCase ("void"))
            moon.write(functionName + "res res " + Common.dictMemSize.get(returnType.toLowerCase()) + "\n");

        for (String item : parameters)
        {
            if (item!=null && !item.isEmpty())
            {
                if (item.split(" ")[1].trim().contains("[")) {
                    VarOrParam.put(functionName + item.split(" ")[1].trim().split("\\[")[0], Common.dictMemSize.get(item.split(" ")[0].trim().toLowerCase ()));
                }
                else {
                    VarOrParam.put(functionName + item.split(" ")[1].trim(), Common.dictMemSize.get(item.split(" ")[0].trim().toLowerCase()));
                }
            }
        }
        List<String> blockCode = new ArrayList<> ();
        int nextContentToRead = 0;
        int nextIndex = 0;
        for (int i = index + 1; i < rootBlockCode.size (); i++)
        {
            blockCode.add(rootBlockCode.get (i));
            if (rootBlockCode.get (i).contains("end functionDef#"))
            {
                index = i + 1;
                break;
            }
        }

        int idx = 0;
        for (String content : blockCode)
        {
            if (content.contains("return "))
            {
                moon.write("lw r1," + content.split("return ")[1].trim() + "\n");
                moon.write("sw " + functionName + "res(r0),r1" + "\n");
                moon.write("jr r15" + "\n");
            }
            nextContentToRead = ProcessingTags(content, idx++, nextContentToRead, blockCode);
        }
        moon.write("hlt" + "\n");
        return index;
    }
}
