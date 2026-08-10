package com.wly.regex.match.control;

import com.wly.regex.match.ChainMatcher;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.search.Pointer;

/**
 * 限定符^的对应匹配器
 */
public class StartMatcher extends ChainMatcher {
    @Override
    public String toString() {
        return "[Start:^]";
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext context) {
        // 检查指针是否在头部位置
        return pointer.index == 0;
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext context) {
        throw new RuntimeException("StartMatcher.doMatch()是无效的方法，不能调用");
    }
}
