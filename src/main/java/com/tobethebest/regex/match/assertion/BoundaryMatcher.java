package com.tobethebest.regex.match.assertion;

import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.Pointer;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.matcher.ChainMatcher;
import com.tobethebest.regex.util.CharRange;
import com.tobethebest.regex.util.MetaUtil;
import lombok.AllArgsConstructor;

/**
 * 基于ASCII模式划定边界
 */
@AllArgsConstructor(staticName = "of")
public class BoundaryMatcher extends ChainMatcher {
    public boolean isMatchBoundary;

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
        // TODO 后续考虑支持中文
        int curIndex = pointer.index;
        char preChar,curChar;
        // 如果是结尾
        if(curIndex == str.length()){
            preChar = str.charAt(curIndex-1);
            if(this.isMatchBoundary) return CharRange.rangeListInclude(MetaUtil.LOW_W_RANGE_LIST,preChar);
            else return !CharRange.rangeListInclude(MetaUtil.LOW_W_RANGE_LIST,preChar);
        }
        curChar = str.charAt(curIndex);
        // 如果是开头
        if(curIndex == 0) {
            if(this.isMatchBoundary) return CharRange.rangeListInclude(MetaUtil.LOW_W_RANGE_LIST,curChar);
            else return !CharRange.rangeListInclude(MetaUtil.LOW_W_RANGE_LIST,curChar);
        }
        preChar = str.charAt(curIndex-1);
        boolean isPreLowW = CharRange.rangeListInclude(MetaUtil.LOW_W_RANGE_LIST,preChar);
        boolean isCurLowW = CharRange.rangeListInclude(MetaUtil.LOW_W_RANGE_LIST,curChar);
        if(this.isMatchBoundary) return isPreLowW != isCurLowW;
        return isPreLowW == isCurLowW;
    }

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return matcherVisitor.visit(this,context);
    }
}
