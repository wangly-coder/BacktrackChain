package com.wly.regex.match;

import com.wly.regex.RegexParser;
import com.wly.regex.ast.exp.RegexExp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class MatcherChainBuilderAndPrinterTest {
    RegexParser regexParser;

    @BeforeEach
    public void init(){
        this.regexParser = new RegexParser();
    }

    @DisplayName("测试CharExp")
    @Test
    public void test_CharExp(){
        String regexString = "a";
        RegexExp regexExp = regexParser.parse(regexString);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        String matcherString = matcherWrapper.toString();
        System.out.printf("<========================\n%s\n========================>\n",matcherString);
        assertEquals("[String:a]",matcherString);
    }

    @DisplayName("测试MetaExp")
    @Test
    public void test_MetaExp(){
        String regexString = "\\w";
        RegexExp regexExp = regexParser.parse(regexString);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        String matcherString = matcherWrapper.toString();
        System.out.printf("<========================\n%s\n========================>\n",matcherString);
        assertEquals("[Meta:\\w]",matcherString);
    }

    @DisplayName("测试CharCollectionExp、CharRangeExp和MatcherChainPrinter的printCollection方法")
    @Test
    public void test_CharCollectionExp(){
        String regexString = "[ae-fb]";
        RegexExp regexExp = regexParser.parse(regexString);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        String matcherString = matcherWrapper.toString();
        System.out.printf("<========================\n%s\n========================>\n",matcherString);
        assertEquals("[Collection:[[CharRange:a-b],[CharRange:e-f]]]",matcherString);
    }

    @DisplayName("测试CharCollectionExp(带有^)和MatcherChainPrinter的printCollection方法")
    @Test
    public void test_CharCollectionExp_Negative(){
        String regexString = "[^ae-fb]";
        RegexExp regexExp = regexParser.parse(regexString);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        String matcherString = matcherWrapper.toString();
        System.out.printf("<========================\n%s\n========================>\n",matcherString);
        assertEquals("[Collection:[[CharRange:\u0000-\u0060],[CharRange:c-d],[CharRange:g-\uffff]]]",matcherString);
    }

    @DisplayName("测试ConcatExp、CharExp和CharCollectionExp和MatcherChainPrinter的printChain方法")
    @Test
    public void test_ConcatExp_CharExp_CharCollectionExp(){
        String regexString = "abc[0-9a-z]dd";
        RegexExp regexExp = regexParser.parse(regexString);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        String matcherChainString = MatcherChainPrinter.printChain(matcherWrapper);
        System.out.printf("<========================\n%s\n========================>\n",matcherChainString);
        assertEquals("[String:abc] -> [Collection:[[CharRange:0-9],[CharRange:a-z]]] -> [String:dd]",matcherChainString);
    }

    @DisplayName("测试RepeatExp的?|*|+")
    @ParameterizedTest
    @CsvSource(delimiterString = "=" , value = {
            "a?=[RepeatCount:{min:0,max:1},RepeatChain:{[String:a]}]",
            "a*=[RepeatCount:{min:0,max:-1},RepeatChain:{[String:a]}]",
            "a+=[RepeatCount:{min:1,max:-1},RepeatChain:{[String:a]}]"
    })
    public void test_RepeatExp_Not_Range(String regex, String expected){
        RegexExp regexExp = regexParser.parse(regex);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        String matcherString = matcherWrapper.toString();
        assertEquals(expected,matcherString);
    }

    @DisplayName("测试RepeatExp、ConcatExp和MatcherChainPrinter的printChain方法")
    @Test
    public void test_RepeatExp_Range(){
        String regexString = "ac(123){1,2}bc";
        RegexExp regexExp = regexParser.parse(regexString);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        String matcherChainString = MatcherChainPrinter.printChain(matcherWrapper);
        System.out.printf("<========================\n%s\n========================>\n",matcherChainString);
        assertEquals("[String:ac] -> [RepeatCount:{min:1,max:2},RepeatChain:{[String:123]}] -> [String:bc]",matcherChainString);
    }

    @DisplayName("测试UnionExp和MatcherChainPrinter的printUnionChains方法")
    @Test
    public void test_UnionExp(){
        String regexString = "a\\w|bc|\\dd";
        RegexExp regexExp = regexParser.parse(regexString);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        // assert type
        assertEquals(UnionMatcher.class,matcherWrapper.matcher.getClass());
        UnionMatcher unionMatcher = (UnionMatcher) matcherWrapper.matcher;
        String matcherString = unionMatcher.toString();
        System.out.printf("<========================\n%s\n========================>\n",matcherString);
        assertEquals("[Union:[[String:a] -> [Meta:\\w] / [String:bc] / [Meta:\\d] -> [String:d]]]",matcherString);
    }

    @DisplayName("测试UnionExp的postProcess方法")
    @Test
    public void test_UnionExp_PostProcess(){
        String regexString = "a(11|(22|33)))b";
        RegexExp regexExp = regexParser.parse(regexString);
        MatcherWrapper matcherWrapper = regexExp.accept(MatcherChainBuilder.INSTANCE,null);
        String matcherChainString = MatcherChainPrinter.printChain(matcherWrapper);
        System.out.printf("<========================\n%s\n========================>\n",matcherChainString);
        assertEquals("[String:a] -> "+
                "[Union:[[String:11] -> [String:b] / [String:22] -> [String:b] / [String:33] -> [String:b]]] -> "+
                "[String:b]",matcherChainString);
    }

}
