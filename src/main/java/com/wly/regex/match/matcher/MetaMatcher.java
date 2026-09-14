package com.wly.regex.match.matcher;

import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.Pointer;
import com.wly.regex.util.CharRange;
import com.wly.regex.util.MetaUtil;

import java.util.List;

public class MetaMatcher implements Matcher{
    public String metaValue;

    public MetaMatcher(String metaValue) {
        this.metaValue = metaValue;
    }

    @Override
    public String toString() {
        return String.format("MetaMatcher:%s", this.metaValue);
    }

    @Override
    public String printSelf() {
        return String.format("[Meta:%s]", this.metaValue);
    }

    @Override
    public boolean match(String str, Pointer pointer, BackContext backContext) {
        List<CharRange> ranges = MetaUtil.MetaToCharRangeMap.get(this.metaValue);
        for(CharRange range : ranges) {
            if (range.isInclude(str.charAt(pointer.index))) {
                pointer.index++;
                return true;
            }
        }
        return false;
    }
}
