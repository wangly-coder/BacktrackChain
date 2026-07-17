package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CharCollectionExp extends RegexExp{
    private List<RegexExp> charSequenceExp;
    // @Getter生成isNegative()方法
    private boolean negative;

    @Override
    public String treeString() {
        return String.format("[CharCollection%s]",this.negative ? ":^":"");
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
