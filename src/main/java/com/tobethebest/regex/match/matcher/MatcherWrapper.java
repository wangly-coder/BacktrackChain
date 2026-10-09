package com.tobethebest.regex.match.matcher;


import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.Pointer;

public class MatcherWrapper extends ChainMatcher {
    public Matcher matcher;
    public boolean matchEmptyString;

    public MatcherWrapper(Matcher matcher) {
        this.matcher = matcher;
        this.matchEmptyString = matcher.isMatchEmptyString();
    }

    public static MatcherWrapper wrap(Matcher matcher){
        return new MatcherWrapper(matcher);
    }

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return this.matcher.accept(matcherVisitor,context);
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
        if(pointer.index == str.length()) return this.matchEmptyString;
        return this.matcher.match(str, pointer, backContext);
    }
}
