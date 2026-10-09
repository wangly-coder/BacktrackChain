package com.tobethebest.regex.ast.exp;

import com.tobethebest.regex.ast.ASTVisitor;
import lombok.AllArgsConstructor;

/**
 * 正则表达式中的连接表达式
 */
@AllArgsConstructor(staticName = "of")
public class ConcatExp extends RegexExp{
    public RegexExp left;
    public RegexExp right;

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }

}
