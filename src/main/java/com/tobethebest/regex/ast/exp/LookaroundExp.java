package com.tobethebest.regex.ast.exp;

import com.tobethebest.regex.ast.ASTVisitor;
import com.tobethebest.regex.ast.LengthMeasurer;

public class LookaroundExp extends RegexExp{
    public LookaroundType lookaroundType;
    public RegexExp innerExp;
    public LengthMeasurer.LengthInfo lengthInfo; // 虽然不消耗字符，但匹配的长度信息可以优化匹配过程。

    public enum LookaroundType {
        RIGHT_POSITIVE,RIGHT_NEGATIVE,LEFT_POSITIVE,LEFT_NEGATIVE;

        public static LookaroundType identity(boolean isRight,boolean isPositive){
            if(isRight) return isPositive ? RIGHT_POSITIVE : RIGHT_NEGATIVE;
            else return isPositive ? LEFT_POSITIVE : LEFT_NEGATIVE;
        }
    }

    public LookaroundExp(LookaroundType lookaroundType,RegexExp innerExp) {
        this.lookaroundType = lookaroundType;
        this.innerExp = innerExp;
    }

    public static LookaroundExp of(LookaroundType lookaroundType, RegexExp innerExp) {
        return new LookaroundExp(lookaroundType,innerExp);
    }

    @Override
    public <R, C> R accept(ASTVisitor<R, C> astVisitor, C context) {
        return astVisitor.visit(this,context);
    }
}
