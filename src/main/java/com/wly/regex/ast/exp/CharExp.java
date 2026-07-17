package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class CharExp extends RegexExp {
    private char charValue;

    @Override
    public String treeString() {
        return String.format("[Char:%s]",this.charValue);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
