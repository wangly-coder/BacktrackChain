package com.wly.regex.match;

import java.util.ArrayList;
import java.util.List;

public class UnionMatcher implements Matcher{
    public List<MatcherWrapper> unionChains; // 多个选择链

    public UnionMatcher(){
        this.unionChains = new ArrayList<>();
    }

    public void postProcess(MatcherWrapper next){
        this.unionChains.forEach(head -> {
            while(head.next != null) head = head.next;
            head.next = next;
        });
    }

    @Override
    public String toString() {
        return String.format("[Union:%s]",MatcherChainPrinter.printUnionChains(this.unionChains));
    }
}
