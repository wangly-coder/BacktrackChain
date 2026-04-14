package com.wly.regex.exp;

import com.wly.regex.util.ASTVisitor;
import lombok.Builder;
import lombok.Data;

/**
 * '|'运算符对应的AST节点类
 */
@Builder
@Data
public class UnionExp extends RegexExp{
    private RegexExp left;
    private RegexExp right;

    public String treeString(){
        return "[Union]";
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
