package com.wly.regex.auto;

import com.wly.regex.auto.builder.NFABuilder;
import com.wly.regex.auto.edge.EpsilonEdge;
import com.wly.regex.exp.RegexExp;
import com.wly.regex.util.CharRange;
import lombok.Builder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 子图，状态机Builder中访问AST的结果产物，作为返回值并在后续连接
 */
@Builder
public class SubGraph {
    public State start;
    public State end;
    public EpsilonEdge startEpsilonEdge; // 连接子图开始节点的空边

    public static SubGraph of(){
        State start = State.getNewState();
        EpsilonEdge startEpsilonEdge = EpsilonEdge.builder().targetState(start).build();
        return new SubGraph(start,State.getNewState(),startEpsilonEdge);
    }

    public static SubGraph of(State start,State end){
        EpsilonEdge startEpsilonEdge = EpsilonEdge.builder().targetState(start).build();
        return new SubGraph(start,end,startEpsilonEdge);
    }

    public static SubGraph buildFromOrRanges(List<CharRange> ranges, NFABuilder builder,Void context){
        // 转化为RegexExp集合
        List<RegexExp> regexExps = CharRange.rangeToRegexExp(ranges);
        // 拼接子图
        State start = State.getNewState();
        State[] end = new State[1];
        regexExps.stream().map(regexExp -> regexExp.accept(builder,context))
                .collect(Collectors.toList())
                .forEach(
                 subGraph -> {
                     if(end[0] == null) end[0] = State.getNewState();
                     start.addEdge(subGraph.startEpsilonEdge);
                     subGraph.end.addEdge(EpsilonEdge.of(end[0]));
                 }
        );
        return SubGraph.of(start,end[0]);
    }
}
