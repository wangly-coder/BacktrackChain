package com.wly.regex.match.back;

import com.wly.regex.match.matcher.ChainMatcher;
import com.wly.regex.match.matcher.RepeatMatcher;

import java.util.List;

/**
 * 回溯点BackPoint，简称BP。由Union/Repeat创建，简称UBP/RBP
 */

public class BackPoint {
    public BPTYPE bptype;
    public ChainMatcher nextMatcher; // 为UNION类型时，下一个匹配器是另一条路径的头匹配器
    public int index;
    public RepeatMatcher preOrSelfRepeatMatcher;
    public List<Integer> preCounts; // 存储外部RM的计数器的值,越位于后面的数则是更外层的RM的计数值
    public enum BPTYPE{
        UNION,REPEAT,NG_REPEAT
    }

    public BackPoint(){}

    public BackPoint(BPTYPE bptype, ChainMatcher nextMatcher, int index, RepeatMatcher preOrSelfRepeatMatcher) {
        this.bptype = bptype;
        this.nextMatcher = nextMatcher;
        this.index = index;
        this.preOrSelfRepeatMatcher = preOrSelfRepeatMatcher;
        if(bptype == BPTYPE.REPEAT) {
            if(preOrSelfRepeatMatcher.preRepeatMatcher == null) this.preCounts = null;
            else this.preCounts = preOrSelfRepeatMatcher.preRepeatMatcher.getPreCounts();
        }
        else this.preCounts = preOrSelfRepeatMatcher == null ? null : preOrSelfRepeatMatcher.getPreCounts();
//        this.preCounts = this.preOrSelfRepeatMatcher == null ? null : this.preOrSelfRepeatMatcher.getPreCounts();
    }
}
