package com.tobethebest.regex.ast;

import com.tobethebest.regex.ast.exp.RegexExp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class RegexParserTest {

    RegexParser regexParser;

    @BeforeEach
    public void setUp(){
        this.regexParser = new RegexParser();
    }

    @DisplayName("测试CharExp")
    @Test
    public void testCharExp1(){
        String regex = "a";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Char:a]\n", tree);
    }

    @DisplayName("测试CharCollectionExp和MetaExp")
    @Test
    public void testCharCollectionExp1(){
        String regex = "[\\w+abA-a\\-]";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[CharCollection]\n"+
            "    ├──[Meta:\\w]\n" +
            "    ├──[Char:+]\n" +
            "    ├──[Char:a]\n" +
            "    ├──[Char:b]\n" +
            "    ├──[CharRange:A-a]\n" +
            "    └──[Char:-]\n"
        ,tree);
    }

    // 测试异常情况
    @DisplayName("异常测试-testCharCollectionExp-exception")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "a[1-\\w]b=预期为CharExp类型，实际left:CharExp，right:MetaExp",
            "a[1-a]b=预期为数字或者字母，实际left:1,right:a",
            "a[z-a]b=预期右字符码值大于左边，实际left:z,right:a"
    })
    public void testCharCollectionExp2(String regex,String eMessage){
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        assertEquals(eMessage,exception.getMessage());
    }

    @DisplayName("测试RepeatExp{min,max}?")
    @Test
    public void testRepeatExp1(){
        String regex = "[\\-\\w+abA-a\\-]{1,5}?";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Repeat:{1,5}?]\n"+
                        "    └──[CharCollection]\n" +
                        "        ├──[Char:-]\n" +
                        "        ├──[Meta:\\w]\n" +
                        "        ├──[Char:+]\n" +
                        "        ├──[Char:a]\n" +
                        "        ├──[Char:b]\n" +
                        "        ├──[CharRange:A-a]\n" +
                        "        └──[Char:-]\n"
                ,tree);
    }

    // 测试异常情况
    @DisplayName("异常测试-testRepeatExp2")
    @Test
    public void testRepeatExp2(){
        String regex = "[\\-\\w+abA-a\\-]{5,1}";
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        assertEquals("预期max值大于等于min值，实际max:1,min:5",exception.getMessage());
    }

    @Test
    @DisplayName("测试RepeatExp{min}")
    public void testRepeatExp3(){
        String regex = "[\\-\\w+abA-a\\-]{1}";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Repeat:{1}]\n"+
                        "    └──[CharCollection]\n" +
                        "        ├──[Char:-]\n" +
                        "        ├──[Meta:\\w]\n" +
                        "        ├──[Char:+]\n" +
                        "        ├──[Char:a]\n" +
                        "        ├──[Char:b]\n" +
                        "        ├──[CharRange:A-a]\n" +
                        "        └──[Char:-]\n"
                ,tree);
    }

    @DisplayName("测试RepeatExp{min,}?")
    @Test
    public void testRepeatExp4(){
        String regex = "[\\-\\w+abA-a]{1,}?";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Repeat:{1,}?]\n"+
                        "    └──[CharCollection]\n" +
                        "        ├──[Char:-]\n" +
                        "        ├──[Meta:\\w]\n" +
                        "        ├──[Char:+]\n" +
                        "        ├──[Char:a]\n" +
                        "        ├──[Char:b]\n" +
                        "        └──[CharRange:A-a]\n",tree);
    }

    @DisplayName("测试UnionExp和ConcatExp")
    @Test
    public void testUnionAndConcatExp1(){
        String regex = "a[^bA-a]c|abb*";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Union]\n"+
                "    ├──[Concat]\n"+
                "    │   ├──[Char:a]\n"+
                "    │   └──[Concat]\n"+
                "    │       ├──[CharCollection:^]\n"+
                "    │       │   ├──[Char:b]\n" +
                "    │       │   └──[CharRange:A-a]\n" +
                "    │       └──[Char:c]\n"+
                "    └──[Concat]\n"+
                "        ├──[Char:a]\n"+
                "        └──[Concat]\n"+
                "            ├──[Char:b]\n"+
                "            └──[Repeat:{0,}]\n"+
                "                └──[Char:b]\n"
                ,tree);
    }

    @DisplayName("联合测试UnionExp和CharGroupExp")
    @Test
    public void testCharGroupExp1(){
        String regex = "a[^\\-bA-a]c|abb*|(a|ba)+";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Union]\n"+
                        "    ├──[Concat]\n"+
                        "    │   ├──[Char:a]\n"+
                        "    │   └──[Concat]\n"+
                        "    │       ├──[CharCollection:^]\n"+
                        "    │       │   ├──[Char:-]\n" +
                        "    │       │   ├──[Char:b]\n" +
                        "    │       │   └──[CharRange:A-a]\n" +
                        "    │       └──[Char:c]\n"+
                        "    └──[Union]\n"+
                        "        ├──[Concat]\n"+
                        "        │   ├──[Char:a]\n"+
                        "        │   └──[Concat]\n"+
                        "        │       ├──[Char:b]\n"+
                        "        │       └──[Repeat:{0,}]\n"+
                        "        │           └──[Char:b]\n"+
                        "        └──[Repeat:{1,}]\n"+
                        "            └──[Group:1]\n"+
                        "                └──[Union]\n"+
                        "                    ├──[Char:a]\n"+
                        "                    └──[Concat]\n"+
                        "                        ├──[Char:b]\n" +
                        "                        └──[Char:a]\n"
                ,tree);
    }

    @DisplayName("测试UnionExp的空串写法1")
    @Test
    public void testUnionExpEmptyString1(){
        String regex = "a|";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Union]\n" +
                "    ├──[Char:a]\n" +
                "    └──[Meta:empty]\n",tree);
    }

    @DisplayName("测试UnionExp的空串写法2")
    @Test
    public void testUnionExpEmptyString2(){
        String regex = "a||b";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Union]\n" +
                "    ├──[Char:a]\n" +
                "    └──[Union]\n"+
                "        ├──[Meta:empty]\n" +
                "        └──[Char:b]\n",tree);
    }

    @DisplayName("测试UnionExp的空串写法3")
    @Test
    public void testUnionExpEmptyString3(){
        String regex = "1(a|)2";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Concat]\n" +
                "    ├──[Char:1]\n" +
                "    └──[Concat]\n"+
                "        ├──[Group:1]\n" +
                "        │   └──[Union]\n"+
                "        │       ├──[Char:a]\n" +
                "        │       └──[Meta:empty]\n" +
                "        └──[Char:2]\n",tree);
    }

    @DisplayName("测试UnionExp的空串写法-异常")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "|=选择表达式中至少一侧有一个非空表达式",
            "a||=选择表达式中至少一侧有一个非空表达式",
            "||b=选择表达式中至少一侧有一个非空表达式",
            "|a|=选择表达式中最多只允许一个空串匹配",
            "a||b|=选择表达式中最多只允许一个空串匹配",
    })
    public void testUnionExpEmptyString_exception(String regex,String eMessage){
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        assertEquals(eMessage,exception.getMessage());
    }

    @DisplayName("测试限定符^和$")
    @Test
    public void test_limit(){
        String regex = "^a$";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = ASTVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>\n",tree);
        assertEquals("[Concat]\n" +
                "    ├──[Meta:^]\n" +
                "    └──[Concat]\n"+
                "        ├──[Char:a]\n" +
                "        └──[Meta:$]\n",tree);
    }

    @DisplayName("测试限定符^和$-异常")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "^a$b=出现不合法的特殊字符$，必须转义",
            "a^b$=出现不合法的特殊字符^，必须转义"
    })
    public void test_limit_exception(String regex,String message){
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        String eMessage = exception.getMessage();
        System.out.printf("<========================\n%s\n========================>\n",eMessage);
        assertEquals(message,eMessage);
    }

    @DisplayName("测试非法转义字符的异常处理")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "a]b=出现不合法的特殊字符]，必须转义",
            ")a(b)b=出现不合法的特殊字符)，必须转义",
            "a]b=出现不合法的特殊字符]，必须转义",
            "a[12[]b=[]中出现不合法的特殊字符[，必须转义",
            "a[-12]b=[]中出现不合法的特殊字符-，必须转义"
    })
    public void test_illegal_escape(String regex,String message){
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        String eMessage = exception.getMessage();
        System.out.printf("<========================\n%s\n========================>\n",eMessage);
        assertEquals(message,eMessage);
    }
}
