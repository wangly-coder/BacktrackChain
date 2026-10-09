package com.tobethebest.regex.match.matcher;

import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.Pointer;

public class StringMatcher implements Matcher{
    public String string;

    public StringMatcher(String string){
        this.string = string;
    }

    @Override
    public boolean isMatchEmptyString() {
        return this.string.isEmpty();
    }

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return matcherVisitor.visit(this,context);
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext backContext) {
        if(this.string.isEmpty()) return true;
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
