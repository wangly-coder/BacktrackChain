package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class CharRangeExp extends RegexExp{
    private CharExp left;
    private CharExp right;

    @Override
    public String treeString() {
        return String.format("[CharRange:%s-%s]",this.left.getCharValue(),this.right.getCharValue());
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
