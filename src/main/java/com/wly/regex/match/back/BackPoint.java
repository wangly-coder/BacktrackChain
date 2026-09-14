package com.wly.regex.match.back;

import com.wly.regex.match.matcher.ChainMatcher;
import com.wly.regex.match.matcher.RepeatMatcher;
import com.wly.regex.match.group.GroupPair;

import java.util.List;
import java.util.Map;

/**
 * 回溯点BackPoint，简称BP。由Union/Repeat创建，简称UBP/RBP
 */

public class BackPoint {
    public BPTYPE bptype;
    public ChainMatcher nextMatcher; // 为UNION类型时，下一个匹配器是另一条路径的头匹配器
    public int index;
    public RepeatMatcher preOrSelfRepeatMatcher; // RM均是self自身，而UM是preRM
    public List<Integer> preCounts; // 存储外部RM的计数器的值,越位于后面的数则是更外层的RM的计数值
    public enum BPTYPE{
        UNION,REPEAT,NG_REPEAT
    }
    public Runnable backFunc;
    public Map<Integer,GroupPair> repeatGroupPairs; // REPEAT类型使用的元组快照，在BackContext中延迟初始化

    public BackPoint(BPTYPE bptype, ChainMatcher nextMatcher, int index, RepeatMatcher preOrSelfRepeatMatcher) {
        this.bptype = bptype;
        this.nextMatcher = nextMatcher;
        this.index = index;
        this.preOrSelfRepeatMatcher = preOrSelfRepeatMatcher;
        if(preOrSelfRepeatMatcher != null){
            if(bptype != BPTYPE.REPEAT) this.preCounts = preOrSelfRepeatMatcher.getPreCounts();
            // REPEAT类型需要迭代一次再判断
            else {
                if(preOrSelfRepeatMatcher.preRepeatMatcher != null) this.preCounts = preOrSelfRepeatMatcher.preRepeatMatcher.getPreCounts();
            }
        }
    }
}
