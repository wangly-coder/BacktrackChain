package com.wly.regex.match;

import com.wly.regex.match.back.BackContext;

public abstract class ChainMatcher implements Matcher{
    public ChainMatcher next;
    public Boolean matchEmptyString = false;
    public abstract boolean doMatch(String str, Pointer pointer, BackContext context);

    public void setNext(ChainMatcher next){
        this.next = next;
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext context) {
        // 检查是否越界
        if(pointer.index > str.length()) return false;
        if(pointer.index == str.length()) return this.matchEmptyString;
        return this.doMatch(str, pointer, context);
    }

    /**
     * 链式匹配调用，从当前matcher开始
     */
    public boolean chainMatch(String str, Pointer pointer, BackContext context){
        ChainMatcher first = this;
        while(first != null && first.match(str, pointer, context)) first = first.next;
        return first == null;
    }

    public boolean chainMatchEmptyString(){
        ChainMatcher first = this;
        while(first != null) {
            // 但凡有一个不匹配，那么整体匹配链不匹配
            if(!first.isMatchEmptyString()) return false;
            first = first.next;
        }
        return true;
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
