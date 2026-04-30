package com.wly.regex.exp;

import com.wly.regex.util.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

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
