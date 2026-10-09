package com.tobethebest.regex.match.matcher;

import com.tobethebest.regex.ast.exp.CharRangeExp;
import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.Pointer;

public class CharRangeMatcher implements Matcher{
    public char left;
    public char right;

    public CharRangeMatcher(char left, char right) {
        this.left = left;
        this.right = right;
    }

    public CharRangeMatcher(CharRangeExp charRangeExp) {
        this.left = charRangeExp.left;
        this.right = charRangeExp.right;
    }

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return matcherVisitor.visit(this,context);
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
