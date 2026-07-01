package com.wly.regex.exp;

import com.wly.regex.util.ASTVisitor;

/**
 * 正则表达式AST中的节点父类
 */
public abstract class RegexExp {

    public int counterNestNum;

    /**
     * 用于提供打印AST的字符串
     * @return
     */
    public abstract String treeString();

    public abstract <R,C> R accept(ASTVisitor<R,C> astVisitor,C context);
}
