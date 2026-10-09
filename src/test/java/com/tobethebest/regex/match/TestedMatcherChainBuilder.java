package com.tobethebest.regex.match;

import com.tobethebest.regex.ast.TestedRegexParser;
import com.tobethebest.regex.ast.exp.*;
import com.tobethebest.regex.match.matcher.ChainMatcher;
import com.tobethebest.regex.match.matcher.MatcherWrapper;
import com.tobethebest.regex.match.matcher.RepeatMatcher;

import java.util.HashMap;

public class TestedMatcherChainBuilder extends MatcherChainBuilder{

    public static int unionNumber; // 联合数量
    public static int repeatNumber; // 重复数量
    public static int lookaroundNumber; // 环视数量

    public static HashMap<ChainMatcher,Integer> matcherToIdMap = new HashMap<>();
    public static HashMap<ChainMatcher, String> matcherToProtoStrMap = new HashMap<>();
    public static HashMap<String,ChainMatcher> nameToMatcherMap = new HashMap<>();

    public static void setMatcherExtraInfo(int id,RegexExp regexExp, ChainMatcher chainMatcher){
        String protoStr = TestedRegexParser.regexExpToProtoStrMap.get(regexExp);
        matcherToIdMap.put(chainMatcher,id);
        matcherToProtoStrMap.put(chainMatcher,protoStr);
        if(!(chainMatcher instanceof MatcherWrapper)){
            String name = String.format("%d-%s",id,protoStr);
            nameToMatcherMap.put(name,chainMatcher);
        }
    }

    public static void clear(){
        unionNumber = 0;
        repeatNumber = 0;
        lookaroundNumber = 0;
        matcherToIdMap.clear();
        matcherToProtoStrMap.clear();
        nameToMatcherMap.clear();
    }

    private static final TestedMatcherChainBuilder INSTANCE = new TestedMatcherChainBuilder();
    public static ChainMatcher build(RegexExp regexExp){
        TestedMatcherChainBuilder.clear();
        return regexExp.accept(INSTANCE,new MatcherBuilderContext());
    }

    public static ChainMatcher build(RegexExp regexExp,TestedMatchContext testedMatchContext){
        TestedMatcherChainBuilder.clear();
        return regexExp.accept(INSTANCE,new MatcherBuilderContext(testedMatchContext));
    }

    @Override
    public ChainMatcher visit(UnionExp unionExp, MatcherBuilderContext context) {
        int unionNumber = ++TestedMatcherChainBuilder.unionNumber;
        ChainMatcher chainMatcher = super.visit(unionExp, context);
        TestedMatcherChainBuilder.setMatcherExtraInfo(unionNumber,unionExp,chainMatcher);
        return chainMatcher;
    }

    @Override
    public ChainMatcher visit(RepeatExp repeatExp, MatcherBuilderContext context) {
        int repeatNumber = ++TestedMatcherChainBuilder.repeatNumber;
        RepeatMatcher.RepeatStartMatcher repeatStartMatcher = (RepeatMatcher.RepeatStartMatcher) super.visit(repeatExp, context);
        TestedMatcherChainBuilder.setMatcherExtraInfo(repeatNumber,repeatExp,repeatStartMatcher);
        TestedMatcherChainBuilder.setMatcherExtraInfo(repeatNumber,repeatExp,repeatStartMatcher.repeatEndMatcher);
        return repeatStartMatcher;
    }

    @Override
    public ChainMatcher visit(CharCollectionExp charCollectionExp, MatcherBuilderContext context) {
        ChainMatcher chainMatcher = super.visit(charCollectionExp, context);
        TestedMatcherChainBuilder.setMatcherExtraInfo(-1,charCollectionExp,chainMatcher);
        return chainMatcher;
    }

//    @Override
//    public ChainMatcher visit(LookaroundExp lookaroundExp, MatcherBuilderContext context) {
//        int lookaroundNumber = ++TestedMatcherChainBuilder.lookaroundNumber;
//        LookaroundMatcher.LookaroundStartMatcher lookaroundStartMatcher = (LookaroundMatcher.LookaroundStartMatcher) super.visit(lookaroundExp, context);
//        TestedMatcherChainBuilder.setMatcherExtraInfo(lookaroundNumber,lookaroundExp,lookaroundStartMatcher);
//        TestedMatcherChainBuilder.setMatcherExtraInfo(lookaroundNumber,lookaroundExp,lookaroundStartMatcher.lookaroundEndMatcher);
//        return lookaroundStartMatcher;
//    }
}
