package com.wly.regex.match;


public class MatcherWrapper implements Matcher {
    public MatcherWrapper next;
    public Matcher matcher;

    public MatcherWrapper() {
    }

    public MatcherWrapper(Matcher matcher) {
        this.matcher = matcher;
    }

    public void setNext(MatcherWrapper next){
        this.next = next;
        // 针对于UnionMatcher的后置处理
        if(this.matcher instanceof UnionMatcher) ((UnionMatcher)this.matcher).postProcess(this.next);
    }

    public static MatcherWrapper wrap(Matcher matcher){
        return new MatcherWrapper(matcher);
    }

    @Override
    public String toString() {
        return this.matcher.toString();
    }
}
