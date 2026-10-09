package com.tobethebest.regex.ast.exp;

import com.tobethebest.regex.ast.ASTVisitor;

import java.util.List;

public class CharCollectionExp extends RegexExp{
    public List<RegexExp> charSequenceExp;
    // @Getter生成isNegative()方法
    public boolean negative;

    public CharCollectionExp(List<RegexExp> charSequenceExp, boolean negative) {
        this.charSequenceExp = charSequenceExp;
        this.negative = negative;
    }

    public static CharCollectionExp of(List<RegexExp> charSequenceExp, boolean negative) {
        return new CharCollectionExp(charSequenceExp,negative);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
