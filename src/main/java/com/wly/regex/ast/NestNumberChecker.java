package com.wly.regex.ast;

import com.wly.regex.ast.exp.*;


public class NestNumberChecker implements ASTVisitor<Void,Void> {
    
    public static int MAX_NEST_NUMBER = 2;
    public static final NestNumberChecker INSTANCE = new NestNumberChecker();
    
    private NestNumberChecker(){}

    @Override
    public Void visit(UnionExp unionExp, Void context) {
        RegexExp leftExp = unionExp.getLeft();
        leftExp.accept(this,context);
        int leftNum = leftExp.nestNumber;
        RegexExp rightExp = unionExp.getRight();
        rightExp.accept(this,context);
        int rightNum = rightExp.nestNumber;
        unionExp.nestNumber = Math.max(leftNum,rightNum);
        return null;
    }

    @Override
    public Void visit(ConcatExp concatExp, Void context) {
        RegexExp leftExp = concatExp.getLeft();
        leftExp.accept(this,context);
        int leftNum = leftExp.nestNumber;
        RegexExp rightExp = concatExp.getRight();
        rightExp.accept(this,context);
        int rightNum = rightExp.nestNumber;
        concatExp.nestNumber = Math.max(leftNum,rightNum);
        return null;
    }

    @Override
    public Void visit(RepeatExp repeatExp, Void context) {
        RegexExp innerExp = repeatExp.getCharCollectionExp();
        innerExp.accept(this,context);
        int innerNum = innerExp.nestNumber;
        // 任何量词都被看做一层嵌套
        repeatExp.nestNumber = innerNum + 1;
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
        regexExp.accept(NestNumberChecker.INSTANCE,null);
        int maxCounterNum = regexExp.nestNumber;
        if(maxCounterNum > MAX_NEST_NUMBER) throw new RuntimeException("不合法的正则表达式：内部的嵌套计数器个数已经超出MAX_NEST_NUMBER的数量2");
    }
}
