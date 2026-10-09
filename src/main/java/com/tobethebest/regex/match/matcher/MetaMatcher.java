package com.tobethebest.regex.match.matcher;

import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.Pointer;
import com.tobethebest.regex.util.CharRange;
import com.tobethebest.regex.util.MetaUtil;

import java.util.List;

public class MetaMatcher implements Matcher{
    public String metaValue;

    public MetaMatcher(String metaValue) {
        this.metaValue = metaValue;
    }

    @Override
    public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
        return matcherVisitor.visit(this, context);
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext backContext) {
        List<CharRange> ranges = MetaUtil.MetaToCharRangeMap.get(this.metaValue);
        if(CharRange.rangeListInclude(ranges, str.charAt(pointer.index))){
            pointer.index++;
            return true;
        }
        return false;
    }
}
