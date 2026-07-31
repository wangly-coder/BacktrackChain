package com.wly.regex.match;


import com.wly.regex.match.back.BackContext;

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
    public boolean doMatch(String str, Pointer pointer, BackContext context) {
        return this.matcher.match(str, pointer, context);
    }
}
