package com.wly.regex.match.matcher;

import com.wly.regex.match.MatcherChainPrinter;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.Pointer;

import java.util.List;

public class CollectionMatcher implements Matcher{
    public List<Matcher> collection;
    public String name;

    public CollectionMatcher(List<Matcher> collection) {
        this.collection = collection;
    }

    public void setName(String protoString){
        this.name = "CollectionMatcher-" + protoString;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String printSelf() {
        return String.format("[Collection:%s]", MatcherChainPrinter.printCollection(this.collection));
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext backContext) {
        for(Matcher matcher:collection){
            if(matcher.match(str,pointer,backContext)){
                return true;
            }
        }
        return false;
    }
}
