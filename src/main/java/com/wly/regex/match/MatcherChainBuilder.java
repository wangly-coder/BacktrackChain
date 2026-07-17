package com.wly.regex.match;

import com.wly.regex.ast.ASTVisitor;
import com.wly.regex.ast.exp.*;
import com.wly.regex.util.CharRange;

import java.util.ArrayList;
import java.util.List;

public class MatcherChainBuilder implements ASTVisitor<MatcherWrapper,Void> {

    private MatcherChainBuilder(){}

    public final static MatcherChainBuilder INSTANCE = new MatcherChainBuilder();

    @Override
    public MatcherWrapper visit(UnionExp unionExp, Void context) {
        UnionMatcher unionMatcher = new UnionMatcher();
        RegexExp leftExp = unionExp.getLeft();
        MatcherWrapper leftHead = leftExp.accept(this,context);
        unionMatcher.unionChains.add(leftHead);
        // 右边路径进行分析，如果依然是union表达式，选择左边部分加入到上述的unionPath中
        RegexExp rightExp = unionExp.getRight();
        while(rightExp instanceof UnionExp){
            leftExp = ((UnionExp) rightExp).getLeft();
            unionMatcher.unionChains.add(leftExp.accept(this,context));
            rightExp = ((UnionExp) rightExp).getRight();
        }
        // 将最后的最右边路径加入到unionPath中
        unionMatcher.unionChains.add(rightExp.accept(this,context));
        return MatcherWrapper.wrap(unionMatcher);
    }

    @Override
    public MatcherWrapper visit(ConcatExp concatExp, Void context) {
        MatcherWrapper head = null,cur = null,wrapper;
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
    public MatcherWrapper visit(RepeatExp repeatExp, Void context) {
        MatcherWrapper repeatChainHead = repeatExp.getCharCollectionExp().accept(this,context);
        int min,max;
        switch (repeatExp.getModifierType()){
            case QUESTION: min = 0;max=1;break;
            case STAR: min =0;max=-1;break;
            case PLUS: min =1;max=-1;break;
            case RANGE: min = repeatExp.getMin();max=repeatExp.getMax();break;
            default:throw new RuntimeException("不应该出现的异常，无效的量词表达式类型："+repeatExp.getModifierType());
        }
        return MatcherWrapper.wrap(new RepeatMatcher(repeatChainHead,min,max));
    }

    @Override
    public MatcherWrapper visit(CharCollectionExp charCollectionExp, Void context) {
        Matcher matcher;
        List<Matcher> collection = new ArrayList<>();
        // 进行区间合并获得合并后的表达式
        List<RegexExp> mergedRegexExps = CharRange.mergeRegexExps(charCollectionExp.getCharSequenceExp(),charCollectionExp.isNegative());
        // 合并普通字符
        StringBuilder stringBuilder = new StringBuilder();
        for(RegexExp regexExp : mergedRegexExps){
            if(regexExp instanceof CharExp) stringBuilder.append(((CharExp) regexExp).getCharValue());
            else if(regexExp instanceof CharRangeExp){
                // 将缓冲区的内容转化为StringMatcher
                if(stringBuilder.length() > 0) {
                    matcher = new StringMatcher(stringBuilder.toString());
                    collection.add(matcher);
                    // 清空缓冲区
                    stringBuilder.setLength(0);
                }
                // 加入CharRangeMatcher
                collection.add(new CharRangeMatcher((CharRangeExp) regexExp));
            }
            else throw new RuntimeException("CharCollectionExp中的表达式集合只能是CharExp或者CharRangeExp类型");
        }
        return MatcherWrapper.wrap(new CollectionMatcher(collection));
    }

    @Override
    public MatcherWrapper visit(CharRangeExp charRangeExp, Void context) {
        return MatcherWrapper.wrap(new CharRangeMatcher(charRangeExp.getLeft().getCharValue(), charRangeExp.getRight().getCharValue()));
    }

    @Override
    public MatcherWrapper visit(MetaExp metaExp, Void context) {
        return MatcherWrapper.wrap(new MetaMatcher(metaExp.getMetaValue()));
    }

    @Override
    public MatcherWrapper visit(CharExp charExp, Void context) {
        return MatcherWrapper.wrap(new StringMatcher(String.valueOf(charExp.getCharValue())));
    }
}
