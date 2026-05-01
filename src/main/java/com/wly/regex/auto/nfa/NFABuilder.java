package com.wly.regex.auto.nfa;

import com.wly.regex.auto.State;
import com.wly.regex.auto.SubGraph;
import com.wly.regex.auto.edge.CharEdge;
import com.wly.regex.auto.edge.CharRangeEdge;
import com.wly.regex.auto.edge.EpsilonEdge;
import com.wly.regex.exp.*;
import com.wly.regex.util.ASTVisitor;
import com.wly.regex.util.CharRange;
import com.wly.regex.util.MetaUtil;

import java.util.ArrayList;
import java.util.List;

public class NFABuilder implements ASTVisitor<SubGraph, NFAContext> {

    private NFABuilder(){}
    public static NFABuilder INSTANCE = new NFABuilder();

    @Override
    public SubGraph visit(UnionExp unionExp, NFAContext context) {
        State startState = context.getNewState();
        // 获得左子图
        SubGraph leftGraph = unionExp.getLeft().accept(this,context);
        // 获得右子图
        SubGraph rightGraph = unionExp.getRight().accept(this,context);
        State endState = context.getNewState();
        // 连接左右子图
        startState.addEdge(leftGraph.startEpsilonEdge);
        startState.addEdge(rightGraph.startEpsilonEdge);
        leftGraph.end.addEdge(EpsilonEdge.builder().targetState(endState).build());
        rightGraph.end.addEdge(EpsilonEdge.builder().targetState(endState).build());
        // 构建新子图
        EpsilonEdge startEdge = EpsilonEdge.builder().targetState(startState).build();
        return SubGraph.builder().startEpsilonEdge(startEdge).start(startState).end(endState).build();
    }

    @Override
    public SubGraph visit(ConcatExp concatExp, NFAContext context) {
        // 获得左子图
        SubGraph leftGraph = concatExp.getLeft().accept(this,context);
        // 获得右子图
        SubGraph rightGraph = concatExp.getRight().accept(this,context);
        // 连接左右子图
        leftGraph.end.addEdge(rightGraph.startEpsilonEdge);
        // 返回新子图
        return SubGraph.builder().startEpsilonEdge(leftGraph.startEpsilonEdge).start(leftGraph.start)
                .end(rightGraph.end).build();
    }

    @Override
    public SubGraph visit(RepeatExp repeatExp, NFAContext context) {
        // 获得子图
        SubGraph subGraph = repeatExp.getCharCollectionExp().accept(this,context);
        // 拼接子图
        switch (repeatExp.getModifierType()){
            case QUESTION:
                // 开始状态空边连接结束状态
                EpsilonEdge forwardEdgeQuestion = EpsilonEdge.builder().targetState(subGraph.end).build();
                subGraph.start.addEdge(forwardEdgeQuestion);
                return subGraph;
            case STAR:
                // 开始状态空边连接结束状态
                EpsilonEdge forwardEdgeStar = EpsilonEdge.builder().targetState(subGraph.end).build();
                subGraph.start.addEdge(forwardEdgeStar);
                // 结束状态反向连接开始状态
                EpsilonEdge backEdgeStar = EpsilonEdge.builder().targetState(subGraph.start).build();
                subGraph.end.addEdge(backEdgeStar);
                return subGraph;
            case PLUS:
                // 将R+变为RR*的模式
                SubGraph subGraphPlus = repeatExp.getCharCollectionExp().accept(this,context);
                // 开始状态空边连接结束状态
                EpsilonEdge forwardEdgePlus = EpsilonEdge.builder().targetState(subGraphPlus.end).build();
                subGraphPlus.start.addEdge(forwardEdgePlus);
                // 结束状态反向连接开始状态
                EpsilonEdge backEdgePlus = EpsilonEdge.builder().targetState(subGraphPlus.start).build();
                subGraphPlus.end.addEdge(backEdgePlus);
                // 连接R和R*子图
                subGraph.end.addEdge(subGraphPlus.startEpsilonEdge);
                return SubGraph.builder().start(subGraph.start).startEpsilonEdge(subGraph.startEpsilonEdge)
                        .end(subGraphPlus.end).build();
            case RANGE:
                int min = repeatExp.getMin();
                int max = repeatExp.getMax();
                // 三种情况都需要构建最小子图,这里先构建，后续再分类讨论
                for(int i=1;i<min;i++){
                    SubGraph subGraphRange = repeatExp.getCharCollectionExp().accept(this,context);
                    // 连接子图
                    subGraph.end.addEdge(subGraphRange.startEpsilonEdge);
                    // 将子图更新为大子图
                    subGraph.end = subGraphRange.end;
                }
                // {min,min}如果是固定数量则直接返回
                if(min == max) return subGraph;
                // {min,} 如果是最小数量则构造RR*模式
                if(max == -1){
                    SubGraph subGraphRangeStar = repeatExp.getCharCollectionExp().accept(this,context);
                    // 开始状态空边连接结束状态
                    EpsilonEdge forwardEdgeRangeStar = EpsilonEdge.builder().targetState(subGraphRangeStar.end).build();
                    subGraphRangeStar.start.addEdge(forwardEdgeRangeStar);
                    // 结束状态反向连接开始状态
                    EpsilonEdge backEdgeRangeStar = EpsilonEdge.builder().targetState(subGraphRangeStar.start).build();
                    subGraphRangeStar.end.addEdge(backEdgeRangeStar);
                    // 连接R和R*子图
                    subGraph.end.addEdge(subGraphRangeStar.startEpsilonEdge);
                    // 更新为大子图
                    subGraph.end = subGraphRangeStar.end;
                    return subGraph;
                }
                // TODO 如果量词给的很大，那么不可能通过下述的无限构造子图连接方式处理，需要一种更好的处理机制
                // {min,max} 有最小值和最大值，通过重复构造R?表示多余的子图
                // 如果min为0，则已经构建了一次
                int count = max - min;
                if(min == 0) {
                    // 之前的subGraph也需要变为R?模式
                    subGraph.start.addEdge(EpsilonEdge.of(subGraph.end));
                    count--;
                }
                for(int i=0;i<count;i++){
                    SubGraph subGraphRangeQuestion = repeatExp.getCharCollectionExp().accept(this,context);
                    // 开始状态连接直达结束状态
                    EpsilonEdge forwardEdgeRangeQuestion = EpsilonEdge.builder().targetState(subGraphRangeQuestion.end).build();
                    subGraphRangeQuestion.start.addEdge(forwardEdgeRangeQuestion);
                    // 拼接子图
                    subGraph.end.addEdge(subGraphRangeQuestion.startEpsilonEdge);
                    // 更新为大子图
                    subGraph.end = subGraphRangeQuestion.end;
                }
                return subGraph;
            default:
                // 不可能出现的情况，用于满足函数返回值通过编译
                throw new RuntimeException("不应该出现的情况：解析的RepeatExp类型不存在");
        }
    }

    @Override
    public SubGraph visit(CharCollectionExp charCollectionExp, NFAContext context) {
    /*
         在集合表达式中，正则表达式表现的语义为或
         拼接子图的过程难度在于如何转换元字符如\d这样的语言，且需要考虑重复和取反的可能
         因此这里需要将各个基础表达式抽象为区间模型， 进行区间的合并和取反
    */
        // 将表达式集合转化为区间
        List<CharRange> unMergedRanges = new ArrayList<>();
        charCollectionExp.getCharSequenceExp().forEach(subExp -> {
            List<CharRange> subRanges = CharRange.regexExpToCharRange(subExp);
            unMergedRanges.addAll(subRanges);
        });
        // 对区间进行合并或者取反
        List<CharRange> result = charCollectionExp.isNegative() ?
                CharRange.negativeMulti(unMergedRanges) : CharRange.mergeMulti(unMergedRanges);
        // 将区间集合转化为子图
        return SubGraph.buildFromOrRanges(result,this,context);
    }

    @Override
    public SubGraph visit(CharRangeExp charRangeExp, NFAContext context) {
        State start = context.getNewState();
        State end = context.getNewState();
        start.addEdge(CharRangeEdge.of(charRangeExp.getLeft().getCharValue(),
                charRangeExp.getRight().getCharValue(),end));
        return SubGraph.of(start,end);
    }

    @Override
    public SubGraph visit(MetaExp metaExp, NFAContext context) {
        List<CharRange> charRanges = MetaUtil.MetaToCharRangeMap.get(metaExp.getMetaValue());
        // 将区间集合转化为子图
        return SubGraph.buildFromOrRanges(charRanges,this,context);
    }

    @Override
    public SubGraph visit(CharExp charExp, NFAContext context) {
        State start = context.getNewState();
        State end = context.getNewState();
        start.addEdge(CharEdge.of(charExp.getCharValue(),end));
        return SubGraph.of(start,end);
    }

    public NFA build(RegexExp regexExp){
        NFAContext nfaContext = new NFAContext();
        SubGraph subGraph = regexExp.accept(this,nfaContext);
        subGraph.startEpsilonEdge = null;
        return new NFA(subGraph.start,subGraph.end,nfaContext);
    }
}
