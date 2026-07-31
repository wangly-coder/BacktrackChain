package com.wly.regex.match;

import com.wly.regex.match.back.BackContext;

public class StringMatcher implements Matcher{
    public String string;

    public StringMatcher(String string){
        this.string = string;
    }

    @Override
    public String toString() {
        return String.format("[String:%s]",this.string);
    }

    @Override
    public boolean isMatchEmptyString() {
        return this.string.isEmpty();
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext context) {
        int length = this.string.length();
        for(int i=0;i<length;i++){
            // 检查是否越界
            if(pointer.index+i >= str.length()) return false;
            if(this.string.charAt(i) != str.charAt(pointer.index + i)) return false;
        }
        pointer.index += length;
        return true;
    }
}
