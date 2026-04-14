package com.wly.regex;

import com.wly.regex.exp.RegexExp;
import com.wly.regex.util.ASTVisualizer;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegexParserTest {

    RegexParser regexParser;
    ASTVisualizer astVisualizer = new ASTVisualizer();

    @BeforeEach
    public void setUp(){
        this.regexParser = new RegexParser();
    }

    @Test
    public void testCharExp1(){
        String regex = "a";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = astVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>%n",tree);
        assertEquals("[Char:a]\n", tree);
    }

    @Test
    public void testCharCollectionExp1(){
        String regex = "[-\\w+abA-a-]";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = astVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>%n",tree);
        assertEquals("[CharCollection]\n"+
            "    ├──[Char:-]\n" +
            "    ├──[Meta:\\w]\n" +
            "    ├──[Char:+]\n" +
            "    ├──[Char:a]\n" +
            "    ├──[Char:b]\n" +
            "    ├──[CharRange:A-a]\n" +
            "    └──[Char:-]\n"
        ,tree);
    }

    // 测试异常情况
    @Test
    public void testCharCollectionExp2(){
        String regex = "[z-a]";
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        assertEquals("预期右字符码值大于左边，实际left:z,right:a",exception.getMessage());
    }

    @Test
    public void testRepeatExp1(){
        String regex = "[-\\w+abA-a-]{1,5}";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = astVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>%n",tree);
        assertEquals("[Repeat:{1,5}]\n"+
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
    @Test
    public void testRepeatExp2(){
        String regex = "[-\\w+abA-a-]{5,1}";
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        assertEquals("预期max值大于等于min值，实际max:1,min:5",exception.getMessage());
    }

    // 测试Union和Concat
    @Test
    public void testUnionAndConcatExp1(){
        String regex = "a[^-bA-a]c|abb*";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = astVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>%n",tree);
        assertEquals("[Union]\n"+
                "    ├──[Concat]\n"+
                "    │   ├──[Char:a]\n"+
                "    │   └──[Concat]\n"+
                "    │       ├──[CharCollection:^]\n"+
                "    │       │   ├──[Char:-]\n" +
                "    │       │   ├──[Char:b]\n" +
                "    │       │   └──[CharRange:A-a]\n" +
                "    │       └──[Char:c]\n"+
                "    └──[Concat]\n"+
                "        ├──[Char:a]\n"+
                "        └──[Concat]\n"+
                "            ├──[Char:b]\n"+
                "            └──[Repeat:*]\n"+
                "                └──[Char:b]\n"
                ,tree);
    }

    // 联合大测试，同时测试CharGroupExp
    @Test
    public void testCharGroupExp1(){
        String regex = "a[^-bA-a]c|abb*|(a|ba)+";
        RegexExp regexExp = regexParser.parse(regex);
        String tree = astVisualizer.getASTString(regexExp);
        System.out.printf("<========================\n%s========================>%n",tree);
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
                        "        │       └──[Repeat:*]\n"+
                        "        │           └──[Char:b]\n"+
                        "        └──[Repeat:+]\n"+
                        "            └──[Union]\n"+
                        "                ├──[Char:a]\n"+
                        "                └──[Concat]\n"+
                        "                    ├──[Char:b]\n" +
                        "                    └──[Char:a]\n"
                ,tree);
    }
}
