package com.wly.regex;

import com.wly.regex.ast.NestNumberChecker;
import com.wly.regex.ast.RegexParser;
import com.wly.regex.match.ChainMatcher;
import com.wly.regex.match.MatcherChainBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class RegexMatcherTest {
    RegexParser regexParser;

    @BeforeEach
    public void init(){
        this.regexParser = new RegexParser();
    }

    @DisplayName("测试matchAll方法-非回溯的Matcher")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "abc=abc=true",
            "abc=abcd=false",
            "a\\wb=aab=true",
            "a[\\dd-g]b=a1b=true",
            "a[\\dd-g]b=adb=true",
            "a[\\dd-g]b=abb=false",
            "a[\\dd-g]b=agb=true"
    })
    public void test_matchAll_no_back(String regex, String str,boolean expected){
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexParser.parse(regex));
        RegexMatcher regexMatcher = new RegexMatcher(chainMatcher);
        boolean isAllMatch = regexMatcher.matchAll(str);
        assertEquals(expected,isAllMatch);
    }

    @DisplayName("测试matchAll方法-非嵌套的回溯的Matcher")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "a|b|c|d=a=true",
            "a|b|c|d=b=true",
            "a|b|c|d=c=true",
            "a|b|c|d=g=false",
            "a|b|c|d=abcd=false",
            "ab?c=ac=true",
            "ab?c=abbc=false",
            "ab*c=ac=true",
            "ab+c=abbc=true",
            "ab+c=ac=false",
            "ab{1,2}c=abc=true",
            "ab{1,2}c=abbbc=false",
            "ab{2,}c=abbbc=true",
            "ab{2,}c=abc=false",
            "ab{2}c=abbc=true",
            "ab{2}c=abbbc=false",
            "ab{2}c=abc=false"
    })
    public void test_matchAll_noNest_back(String regex, String str,boolean expected){
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexParser.parse(regex));
        RegexMatcher regexMatcher = new RegexMatcher(chainMatcher);
        boolean isAllMatch = regexMatcher.matchAll(str);
        assertEquals(expected,isAllMatch);
    }

    @DisplayName("测试matchAll方法-非嵌套的回溯的Matcher-空串匹配")
    @Test
    public void test_matchAll_noNest_back_emptyStringMatch(){
        String regex = "ab{1,2}c?";
        String str = "ab";
        boolean expected = true;
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexParser.parse(regex));
        RegexMatcher regexMatcher = new RegexMatcher(chainMatcher);
        boolean isAllMatch = regexMatcher.matchAll(str);
        assertEquals(expected,isAllMatch);
    }

    @DisplayName("测试matchAll方法-嵌套回溯的Matcher")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "a|b|1(2|3)4|c=b=true",
            "a|b|1(2|3)4|c=124=true",
            "a|b|1(2|3)4|c=134=true",
            "a|b|1(2|3)4|c=14=false",
            "(a+)+b=aaaaaac=false",
            "(\\d+\\w+)+=123abc456def789=true",
            "([a-z]+[0-9]+)+!=abc123def456ghi789!=true",
            "([a-zA-Z0-9]{2,4})+=A1B2C3D4E5F=true",
            "(a|b)+c=abbaabbc=true",
            "(a|bc)+d=abcbcaad=true",
            "(a|b*c)+d=ad=true",
            "(a|b*c)+d=acd=true",
            "(a+|b)+c=aaabaaabc=true",
            "(a+|b)+c=aaabaaabd=false",
            "(a|b(1+|2)c)+d=ab11cb2caad=true",
            "(a|b(1+|2)c)+d=ab11cb2aad=false",
            "(a|b+(12|3?4)+c)+d=abb1234124cb4cd=true",
    })
    public void test_matchAll_nest_back(String regex, String str,boolean expected){
        NestNumberChecker.MAX_NEST_NUMBER = 3;
        ChainMatcher chainMatcher = MatcherChainBuilder.build(regexParser.parse(regex));
        RegexMatcher regexMatcher = new RegexMatcher(chainMatcher);
        boolean isAllMatch = regexMatcher.matchAll(str);
        assertEquals(expected,isAllMatch);
    }
}
