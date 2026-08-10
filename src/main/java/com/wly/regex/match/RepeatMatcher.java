package com.wly.regex.match;

import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.back.BackPoint;
import com.wly.regex.match.back.Backer;
import com.wly.regex.match.search.Pointer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepeatMatcher extends ChainMatcher implements Backer {
    public ChainMatcher repeatChainHead;
    private int min;
    public int max;
    public boolean greedy;
    public int count; // 当前计数值
    public RepeatMatcher preRepeatMatcher;
    String name; // Repeat-m-n形式，m表示外层有几个RM，n表示是该层第几个RM

    // 防止匹配-回溯循环问题
    // 无法匹配的下标，以该下标为起点的子串无法正常匹配一次
    public int unMatchIndex = -1;
    // 空串下标
    public int emptyStringIndex = -1;

    // 非贪婪匹配相关上下文
    public Set<Integer> emptyStringIndexSet = new HashSet<>();

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

    public void setGreedy(boolean greedy) {
        this.greedy = greedy;
        if(!greedy) this.emptyStringIndexSet = new HashSet<>();
    }

    @Override
    public String toString() {
        return String.format("[%s:[Pre:%s,Count:{min:%d,max:%d,greedy:%s},Chain:{%s}]]",this.name,this.preRepeatMatcher == null ? "null" : this.preRepeatMatcher.name,
                this.min,this.max,this.greedy,MatcherChainPrinter.printChain(this.repeatChainHead));
    }

    // 创建回溯点
    public BackPoint createBackPoint(int index){
        BackPoint.BPTYPE bptype = this.greedy ? BackPoint.BPTYPE.REPEAT : BackPoint.BPTYPE.NG_REPEAT;
        return new BackPoint(bptype,this.next,index,this);
    }

    // 记录回溯点
    public void setBackPoint(int index,BackContext context){
        BackPoint backPoint = this.createBackPoint(index);
        context.store(backPoint);
    }

    public void setBackPoint(BackPoint backPoint,BackContext context){
        if(backPoint == null) return;
        context.store(backPoint);
    }

    // 设置ZBP
    public void setZeroBackPoint(BackPoint zeroBackPoint,BackContext context){
        if(zeroBackPoint == null) return;
        context.store(zeroBackPoint);
    }

    // 是否匹配空串
    public boolean matchEmptyString(int index){
        if(this.matchEmptyString){
            if(!this.greedy) return this.notGreedyEmptyStringMatch(index);
            if(this.emptyStringIndex == index){
                if(this.preRepeatMatcher != null && this.preRepeatMatcher.greedy) this.emptyStringIndex = -1;
                return false;
            }
            this.emptyStringIndex = index;
            return true;
        }
        return false;
    }

    public boolean notGreedyEmptyStringMatch(int index){
        if(this.emptyStringIndexSet.contains(index)) return false;
        this.emptyStringIndexSet.add(index);
        return true;
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

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext context) {
        // 非贪婪匹配
        if(!this.greedy) return this.notGreedyMatch(str,pointer,context);
        int curIndex = pointer.index;
        // 防止RM嵌套发生匹配-循环问题
        if(this.unMatchIndex == curIndex) return false;
        // 每次调用该方法都是从头开始匹配。这里需要清空回溯导致的计数缓存
        this.count = 0;
        // 记录ZBP
        BackPoint zeroBackPoint = null;
        if(this.matchEmptyString) zeroBackPoint = this.createBackPoint(curIndex);
        // 保留最近一个非ZBP的回溯点，当匹配失败时不回溯，而是使用上一次的成功匹配
        BackPoint preBackPoint = null;
        // 开始循环匹配
        while(this.count < this.max || this.max == -1){
            if(!this.repeatChainHead.chainMatch(str,pointer,context)) {
//                this.unMatchIndex = preIndex;
                // 如果没有任何匹配次数，可以尝试匹配空串并交付给后续匹配器
                // 这里优先采用空匹配而不是回溯的ZBP，二者本质相同
                if(preBackPoint == null) {
                    // 如果可以匹配空串
                    if(zeroBackPoint != null){
                        int zeroIndex = zeroBackPoint.index;
                        pointer.index = zeroIndex;
                        return this.matchEmptyString(zeroIndex);
                    }
                    return false;
                }
                else {
                    pointer.index = preBackPoint.index;
                    return true;
                }
            }
            curIndex = pointer.index;
            // 如果已经达到了最大值，就直接退出该RM
            if(++this.count == this.max) {
                this.setZeroBackPoint(zeroBackPoint,context);
                this.setBackPoint(preBackPoint,context);
                return true;
            }
            // 如果没有达到最大值但是达到了最小值则需要记录回溯
            if(this.count >= this.min) {
                this.setZeroBackPoint(zeroBackPoint,context);
                zeroBackPoint = null;
                this.setBackPoint(preBackPoint,context);
                preBackPoint = this.createBackPoint(curIndex);
            }
        }
        throw new RuntimeException("不应该出现的异常，可能是RepeatMatcher的min和max设置问题！");
    }

    @Override
    public boolean back(String str, Pointer pointer, BackContext context,BackPoint backPoint) {
        // 非贪婪回退
        if(!this.greedy) return this.notGreedyBack(str,pointer,context,backPoint);
        // 这里的nextMatcher是当前RM的内部RM的next
        ChainMatcher nextMatcher = backPoint.nextMatcher;
        // 内部RM走完，走当前RM的内部匹配链，如果是Union类型，直接通过，因为Union把外部的匹配链也走完了
        if(backPoint.bptype == BackPoint.BPTYPE.REPEAT
                && nextMatcher != null
                && !nextMatcher.chainMatch(str, pointer, context)) return false;
        // 内层成功匹配了一次
        BackPoint preBackPoint = null;
        int curIndex = pointer.index;
        while(this.count < this.max || this.max == -1){
            // 如果已经达到了最大值，就直接退出该RM
            if(++this.count == this.max) break;
            // 如果没有达到最大值但是达到了最小值则需要记录回溯
            if(this.count >= this.min) {
                this.setBackPoint(preBackPoint,context);
                preBackPoint = this.createBackPoint(pointer.index);
            }
            if(!repeatChainHead.chainMatch(str, pointer, context)) {
                if(preBackPoint == null) return this.matchEmptyString(curIndex);
                // 使用上一次完全匹配视为成功
                else{
                    pointer.index = preBackPoint.index;
                    break;
                }
            }
        }
        // 更新BP的nextMatcher为当前RM的外部Matcher，以便上一层使用
        backPoint.nextMatcher = this.next;
        // 如果当前已经是最外层RM了，那么交给全局上下文处理
        if(this.preRepeatMatcher == null) return true;
        // 不是最外层，交给上一层RM处理
        return this.preRepeatMatcher.back(str,pointer,context,backPoint);
    }

    /**
     * 非贪婪匹配，核心就是尽可能少的匹配，达到最低次数后就退出，并记录回溯点
     */
    public boolean notGreedyMatch(String str, Pointer pointer, BackContext context){
        // 每次调用该方法都是从头匹配
        this.count = 0;
        int curIndex = pointer.index;
        // 如果最小次数为0，需要记录一次
        // 避免自循环，不能在同一地方多次记录ZBP。记过了就返回失败，并尝试匹配
        if(this.matchEmptyString(curIndex)) {
            this.setBackPoint(curIndex,context);
            return true;
        }
        // 开始循环匹配
        while(this.count < this.max || this.max == -1){
            if(!this.repeatChainHead.chainMatch(str,pointer,context)) {
                return false;
            }
            // 如果已经达到了最小值就退出
            if(++this.count >= this.min) {
                if(this.max != this.min) this.setBackPoint(pointer.index,context);
                return true;
            }
        }
        throw new RuntimeException("不应该出现的异常，可能是RepeatMatcher的min和max设置问题！");
    }

    /**
     * 非贪婪匹配回退
     */
    public boolean notGreedyBack(String str, Pointer pointer, BackContext context,BackPoint backPoint){
        // 如果当前RM是记录回溯点的RM，而且都是非贪婪类型
        if(this == backPoint.preOrSelfRepeatMatcher && backPoint.bptype == BackPoint.BPTYPE.NG_REPEAT){
            if(!this.notGreedyMatch(str,pointer,context)) return false;
        }
        // 不是记录回溯点的RM，那么需要处理外部匹配链
        else {
            // 这里的nextMatcher是当前RM的内部RM的next
            ChainMatcher nextMatcher = backPoint.nextMatcher;
            // 内部RM走完，走当前RM的内部匹配链，如果是Union类型，直接通过，因为Union把外部的匹配链也走完了
            if(backPoint.bptype == BackPoint.BPTYPE.REPEAT
                    && nextMatcher != null
                    && !nextMatcher.chainMatch(str, pointer, context)) return false;
            // 内层成功匹配了一次即可跳出，因为是非贪婪匹配
            if(++this.count >= min && (this.count < max || this.max == -1)) {
                this.setBackPoint(pointer.index,context);
            }
        }
        // 更新BP的nextMatcher为当前RM的外部Matcher，以便上一层使用
        backPoint.nextMatcher = this.next;
        // 如果当前已经是最外层RM了，那么交给全局上下文处理
        if(this.preRepeatMatcher == null) return true;
        // 不是最外层，交给上一层RM处理
        return this.preRepeatMatcher.back(str,pointer,context,backPoint);
    }
}
