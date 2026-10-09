package com.tobethebest.regex.match;

import com.tobethebest.regex.ast.LengthMeasurer;
import com.tobethebest.regex.ast.TestedRegexParser;
import com.tobethebest.regex.ast.exp.RegexExp;
import com.tobethebest.regex.match.group.GroupMatcher;

import java.util.ArrayList;

public class TestedMatchContext extends MatchContext{
    public TestedMatchContext(TestedRegexParser parser){
        RegexExp regexExp = parser.parse();
        // 从parser中获取匹配控制信息
        this.hasStartLimit = parser.hasStartLimit;
        this.hasEndLimit = parser.hasEndLimit;
        this.groupNumber = parser.groupNumber;
        this.groupNameToIdMap= parser.groupNameToIdMap;
        // 获取长度信息
        this.lengthInfo = LengthMeasurer.measureLength(regexExp);
        // 构建匹配链
        this.repeatMatcherList = new ArrayList<>(2);
        this.groupEndMatchers = new GroupMatcher.GroupEndMatcher[this.groupNumber];
        this.headMatcher = TestedMatcherChainBuilder.build(regexExp,this);
    }
}
