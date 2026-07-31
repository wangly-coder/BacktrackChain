package com.wly.regex.match.back;

import com.wly.regex.match.ChainMatcher;
import com.wly.regex.match.RepeatMatcher;

import java.util.Stack;

public class BackContext {
    public Stack<BackPoint> backStack;

    public BackContext() {
        this.backStack = new Stack<>();
    }

    // 设置回溯点
    public void store(BackPoint backPoint){
        this.backStack.push(backPoint);
    }

    // 从最近的回溯点回溯
    public BackPoint restore(){
        if(this.backStack.empty()) return null;
        BackPoint backPoint = this.backStack.pop();
        // 恢复外部各个RM的计数值
        RepeatMatcher preRepeatMatcher = backPoint.preRepeatMatcher;
        int i = 0;
        while(preRepeatMatcher != null) {
            preRepeatMatcher.count = backPoint.preCounts.get(i++);
            preRepeatMatcher = preRepeatMatcher.preRepeatMatcher;
        }
        return backPoint;
    }
}
