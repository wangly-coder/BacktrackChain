package com.wly.regex;

import com.wly.regex.ast.RegexParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

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
        RegexMatcher regexMatcher = new RegexMatcher(regex);
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
            "ab{2}c=abc=false",
            "ab{1,2}c?=ab=true",
            "a|b|=empty=true",
            "1(a|b|)2=12=true"
    })
    public void test_matchAll_noNest_back(String regex, String str,boolean expected){
        if("empty".equals(str)) str = "";
        RegexMatcher regexMatcher = new RegexMatcher(regex);
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
            "(\\d+[a-z]+)+=123abc456def=true",
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
            "(1(ab+c)?2)+=1abbc2=true"
    })
    public void test_matchAll_nest_back(String regex, String str,boolean expected){
        this.regexParser.closeNestCheck();
        this.regexParser.setRegexString(regex);
        RegexMatcher regexMatcher = new RegexMatcher(this.regexParser);
        boolean isAllMatch = regexMatcher.matchAll(str);
        assertEquals(expected,isAllMatch);
    }

    @DisplayName("测试重载的所有search和searchAll方法-贪婪匹配")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "(a+|ab)+c=xxxaabababccyyyaabababccxxxaababcc=aabababc,aabababc,aababc",
            "(a|b|1(23|45))+=1123456789=123",
            "(\\d+|a+)+c=123aac456c=123aac,456c",
            "(\\w+|12)+3=xx123456123yy1237893=xx123456123yy1237893",
            "(\\d{3,}|a+)+c=123aab456ccc789aac=456c,789aac",
            "(a|b|\\d?)+=12a3b4c5d6=12a3b4,,5,,6," // 最后的空串匹配，匹配-回溯循环问题
    })
    public void test_search_searchAll_greedyMatch(String regex, String str, String expected){
        RegexMatcher regexMatcher = new RegexMatcher(new RegexParser(regex));
        List<String> result = regexMatcher.searchAll(str);
        result = result == null ? new ArrayList<>() : result;
        System.out.printf("<========================\n%s\n========================>\n",result.size());
        String resultStr = String.join(",",result);
        System.out.printf("<========================\n%s\n========================>\n",resultStr);
        assertEquals(expected,resultStr);
    }

    @DisplayName("测试重载的所有search和searchAll方法-非贪婪匹配")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "(a*|ab)+?c=xxxaabababccyyyaababcc=aabababc,c,aababc,c",
            "(\\d*|a?)+?b=123456aaab7890b=123456aaab,7890b",
            "(a|b|1(23|45))*?=11233456789=,,,,,,,,,,,",
            "(\\w*|12)+?3=xx123456123yy1237893=xx123456123yy1237893",
            "(\\d*|ab)+?b=123ab456ab789ab=b,b,b",
            "(a?|b*)+?c=aaaabbbcccddddccceeeccc=aaaabbbc,c,c,c,c,c,c,c,c",
            "(.?|a)*?b=xxxaabyyyaabzzzaab=xxxaab,yyyaab,zzzaab",
            "(1?|12)+?3=111223333123=3,3,3,3,123",
            "(\\d{2,}|a?)*?c=123aab456ccc789aac=456c,c,c,789aac",
            "(a|b|\\d*)+?=12a3b4c5d6=12,a,3,b,4,,5,,6,"
    })
    public void test_search_searchAll_notGreedyMatch(String regex, String str, String expected){
        RegexMatcher regexMatcher = new RegexMatcher(new RegexParser(regex));
        List<String> result = regexMatcher.searchAll(str);
        result = result == null ? new ArrayList<>() : result;
//        System.out.printf("<========================\n%s\n========================>\n",result.size());
        String resultStr = String.join(",",result);
        System.out.printf("<========================\n%s\n========================>\n",resultStr);
        assertEquals(expected,resultStr);
    }
}
