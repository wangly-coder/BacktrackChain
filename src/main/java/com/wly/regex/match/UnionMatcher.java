package com.wly.regex.match;

import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.back.BackPoint;
import com.wly.regex.match.back.Backer;
import com.wly.regex.match.search.Pointer;

import java.util.ArrayList;
import java.util.List;

public class UnionMatcher extends ChainMatcher implements Backer {
    private final List<ChainMatcher> unionChainHeads; // 多个选择链
    public RepeatMatcher preRepeatMatcher; // 外层的RepeatMatcher
    boolean hasEmptyStringMatcher;

    public UnionMatcher() {
        this.unionChainHeads = new ArrayList<>();
    }

    @Override
    public void setNext(ChainMatcher chainMatcher) {
        this.next = chainMatcher;
        for (ChainMatcher headMatcher : this.unionChainHeads) {
            headMatcher.getChainLastMatcher().next = chainMatcher;
        }
    }

    public void addUnionChainHead(ChainMatcher chainMatcher) {
        // 剔除重复的空串匹配器，只需要一个即可
        if (chainMatcher instanceof MatcherWrapper) {
            MatcherWrapper wrapper = (MatcherWrapper) chainMatcher;
            // 只有空串的StringMatcher才匹配空串
            if (wrapper.matchEmptyString) {
                if (!this.hasEmptyStringMatcher) this.hasEmptyStringMatcher = true;
                else return;
            }
        }
        this.unionChainHeads.add(chainMatcher);
    }

    @Override
    public String toString() {
        return String.format("[Union:[Pre:%s,Chains:%s]]", this.preRepeatMatcher == null ? "null" : this.preRepeatMatcher.name
                , MatcherChainPrinter.printUnionChains(this.unionChainHeads));
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext context) {
        /*
            满足规则，A|B|C的匹配顺序为A->A回溯->B->B回溯->C->C回溯
        */
        boolean isSuccess = false;
        int preSize = context.size(),chainSize = this.unionChainHeads.size();
        int index = pointer.index;
        int i;
        for( i = 0; i < chainSize ; i++){
            ChainMatcher headMatcher = this.unionChainHeads.get(i);
            if(headMatcher.chainMatch(str, pointer, context,this.next)){
                isSuccess = true;
                break;
            }
            // 失败则检查回溯点是否增多，有的话退出，否则继续下一条路径
            if(context.size() > preSize) break;
        }
        // 存储剩余路径
        BackPoint backPoint;
        for(int j = chainSize-1; j > i ; j--){
            backPoint = new BackPoint(BackPoint.BPTYPE.UNION, this.unionChainHeads.get(j), index, this.preRepeatMatcher);
            context.store(backPoint);
        }
        return isSuccess;
    }

    @Override
    public boolean back(String str, Pointer pointer,  BackContext context,BackPoint backPoint) {
        throw new RuntimeException("UnionMatcher.back()是无效的方法，不能调用");
    }
}
