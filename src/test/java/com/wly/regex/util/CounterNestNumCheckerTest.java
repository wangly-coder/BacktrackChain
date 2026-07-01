package com.wly.regex.util;

import com.wly.regex.RegexParser;
import com.wly.regex.exp.RegexExp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class CounterNestNumCheckerTest {

    RegexParser regexParser;

    @BeforeEach
    public void setUp(){
        this.regexParser = new RegexParser();
    }

    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "(a{1,2}b){2,3}=2",
            "abc+|a+b|aba=0",
            "a{2,3}bc+|a+b|ab+a=1"
    })
    public void testNormal(String regex,int expected){
        RegexExp regexExp = this.regexParser.parse(regex);
        System.out.printf("<========================\n%d\n========================>\n",regexExp.counterNestNum);
        assertEquals(expected,regexExp.counterNestNum);
    }

    @Test
    public void testException(){
        String regex = "((1(ab{2,3}c)2){1,2}){1,2}";
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        assertEquals("不合法的正则表达式：内部的嵌套计数器个数已经超出MAX_NEST_COUNTER_NUM的数量2",exception.getMessage());
    }
}
