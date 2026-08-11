package com.wly.regex.match.back;

import com.wly.regex.match.matcher.RepeatMatcher;

import java.util.Stack;

public class BackContext {
    public Stack<BackPoint> backStack;

    public BackContext() {
        this.backStack = new Stack<>();
    }

    // 设置回溯点
    public void store(BackPoint backPoint){
        if(backPoint == null) return;
        this.backStack.push(backPoint);
    }

    // 从最近的回溯点回溯
    public BackPoint restore(){
        if(this.backStack.empty()) return null;
        BackPoint backPoint = this.backStack.pop();
        // 设置当前RM的maxIndex
        if(backPoint.bptype == BackPoint.BPTYPE.REPEAT) backPoint.preOrSelfRepeatMatcher.unMatchIndex = backPoint.index;
        // 恢复外部各个RM的计数值
        RepeatMatcher preRepeatMatcher;
        if(backPoint.bptype == BackPoint.BPTYPE.REPEAT) {
            preRepeatMatcher = backPoint.preOrSelfRepeatMatcher.preRepeatMatcher;
            // 将外层匹配器重置为真正的上一层匹配器
            backPoint.preOrSelfRepeatMatcher = backPoint.preOrSelfRepeatMatcher.preRepeatMatcher;
        }
        else preRepeatMatcher = backPoint.preOrSelfRepeatMatcher;
        int i = 0;
        while(preRepeatMatcher != null) {
            preRepeatMatcher.count = backPoint.preCounts.get(i++);
            preRepeatMatcher = preRepeatMatcher.preRepeatMatcher;
        }
        return backPoint;
    }

    public int size(){
        return this.backStack.size();
    }

    public void reset(){
        this.backStack.clear();
    }
}
