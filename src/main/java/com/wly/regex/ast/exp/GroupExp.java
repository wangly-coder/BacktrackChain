package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class GroupExp extends RegexExp{
    public int groupId;
    public RegexExp regexExp;

    @Override
    public String treeString() {
        return String.format("[Group:%d]", this.groupId);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
