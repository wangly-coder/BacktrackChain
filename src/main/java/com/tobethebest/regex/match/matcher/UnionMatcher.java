package com.tobethebest.regex.match.matcher;

import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.back.BackPoint;
import com.tobethebest.regex.match.Pointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UnionMatcher extends ChainMatcher {
    public List<ChainMatcher> unionChainHeads; // 多个选择链
    public List<List<Integer>> unionChainGroupIdList; // 每个不同分支的内部组id集合
    public List<Integer> unionChainMaxGroupIdList; // 每个不同分支的内部组id最大值集合
    public RepeatMatcher preRepeatMatcher; // 外层的RepeatMatcher

    public UnionMatcher() {
        this.unionChainHeads = new ArrayList<>();
        this.unionChainGroupIdList = new ArrayList<>();
        this.unionChainMaxGroupIdList = new ArrayList<>();
    }

    public void setNext(ChainMatcher chainMatcher) {
        this.next = chainMatcher;
        for (ChainMatcher headMatcher : this.unionChainHeads) {
            headMatcher.getChainLastMatcher().next = chainMatcher;
        }
    }

    // 添加分支，同时将groupId等信息写入
    public void addUnionChainHead(ChainMatcher chainMatcher,List<Integer> groupIds) {
        this.unionChainHeads.add(chainMatcher);
        // 如果为空内部没有组
        this.unionChainGroupIdList.add(groupIds);
        this.unionChainMaxGroupIdList.add(groupIds.stream().max(Integer::compareTo).orElse(0));
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
        /*
            满足规则，A|B|C的匹配顺序为A->A回溯->B->B回溯->C->C回溯
        */
        boolean isSuccess = false;
        int preContextSize = backContext.backStack.size(),unionChainSize = this.unionChainHeads.size();
        int index = pointer.index;
        Stack<BackPoint> popedBackPointStack = new Stack<>();
        int i;
        for( i = 0; i < unionChainSize ; i++){
            ChainMatcher headMatcher = this.unionChainHeads.get(i);
            if(headMatcher.chainMatch(str, pointer, backContext,this.next)) isSuccess = true;
            // 不管是否成功都要检查回溯点是否增多，有的话退出，否则继续下一条路径
            int stackSize = backContext.backStack.size();
            if(stackSize > preContextSize) {
                // 将新增的BP取出，用于保证UM的选择和回溯顺序
                for(int popTimes =0;popTimes<stackSize-preContextSize;popTimes++)
                    popedBackPointStack.push(backContext.backStack.pop());
                break;
            }
            // 如果没有新增但成功了同样退出
            else if(isSuccess) break;
            // 继续下一条路径前需要将上一条路径的组状态清空
            else backContext.resetGroupPairs(this.unionChainGroupIdList.get(i));
        }
        // 存储剩余路径，基于栈的倒序遍历
        BackPoint backPoint;
        for(int j = unionChainSize-1; j > i ; j--){
            backPoint = new BackPoint(index,this.unionChainHeads.get(j),this.preRepeatMatcher);
            // 定义backFunc，有意思的处理函数
            int finalJ = j;
            backPoint.backFunc = () -> {
                // 清空上一条路径的组状态
                backContext.resetGroupPairs(unionChainGroupIdList.get(finalJ -1));
                // 回溯时需要清空比当前分支最大组id还大的所有组状态
                backContext.resetGroupParis(this.unionChainMaxGroupIdList.get(finalJ));
            };
            backContext.store(backPoint);
        }
        // 将被选择的路径中被移除的BP归位
        while(!popedBackPointStack.isEmpty()) backContext.store(popedBackPointStack.pop());
        return isSuccess;
    }

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return matcherVisitor.visit(this,context);
    }
}
