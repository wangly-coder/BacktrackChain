package com.wly.regex.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CharRangeTest {

    @DisplayName("测试多区间合并-已排序1")
    @Test
    public void mergeMultiTest1(){
        List<CharRange> testRanges = Lists.newArrayList(
                CharRange.of('0'),
                CharRange.of('1','4'),
                CharRange.of('5','7'),
                CharRange.of('8','9')
        );
        List<CharRange> result = CharRange.mergeMulti(testRanges);
        String printResult = Joiner.on(',').join(result.toArray());
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals("[0,9]",printResult);
    }

    @DisplayName("测试多区间合并-未排序1")
    @Test
    public void mergeMultiTest2(){
        List<CharRange> testRanges = Lists.newArrayList(
                CharRange.of('3','6'),
                CharRange.of('0'),
                CharRange.of('1','4'),
                CharRange.of('8','9')
        );
        List<CharRange> result = CharRange.mergeMulti(testRanges);
        String printResult = Joiner.on(',').join(result.toArray());
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals("[0,6],[8,9]",printResult);
    }

    @DisplayName("测试多区间合并-未排序2")
    @Test
    public void mergeMultiTest3(){
        List<CharRange> testRanges = Lists.newArrayList(
                CharRange.of('5','9'),
                CharRange.of('3','7'),
                CharRange.of('0'),
                CharRange.of('0','3')
        );
        List<CharRange> result = CharRange.mergeMulti(testRanges);
        String printResult = Joiner.on(',').join(result.toArray());
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals("[0,9]",printResult);
    }

    @DisplayName("测试多区间合并-未排序3不同类型字符")
    @Test
    public void mergeMultiTest4(){
        List<CharRange> testRanges = Lists.newArrayList(
                CharRange.of('2'),
                CharRange.of('c','p'),
                CharRange.of('B','H'),
                CharRange.of('y')
        );
        List<CharRange> result = CharRange.mergeMulti(testRanges);
        String printResult = Joiner.on(',').join(result.toArray());
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals("[2],[B,H],[c,p],[y]",printResult);
    }

    @DisplayName("取多区间补集测试-已排序")
    @Test
    public void negativeMultiTest1(){
        List<CharRange> testRanges = Lists.newArrayList(
                CharRange.of('0'),
                CharRange.of('1','4'),
                CharRange.of('5','7'),
                CharRange.of('8','9')
        );
        List<CharRange> result = CharRange.negativeMulti(testRanges);
        String printResult = Joiner.on(',').join(result.toArray());
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals("[\u0000,\u002F],[\u003A,\uFFFF]",printResult);
    }

    @DisplayName("取多区间补集测试-未排序1")
    @Test
    public void negativeMultiTest2(){
        List<CharRange> testRanges = Lists.newArrayList(
                CharRange.of('c','p'),
                CharRange.of('2'),
                CharRange.of('B','H'),
                CharRange.of('y')
        );
        List<CharRange> result = CharRange.negativeMulti(testRanges);
        String printResult = Joiner.on(',').join(result.toArray());
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals("[\u0000,1],[3,A],[I,b],[q,x],[z,\uFFFF]",printResult);
    }

    @DisplayName("取多区间补集测试-未排序2")
    @Test
    public void negativeMultiTest3(){
        List<CharRange> testRanges = Lists.newArrayList(
                CharRange.of('2'),
                CharRange.of('4','H'),
                CharRange.of('y')
        );
        List<CharRange> result = CharRange.negativeMulti(testRanges);
        String printResult = Joiner.on(',').join(result.toArray());
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals("[\u0000,1],[3],[I,x],[z,\uFFFF]",printResult);
    }

    @DisplayName("取多区间补集测试-结果为空集")
    @Test
    public void negativeMultiTest4(){
        List<CharRange> testRanges = Lists.newArrayList(
                CharRange.of('\u0000','a'),
                CharRange.of('a','\uffff')
        );
        List<CharRange> result = CharRange.negativeMulti(testRanges);
        assertEquals(0,result.size());
        String printResult = Joiner.on(',').join(result.toArray());
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals("",printResult);
    }
}
