package com.wly.regex.match.matcher;

import com.wly.regex.match.MatcherChainPrinter;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.Pointer;

import java.util.List;

public class CollectionMatcher implements Matcher{
    public List<Matcher> collection;

    public CollectionMatcher(List<Matcher> collection) {
        this.collection = collection;
    }

    @Override
    public String toString() {
        return String.format("[Collection:%s]", MatcherChainPrinter.printCollection(this.collection));
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext context) {
        for(Matcher matcher:collection){
            if(matcher.match(str,pointer,context)){
                return true;
            }
        }
        return false;
    }
}
