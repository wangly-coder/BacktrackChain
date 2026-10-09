package com.tobethebest.regex.match;

import com.tobethebest.regex.match.assertion.BoundaryMatcher;
import com.tobethebest.regex.match.assertion.EndPosMatcher;
import com.tobethebest.regex.match.assertion.LookaroundMatcher;
import com.tobethebest.regex.match.assertion.StartPosMatcher;
import com.tobethebest.regex.match.group.GroupMatcher;
import com.tobethebest.regex.match.group.GroupRefMatcher;
import com.tobethebest.regex.match.matcher.*;

import java.util.List;

/**
 * 匹配器链打印器
 */
public class MatcherChainPrinter implements MatcherVisitor<String,Void>{
    public static final String ARROW_DELIM = " -> ";
    public static final String COLLECT_DELIM = ",";
    public static final String UNION_DELIM = " / ";

    public static final MatcherChainPrinter INSTANCE = new MatcherChainPrinter();

    public static String printChain(ChainMatcher head, String delim){
        String delimiter = delim == null ? ARROW_DELIM : delim;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(head.accept(INSTANCE,null));
        while(head.next != null) {
            head = head.next;
            stringBuilder.append(delimiter).append(head.accept(INSTANCE,null));
        }
        return stringBuilder.toString();
    }

    public static String printChain(ChainMatcher head){
        return MatcherChainPrinter.printChain(head, ARROW_DELIM);
    }

    public static String printCollection(List<Matcher> collection){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for(Matcher matcher : collection) stringBuilder.append(matcher.accept(INSTANCE,null)).append(COLLECT_DELIM);
        stringBuilder.setCharAt(stringBuilder.length() - 1, ']');
        return stringBuilder.toString();
    }

    public static String printUnionChains(List<ChainMatcher> unionChains){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for(ChainMatcher chain : unionChains) stringBuilder.append(MatcherChainPrinter.printChain(chain)).append(UNION_DELIM);
        stringBuilder.setLength(stringBuilder.length()-UNION_DELIM.length());
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public String visit(UnionMatcher unionMatcher, Void context) {
        RepeatMatcher preRepeatMatcher = unionMatcher.preRepeatMatcher;
        Integer preId = TestedMatcherChainBuilder.matcherToIdMap.get(preRepeatMatcher);
        return String.format("[Union:[Pre:%s,Chains:%s]]"
                , preId == null ? "null" : preId,MatcherChainPrinter.printUnionChains(unionMatcher.unionChainHeads));
    }

    @Override
    public String visit(RepeatMatcher.RepeatStartMatcher repeatStartMatcher, Void context) {
        Integer selfId = TestedMatcherChainBuilder.matcherToIdMap.get(repeatStartMatcher);
        String protoStr = TestedMatcherChainBuilder.matcherToProtoStrMap.get(repeatStartMatcher);
        String name = String.format("%d-%s",selfId,protoStr);
        RepeatMatcher preRepeatMatcher = repeatStartMatcher.repeatEndMatcher.preRepeatMatcher;
        Integer preId = TestedMatcherChainBuilder.matcherToIdMap.get(preRepeatMatcher);
        return String.format("[RS:[Name:%s,Pre:%s]]",name,preId == null ? "null" : preId);
    }

    @Override
    public String visit(RepeatMatcher.RepeatEndMatcher repeatEndMatcher, Void context) {
        int selfId = TestedMatcherChainBuilder.matcherToIdMap.get(repeatEndMatcher);
        return String.format("[RE:%s]",selfId);
    }

    @Override
    public String visit(CollectionMatcher collectionMatcher, Void context) {
        return String.format("[Collection:%s]",MatcherChainPrinter.printCollection(collectionMatcher.collection));
    }

    @Override
    public String visit(CharRangeMatcher charRangeMatcher, Void context) {
        return String.format("[CharRange:%s-%s]",charRangeMatcher.left, charRangeMatcher.right);
    }

    @Override
    public String visit(MetaMatcher metaMatcher, Void context) {
        return String.format("[Meta:%s]", metaMatcher.metaValue);
    }

    @Override
    public String visit(StringMatcher stringMatcher, Void context) {
        return String.format("[String:%s]",stringMatcher.string);
    }

    @Override
    public String visit(StartPosMatcher startPosMatcher, Void context) {
        return "[StartPos:^]";
    }

    @Override
    public String visit(EndPosMatcher endPosMatcher, Void context) {
        return "[EndPos:$]";
    }

    @Override
    public String visit(GroupMatcher.GroupStartMatcher groupStartMatcher, Void context) {
        return String.format("[GS:%d]",groupStartMatcher.groupEndMatcher.groupId);
    }

    @Override
    public String visit(GroupMatcher.GroupEndMatcher groupEndMatcher, Void context) {
        return String.format("[GE:%d]",groupEndMatcher.groupId);
    }

    @Override
    public String visit(GroupRefMatcher groupRefMatcher, Void context) {
        return String.format("[GRef:%d]",groupRefMatcher.refId);
    }

//    @Override
//    public String visit(LookaroundMatcher.LookaroundStartMatcher lookaroundStartMatcher, Void context) {
//        Integer selfId = TestedMatcherChainBuilder.matcherToIdMap.get(lookaroundStartMatcher);
//        String typeStr = LookaroundTest.getTypeStr(lookaroundStartMatcher.lookaroundType);
//        return String.format("[LS:[Type:%s,Id:%d]]",typeStr,selfId);
//    }
//
//    @Override
//    public String visit(LookaroundMatcher.LookaroundEndMatcher lookaroundEndMatcher, Void context) {
//        Integer selfId = TestedMatcherChainBuilder.matcherToIdMap.get(lookaroundEndMatcher);
//        return String.format("[LE:%d]",selfId);
//    }

    @Override
    public String visit(LookaroundMatcher lookaroundMatcher, Void context) {
        String typeStr = AssertionTest.getLookTypeStr(lookaroundMatcher.lookaroundType);
        String chainStr = MatcherChainPrinter.printChain(lookaroundMatcher.lookaroundChainHead);
        return String.format("[LM:[Type:%s,Chains:{%s}]]",typeStr,chainStr);
    }

    @Override
    public String visit(BoundaryMatcher boundaryMatcher, Void context) {
        String boundaryStr = boundaryMatcher.isMatchBoundary ? "\\b":"\\B";
        return String.format("[Boundary:%s]",boundaryStr);
    }
}
