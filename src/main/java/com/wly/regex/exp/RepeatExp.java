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

    public RegexExp process(){
        /*
        需要将Range类型进一步处理
        {0,1}变为Question类型,{0,}变为Star类型,{1,}变为Plus类型
         */
        if(this.modifierType == RepeatExpType.RANGE){
            if(this.min == 0){
                if(this.max==1) this.modifierType = RepeatExpType.QUESTION;
                else if(this.max==-1) this.modifierType = RepeatExpType.STAR;
            }
            else if(this.min == 1 && this.max == -1) this.modifierType = RepeatExpType.PLUS;
        }
        return this;
    }

    @Override
    public String treeString() {
        if(modifierType == RepeatExpType.RANGE) {
            if(min == max) return String.format("[Repeat:{%d}]",min);
            if(max == -1) return String.format("[Repeat:{%d,}]",min);
            return String.format("[Repeat:{%d,%d}]",min,max);
        }
        return String.format("[Repeat:%s]",this.modifierType.modifier);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
