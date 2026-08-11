package com.wly.regex.match.search;

import com.wly.regex.RegexMatcher;
import com.wly.regex.match.Pointer;

public class SearchResult {
    private final RegexMatcher regexMatcher;
    private final Pointer pointer;
    private final String searchString;
    /*
     当前字符串已经没有任何匹配的内容了
     可能是模式串中有^$限定符或者已经遍历匹配了目标字符串的所有子串都找不到任何匹配的
     再下一次匹配已经没有任何意义了
     */
    private boolean isAlreadyOver;

    public SearchResult(RegexMatcher regexMatcher, String searchString,Pointer pointer,boolean isAlreadyOver) {
        this.regexMatcher = regexMatcher;
        this.searchString = searchString;
        this.pointer = pointer;
        this.isAlreadyOver = isAlreadyOver;
    }

    public String getMatchedString(){
        int preIndex = pointer.preIndex;
        int index = pointer.index;
        return index >= preIndex && index <= this.searchString.length() ? this.searchString.substring(preIndex,index) : null;
    }

    /**
     * 返回下一个搜索结果
     * @return 是否还有下一个搜索结果
     */
    public boolean next(){
        if(this.isAlreadyOver) return false;
        pointer.forward();
        // 找不到说明所有子串都匹配失败，后续不再需要寻找了
        if(!this.regexMatcher.match(this.searchString,this.pointer)) {
            this.isAlreadyOver = true;
            return false;
        }
        // 如果两个指针相同，那么则是空串。只能匹配一次
        if(this.pointer.index == this.pointer.preIndex){
            // 如果达到了末尾位置，那么匹配结束
            if(this.pointer.index == this.searchString.length()) this.isAlreadyOver = true;
            // 没有则都向前推进一步，防止多次空串且死循环
            else pointer.bothForward();
        }
        return true;
    }
}
