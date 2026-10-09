package com.tobethebest.regex.match.assertion;

import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.matcher.ChainMatcher;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.Pointer;

/**
 * 限定符$的对应匹配器
 */
public class EndPosMatcher extends ChainMatcher {

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return matcherVisitor.visit(this, context);
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext backContext) {
        // 检查指针是否在尾部位置
        return pointer.index == str.length();
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
        throw new RuntimeException("EndMatcher.doMatch()是无效的方法，不能调用");
    }
}
