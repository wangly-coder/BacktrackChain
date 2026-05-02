package com.wly.regex.auto;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.wly.regex.RegexParser;
import com.wly.regex.auto.nfa.NFA;
import com.wly.regex.auto.nfa.NFABuilder;
import com.wly.regex.exp.RegexExp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class NFATest {

    private RegexParser regexParser;

    @BeforeEach
    public void setUp(){
        this.regexParser = new RegexParser();
    }

    public static String statesToString(State stated,Set<State> states){
        String statesString = "";
        if(!states.isEmpty()){
            List<State>  stateList = states.stream().sorted(Comparator.comparingInt(state -> state.id))
                    .collect(Collectors.toList());
            statesString = Joiner.on(",").join(stateList);
        }
        return String.format("%s --> [%s]",stated.printSelf(),statesString);
    }

    @DisplayName("测试NFA空闭包计算1")
    @ParameterizedTest
    @CsvSource(delimiterString = "|",value = {
            "1|S1 --> [S1,S2,S4,S6,S8]",
            "10|S10 --> [S10,S11]",
            "3|S3 --> [S3,S10,S11]",
            "7|S7 --> [S7,S10,S11]"
    })
    public void testEpsilonClosure1(int statedId,String expected){
        String regex = "\\wa";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        State stated = nfa.nfaContext.getState(statedId);
        Set<State> epsilonClosure = nfa.nfaContext.getEpsilonClosure(stated);
        String printResult = NFATest.statesToString(stated,epsilonClosure);
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        //        assertEquals("Start:S1\n"+
//                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
//                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
//                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
//                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
//                        "S2" + Edge.MOVE + "[0,9]" + Edge.MOVE + "S3\n"+
//                        "S4" + Edge.MOVE + "[A,Z]" + Edge.MOVE+"S5\n"+
//                        "S6" + Edge.MOVE + "[_]" + Edge.MOVE+"S7\n"+
//                        "S8" + Edge.MOVE + "[a,z]" + Edge.MOVE+"S9\n"+
//                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
//                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
//                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
//                        "S9" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
//                        "S10" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S11\n"+
//                        "S11" + Edge.MOVE + "[a]" + Edge.MOVE+"S12\n"+
//                        "End:S12\n"
//                ,printResult);
        assertEquals(expected,printResult);
    }

    @DisplayName("测试NFA空闭包计算2")
    @ParameterizedTest
    @CsvSource(delimiterString = "|",value = {
            "8|S8 --> [S1,S2,S6,S8]",
            "1|S1 --> [S1,S2,S6,S8]",
            "3|S3 --> [S3,S4]"
    })
    public void testEpsilonClosure2(int statedId,String expected){
        String regex = "(ab|c)*";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        State stated = nfa.nfaContext.getState(statedId);
        Set<State> epsilonClosure = nfa.nfaContext.getEpsilonClosure(stated);
        String printResult = NFATest.statesToString(stated,epsilonClosure);
        System.out.printf("<========================\n%s\n========================>\n",printResult);
//        assertEquals("Start:S1\n"+
//                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
//                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
//                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
//
//                        "S2" + Edge.MOVE + "[a]" + Edge.MOVE + "S3\n"+
//                        "S6" + Edge.MOVE + "[c]" + Edge.MOVE + "S7\n"+
//                        "S8" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S1\n"+
//
//                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
//                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
//
//                        "S4" + Edge.MOVE + "[b]" + Edge.MOVE + "S5\n"+
//
//                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
//                        "End:S8\n"
//                ,printResult);
        assertEquals(expected,printResult);
    }

    @DisplayName("测试NFA转移1")
    @ParameterizedTest
    @CsvSource(delimiterString = "|",value = {
            "5|a|S5 --> [S6,S7,S8]",
            "1|a|S1 --> [S2,S3]",
            "3|a|S3 --> [S4,S5,S6,S7,S8]"
    })
    public void testMove1(int statedId,char moveChar,String expected){
        String regex = "a{2,4}";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        State stated = nfa.nfaContext.getState(statedId);
        Set<State> moveClosure = nfa.nfaContext.move(Sets.newHashSet(stated),moveChar);
        String printResult = NFATest.statesToString(stated,moveClosure);
        System.out.printf("<========================\n%s\n========================>\n",printResult);
//        assertEquals("Start:S1\n"+
//                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+
//
//                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+
//
//                        "S3" + Edge.MOVE + "[a]" + Edge.MOVE + "S4\n"+
//
//                        "S4" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S5\n"+
//
//                        "S5" + Edge.MOVE + "[a]" + Edge.MOVE + "S6\n"+
//                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
//
//                        "S6" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S7\n"+
//
//                        "S7" + Edge.MOVE + "[a]" + Edge.MOVE + "S8\n"+
//                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
//                        "End:S8\n"
//                ,printResult);
        assertEquals(expected,printResult);
    }

    @DisplayName("测试NFA转移2")
    @ParameterizedTest
    @CsvSource(delimiterString = "|",value = {
            "6|B|S6 --> [S3,S4,S6,S7,S8,S10,S12]",
            "1|a|S1 --> [S1,S2,S3,S4,S6,S8,S10,S11,S12]",
            "10|A|S10 --> []",
            "8|_|S8 --> [S3,S4,S6,S8,S9,S10,S12]",
            "4|1|S4 --> [S3,S4,S5,S6,S8,S10,S12]"
    })
    public void testMove2(int statedId,char moveChar,String expected){
        String regex = "a*[\\wabc2-8]*";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        State stated = nfa.nfaContext.getState(statedId);
        Set<State> moveClosure = nfa.nfaContext.move(Sets.newHashSet(stated),moveChar);
        String printResult = NFATest.statesToString(stated,moveClosure);
        System.out.printf("<========================\n%s\n========================>\n",printResult);
//        assertEquals("Start:S1\n"+
//                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+
//                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
//                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S1\n"+
//
//                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+
//
//                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
//                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
//                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
//                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
//                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
//
//                        "S4" + Edge.MOVE + "[0,9]" + Edge.MOVE + "S5\n"+
//                        "S6" + Edge.MOVE + "[A,Z]" + Edge.MOVE+"S7\n"+
//                        "S8" + Edge.MOVE + "[_]" + Edge.MOVE+"S9\n"+
//                        "S10" + Edge.MOVE + "[a,z]" + Edge.MOVE+"S11\n"+
//
//                        "S12" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+
//
//                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
//                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
//                        "S9" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
//                        "S11" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
//                        "End:S12\n"
//                ,printResult);
        assertEquals(expected,printResult);
    }

    @DisplayName("测试NFA匹配1")
    @ParameterizedTest
    @CsvSource(delimiterString = " = ",value = {
            "\\wa = xa = Match true",
            "[^2-6Cb-g]{1} = g = Match false",
            "a*[\\wabc2-8]* = a2e1g3 = Match true",
            "a{2,}b = aac = Match false",
            "(ab|c)* = abcababccc = Match true"
    })
    public void testMatch1(String regex,String target,String expected){
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = "Match " + nfa.match(target);
        System.out.printf("<========================\n%s\n========================>\n",printResult);
        assertEquals(expected,printResult);
    }

    @DisplayName("测试NFA空白符匹配1")
    @Test
    public void testMatchBlank1(){
        RegexExp regexExp = this.regexParser.parse("\\s");
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        assertTrue(nfa.match(" "));
        assertFalse(nfa.match("a"));
        assertTrue(nfa.match("\r"));
    }

    @DisplayName("测试NFA点匹配1")
    @Test
    public void testMatchDot1(){
        RegexExp regexExp = this.regexParser.parse(".");
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        assertTrue(nfa.match("\u0000"));
        assertFalse(nfa.match("\n"));
    }
}
