package com.wly.regex.match;

import com.wly.regex.ast.exp.CharRangeExp;

public class CharRangeMatcher implements Matcher{
    public char left;
    public char right;

    public CharRangeMatcher(char left, char right) {
        this.left = left;
        this.right = right;
    }

    public CharRangeMatcher(CharRangeExp charRangeExp) {
        this.left = charRangeExp.getLeft().getCharValue();
        this.right = charRangeExp.getRight().getCharValue();
    }

    @Override
    public String toString() {
        return String.format("[CharRange:%s-%s]", this.left, this.right);
    }

}
