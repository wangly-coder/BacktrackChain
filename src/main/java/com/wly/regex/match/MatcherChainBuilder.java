package com.wly.regex.match;

import com.wly.regex.ast.ASTVisitor;
import com.wly.regex.ast.exp.*;
import com.wly.regex.match.control.EndMatcher;
import com.wly.regex.match.control.StartMatcher;
import com.wly.regex.util.CharRange;

import java.util.ArrayList;
import java.util.List;

public class MatcherChainBuilder implements ASTVisitor<ChainMatcher, MatcherChainBuilder.MatcherBuilderContext> {

    private MatcherChainBuilder(){}

    public final static MatcherChainBuilder INSTANCE = new MatcherChainBuilder();

    protected static class MatcherBuilderContext{
        public RepeatMatcher preRepeatMatcher;
        public int nestNumber;
        public int orderNumber;
    }
    
    public static ChainMatcher build(RegexExp regexExp){
        return regexExp.accept(INSTANCE,new MatcherBuilderContext());
    }

    @Override
    public ChainMatcher visit(UnionExp unionExp, MatcherBuilderContext context) {
        UnionMatcher unionMatcher = new UnionMatcher();
        RegexExp leftExp = unionExp.getLeft();
        ChainMatcher leftHead = leftExp.accept(this,context);
        unionMatcher.addUnionChainHead(leftHead);
        // 路径展开，右边路径进行分析，如果依然是union表达式，选择左边部分加入到上述的unionPath中
        RegexExp rightExp = unionExp.getRight();
        while(rightExp instanceof UnionExp){
            leftExp = ((UnionExp) rightExp).getLeft();
            unionMatcher.addUnionChainHead(leftExp.accept(this,context));
            rightExp = ((UnionExp) rightExp).getRight();
        }
        // 将最后的最右边路径加入到unionPath中
        unionMatcher.addUnionChainHead((rightExp.accept(this,context)));
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
            leftExp = concatExp.getLeft();
            rightExp = concatExp.getRight();
            concatExp = null;
            // 左路径分析
            if(leftExp instanceof CharExp) stringBuilder.append(((CharExp) leftExp).getCharValue());
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
                stringBuilder.append(((CharExp) rightExp).getCharValue());
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
        // 设置子节点与父节点关系
        repeatMatcher.preRepeatMatcher = context.preRepeatMatcher;
        context.preRepeatMatcher = repeatMatcher;
        int nest = context.nestNumber++;
        int order = ++context.orderNumber;
        // 进入子树
        context.orderNumber = 0;
        ChainMatcher repeatChainHead = repeatExp.getCharCollectionExp().accept(this,context);
        // 退出子树，恢复上下文
        context.orderNumber = order;
        context.nestNumber = nest;
        context.preRepeatMatcher = repeatMatcher.preRepeatMatcher;
        int min,max;
        switch (repeatExp.getModifierType()){
            case QUESTION: min = 0;max=1;break;
            case STAR: min =0;max=-1;break;
            case PLUS: min =1;max=-1;break;
            case RANGE: min = repeatExp.getMin();max=repeatExp.getMax();break;
            default:throw new RuntimeException("不应该出现的异常，无效的量词表达式类型："+repeatExp.getModifierType());
        }
        String name = String.format("Repeat-%d-%d",nest,order);
        repeatMatcher.repeatChainHead = repeatChainHead;
        repeatMatcher.setMin(min);
        repeatMatcher.max = max;
        repeatMatcher.setGreedy(repeatExp.isGreedy());
        repeatMatcher.name = name;
        return repeatMatcher;
    }

    @Override
    public ChainMatcher visit(CharCollectionExp charCollectionExp, MatcherBuilderContext context) {
        List<Matcher> collection = new ArrayList<>();
        // 进行区间合并获得合并后的表达式
        List<RegexExp> mergedRegexExps = CharRange.mergeRegexExps(charCollectionExp.getCharSequenceExp(),charCollectionExp.isNegative());
        // 转化如Matcher加入集合中
        for(RegexExp regexExp : mergedRegexExps){
            collection.add(regexExp.accept(this,context));
        }
        return MatcherWrapper.wrap(new CollectionMatcher(collection));
    }

    @Override
    public ChainMatcher visit(CharRangeExp charRangeExp, MatcherBuilderContext context) {
        return MatcherWrapper.wrap(new CharRangeMatcher(charRangeExp.getLeft().getCharValue(), charRangeExp.getRight().getCharValue()));
    }

    @Override
    public ChainMatcher visit(MetaExp metaExp, MatcherBuilderContext context) {
        // 对限定符^$做处理
        if("^".equals(metaExp.getMetaValue())) return new StartMatcher();
        if("$".equals(metaExp.getMetaValue())) return new EndMatcher();
        // 如果是UnionExp里产生的空字符串MetaExp，那么返回StringMatcher
        if("".equals(metaExp.getMetaValue())) return MatcherWrapper.wrap(new StringMatcher(""));
        return MatcherWrapper.wrap(new MetaMatcher(metaExp.getMetaValue()));
    }

    @Override
    public ChainMatcher visit(CharExp charExp, MatcherBuilderContext context) {
        return MatcherWrapper.wrap(new StringMatcher(String.valueOf(charExp.getCharValue())));
    }
}
