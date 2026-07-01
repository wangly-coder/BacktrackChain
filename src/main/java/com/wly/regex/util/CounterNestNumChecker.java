package com.wly.regex.util;

import com.wly.regex.exp.*;


public class CounterNestNumChecker implements ASTVisitor<Void,Void> {

    public static final int MAX_NEST_COUNTER_NUM = 2;
    public static final CounterNestNumChecker INSTANCE = new CounterNestNumChecker();

    @Override
    public Void visit(UnionExp unionExp, Void context) {
        RegexExp leftExp = unionExp.getLeft();
        leftExp.accept(this,context);
        int leftNum = leftExp.counterNestNum;
        RegexExp rightExp = unionExp.getRight();
        rightExp.accept(this,context);
        int rightNum = rightExp.counterNestNum;
        unionExp.counterNestNum = Math.max(leftNum,rightNum);
        return null;
    }

    @Override
    public Void visit(ConcatExp concatExp, Void context) {
        RegexExp leftExp = concatExp.getLeft();
        leftExp.accept(this,context);
        int leftNum = leftExp.counterNestNum;
        RegexExp rightExp = concatExp.getRight();
        rightExp.accept(this,context);
        int rightNum = rightExp.counterNestNum;
        concatExp.counterNestNum = Math.max(leftNum,rightNum);
        return null;
    }

    @Override
    public Void visit(RepeatExp repeatExp, Void context) {
        RegexExp innerExp = repeatExp.getCharCollectionExp();
        innerExp.accept(this,context);
        int innerNum = innerExp.counterNestNum;
        switch (repeatExp.getModifierType()){
            case QUESTION: repeatExp.counterNestNum = innerNum;break;
            case STAR: repeatExp.counterNestNum = innerNum>0?innerNum+1:innerNum;break;
            case PLUS: repeatExp.counterNestNum = innerNum>0?innerNum+1:innerNum;break;
            case RANGE: repeatExp.counterNestNum = innerNum+1;break;
        }
        return null;
    }

    @Override
    public Void visit(CharCollectionExp charCollectionExp, Void context) {
        return null;
    }

    @Override
    public Void visit(CharRangeExp charRangeExp, Void context) {
        return null;
    }

    @Override
    public Void visit(MetaExp metaExp, Void context) {
        return null;
    }

    @Override
    public Void visit(CharExp charExp, Void context) {
        return null;
    }

    public static void check(RegexExp regexExp){
        regexExp.accept(CounterNestNumChecker.INSTANCE,null);
        int maxCounterNum = regexExp.counterNestNum;
        if(maxCounterNum > MAX_NEST_COUNTER_NUM) throw new RuntimeException("不合法的正则表达式：内部的嵌套计数器个数已经超出MAX_NEST_COUNTER_NUM的数量2");
    }
}
