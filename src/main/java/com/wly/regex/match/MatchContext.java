package com.wly.regex.match;

import com.wly.regex.ast.LengthMeasurer;
import com.wly.regex.ast.LengthMeasurer.LengthInfo;
import com.wly.regex.ast.RegexParser;
import com.wly.regex.ast.exp.RegexExp;
import com.wly.regex.match.matcher.ChainMatcher;
import com.wly.regex.match.matcher.RepeatMatcher;
import com.wly.regex.match.group.GroupMatcher;
import com.wly.regex.match.group.GroupPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 匹配控制器，包含^$限定符和正则修饰符等一系列匹配控制信息。皆由布尔值表示
 */
public class MatchContext {
    public ChainMatcher headMatcher;
    public boolean hasStartLimit;
    public boolean hasEndLimit;
    public LengthInfo lengthInfo;
    public List<RepeatMatcher> repeatMatchers; // 待清理的RM
    public int groupNumber; // 组数量，用于后续初始化组内容容器
    public GroupMatcher.GroupEndMatcher[] groupEndMatchers; // 所有的组结尾匹配器
    public LinkedHashMap<String,Integer> groupNameToIdMap; // 组名->组id映射


    public MatchContext(){}

    public MatchContext(RegexParser parser){
        RegexExp regexExp = parser.parse();
        // 从parser中获取匹配控制信息
        this.hasStartLimit = parser.hasStartLimit;
        this.hasEndLimit = parser.hasEndLimit;
        this.groupNumber = parser.groupNumber;
        this.groupNameToIdMap= parser.groupNameToIdMap;
        // 获取长度信息
        this.lengthInfo = LengthMeasurer.measureLength(regexExp);
        // 构建匹配链
        this.repeatMatchers = new ArrayList<>(2);
        this.groupEndMatchers = new GroupMatcher.GroupEndMatcher[this.groupNumber];
        this.headMatcher = MatcherChainBuilder.build(regexExp,this);
    }

    public void addRepeatMatcher(RepeatMatcher repeatMatcher){
        this.repeatMatchers.add(repeatMatcher);
    }

    public void addGroupEndMatcher(GroupMatcher.GroupEndMatcher groupEndMatcher){
        this.groupEndMatchers[groupEndMatcher.groupId -1] = groupEndMatcher;
    }

    public void updateGroupEndMatcher(int groupId,GroupPair groupPair){
        if(groupPair == null) return;
        this.groupEndMatchers[groupId-1].updateIndex(groupPair);
    }

    public void clear(){
        for(RepeatMatcher repeatMatcher:this.repeatMatchers) repeatMatcher.clear();
        for(GroupMatcher.GroupEndMatcher groupEndMatcher:this.groupEndMatchers) groupEndMatcher.clear();
    }
}
