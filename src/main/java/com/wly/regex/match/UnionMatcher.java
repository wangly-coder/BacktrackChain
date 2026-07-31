package com.wly.regex.match;

import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.back.BackPoint;
import com.wly.regex.match.back.Backer;

import java.util.ArrayList;
import java.util.List;

public class UnionMatcher extends ChainMatcher implements Backer {
    private List<ChainMatcher> unionChainHeads; // 多个选择链
    public RepeatMatcher preRepeatMatcher; // 外层的RepeatMatcher

    public UnionMatcher(){
        this.unionChainHeads = new ArrayList<>();
    }

    @Override
    public void setNext(ChainMatcher chainMatcher){
        this.next = chainMatcher;
        for(ChainMatcher headMatcher:this.unionChainHeads){
            headMatcher.getChainLastMatcher().next = chainMatcher;
        }
    }

    public void addUnionChainHead(ChainMatcher chainMatcher){
        this.unionChainHeads.add(chainMatcher);
        // 判断是否匹配空串
        if(!this.matchEmptyString) this.matchEmptyString = chainMatcher.chainMatchEmptyString();
    }

    @Override
    public String toString() {
        return String.format("[Union:[Pre:%s,Chains:%s]]",this.preRepeatMatcher == null ? "null":this.preRepeatMatcher.name
                ,MatcherChainPrinter.printUnionChains(this.unionChainHeads));
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext context) {
        // 核心是选一路走，其他三路作为回溯点保存
        // 先将剩下的路径保存
        for(int i=1;i<this.unionChainHeads.size();i++) {
            // 创建BackPoint
            BackPoint backPoint = new BackPoint(BackPoint.BPTYPE.UNION,this.unionChainHeads.get(i),pointer.index,this.preRepeatMatcher);
            // 保存到回溯栈中
            context.store(backPoint);
        }
        // 走第一条路
        ChainMatcher first = this.unionChainHeads.get(0);
        while(first != null && first.match(str, pointer, context)) {
            first = first.next;
            // 这里要做截断处理，只处理自己的部分，不能处理UM的next后续匹配链
            if(first == this.next) return true;
        }
        return false;
    }

    @Override
    public boolean back(String str, Pointer pointer, BackPoint backPoint, BackContext context) {
        throw new RuntimeException("UnionMatcher.back()是无效的方法！");
    }
}
