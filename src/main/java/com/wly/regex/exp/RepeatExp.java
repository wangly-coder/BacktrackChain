package com.wly.regex.exp;

import com.wly.regex.util.ASTVisitor;
import lombok.Builder;
import lombok.Data;

/**
 * 正则表达式中的重复表达式
 * 关于{}修饰符，只支持{m},{m,}, {m, n}三种形式，且内部不支持任何带有空格的写法
 */

@Data
@Builder
public class RepeatExp extends RegexExp{
    private RegexExp charCollectionExp;
    private int min;
    private int max;
    private RepeatExpType modifierType;

    public enum RepeatExpType{
        QUESTION('?'),STAR('*'),PLUS('+'),RANGE('\0');
        public char modifier;
        RepeatExpType(char modifier){
            this.modifier = modifier;
        }
    }

    @Override
    public String treeString() {
        if(modifierType == RepeatExpType.RANGE) return String.format("[Repeat:{%d,%d}]",min,max);
        return String.format("[Repeat:%s]",this.modifierType.modifier);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
