package com.wly.regex.match;

import com.wly.regex.ast.RegexParser;
import com.wly.regex.ast.exp.RegexExp;
import com.wly.regex.match.matcher.ChainMatcher;
import com.wly.regex.match.matcher.CollectionMatcher;
import com.wly.regex.match.matcher.MatcherWrapper;
import com.wly.regex.match.matcher.UnionMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import static org.junit.jupiter.api.Assertions.*;

@Suite
@SelectClasses({
        TestBasicMatcher.class,
        TestBacker.class,
        TestSpecialMetaChar.class
})
public class MatcherChainBuilderAndPrinterTest {

}

class TestBasicMatcher {

    RegexParser regexParser;

    @BeforeEach
    public void init() {
        this.regexParser = new RegexParser();
    }

    @DisplayName("测试CharExp")
    @Test
    public void test_CharExp() {
        String regexString = "a";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        String matcherString = chainMatcher.printSelf();
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[String:a]", matcherString);
    }

    @DisplayName("测试MetaExp")
    @Test
    public void test_MetaExp() {
        String regexString = "\\w";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        String matcherString = chainMatcher.printSelf();
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Meta:\\w]", matcherString);
    }

    @DisplayName("测试CharCollectionExp、CharRangeExp和MatcherChainPrinter的printCollection方法")
    @Test
    public void test_CharCollectionExp() {
        String regexString = "[ae-fb]";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        String matcherString = chainMatcher.printSelf();
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Collection:[[CharRange:a-b],[CharRange:e-f]]]", matcherString);
    }

    @DisplayName("测试CollectionMatcher是否防止了字符串合并以及name是否正确")
    @Test
    public void test_CharCollectionExp_2() {
        String regexString = "[adr-t]";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        CollectionMatcher collectionMatcher = (CollectionMatcher)((MatcherWrapper) chainMatcher).matcher;
        String matcherString = collectionMatcher.printSelf();
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Collection:[[String:a],[String:d],[CharRange:r-t]]]", matcherString);
        assertEquals("CollectionMatcher-[adr-t]",collectionMatcher.name);
    }

    @DisplayName("测试CharCollectionExp(带有^)和MatcherChainPrinter的printCollection方法以及name是否正确")
    @Test
    public void test_CharCollectionExp_Negative() {
        String regexString = "[^ae-fb]";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        CollectionMatcher collectionMatcher = (CollectionMatcher)((MatcherWrapper) chainMatcher).matcher;
        String matcherString = collectionMatcher.printSelf();
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Collection:[[CharRange:\u0000-\u0060],[CharRange:c-d],[CharRange:g-\uffff]]]", matcherString);
        assertEquals("CollectionMatcher-[^ae-fb]",collectionMatcher.name);
    }

    @DisplayName("测试ConcatExp、CharExp和CharCollectionExp和MatcherChainPrinter的printChain方法")
    @Test
    public void test_ConcatExp_CharExp_CharCollectionExp() {
        String regexString = "abc[0-9a-z]dd";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[String:abc] -> [Collection:[[CharRange:0-9],[CharRange:a-z]]] -> [String:dd]", matcherChainString);
    }
}

class TestBacker {
    RegexParser regexParser;

    @BeforeEach
    public void init() {
        this.regexParser = new RegexParser();
    }

    @DisplayName("测试RepeatExp的?|*|+")
    @ParameterizedTest(name = "测试{0}")
    @CsvSource(delimiterString = "=", value = {
            "a?=[Repeat-0-1:[Pre:null,Count:{min:0,max:1,greedy:true},Chain:{[String:a]}]]",
            "a*=[Repeat-0-1:[Pre:null,Count:{min:0,max:-1,greedy:true},Chain:{[String:a]}]]",
            "a+=[Repeat-0-1:[Pre:null,Count:{min:1,max:-1,greedy:true},Chain:{[String:a]}]]"
    })
    public void test_RepeatExp_Not_Range(String regexString, String expected) {
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        String matcherString = chainMatcher.printSelf();
        assertEquals(expected, matcherString);
    }

    @DisplayName("测试RepeatExp、ConcatExp和MatcherChainPrinter的printChain方法")
    @Test
    public void test_RepeatExp_Range() {
        String regexString = "ac(123){1,2}bc";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[String:ac] -> [Repeat-0-1:[Pre:null,Count:{min:1,max:2,greedy:true},Chain:{[GroupStart:1] -> [String:123] -> [GroupEnd:1]}]] -> [String:bc]", matcherChainString);
    }

    @DisplayName("测试RepeatExp的嵌套")
    @Test
    public void test_RepeatExp_Nest() {
        String regexString = "a(12*(3+4)?){1,2}b";
        this.regexParser = new RegexParser(regexString);
        this.regexParser.closeNestCheck();
        ChainMatcher chainMatcher = MatcherChainBuilder.build(this.regexParser.parse());
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[String:a] -> " +
                "[Repeat-0-1:[Pre:null,Count:{min:1,max:2,greedy:true},Chain:{" +
                "[GroupStart:1] -> [String:1] -> [Repeat-1-1:[Pre:Repeat-0-1,Count:{min:0,max:-1,greedy:true},Chain:{[String:2]}]] " +
                "-> [Repeat-1-2:[Pre:Repeat-0-1,Count:{min:0,max:1,greedy:true},Chain:{" +
                "[GroupStart:2] -> [Repeat-2-1:[Pre:Repeat-1-2,Count:{min:1,max:-1,greedy:true},Chain:{[String:3]}]] -> [String:4] -> [GroupEnd:2]}]] " +
                "-> [GroupEnd:1]}]]" +
                " -> [String:b]", matcherChainString);
    }

    @DisplayName("测试UnionExp和MatcherChainPrinter的printUnionChains方法以及name")
    @Test
    public void test_UnionExp() {
        String regexString = "a\\w|bc|\\dd";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        // assert type
        assertEquals(UnionMatcher.class, chainMatcher.getClass());
        UnionMatcher unionMatcher = (UnionMatcher) chainMatcher;
        String matcherString = unionMatcher.printSelf();
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Union:[Pre:null,Chains:[" +
                "[String:a] -> [Meta:\\w] / [String:bc] / [Meta:\\d] -> [String:d]" +
                "]]]", matcherString);
        assertEquals("UnionMatcher-a\\w|bc|\\dd", unionMatcher.toString());
    }

    @DisplayName("测试RepeatExp里嵌套UnionExp时，UnionMatcher是否有外部RepeatMatcher的引用")
    @Test
    public void test_RepeatExp_Nest_UnionExp() {
        String regexString = "(a(b|c)d){1,2}";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[Repeat-0-1:[Pre:null,Count:{min:1,max:2,greedy:true},Chain:{" +
                "[GroupStart:1] -> [String:a] -> [GroupStart:2] -> [Union:[Pre:Repeat-0-1,Chains:[" +
                "[String:b] -> [GroupEnd:2] -> [String:d] -> [GroupEnd:1] / [String:c] -> [GroupEnd:2] -> [String:d] -> [GroupEnd:1]]" +
                "]] " +
                "-> [GroupEnd:2] -> [String:d] -> [GroupEnd:1]" +
                "}]]", matcherChainString);
    }
}

class TestSpecialMetaChar{
    RegexParser regexParser;

    @BeforeEach
    public void init(){
        this.regexParser = new RegexParser();
    }

    @DisplayName("测试^和$限定符，以及它们对应的Matcher")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "^a$=[StartPos:^] -> [String:a] -> [EndPos:$]",
            "^a=[StartPos:^] -> [String:a]",
            "a$=[String:a] -> [EndPos:$]",
            "^a|b|c$=[Union:[Pre:null,Chains:[[StartPos:^] -> [String:a] / [String:b] / [String:c] -> [EndPos:$]]]]",
    })
    public void test_limit(String regexString,String expectedString){
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n",matcherChainString);
        assertEquals(expectedString,matcherChainString);
    }
}
