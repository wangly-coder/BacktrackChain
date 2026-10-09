package com.tobethebest.regex.ast;

import com.tobethebest.regex.ast.exp.RegexExp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class LengthMeasurerTest {
    RegexParser regexParser;

    @BeforeEach
    public void setUp(){
        this.regexParser = new RegexParser();
    }

    @DisplayName("测试LengthMeasurer是否正确")
    @ParameterizedTest
    @CsvSource(delimiterString = "=",value = {
            "[\\-\\w+abA-a\\-]{1,5}=1=5",
            "a[^\\-bA-a]c|abb*|(a|ba)+=1=-1",
            "ac(123){1,2}bc=7=10",
            "a(12*(3+4)?){1,2}b=3=-1",
            "(a|b+(12|3?4)+c)+d=2=-1",
            "(a|b|1(23|45))+=1=-1",
            "(\\d+|a+)+c=2=-1",
            "(\\w+|12)+3=2=-1",
            "(\\d{3,}|a+)+c=2=-1",
            "(a|b|\\d+)+=1=-1",
            "^(\\d\\d)(\\d\\d)\\2\\1\\2\\1$=12=12"
    })
    public void test_LengthMeasurer_accuracy(String regex, int minLength,int maxLength){
        RegexExp regexExp = this.regexParser.parse(regex,true);
        LengthMeasurer.LengthInfo lengthInfo = LengthMeasurer.measureLength(regexExp);
        System.out.println("<======================对比MinLength=========================>");
        assertEquals(minLength,lengthInfo.minLength);
        System.out.println("<======================对比MaxLength=========================>");
        assertEquals(maxLength,lengthInfo.maxLength);
    }
}
