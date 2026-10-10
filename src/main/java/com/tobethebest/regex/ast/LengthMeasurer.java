package com.tobethebest.regex.ast;

import com.tobethebest.regex.ast.exp.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

public class LengthMeasurer implements ASTVisitor<LengthMeasurer.LengthInfo, LengthMeasurer.LengthMeasurerContext>{
    protected LengthMeasurer(){}

    public static final LengthMeasurer INSTANCE = new LengthMeasurer();

    public static LengthInfo measureLength(RegexExp regexExp){
        return regexExp.accept(INSTANCE,INSTANCE.new LengthMeasurerContext());
    }

    protected class LengthMeasurerContext{
        Map<Integer,LengthInfo> groupLengthMap;
        LengthMeasurerContext(){
            this.groupLengthMap = new HashMap<>();
        }
    }

    @AllArgsConstructor(staticName = "of")
    @NoArgsConstructor
    public static class LengthInfo {
        public int minLength;
        public int maxLength; // -1表示无穷

        public void compare(int min,int max){
            this.minLength = Math.min(this.minLength,min);
            if(this.maxLength == -1 || max == -1) this.maxLength = -1;
            else this.maxLength = Math.max(this.maxLength,max);
        }

        public void add(int min,int max){
            this.minLength = this.minLength + min;
            if(this.maxLength == -1 || max == -1) this.maxLength = -1;
            else this.maxLength = this.maxLength + max;
        }

        public void multiply(int min,int max){
            this.minLength = this.minLength * min;
            if(this.maxLength == -1 || max == -1) this.maxLength = -1;
            else this.maxLength = this.maxLength * max;
        }
    }

    @Override
    public LengthInfo visit(UnionExp unionExp, LengthMeasurerContext context) {
        LengthInfo leftInfo = unionExp.left.accept(this,context);
        LengthInfo rightInfo = unionExp.right.accept(this,context);
        leftInfo.compare(rightInfo.minLength,rightInfo.maxLength);
        return leftInfo;
    }

    @Override
    public LengthInfo visit(ConcatExp concatExp, LengthMeasurerContext context) {
        LengthInfo leftInfo = concatExp.left.accept(this,context);
        LengthInfo rightInfo = concatExp.right.accept(this,context);
        leftInfo.add(rightInfo.minLength,rightInfo.maxLength);
        return leftInfo;
    }

    @Override
    public LengthInfo visit(RepeatExp repeatExp, LengthMeasurerContext context) {
        LengthInfo innerInfo = repeatExp.charCollectionExp.accept(this,context);
        innerInfo.multiply(repeatExp.min,repeatExp.max);
        return innerInfo;
    }

    @Override
    public LengthInfo visit(CharCollectionExp charCollectionExp, LengthMeasurerContext context) {
        return LengthInfo.of(1,1);
    }

    @Override
    public LengthInfo visit(CharRangeExp charRangeExp, LengthMeasurerContext context) {
        return LengthInfo.of(1,1);
    }

    @Override
    public LengthInfo visit(MetaExp metaExp, LengthMeasurerContext context) {
        String metaValue = metaExp.metaValue;
        if("".equals(metaValue) || "^".equals(metaValue) || "$".equals(metaValue)
                || "\\b".equals(metaValue) || "\\B".equals(metaValue)) return new LengthInfo();
        return LengthInfo.of(1,1);
    }

    @Override
    public LengthInfo visit(CharExp charExp, LengthMeasurerContext context) {
        return LengthInfo.of(1,1);
    }

    @Override
    public LengthInfo visit(GroupExp groupExp, LengthMeasurerContext context) {
        LengthInfo lengthInfo = groupExp.innerExp.accept(this,context);
        context.groupLengthMap.put(groupExp.groupId,LengthInfo.of(lengthInfo.minLength,lengthInfo.maxLength));
        return lengthInfo;
    }

    @Override
    public LengthInfo visit(GroupRefExp groupRefExp, LengthMeasurerContext context) {
        LengthInfo groupLengthInfo = context.groupLengthMap.get(groupRefExp.refId);
        return LengthInfo.of(groupLengthInfo.minLength,groupLengthInfo.maxLength);
    }

    @Override
    public LengthInfo visit(LookaroundExp lookaroundExp, LengthMeasurerContext context) {
        lookaroundExp.setLengthInfo(lookaroundExp.innerExp.accept(this,context));
        // 不消耗任何字符，长度为0
        return LengthInfo.of(0,0);
    }
}
