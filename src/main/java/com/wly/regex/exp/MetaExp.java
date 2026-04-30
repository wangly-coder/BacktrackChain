package com.wly.regex.exp;

import com.wly.regex.util.ASTVisitor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetaExp extends RegexExp{
    // 支持的元字符
    public static final String LOW_D = "\\d";
    public static final String UP_D = "\\D";
    public static final String LOW_W = "\\w";
    public static final String UP_W = "\\W";
    public static final String LOW_S = "\\s";
    public static final String UP_S = "\\S";
    public static final String DOT = ".";

    private String metaValue;

    @Override
    public String treeString() {
        return String.format("[Meta:%s]",this.metaValue);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
