package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
public class MetaExp extends RegexExp{
    // 支持的元字符
    public static final String LOW_D = "\\d";
    public static final String UP_D = "\\D";
    public static final String LOW_W = "\\w";
    public static final String UP_W = "\\W";
    public static final String LOW_S = "\\s";
    public static final String UP_S = "\\S";
    public static final String DOT = ".";

    public String metaValue;

    @Override
    public String treeString() {
        return String.format("[Meta:%s]",this.metaValue.isEmpty() ? "empty" : this.metaValue);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
