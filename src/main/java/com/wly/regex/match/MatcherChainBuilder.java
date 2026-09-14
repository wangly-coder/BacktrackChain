package com.wly.regex.match;

import com.wly.regex.ast.ASTVisitor;
import com.wly.regex.ast.exp.*;
import com.wly.regex.match.control.EndPosMatcher;
import com.wly.regex.match.control.StartPosMatcher;
import com.wly.regex.match.matcher.*;
import com.wly.regex.match.group.GroupMatcher;
import com.wly.regex.match.group.GroupRefMatcher;
import com.wly.regex.util.CharRange;

import java.util.ArrayList;
import java.util.List;

public class MatcherChainBuilder implements ASTVisitor<ChainMatcher, MatcherChainBuilder.MatcherBuilderContext> {

    protected MatcherChainBuilder(){}

    public final static MatcherChainBuilder INSTANCE = new MatcherChainBuilder();

    protected static class MatcherBuilderContext{
        public RepeatMatcher preRepeatMatcher;
        public int nestNumber;
        public int orderNumber;
        public List<List<Integer>> collectors; // 组id收集器
        public MatchContext matchContext;
        public MatcherBuilderContext(){
            this.collectors = new ArrayList<>();
        }

        public MatcherBuilderContext(MatchContext matchContext){
            this();
            this.matchContext = matchContext;
        }

        public void addCollector(){
            this.collectors.add(new ArrayList<>());
        }

        public List<Integer> removeCollector(){
            return this.collectors.remove(this.collectors.size() -1);
        }

        public void addGroupId(int groupId){
            for (List<Integer> collector : collectors) collector.add(groupId);
        }
    }

    public static ChainMatcher build(RegexExp regexExp){
        return regexExp.accept(INSTANCE,new MatcherBuilderContext());
    }

    public static ChainMatcher build(RegexExp regexExp,MatchContext matchContext){
        return regexExp.accept(INSTANCE,new MatcherBuilderContext(matchContext));
    }

    @Override
    public ChainMatcher visit(UnionExp unionExp, MatcherBuilderContext context) {
        UnionMatcher unionMatcher = new UnionMatcher();
        // 设置名字
        unionMatcher.setName(unionExp.protoString);
        RegexExp leftExp = unionExp.left;
        // 添加groupId收集器
        context.addCollector();
        ChainMatcher leftHead = leftExp.accept(this,context);
        // 使用完后移除
        unionMatcher.addUnionChainHead(leftHead,context.removeCollector());
        // 路径展开，右边路径进行分析，如果依然是union表达式，选择左边部分加入到上述的unionPath中
        RegexExp rightExp = unionExp.right;
        while(rightExp instanceof UnionExp){
            UnionExp rightUnionExp = (UnionExp) rightExp;
            // 添加groupId收集器
            context.addCollector();
            leftHead = rightUnionExp.left.accept(this,context);
            unionMatcher.addUnionChainHead(leftHead,context.removeCollector());
            rightExp = rightUnionExp.right;
        }
        // 将最后的最右边路径加入到unionPath中
        // 添加groupId收集器
        context.addCollector();
        ChainMatcher rightHead = rightExp.accept(this,context);
        unionMatcher.addUnionChainHead(rightHead,context.removeCollector());
        unionMatcher.preRepeatMatcher = context.preRepeatMatcher;
        return unionMatcher;
    }

    @Override
    public ChainMatcher visit(ConcatExp concatExp, MatcherBuilderContext context) {
        ChainMatcher head = null,cur = null,wrapper;
        // 合并普通字符
        StringBuilder stringBuilder = new StringBuilder();
        RegexExp leftExp,rightExp;
        while(concatExp != null){
            leftExp = concatExp.left;
            rightExp = concatExp.right;
            concatExp = null;
            // 左路径分析
            if(leftExp instanceof CharExp) stringBuilder.append(((CharExp) leftExp).charValue);
            else {
                // 如果还有缓存字符需要先构造StringMatcher
                if(stringBuilder.length() > 0) {
                    wrapper = MatcherWrapper.wrap(new StringMatcher(stringBuilder.toString()));
                    if(head == null) {
                        head = wrapper;
                        cur = head;
                    }
                    else {
                        cur.setNext(wrapper);
                        cur = wrapper;
                    }
                    // 清空缓冲区
                    stringBuilder.setLength(0);
                }
                // 加入左Matcher
                wrapper = leftExp.accept(this,context);
                if(head == null) {
                    head = wrapper;
                    cur = head;
                }
                else {
                    cur.setNext(wrapper);
                    cur = wrapper;
                }
            }
            // 右路径分析
            if(rightExp instanceof CharExp) {
                stringBuilder.append(((CharExp) rightExp).charValue);
                wrapper = MatcherWrapper.wrap(new StringMatcher(stringBuilder.toString()));
                // 到这之前都没有新的Matcher，那么到这里就结束了
                if(head == null) head = wrapper;
                else cur.setNext(wrapper);
            }
            // 如果还是ConcatExp则继续解析
            else if(rightExp instanceof ConcatExp) concatExp = (ConcatExp) rightExp;
            else {
                // 如果还有缓存字符需要先构造StringMatcher
                if(stringBuilder.length() > 0) {
                    wrapper = MatcherWrapper.wrap(new StringMatcher(stringBuilder.toString()));
                    if(head == null) {
                        head = wrapper;
                        cur = head;
                    }
                    else {
                        cur.setNext(wrapper);
                        cur = wrapper;
                    }
                }
                // 这里之前一定至少有一个Matcher了，直接更新右Matcher
                wrapper = rightExp.accept(this,context);
                cur.setNext(wrapper);
                cur = wrapper;
            }
        }
        return head;
    }

    @Override
    public ChainMatcher visit(RepeatExp repeatExp, MatcherBuilderContext context) {
        RepeatMatcher repeatMatcher = new RepeatMatcher();
        // 加入匹配上下文中
        if(context.matchContext != null) context.matchContext.addRepeatMatcher(repeatMatcher);
        // 设置子节点与父节点关系
        repeatMatcher.preRepeatMatcher = context.preRepeatMatcher;
        context.preRepeatMatcher = repeatMatcher;
        int nest = context.nestNumber++;
        int order = ++context.orderNumber;
        // 进入子树
        context.orderNumber = 0;
        // 添加groupId收集器
        context.addCollector();
        ChainMatcher repeatChainHead = repeatExp.charCollectionExp.accept(this,context);
        // 设置groupId集合
        repeatMatcher.setGroupIds(context.removeCollector());
        // 退出子树，恢复上下文
        context.orderNumber = order;
        context.nestNumber = nest;
        context.preRepeatMatcher = repeatMatcher.preRepeatMatcher;
        int min,max;
        switch (repeatExp.modifierType){
            case QUESTION: min = 0;max=1;break;
            case STAR: min =0;max=-1;break;
            case PLUS: min =1;max=-1;break;
            case RANGE: min = repeatExp.min;max=repeatExp.max;break;
            default:throw new RuntimeException("不应该出现的异常，无效的量词表达式类型："+repeatExp.max);
        }
        String name = String.format("Repeat-%d-%d",nest,order);
        repeatMatcher.repeatChainHead = repeatChainHead;
        repeatMatcher.setMin(min);
        repeatMatcher.max = max;
        repeatMatcher.setGreedy(repeatExp.greedy);
        repeatMatcher.name = name;
        return repeatMatcher;
    }

    @Override
    public ChainMatcher visit(CharCollectionExp charCollectionExp, MatcherBuilderContext context) {
        List<Matcher> collection = new ArrayList<>();
        // 进行区间合并获得合并后的表达式
        List<RegexExp> mergedRegexExps = CharRange.mergeRegexExps(charCollectionExp.charSequenceExp,charCollectionExp.negative);
        // 转化如Matcher加入集合中
        for(RegexExp regexExp : mergedRegexExps){
            collection.add(regexExp.accept(this,context));
        }
        CollectionMatcher collectionMatcher = new CollectionMatcher(collection);
        collectionMatcher.setName(charCollectionExp.protoString);
        return MatcherWrapper.wrap(collectionMatcher);
    }

    @Override
    public ChainMatcher visit(CharRangeExp charRangeExp, MatcherBuilderContext context) {
        return MatcherWrapper.wrap(new CharRangeMatcher(charRangeExp.left.charValue, charRangeExp.right.charValue));
    }

    @Override
    public ChainMatcher visit(MetaExp metaExp, MatcherBuilderContext context) {
        // 对限定符^$做处理
        if("^".equals(metaExp.metaValue)) return new StartPosMatcher();
        if("$".equals(metaExp.metaValue)) return new EndPosMatcher();
        // 如果是UnionExp里产生的空字符串MetaExp，那么返回StringMatcher
        if("".equals(metaExp.metaValue)) return MatcherWrapper.wrap(new StringMatcher(""));
        return MatcherWrapper.wrap(new MetaMatcher(metaExp.metaValue));
    }

    @Override
    public ChainMatcher visit(CharExp charExp, MatcherBuilderContext context) {
        return MatcherWrapper.wrap(new StringMatcher(String.valueOf(charExp.charValue)));
    }

    @Override
    public ChainMatcher visit(GroupExp groupExp, MatcherBuilderContext context) {
        // 添加组id，保证组id收集顺序是从小到大
        int groupId = groupExp.groupId;
        context.addGroupId(groupId);
        GroupMatcher.GroupStartMatcher groupStartMatcher = new GroupMatcher.GroupStartMatcher();
        ChainMatcher groupHead = groupExp.regexExp.accept(this,context);
        GroupMatcher.GroupEndMatcher groupEndMatcher = new GroupMatcher.GroupEndMatcher(groupId,groupStartMatcher);
        groupStartMatcher.groupEndMatcher = groupEndMatcher;
        // 收集GEM
        if(context.matchContext != null) context.matchContext.addGroupEndMatcher(groupEndMatcher);
        // 连接它们
        groupStartMatcher.next = groupHead;
        groupHead.getChainLastMatcher().setNext(groupEndMatcher);
        // 返回GSM
        return groupStartMatcher;
    }

    @Override
    public ChainMatcher visit(GroupRefExp groupRefExp, MatcherBuilderContext context) {
        return new GroupRefMatcher(groupRefExp.refId);
    }
}
