package com.wly.regex.ast;

import com.wly.regex.ast.exp.*;


public class NumberNestChecker implements ASTVisitor<Void,Void> {

    public static final int MAX_NEST_COUNTER_NUM = 2;
    public static final NumberNestChecker INSTANCE = new NumberNestChecker();

    @Override
    public Void visit(UnionExp unionExp, Void context) {
        RegexExp leftExp = unionExp.getLeft();
        leftExp.accept(this,context);
        int leftNum = leftExp.numberNest;
        RegexExp rightExp = unionExp.getRight();
        rightExp.accept(this,context);
        int rightNum = rightExp.numberNest;
        unionExp.numberNest = Math.max(leftNum,rightNum);
        return null;
    }

    @Override
    public Void visit(ConcatExp concatExp, Void context) {
        RegexExp leftExp = concatExp.getLeft();
        leftExp.accept(this,context);
        int leftNum = leftExp.numberNest;
        RegexExp rightExp = concatExp.getRight();
        rightExp.accept(this,context);
        int rightNum = rightExp.numberNest;
        concatExp.numberNest = Math.max(leftNum,rightNum);
        return null;
    }

    @Override
    public Void visit(RepeatExp repeatExp, Void context) {
        RegexExp innerExp = repeatExp.getCharCollectionExp();
        innerExp.accept(this,context);
        int innerNum = innerExp.numberNest;
        // 任何量词都被看做一层嵌套
        repeatExp.numberNest = innerNum + 1;
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
        regexExp.accept(NumberNestChecker.INSTANCE,null);
        int maxCounterNum = regexExp.numberNest;
        if(maxCounterNum > MAX_NEST_COUNTER_NUM) throw new RuntimeException("不合法的正则表达式：内部的嵌套计数器个数已经超出MAX_NEST_COUNTER_NUM的数量2");
    }
}
