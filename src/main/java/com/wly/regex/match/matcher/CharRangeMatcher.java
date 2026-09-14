package com.wly.regex.match.matcher;

import com.wly.regex.ast.exp.CharRangeExp;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.Pointer;

public class CharRangeMatcher implements Matcher{
    public char left;
    public char right;

    public CharRangeMatcher(char left, char right) {
        this.left = left;
        this.right = right;
    }

    public CharRangeMatcher(CharRangeExp charRangeExp) {
        this.left = charRangeExp.left.charValue;
        this.right = charRangeExp.right.charValue;
    }

    @Override
    public String toString() {
        return String.format("CharRangeMatcher:%s-%s",this.left,this.right);
    }

    @Override
    public String printSelf() {
        return String.format("[CharRange:%s-%s]", this.left, this.right);
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext backContext) {
        char curChar = str.charAt(pointer.index);
        if(curChar >= this.left && curChar <= this.right){
            pointer.index++;
            return true;
        }
        return false;
    }
}
