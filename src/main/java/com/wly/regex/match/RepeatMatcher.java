package com.wly.regex.match;

import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.back.BackPoint;
import com.wly.regex.match.back.Backer;

import java.util.ArrayList;
import java.util.List;

public class RepeatMatcher extends ChainMatcher implements Backer {
    public ChainMatcher repeatChainHead;
    private int min;
    public int max;
    public int count; // 当前计数值
    public RepeatMatcher preRepeatMatcher;
    String name; // Repeat-m-n形式，m表示外层有几个RM，n表示是该层第几个RM

    public RepeatMatcher(){}

    public RepeatMatcher(ChainMatcher repeatChainHead, int min, int max,String name) {
        this.repeatChainHead = repeatChainHead;
        this.max = max;
        this.name = name;
        this.setMin(min);
    }

    public void setMin(int min) {
        this.min = min;
        this.matchEmptyString = min == 0;
    }

    @Override
    public String toString() {
        return String.format("[%s:[Pre:%s,Count:{min:%d,max:%d},Chain:{%s}]]",this.name,this.preRepeatMatcher == null ? "null" : this.preRepeatMatcher.name,
                this.min,this.max,MatcherChainPrinter.printChain(this.repeatChainHead));
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext context) {
        // 每次调用该方法都是从头开始匹配。这里需要清空回溯导致的计数缓存
        this.count = 0;
        // 如果最小次数为0，则记录一次回溯
        if(this.min == 0) {
            BackPoint backPoint = new BackPoint(BackPoint.BPTYPE.REPEAT,this.next,pointer.index,this.preRepeatMatcher);
            context.store(backPoint);
        }
        // 开始循环匹配
        while(this.count < this.max || this.max == -1){
            if(!this.repeatChainHead.chainMatch(str,pointer,context)) return false;
            // 如果已经达到了最大值，就直接退出该RM
            if(++this.count == this.max) return true;
            // 如果没有达到最大值但是达到了最小值则需要记录回溯
            if(this.count >= this.min) {
                BackPoint backPoint = new BackPoint(BackPoint.BPTYPE.REPEAT, this.next, pointer.index, this.preRepeatMatcher);
                context.store(backPoint);
            }
        }
        throw new RuntimeException("不应该出现的异常，可能是RepeatMatcher的min和max设置问题！");
    }

    /**
     * 回退到最外层的RM就返回成功，否则失败
     */
    @Override
    public boolean back(String str, Pointer pointer, BackPoint backPoint, BackContext context) {
        // 这里的nextMatcher是当前RM的内部RM的next
        ChainMatcher nextMatcher = backPoint.nextMatcher;
        // 内部RM走完，走当前RM的内部匹配链，如果是Union类型，直接通过
        if(backPoint.bptype == BackPoint.BPTYPE.REPEAT
                && nextMatcher != null
                && !nextMatcher.chainMatch(str, pointer, context)) return false;
        // 内层成功匹配了一次
        while(this.count < this.max || this.max == -1){
            // 如果已经达到了最大值，就直接退出该RM
            if(++this.count == this.max) break;
            // 如果没有达到最大值但是达到了最小值则需要记录回溯
            if(this.count >= this.min) {
                BackPoint backPointNew = new BackPoint(BackPoint.BPTYPE.REPEAT, this.next, pointer.index, this.preRepeatMatcher);
                context.store(backPointNew);
            }
            if(!repeatChainHead.chainMatch(str, pointer, context)) return false;
        }
        // 如果当前已经是最外层RM了，那么成功并交给全局处理后续的匹配链
        if(this.preRepeatMatcher == null) {
            backPoint.nextMatcher = this.next;
            return true;
        }
        // 继续向外层RM回退
        return this.preRepeatMatcher.back(str,pointer, backPoint,context);
    }

    // 获取以当前对象开始到最外层的RM的当前计数信息
    public List<Integer> getPreCounts(){
        List<Integer> preCounts = new ArrayList<>();
        RepeatMatcher preRepeatMatcher = this;
        while(preRepeatMatcher != null){
            preCounts.add(preRepeatMatcher.count);
            preRepeatMatcher = preRepeatMatcher.preRepeatMatcher;
        }
        return preCounts;
    }

}
