package com.wly.regex.match;

import java.util.List;

public class RepeatMatcher implements Matcher{
    public MatcherWrapper repeatChainHead;
    public int min;
    public int max;

    public RepeatMatcher(MatcherWrapper repeatChainHead, int min, int max) {
        this.repeatChainHead = repeatChainHead;
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return String.format("[RepeatCount:{min:%d,max:%d},RepeatChain:{%s}]",this.min,this.max,MatcherChainPrinter.printChain(this.repeatChainHead));
    }
}
