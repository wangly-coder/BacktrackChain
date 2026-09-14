package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * '|'运算符对应的AST节点类
 */
public class UnionExp extends RegexExp{
    public RegexExp left;
    public RegexExp right;

    public UnionExp(String protoString) {
        super(protoString);
    }

    public UnionExp(String protoString,RegexExp left, RegexExp right) {
        super(protoString);
        this.left = left;
        this.right = right;
    }

    public static UnionExp of(String protoString,RegexExp left, RegexExp right) {
        return new UnionExp(protoString,left,right);
    }

    public String treeString(){
        return "[Union]";
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
