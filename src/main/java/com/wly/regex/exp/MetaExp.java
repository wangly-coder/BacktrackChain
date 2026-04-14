package com.wly.regex.exp;

import com.wly.regex.util.ASTVisitor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetaExp extends RegexExp{
    private String metaValue;

    @Override
    public String treeString() {
        return String.format("[Meta:%s]",this.metaValue);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
