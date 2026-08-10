package com.wly.regex.match;

import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.search.Pointer;

public interface Matcher {

    /**
     * 匹配器的匹配方法
     * @param str 匹配的目标字符串
     * @param pointer 移动的指针
     * @param context 回溯上下文
     * @return 是否匹配成功
     */
     boolean match(String str, Pointer pointer, BackContext context);

    /**
     * 是否匹配空字符串，子类根据自身情况重写
     */
    default boolean isMatchEmptyString(){return false;}
}
