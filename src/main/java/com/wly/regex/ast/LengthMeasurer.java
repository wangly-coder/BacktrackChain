package com.wly.regex.ast;

import com.wly.regex.ast.exp.*;

public class LengthMeasurer implements ASTVisitor<LengthInfo,Void>{
    private LengthMeasurer(){}

    public static final LengthMeasurer INSTANCE = new LengthMeasurer();

    public static LengthInfo measureLength(RegexExp regexExp){
        return regexExp.accept(INSTANCE,null);
    }

    @Override
    public LengthInfo visit(UnionExp unionExp, Void context) {
        LengthInfo leftInfo = unionExp.getLeft().accept(this,context);
        LengthInfo rightInfo = unionExp.getRight().accept(this,context);
        leftInfo.compare(rightInfo.minLength,rightInfo.maxLength);
        return leftInfo;
    }

    @Override
    public LengthInfo visit(ConcatExp concatExp, Void context) {
        LengthInfo leftInfo = concatExp.getLeft().accept(this,context);
        LengthInfo rightInfo = concatExp.getRight().accept(this,context);
        leftInfo.add(rightInfo.minLength,rightInfo.maxLength);
        return leftInfo;
    }

    @Override
    public LengthInfo visit(RepeatExp repeatExp, Void context) {
        LengthInfo innerInfo = repeatExp.getCharCollectionExp().accept(this,context);
        switch (repeatExp.getModifierType()){
            case QUESTION:innerInfo.multiply(0,1);break;
            case STAR:innerInfo.multiply(0,-1);break;
            case PLUS:innerInfo.multiply(1,-1);break;
            case RANGE:innerInfo.multiply(repeatExp.getMin(),repeatExp.getMax());break;
        }
        return innerInfo;
    }

    @Override
    public LengthInfo visit(CharCollectionExp charCollectionExp, Void context) {
        return LengthInfo.of(1,1);
    }

    @Override
    public LengthInfo visit(CharRangeExp charRangeExp, Void context) {
        return LengthInfo.of(1,1);
    }

    @Override
    public LengthInfo visit(MetaExp metaExp, Void context) {
        String metaValue = metaExp.getMetaValue();
        if("".equals(metaValue) || "^".equals(metaValue) || "$".equals(metaValue)) return new LengthInfo();
        return LengthInfo.of(1,1);
    }

    @Override
    public LengthInfo visit(CharExp charExp, Void context) {
        return LengthInfo.of(1,1);
    }
}
