package com.tobethebest.regex.match;

import com.tobethebest.regex.match.assertion.BoundaryMatcher;
import com.tobethebest.regex.match.assertion.EndPosMatcher;
import com.tobethebest.regex.match.assertion.LookaroundMatcher;
import com.tobethebest.regex.match.assertion.StartPosMatcher;
import com.tobethebest.regex.match.group.GroupMatcher;
import com.tobethebest.regex.match.group.GroupRefMatcher;
import com.tobethebest.regex.match.matcher.*;

public interface MatcherVisitor<R,C>{
    R visit(UnionMatcher unionMatcher, C context);
    R visit(RepeatMatcher.RepeatStartMatcher repeatStartMatcher, C context);
    R visit(RepeatMatcher.RepeatEndMatcher repeatEndMatcher, C context);
    R visit(CollectionMatcher collectionMatcher, C context);
    R visit(CharRangeMatcher charRangeMatcher, C context);
    R visit(MetaMatcher metaMatcher, C context);
    R visit(StringMatcher stringMatcher, C context);
    R visit(StartPosMatcher startPosMatcher, C context);
    R visit(EndPosMatcher endPosMatcher, C context);
    R visit(GroupMatcher.GroupStartMatcher groupStartMatcher, C context);
    R visit(GroupMatcher.GroupEndMatcher groupEndMatcher, C context);
    R visit(GroupRefMatcher groupRefMatcher, C context);
//    R visit(LookaroundMatcher.LookaroundStartMatcher lookaroundStartMatcher, C context);
//    R visit(LookaroundMatcher.LookaroundEndMatcher lookaroundEndMatcher, C context);
    R visit(LookaroundMatcher lookaroundMatcher, C context);
    R visit(BoundaryMatcher boundaryMatcher, C context);
}
