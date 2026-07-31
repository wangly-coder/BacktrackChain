package com.wly.regex.match.back;

import com.wly.regex.match.ChainMatcher;
import com.wly.regex.match.RepeatMatcher;

import java.util.List;

/**
 * 回溯点BackPoint，简称BP。由Union/Repeat创建，简称UBP/RBP
 */

public class BackPoint {
    public BPTYPE bptype;
    public ChainMatcher nextMatcher; // 为UNION类型时，下一个匹配器是另一条路径的头匹配器
    public int index;
    public RepeatMatcher preRepeatMatcher;
    public List<Integer> preCounts; // 存储外部RM的计数器的值,越位于后面的数则是更外层的RM的计数值
    public static enum BPTYPE{
        REPEAT,UNION
    }

    public BackPoint(){}

    public BackPoint(BPTYPE bptype, ChainMatcher nextMatcher, int index, RepeatMatcher preRepeatMatcher) {
        this.bptype = bptype;
        this.nextMatcher = nextMatcher;
        this.index = index;
        this.preRepeatMatcher = preRepeatMatcher;
        this.preCounts = this.preRepeatMatcher == null ? null : this.preRepeatMatcher.getPreCounts();
    }
}
