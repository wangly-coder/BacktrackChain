package com.wly.regex.ast;

import com.wly.regex.ast.exp.*;


public class NestNumberChecker implements ASTVisitor<Integer,Void> {
    
    public final static int MAX_NEST_NUMBER = 2;
    public static final NestNumberChecker INSTANCE = new NestNumberChecker();
    
    protected NestNumberChecker(){}

    public static int getNestNumber(RegexExp regexExp){
        return regexExp.accept(INSTANCE, null);
    }

    @Override
    public Integer visit(UnionExp unionExp, Void context) {
        int leftNum = unionExp.left.accept(this,context);
        int rightNum = unionExp.right.accept(this,context);
        return Math.max(leftNum,rightNum);
    }

    @Override
    public Integer visit(ConcatExp concatExp, Void context) {
        int leftNum = concatExp.left.accept(this,context);
        int rightNum = concatExp.right.accept(this,context);
        return Math.max(leftNum,rightNum);
    }

    @Override
    public Integer visit(RepeatExp repeatExp, Void context) {
        return repeatExp.charCollectionExp.accept(this,context) + 1;
    }

    @Override
    public Integer visit(CharCollectionExp charCollectionExp, Void context) {
        return 0;
    }

    @Override
    public Integer visit(CharRangeExp charRangeExp, Void context) {
        return 0;
    }

    @Override
    public Integer visit(MetaExp metaExp, Void context) {
        return 0;
    }

    @Override
    public Integer visit(CharExp charExp, Void context) {
        return 0;
    }

    @Override
    public Integer visit(GroupExp groupExp, Void context) {
        return groupExp.regexExp.accept(this,context);
    }

    @Override
    public Integer visit(GroupRefExp groupRefExp, Void context) {
        return 0;
    }

    public static void check(RegexExp regexExp){
        int maxCounterNum = NestNumberChecker.getNestNumber(regexExp);
        if(maxCounterNum > MAX_NEST_NUMBER) throw new RuntimeException(
                String.format("不合法的正则表达式：内部的嵌套计数器个数已经超出MAX_NEST_NUMBER的数量%d",MAX_NEST_NUMBER));
    }
}
