package com.wly.regex;

import com.wly.regex.match.ChainMatcher;
import com.wly.regex.match.Pointer;
import com.wly.regex.match.RepeatMatcher;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.back.BackPoint;

public class RegexMatcher {
    public ChainMatcher headMatcher;

    public RegexMatcher(ChainMatcher headMatcher) {
        this.headMatcher = headMatcher;
    }

    /**
     * 判断给定字符串是否整个符合正则表达式
     * @param str 判断的目标字符串
     * @return 是否完全匹配
     */
    public boolean matchAll(String str){
        Pointer pointer = new Pointer();
        BackContext context = new BackContext();
        // TODO RM是非贪婪匹配的话，可能提前结束str的匹配过程，此时需要继续回溯匹配
        if(this.match(str,pointer,context)) return pointer.index == str.length();
        return false;
    }

    public boolean match(String str,Pointer pointer,BackContext context) {
        // 匹配链开始匹配
        if(this.headMatcher.chainMatch(str,pointer,context)) return true;
        // 失败则回溯
        BackPoint backPoint = context.restore();
        while(backPoint != null) {
            // 恢复指针位置
            pointer.index = backPoint.index;
            /*
                根据不同类型进行不同处理
                始终贯穿回溯在外部，外部处理器在外部处理的原则
            */
            if(backPoint.bptype == BackPoint.BPTYPE.UNION){
                ChainMatcher headMatcher = backPoint.nextMatcher;
                // 走完该UM的这条路径，失败则继续回溯
                if(!headMatcher.chainMatch(str, pointer, context)) {
                    backPoint = context.restore();
                    continue;
                }
                // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯处理
                if(this.handleBackRepeatNest(str,pointer,context,backPoint)) return true;
                backPoint = context.restore();
            }
            // backPoint.bptype == BackPoint.BPTYPE.REPEAT
            else{
                // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯处理
                if(this.handleBackRepeatNest(str,pointer,context,backPoint)) return true;
                backPoint = context.restore();
            }
        }
        return false;
    }

    // 处理回溯时的RM嵌套
    private boolean handleBackRepeatNest(String str,Pointer pointer,BackContext context,BackPoint backPoint){
        // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯
        RepeatMatcher preRepeatMatcher = backPoint.preRepeatMatcher;
        // 如果处理的是最外层的UM，其外部匹配链已经处理好了，无需再匹配
        if(preRepeatMatcher == null && backPoint.bptype == BackPoint.BPTYPE.UNION) return true;
        if(preRepeatMatcher != null) {
            // 如果回溯处理失败，那么继续回溯
            if(!preRepeatMatcher.back(str,pointer,backPoint,context)) return false;
        }
        // 达到了是最外部的RM了，直接拿取外部匹配链
        ChainMatcher nextMatcher = backPoint.nextMatcher;
        if(nextMatcher == null) return true;
        // 匹配成功则返回，否则继续回溯
        return nextMatcher.chainMatch(str, pointer, context);
    }
}
