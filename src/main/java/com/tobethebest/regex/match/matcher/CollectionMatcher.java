package com.tobethebest.regex.match.matcher;

import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.Pointer;

import java.util.List;

public class CollectionMatcher implements Matcher{
    public List<Matcher> collection;

    public CollectionMatcher(List<Matcher> collection) {
        this.collection = collection;
    }

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return matcherVisitor.visit(this,context);
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
