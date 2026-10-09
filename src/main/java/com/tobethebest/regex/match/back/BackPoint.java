package com.tobethebest.regex.match.back;

import com.tobethebest.regex.match.matcher.ChainMatcher;
import com.tobethebest.regex.match.matcher.RepeatMatcher;
import com.tobethebest.regex.match.group.GroupPair;

import java.util.List;
import java.util.Map;

/**
 * 回溯点BackPoint，简称BP。由Union/Repeat创建，简称UBP/RBP
 */

public class BackPoint {
    public ChainMatcher nextMatcher; // 为UNION类型时，下一个匹配器是另一条路径的头匹配器
    public int index;
    public RepeatMatcher preOrSelfRepeatMatcher; // RM均是self自身，而UM是preRM
    public List<Integer> preCounts; // 存储外部RM的计数器的值,越位于后面的数则是更外层的RM的计数值
    public Runnable backFunc;
    public Map<Integer,GroupPair> greedyRepeatGroupPairs; // GRM使用的捕获组快照

    public BackPoint(int index, ChainMatcher nextMatcher,RepeatMatcher preOrSelfRepeatMatcher) {
        this.nextMatcher = nextMatcher;
        this.index = index;
        this.preOrSelfRepeatMatcher = preOrSelfRepeatMatcher;
        this.preCounts = preOrSelfRepeatMatcher == null ? null : preOrSelfRepeatMatcher.getPreCounts();
    }
}
