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
    private int max; // -1代表无穷大
    private RepeatExpType modifierType;

    public enum RepeatExpType{
        QUESTION('?'),STAR('*'),PLUS('+'),RANGE('\0');
        public final char modifier;
        RepeatExpType(char modifier){
            this.modifier = modifier;
        }
    }

    public RegexExp process(){
        /*
        需要将Range类型进一步处理
        {0,1}变为Question类型,{0,}变为Star类型,{1,}变为Plus类型

        需要解决量词嵌套问题，指的是括号表达式是一个量词表达式，外层又嵌套修饰符。如(a+)+这种
        优化：
        1.对于?*+这几种嵌套，两个任取两个不同的都是*，相同嵌套则为相同
        2.?*+和{}嵌套，内层是{}不能随意优化
        外层是{min,max}，内层是三者其中之一可以优化，如下：
        ?可以优化为{0,max}，*可以优化为*，+可以优化为{min,}
        3.{}之间嵌套不能随意优化，应当保留
         */
        if(this.modifierType == RepeatExpType.RANGE){
            if(this.min == 0){
                if(this.max==1) this.modifierType = RepeatExpType.QUESTION;
                else if(this.max==-1) this.modifierType = RepeatExpType.STAR;
            }
            else if(this.min == 1 && this.max == -1) this.modifierType = RepeatExpType.PLUS;
        }
        RegexExp result = this;
        // 量词嵌套优化
        RegexExp regexExp = this.getCharCollectionExp();
        if(regexExp instanceof RepeatExp){
            RepeatExp innerExp = (RepeatExp) regexExp;
            RepeatExpType innerType = innerExp.modifierType;
            RepeatExpType outerType = this.getModifierType();
            // 优化只需要修改内层修饰符即可
            result = innerExp;
            // 先判断内层再判断外层
            if (innerType == RepeatExpType.STAR) return result;
            else if(innerType == RepeatExpType.QUESTION){
                if(outerType == RepeatExpType.QUESTION) return result;
                else if(outerType == RepeatExpType.STAR || outerType == RepeatExpType.PLUS)
                    innerExp.modifierType = RepeatExpType.STAR;
                else {
                    // Range取0和max
                    if(this.max == -1) innerExp.modifierType = RepeatExpType.STAR;
                    else{
                        innerExp.min = 0;
                        innerExp.max = this.max;
                        innerExp.modifierType = RepeatExpType.RANGE;
                    }
                }
            }
            else if(innerType == RepeatExpType.PLUS){
                if(outerType == RepeatExpType.PLUS) return result;
                else if(outerType == RepeatExpType.QUESTION || outerType == RepeatExpType.STAR)
                    innerExp.modifierType = RepeatExpType.STAR;
                else{
                    // Range取min和无穷
                    if(this.min == 0) innerExp.modifierType = RepeatExpType.STAR;
                    else{
                        innerExp.min = this.min;
                        innerExp.max = -1;
                        innerExp.modifierType = RepeatExpType.RANGE;
                    }
                }
            }
            // Range的话不进行任何优化
            else return this;
        }
        return result;
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
