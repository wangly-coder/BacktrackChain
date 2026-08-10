package com.wly.regex.ast.exp;

import com.wly.regex.ast.ASTVisitor;
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
    private int max; // -1代表无穷大
    private RepeatExpType modifierType;
    @Builder.Default
    private boolean greedy = true;

    public enum RepeatExpType{
        QUESTION('?'),STAR('*'),PLUS('+'),RANGE('\0');
        public final char modifier;
        RepeatExpType(char modifier){
            this.modifier = modifier;
        }
    }

    public RepeatExp process(){
        /*
        需要将Range类型进一步处理
        {0,1}变为Question类型,{0,}变为Star类型,{1,}变为Plus类型
         */
        // 类型变换处理
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
        String greedy = this.greedy ? "" : "?";
        if(modifierType == RepeatExpType.RANGE) {
            if(min == max) return String.format("[Repeat:{%d}%s]",min,greedy);
            if(max == -1) return String.format("[Repeat:{%d,}%s]",min,greedy);
            return String.format("[Repeat:{%d,%d}%s]",min,max,greedy);
        }
        return String.format("[Repeat:%s%s]",this.modifierType.modifier,greedy);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
