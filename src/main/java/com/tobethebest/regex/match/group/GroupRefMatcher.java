package com.tobethebest.regex.match.group;

import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.Pointer;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.matcher.ChainMatcher;

public class GroupRefMatcher extends ChainMatcher {
    public int refId;

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return matcherVisitor.visit(this,context);
    }

    public GroupRefMatcher(int refId) {
        this.refId = refId;
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
        GroupPair refGroupPair = backContext.getGroupPair(this.refId);
        if(refGroupPair == null) return false;
        if(refGroupPair.isEquals()) return true;
        // 比较字符串
        int startIndex = refGroupPair.startIndex;
        int endIndex = refGroupPair.endIndex;
        int i = 0;
        for(;i<endIndex-startIndex;i++){
            // 如果指针超过了可索引的范围，那么直接失败
            int pIndex = i + pointer.index;
            if(pIndex > str.length() - 1) return false;
            if(str.charAt(i+startIndex) != str.charAt(pIndex)) return false;
        }
        pointer.index += i;
        return true;
    }
}
