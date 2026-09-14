package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 正则表达式中的连接表达式
 */
@AllArgsConstructor(staticName = "of")
public class ConcatExp extends RegexExp{
    public RegexExp left;
    public RegexExp right;

    @Override
    public String treeString() {
        return "[Concat]";
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }

}
