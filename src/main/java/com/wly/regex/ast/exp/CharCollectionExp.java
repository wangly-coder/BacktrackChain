package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class CharCollectionExp extends RegexExp{
    public List<RegexExp> charSequenceExp;
    // @Getter生成isNegative()方法
    public boolean negative;

    public CharCollectionExp(String protoString, List<RegexExp> charSequenceExp, boolean negative) {
        super(protoString);
        this.charSequenceExp = charSequenceExp;
        this.negative = negative;
    }

    public static CharCollectionExp of(String protoString, List<RegexExp> charSequenceExp, boolean negative) {
        return new CharCollectionExp(protoString,charSequenceExp,negative);
    }

    @Override
    public String treeString() {
        return String.format("[CharCollection%s]",this.negative ? ":^":"");
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
