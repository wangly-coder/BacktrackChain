package com.wly.regex.match.control;

import com.wly.regex.match.matcher.ChainMatcher;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.Pointer;

/**
 * 限定符$的对应匹配器
 */
public class EndMatcher extends ChainMatcher {
    @Override
    public String toString() {
        return "[End:$]";
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext context) {
        // 检查指针是否在尾部位置
        return pointer.index == str.length();
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext context) {
        throw new RuntimeException("EndMatcher.doMatch()是无效的方法，不能调用");
    }
}
