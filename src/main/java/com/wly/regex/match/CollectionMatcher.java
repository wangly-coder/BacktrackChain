package com.wly.regex.match;

import java.util.List;

public class CollectionMatcher implements Matcher{
    public List<Matcher> collection;

    public CollectionMatcher(List<Matcher> collection) {
        this.collection = collection;
    }

    @Override
    public String toString() {
        return String.format("[Collection:%s]",MatcherChainPrinter.printCollection(this.collection));
    }
}
