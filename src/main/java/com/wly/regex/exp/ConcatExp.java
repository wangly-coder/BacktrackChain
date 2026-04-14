package com.wly.regex.exp;

import com.wly.regex.util.ASTVisitor;
import lombok.Builder;
import lombok.Data;

/**
 * 正则表达式中的连接表达式
 */
@Builder
@Data
public class ConcatExp extends RegexExp{
    private RegexExp left;
    private RegexExp right;

    @Override
    public String treeString() {
        return "[Concat]";
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }

}
