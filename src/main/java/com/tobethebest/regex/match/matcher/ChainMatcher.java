package com.tobethebest.regex.match.matcher;

import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.Pointer;
import lombok.Setter;

public abstract class ChainMatcher implements Matcher{
    @Setter
    public ChainMatcher next;
    public abstract boolean doMatch(String str, Pointer pointer, BackContext backContext);

    @Override
    public boolean match(String str, Pointer pointer, BackContext backContext) {
        // 检查是否越界
        if(pointer.index > str.length()) return false;
        return this.doMatch(str, pointer, backContext);
    }

    /**
     * 从当前matcher开始链式匹配调用
     */
    public boolean chainMatch(String str, Pointer pointer, BackContext backContext){
        ChainMatcher first = this;
        while(first != null && first.match(str, pointer, backContext)) first = first.next;
        return first == null;
    }

    /**
     *从当前matcher开始链式匹配调用直到遇到指定Matcher停止
     */
    public boolean chainMatch(String str, Pointer pointer, BackContext backContext,ChainMatcher stopMatcher){
        ChainMatcher first = this;
        if(first == stopMatcher) throw new RuntimeException("stopMatcher不能是当前matcher");
        while (first.match(str, pointer, backContext)) {
            first = first.next;
            // 这里要做截断处理，只处理自己的部分，不能处理UM的next后续匹配链
            if (first == stopMatcher) return true;
        }
        return false;
    }

    /**
     * 获取匹配链中最后一个Matcher
     * @return 匹配链中的最后一个Matcher
     */
    public ChainMatcher getChainLastMatcher(){
        ChainMatcher last = this;
        while(last.next != null) last = last.next;
        return last;
    }
}
