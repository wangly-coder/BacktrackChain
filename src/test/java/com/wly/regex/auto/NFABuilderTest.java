package com.wly.regex.auto;

import com.wly.regex.RegexParser;
import com.wly.regex.auto.builder.NFABuilder;
import com.wly.regex.auto.edge.Edge;
import com.wly.regex.auto.edge.EpsilonEdge;
import com.wly.regex.exp.RegexExp;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NFABuilderTest {

    RegexParser regexParser;

    @BeforeEach
    public void setUp(){
        this.regexParser = new RegexParser();
        State.resetSharedId(1);
    }

    @DisplayName("测试基础字符以及ConcatExp")
    @Test
    public void testCharExp1(){
        String regex = "abc";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                "S1" + Edge.MOVE + "[a]" + Edge.MOVE+"S2\n"+
                "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+
                "S3" + Edge.MOVE + "[b]" + Edge.MOVE+"S4\n"+
                "S4" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S5\n"+
                "S5" + Edge.MOVE + "[c]" + Edge.MOVE+"S6\n"+
                "End:S6\n"
                ,printResult);
    }

    @DisplayName("测试元字符以及Concat")
    @Test
    public void testMetaExp1(){
        String regex = "\\wa";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
                        "S2" + Edge.MOVE + "[0,9]" + Edge.MOVE + "S3\n"+
                        "S4" + Edge.MOVE + "[A,Z]" + Edge.MOVE+"S5\n"+
                        "S6" + Edge.MOVE + "[_]" + Edge.MOVE+"S7\n"+
                        "S8" + Edge.MOVE + "[a,z]" + Edge.MOVE+"S9\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S9" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S10" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S11\n"+
                        "S11" + Edge.MOVE + "[a]" + Edge.MOVE+"S12\n"+
                        "End:S12\n"
                ,printResult);
    }

    @DisplayName("测试字符集合的区间合并，元字符")
    @Test
    public void testCharCollectionExp1(){
        String regex = "[\\wabc2-8]{1}";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
                        "S2" + Edge.MOVE + "[0,9]" + Edge.MOVE + "S3\n"+
                        "S4" + Edge.MOVE + "[A,Z]" + Edge.MOVE+"S5\n"+
                        "S6" + Edge.MOVE + "[_]" + Edge.MOVE+"S7\n"+
                        "S8" + Edge.MOVE + "[a,z]" + Edge.MOVE+"S9\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S9" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "End:S10\n"
                ,printResult);
    }

    @DisplayName("测试字符集合^取反情况")
    @Test
    public void testCharCollectionExp2(){
        String regex = "[^2-6Cb-g]{1}";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
                        "S2" + Edge.MOVE + "[\u0000,1]" + Edge.MOVE + "S3\n"+
                        "S4" + Edge.MOVE + "[7,B]" + Edge.MOVE+"S5\n"+
                        "S6" + Edge.MOVE + "[D,a]" + Edge.MOVE+"S7\n"+
                        "S8" + Edge.MOVE + "[h,\uffff]" + Edge.MOVE+"S9\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S9" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "End:S10\n"
                ,printResult);
    }

    @DisplayName("测试RepeatExp的?")
    @Test
    public void testRepeatExpQuestion1(){
        String regex = "a?[\\wabc2-8]?";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+

                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+

                        "S4" + Edge.MOVE + "[0,9]" + Edge.MOVE + "S5\n"+
                        "S6" + Edge.MOVE + "[A,Z]" + Edge.MOVE+"S7\n"+
                        "S8" + Edge.MOVE + "[_]" + Edge.MOVE+"S9\n"+
                        "S10" + Edge.MOVE + "[a,z]" + Edge.MOVE+"S11\n"+

                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
                        "S9" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
                        "S11" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
                        "End:S12\n"
                ,printResult);
    }

    @DisplayName("测试RepeatExp的*")
    @Test
    public void testRepeatExpStar1(){
        String regex = "a*[\\wabc2-8]*";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S1\n"+

                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S10\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+

                        "S4" + Edge.MOVE + "[0,9]" + Edge.MOVE + "S5\n"+
                        "S6" + Edge.MOVE + "[A,Z]" + Edge.MOVE+"S7\n"+
                        "S8" + Edge.MOVE + "[_]" + Edge.MOVE+"S9\n"+
                        "S10" + Edge.MOVE + "[a,z]" + Edge.MOVE+"S11\n"+

                        "S12" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
                        "S9" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
                        "S11" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S12\n"+
                        "End:S12\n"
                ,printResult);
    }

    @DisplayName("测试RepeatExp的+")
    @Test
    public void testRepeatExpPlus1(){
        String regex = "a+b";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+

                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S3" + Edge.MOVE + "[a]" + Edge.MOVE + "S4\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
                        "S4" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S4" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S5\n"+

                        "S5" + Edge.MOVE + "[b]" + Edge.MOVE + "S6\n"+
                        "End:S6\n"
                ,printResult);
    }

    @DisplayName("测试min=0的情况")
    @Test
    public void testRepeatExpRange1(){
        String regex = "a{0,2}b";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+

                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S3" + Edge.MOVE + "[a]" + Edge.MOVE + "S4\n"+
                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+

                        "S4" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S5\n"+

                        "S5" + Edge.MOVE + "[b]" + Edge.MOVE + "S6\n"+
                        "End:S6\n"
                ,printResult);
    }

    @DisplayName("测试{min,max}结构")
    @Test
    public void testRepeatExpRange2(){
        String regex = "a{2,4}";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+

                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S3" + Edge.MOVE + "[a]" + Edge.MOVE + "S4\n"+

                        "S4" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S5\n"+

                        "S5" + Edge.MOVE + "[a]" + Edge.MOVE + "S6\n"+
                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+

                        "S6" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S7\n"+

                        "S7" + Edge.MOVE + "[a]" + Edge.MOVE + "S8\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
                        "End:S8\n"
                ,printResult);
    }

    @DisplayName("测试{min,}的结构")
    @Test
    public void testRepeatExpRange3(){
        String regex = "a{2,}b";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+

                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S3" + Edge.MOVE + "[a]" + Edge.MOVE + "S4\n"+

                        "S4" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S5\n"+

                        "S5" + Edge.MOVE + "[a]" + Edge.MOVE + "S6\n"+
                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
                        "S6" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S5\n"+

                        "S6" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S7\n"+
                        "S7" + Edge.MOVE + "[b]" + Edge.MOVE + "S8\n"+
                        "End:S8\n"
                ,printResult);
    }

    @DisplayName("测试{min}的结构")
    @Test
    public void testRepeatExpRange4(){
        String regex = "a{2}";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + "[a]" + Edge.MOVE + "S2\n"+

                        "S2" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S3\n"+

                        "S3" + Edge.MOVE + "[a]" + Edge.MOVE + "S4\n"+
                        "End:S4\n"
                ,printResult);
    }

    @DisplayName("测试UnionExp和ConcatExp")
    @Test
    public void testUnionExpAndConcatExp1(){
        String regex = "ab|c*";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+

                        "S2" + Edge.MOVE + "[a]" + Edge.MOVE + "S3\n"+
                        "S6" + Edge.MOVE + "[c]" + Edge.MOVE + "S7\n"+
                        "S6" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S7\n"+

                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+

                        "S4" + Edge.MOVE + "[b]" + Edge.MOVE + "S5\n"+

                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
                        "End:S8\n"
                ,printResult);
    }

    @DisplayName("测试UnionExp和CharGroupExp")
    @Test
    public void testUnionExpAndCharGroupExp1(){
        String regex = "(ab|c)*";
        RegexExp regexExp = this.regexParser.parse(regex);
        NFA nfa = NFABuilder.INSTANCE.build(regexExp);
        String printResult = nfa.printSelf();
        System.out.printf("<========================\n%s========================>\n",printResult);
        assertEquals("Start:S1\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S2\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S6\n"+
                        "S1" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+

                        "S2" + Edge.MOVE + "[a]" + Edge.MOVE + "S3\n"+
                        "S6" + Edge.MOVE + "[c]" + Edge.MOVE + "S7\n"+
                        "S8" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S1\n"+

                        "S3" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S4\n"+
                        "S7" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+

                        "S4" + Edge.MOVE + "[b]" + Edge.MOVE + "S5\n"+

                        "S5" + Edge.MOVE + EpsilonEdge.SIGN + Edge.MOVE + "S8\n"+
                        "End:S8\n"
                ,printResult);
    }

}
