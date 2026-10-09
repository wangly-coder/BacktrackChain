package com.tobethebest.regex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Suite
@SelectClasses({
        TestMatchAll.class,
        TestMatchAndSearch.class
})
public class RegexMatcherTest {}

class TestMatchAll {

    @DisplayName("测试matchAll方法-非回溯的Matcher")
    @ParameterizedTest
    @CsvSource(delimiterString = "=", value = {
            "abc=abc=true",
            "abc=abcd=false",
            "a\\wb=aab=true",
            "a[\\dd-g]b=a1b=true",
            "a[\\dd-g]b=adb=true",
            "a[\\dd-g]b=abb=false",
            "a[\\dd-g]b=agb=true"
    })
    public void test_matchAll_no_back(String regex, String str, boolean expected) {
        TestedRegexMatcher TestedRegexMatcher = new TestedRegexMatcher(regex);
        boolean isAllMatch = TestedRegexMatcher.matchAll(str);
        assertEquals(expected, isAllMatch);
    }

    @DisplayName("测试matchAll方法-非嵌套的回溯的Matcher")
    @ParameterizedTest
    @CsvSource(delimiterString = "=", value = {
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
    public void test_matchAll_noNestBack(String regex, String str, boolean expected) {
        if ("empty".equals(str)) str = "";
        TestedRegexMatcher TestedRegexMatcher = new TestedRegexMatcher(regex);
        boolean isAllMatch = TestedRegexMatcher.matchAll(str);
        assertEquals(expected, isAllMatch);
    }

    @DisplayName("测试matchAll方法-嵌套回溯的Matcher")
    @ParameterizedTest
    @CsvSource(delimiterString = "=", value = {
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
            // 量词嵌套三层及以上
            "(a|b+(12|3?4)+c)+d=abb1234124cb4cd=true",
            "(1(ab+c)?2)+=1abbc2=true",
            "^((a+b|c+)+)+$=aab=true",
            "(a{2,3}b+|c+d)+=aabcc=false",
            "^((ab|c{2,3})+)+$=abccc=true",
            // 四层
            "(((a+b)+)+)+=aab=true",
            "(a{2,3}b+|c{2,3}d+)+=aabbccd=true",
            "((a+b+|c+)+)+=aabc=true",
            // 五层
            "((((a|b)+)+)+)+=ab=true",
            "(((a+b|c)+)+)+=aabc=true",
            "(((a{2,3}|b+)+)+)+=aaabb=true",
            "^(((a+b|c)+)+)+$=aabcx=false",
    })
    public void test_matchAll_nest_back(String regex, String str, boolean expected) {
        TestedRegexMatcher TestedRegexMatcher = new TestedRegexMatcher(regex,true);
        boolean isAllMatch = TestedRegexMatcher.matchAll(str);
        assertEquals(expected, isAllMatch);
    }
}

class TestMatchAndSearch {
    // search里面包含了match方法
    @DisplayName("测试所有的重载search和searchAll方法-贪婪匹配")
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
        TestedRegexMatcher TestedRegexMatcher = new TestedRegexMatcher(regex);
        List<String> result = TestedRegexMatcher.searchAll(str);
        result = result == null ? new ArrayList<>() : result;
//        System.out.printf("<========================\n%s\n========================>\n",result.size());
        String resultStr = String.join(",",result);
        System.out.printf("<========================\n%s\n========================>\n",resultStr);
        assertEquals(expected,resultStr);
    }

    @DisplayName("测试所有的重载search和searchAll方法-非贪婪匹配")
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
            "(\\d{2,}|a?)*?c=456ccc789aac=456c,c,c,789aac",
            "(a|b|\\d*)+?=12a3b4c5d6=12,a,3,b,4,,5,,6,",
    })
    public void test_search_searchAll_notGreedyMatch(String regex, String str, String expected){
        TestedRegexMatcher TestedRegexMatcher = new TestedRegexMatcher(regex);
        List<String> result = TestedRegexMatcher.searchAll(str);
        result = result == null ? new ArrayList<>() : result;
//        System.out.printf("<========================\n%s\n========================>\n",result.size());
        String resultStr = String.join(",",result);
        System.out.printf("<========================\n%s\n========================>\n",resultStr);
        assertEquals(expected,resultStr);
    }

    @DisplayName("测试所有的重载search和searchAll方法-总测试-至少2层或以上的量词嵌套")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            // 2层嵌套
            "([\\da-f]+?|[A-Z]{2,3})+=1a2b3c#4d5e6f=1a2b3c,4d5e6f",
            "(\\w+?|\\d{2,3})+=a1b2c3#d4e5f6=a1b2c3,d4e5f6",
            "([a-z]+?|[\\d\\W]{2,3})+=abc999#def888=abc999,def888",
            "([\\dA-Z]+?|[a-z]{2,3})+=1A2B3C#4D5E6F=1A2B3C,4D5E6F",
            "([\\da-f]+?|\\w{2,3})+=1a2b3c#4d5e6f=1a2b3c,4d5e6f",
            "(\\w+?|[A-Z]{2,3})+=a1b2c3#d4e5f6=a1b2c3,d4e5f6",
            "(([a-z]+?\\d|[A-Z]{2,3})+)=a1b2c3!d4e5f6!=a1b2c3,d4e5f6",
            "(([x-z]+?\\d|[A-Z]{2,3})+)=x1y2z3#w4v5u6=x1y2z3",
            "(([\\da-c]+?[a-c]|[A-Z]{2,3})+)=1a2b3c#4a5b6c=1a2b3c,4a5b6c",
            "(([a-z]+?\\d|[\\dA-Z]{2,3})+)=a1b2c3#d4e5f6=a1b2c3,d4e5f6",
            "([\\da-f]+?\\d|[a-z]{2,3})+=1a2b3c#4d5e6f=1a2b3,4d5e6", // ?
            "((\\w+?[a-z]|\\d{2,3})+)=a1b2c3#d4e5f6=a1b2c,d4e5f",
            "(([\\da-c]+?\\d|[A-Z]{2,3})+)=1a2b3c#4a5b6c=1a2b3,4a5b6",
            "(([a-z]+?\\d|\\w{2,3})+)=a1b2c3!d4e5f6!=a1b2c3,d4e5f6",
            "((([a-z]+?\\d|[A-Z]{2,3})+))=a1b2c3!d4e5f6!=a1b2c3,d4e5f6",
            "((([x-z]+?\\d|[A-Z]{2,3})+))=x1y2z3#w4v5u6=x1y2z3",
            "(((\\w+?\\d|[A-Z]{2,3})+))=a1b2c3#d4e5f6=a1b2c3,d4e5f6",
            "((([\\da-f]+?\\d|[a-z]{2,3})+))=1a2b3c#4d5e6f=1a2b3,4d5e6",
            "((([a-z]+?\\d|[\\dA-Z]{2,3})+))=a1b2c3#d4e5f6=a1b2c3,d4e5f6",
            "(((\\w+?[a-z]|\\d{2,3})+))=a1b2c3#d4e5f6=a1b2c,d4e5f",
            // 3层嵌套
            "((a+?|b{2,3})+c)+=acbbbcxacbbbcx=acbbbc,acbbbc",
            "((a+?b{1,2}|c{2,3})+d)+=abbdxabbdd=abbd,abbd",
            "((a{1,3}?b|c{2,3})+d)+=abdxccabdd=abd,ccabd",
            "((a+?|b{2,4}c)+d)+=adxbbbcadx=ad,bbbcad",
            "((a{1,3}?|b{2,3})+c)+=abbcxabbcc=abbc,abbc",
            "((a+?b|c{2,3})+d)+=abdccabdxx=abdccabd",
            // 4层嵌套
            "(((a+?|b{2,3})+c)+d)+=acdxacdxacdx=acd,acd,acd",
            "(((a+?b|c)+d)+e)+=abdeabdeabde=abdeabdeabde",
            "(((a{1,3}?b|c{2,3})+d)+e)+=abdeccabde=abdeccabde",
            "(((a+?|b{2,4}c)+d)+e)+=adebbbdeade=ade,ade",
            "(((a{1,2}?|b{2,3}c)+d)+e)+=adexadbbcade=ade,adbbcade",
            "(((a+?b{1,2}|c{2,3})+d)+e)+=abbdeccabbde=abbdeccabbde",
            "(((a+?|b{2,3}c)+d)+e)+=adexadbbcde=ade,adbbcde",
            "(((a{1,3}?|b{2,3})+c)+d)+=acdxaccdxx=acd",
            // 5层嵌套
            "((((a+?|b{2,3})+c)+d)+e)+=acdeacdeacde=acdeacdeacde",
            "((((a+?b|c)+d)+e)+f)+=abdefabdef=abdefabdef",
            "((((a{1,3}?|b{2,3}c)+d)+e)+f)+=adefbbcdef=adefbbcdef",
            "((((a+?|b{2,4})+c)+d)+e)+=acdebbacde=acdebbacde",
            "((((a+?|b{2,3}c)+d)+e)+f)+=adefbbcadef=adefbbcadef",
            "((((a{1,2}?b|c{2,3})+d)+e)+f)+=abdefccabdef=abdefccabdef",
    })
    public void test_search_searchAll_atLeastThreeNestOrMore(String regex, String str, String expected){
        TestedRegexMatcher TestedRegexMatcher = new TestedRegexMatcher(regex,true);
        List<String> result = TestedRegexMatcher.searchAll(str);
        result = result == null ? new ArrayList<>() : result;
//        System.out.printf("<========================\n%s\n========================>\n",result.size());
        String resultStr = String.join(",",result);
        System.out.printf("<========================\n%s\n========================>\n",resultStr);
        assertEquals(expected,resultStr);
    }
}