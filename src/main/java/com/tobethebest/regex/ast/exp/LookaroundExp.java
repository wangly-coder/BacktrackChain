package com.tobethebest.regex.ast.exp;

import com.tobethebest.regex.ast.ASTVisitor;
import com.tobethebest.regex.ast.LengthMeasurer;
import lombok.Getter;

public class LookaroundExp extends RegexExp{
    public LookaroundType lookaroundType;
    public RegexExp innerExp;
    @Getter
    private LengthMeasurer.LengthInfo lengthInfo; // 虽然不消耗字符，但匹配的长度信息可以优化匹配过程。

    public void setLengthInfo(LengthMeasurer.LengthInfo lengthInfo) {
        // 对于左向断言，其最大长度需要有数值而不能无穷大
        if((this.lookaroundType == LookaroundType.LEFT_POSITIVE || this.lookaroundType == LookaroundType.LEFT_NEGATIVE)
        && lengthInfo.maxLength == -1) throw new RuntimeException("左向断言内部表达式最大长度不能是无穷大");
        this.lengthInfo = lengthInfo;
    }

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
