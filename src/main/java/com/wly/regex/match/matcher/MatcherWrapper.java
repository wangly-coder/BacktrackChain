package com.wly.regex.match.matcher;


import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.Pointer;

public class MatcherWrapper extends ChainMatcher {
    public Matcher matcher;

    public MatcherWrapper(Matcher matcher) {
        this.matcher = matcher;
        super.matchEmptyString = matcher.isMatchEmptyString();
    }

    public static MatcherWrapper wrap(Matcher matcher){
        return new MatcherWrapper(matcher);
    }

    @Override
    public String toString() {
        return this.matcher.toString();
    }

    @Override
    public String printSelf() {
        return this.matcher.printSelf();
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
        if(pointer.index == str.length()) return this.matchEmptyString;
        return this.matcher.match(str, pointer, backContext);
    }
}
