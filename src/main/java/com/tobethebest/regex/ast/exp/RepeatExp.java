package com.tobethebest.regex.ast.exp;

import com.tobethebest.regex.ast.ASTVisitor;

/**
 * 正则表达式中的重复表达式
 * 关于{}修饰符，只支持{m},{m,}, {m, n}三种形式，且内部不支持任何带有空格的写法
 */

public class RepeatExp extends RegexExp{
    public RegexExp charCollectionExp;
    public int min;
    public int max; // -1代表无穷大
    public boolean greedy = true;

    public RepeatExp(RegexExp charCollectionExp,int min,int max) {
        this.charCollectionExp = charCollectionExp;
        this.min = min;
        this.max = max;
    }

    public static RepeatExp of(RegexExp charCollectionExp,int min,int max) {
        return new RepeatExp(charCollectionExp,min,max);
    }

    public boolean isQuestion(){
        return min == 0 && max == 1;
    }

    public boolean isStar(){
        return min == 0 && max == -1;
    }

    public boolean isPlus(){
        return min == 1 && max == -1;
    }

    public RegexExp process(){
        if(this.charCollectionExp instanceof LookaroundExp){
            if(this.isPlus()) return this.charCollectionExp;
            if(this.isStar()) this.max = 1;
        }
        return this;
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
