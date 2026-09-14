package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
public class CharRangeExp extends RegexExp{
    public CharExp left;
    public CharExp right;

    @Override
    public String treeString() {
        return String.format("[CharRange:%s-%s]",this.left.charValue,this.right.charValue);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
