package com.tobethebest.regex.ast.exp;

import com.tobethebest.regex.ast.ASTVisitor;

/**
 * '|'运算符对应的AST节点类
 */
public class UnionExp extends RegexExp{
    public RegexExp left;
    public RegexExp right;
    public boolean hasEmptyString;

    public UnionExp() {}

    public UnionExp(RegexExp left, RegexExp right) {
        this.left = left;
        this.right = right;
    }

    public static UnionExp of(RegexExp left, RegexExp right) {
        return new UnionExp(left,right);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
