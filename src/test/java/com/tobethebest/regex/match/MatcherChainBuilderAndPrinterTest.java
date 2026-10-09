package com.tobethebest.regex.match;

import com.tobethebest.regex.ast.RegexParser;
import com.tobethebest.regex.ast.TestedRegexParser;
import com.tobethebest.regex.ast.exp.RegexExp;
import com.tobethebest.regex.match.matcher.*;
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
        this.regexParser = new TestedRegexParser();
    }

    @DisplayName("测试CharExp")
    @Test
    public void test_CharExp() {
        String regexString = "a";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[String:a]", matcherString);
    }

    @DisplayName("测试MetaExp")
    @Test
    public void test_MetaExp() {
        String regexString = "\\w";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Meta:\\w]", matcherString);
    }

    @DisplayName("测试CharCollectionExp、CharRangeExp和MatcherChainPrinter的printCollection方法")
    @Test
    public void test_CharCollectionExp() {
        String regexString = "[ae-fb]";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Collection:[[CharRange:a-b],[CharRange:e-f]]]", matcherString);
    }

    @DisplayName("测试CollectionMatcher是否防止了字符串合并以及name是否正确")
    @Test
    public void test_CharCollectionExp_2() {
        String regexString = "[adr-t]";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Collection:[[String:a],[String:d],[CharRange:r-t]]]", matcherString);
        String protoStr = TestedMatcherChainBuilder.matcherToProtoStrMap.get(chainMatcher);
        assertEquals("[adr-t]",protoStr);
    }

    @DisplayName("测试CharCollectionExp(带有^)和MatcherChainPrinter的printCollection方法以及name是否正确")
    @Test
    public void test_CharCollectionExp_Negative() {
        String regexString = "[^ae-fb]";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Collection:[[CharRange:\u0000-\u0060],[CharRange:c-d],[CharRange:g-\uffff]]]", matcherString);
        String protoStr = TestedMatcherChainBuilder.matcherToProtoStrMap.get(chainMatcher);
        assertEquals("[^ae-fb]",protoStr);
    }

    @DisplayName("测试ConcatExp、CharExp和CharCollectionExp和MatcherChainPrinter的printChain方法")
    @Test
    public void test_ConcatExp_CharExp_CharCollectionExp() {
        String regexString = "abc[0-9a-z]dd";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[String:abc] -> [Collection:[[CharRange:0-9],[CharRange:a-z]]] -> [String:dd]", matcherChainString);
    }
}

class TestBacker {
    RegexParser regexParser;

    @BeforeEach
    public void init() {
        this.regexParser = new TestedRegexParser();
    }

    @DisplayName("测试RepeatExp的?|*|+")
    @ParameterizedTest(name = "测试{0}")
    @CsvSource(delimiterString = "=", value = {
            "a?=[RS:[Name:1-a?,Pre:null]] -> [String:a] -> [RE:1]",
            "a*=[RS:[Name:1-a*,Pre:null]] -> [String:a] -> [RE:1]",
            "a+=[RS:[Name:1-a+,Pre:null]] -> [String:a] -> [RE:1]",
    })
    public void test_RepeatExp_Not_Range(String regexString, String expected) {
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        assertEquals(expected, matcherString);
    }

    @DisplayName("测试RepeatExp、ConcatExp和MatcherChainPrinter的printChain方法")
    @Test
    public void test_RepeatExp_Range() {
        String regexString = "ac(123){1,2}bc";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[String:ac] -> [RS:[Name:1-(123){1,2},Pre:null]] -> [GS:1] -> [String:123] -> [GE:1]" +
                " -> [RE:1] -> [String:bc]", matcherChainString);
    }

    @DisplayName("测试RepeatExp的嵌套")
    @Test
    public void test_RepeatExp_Nest() {
        String regexString = "a(12*(3+4)?){1,2}b";
        this.regexParser = new TestedRegexParser(regexString);
        this.regexParser.closeNestCheck();
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(this.regexParser.parse());
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[String:a]" +
                " -> [RS:[Name:1-(12*(3+4)?){1,2},Pre:null]]" +
                " -> [GS:1] -> [String:1] -> [RS:[Name:2-2*,Pre:1]] -> [String:2] -> [RE:2]" +
                " -> [RS:[Name:3-(3+4)?,Pre:1]]" +
                " -> [GS:2] -> [RS:[Name:4-3+,Pre:3]] -> [String:3] -> [RE:4] -> [String:4] -> [GE:2]" +
                " -> [RE:3]" +
                " -> [GE:1] -> [RE:1]" +
                " -> [String:b]", matcherChainString);
    }

    @DisplayName("测试UnionExp和MatcherChainPrinter的printUnionChains方法以及name")
    @Test
    public void test_UnionExp() {
        String regexString = "a\\w|bc|\\dd";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        // assert type
        String matcherString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherString);
        assertEquals("[Union:[Pre:null,Chains:[" +
                "[String:a] -> [Meta:\\w] / [String:bc] / [Meta:\\d] -> [String:d]" +
                "]]]", matcherString);
        int id = TestedMatcherChainBuilder.matcherToIdMap.get(chainMatcher);
        String protoStr = TestedMatcherChainBuilder.matcherToProtoStrMap.get(chainMatcher);
        assertEquals("1-a\\w|bc|\\dd", id + "-" + protoStr);
    }

    @DisplayName("测试RepeatExp里嵌套UnionExp时，UnionMatcher是否有外部RepeatMatcher的引用")
    @Test
    public void test_RepeatExp_Nest_UnionExp() {
        String regexString = "(a(b|c)d){1,2}";
        RegexExp regexExp = regexParser.parse(regexString);
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n", matcherChainString);
        assertEquals("[RS:[Name:1-(a(b|c)d){1,2},Pre:null]]" +
                " -> [GS:1] -> [String:a] -> [GS:2] -> [Union:[Pre:1,Chains:[" +
                "[String:b] -> [GE:2] -> [String:d] -> [GE:1] -> [RE:1]" +
                " / [String:c] -> [GE:2] -> [String:d] -> [GE:1] -> [RE:1]]" +
                "]]" +
                " -> [GE:2] -> [String:d] -> [GE:1]" +
                " -> [RE:1]", matcherChainString);
    }
}

class TestSpecialMetaChar{
    RegexParser regexParser;

    @BeforeEach
    public void init(){
        this.regexParser = new TestedRegexParser();
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
        ChainMatcher chainMatcher = TestedMatcherChainBuilder.build(regexExp);
        String matcherChainString = MatcherChainPrinter.printChain(chainMatcher);
        System.out.printf("<========================\n%s\n========================>\n",matcherChainString);
        assertEquals(expectedString,matcherChainString);
    }
}
