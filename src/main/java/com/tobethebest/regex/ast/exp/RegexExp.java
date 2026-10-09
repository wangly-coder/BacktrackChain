package com.tobethebest.regex.ast.exp;

import com.tobethebest.regex.ast.ASTVisitor;

/**
 * 正则表达式AST中的节点父类
 */
public abstract class RegexExp {
    public RegexExp(){}

    public abstract <R,C> R accept(ASTVisitor<R,C> astVisitor,C context);
}
