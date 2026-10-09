package com.tobethebest.regex.ast.exp;

import com.tobethebest.regex.ast.ASTVisitor;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class GroupRefExp extends RegexExp{
    public int refId;

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
