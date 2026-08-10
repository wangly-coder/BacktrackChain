package com.wly.regex.match;

import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.search.Pointer;

public abstract class ChainMatcher implements Matcher{
    public ChainMatcher next;
    public Boolean matchEmptyString = false;
    public abstract boolean doMatch(String str, Pointer pointer, BackContext context);

    public void setNext(ChainMatcher next){
        this.next = next;
    }

    @Override
    public boolean isMatchEmptyString() {
        return this.matchEmptyString;
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext context) {
        // 检查是否越界
        if(pointer.index > str.length()) return false;
        return this.doMatch(str, pointer, context);
    }

    /**
     * 从当前matcher开始链式匹配调用
     */
    public boolean chainMatch(String str, Pointer pointer, BackContext context){
        ChainMatcher first = this;
        while(first != null && first.match(str, pointer, context)) first = first.next;
        return first == null;
    }

    /**
     *从当前matcher开始链式匹配调用直到遇到指定Matcher停止
     */
    public boolean chainMatch(String str, Pointer pointer, BackContext context,ChainMatcher stopMatcher){
        ChainMatcher first = this;
        while (first.match(str, pointer, context)) {
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
