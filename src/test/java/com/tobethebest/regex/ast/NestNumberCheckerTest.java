package com.tobethebest.regex.ast;

import com.tobethebest.regex.ast.exp.RegexExp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class NestNumberCheckerTest {

    RegexParser regexParser;

    @BeforeEach
    public void setUp(){
        this.regexParser = new RegexParser();
    }

    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "(a{1,2}b){2,3}=2",
            "abc+|a+b|aba=1",
            "a{2,3}bc+|a+b|ab+a=1",
            "a?|a+|a*=1",
            "(a{1,2})?=2"
    })
    public void testNormal(String regex,int expected){
        RegexExp regexExp = this.regexParser.parse(regex);
        assertEquals(expected,NestNumberChecker.getNestNumber(regexExp));
    }

    @Test
    public void testException(){
        String regex = "((1(ab{2,3}c)2){1,2}){1,2}";
        Exception exception = assertThrows(RuntimeException.class,() -> this.regexParser.parse(regex));
        assertEquals("不合法的正则表达式：内部的嵌套计数器个数已经超出MAX_NEST_NUMBER的数量2",exception.getMessage());
    }
}
