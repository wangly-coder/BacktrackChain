package com.tobethebest.regex.match;

import com.tobethebest.regex.ast.LengthMeasurer;
import com.tobethebest.regex.ast.LengthMeasurer.LengthInfo;
import com.tobethebest.regex.ast.RegexParser;
import com.tobethebest.regex.ast.exp.RegexExp;
import com.tobethebest.regex.match.matcher.ChainMatcher;
import com.tobethebest.regex.match.matcher.RepeatMatcher;
import com.tobethebest.regex.match.group.GroupMatcher;
import com.tobethebest.regex.match.group.GroupPair;

import java.util.ArrayList;
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
    public int groupNumber; // 组数量，用于后续初始化组内容容器

    public List<RepeatMatcher> repeatMatcherList; // 待清理的RM
    public GroupMatcher.GroupEndMatcher[] groupEndMatchers; // 所有的组结尾匹配器
    public LinkedHashMap<String,Integer> groupNameToIdMap; // 组名->组id映射


    protected MatchContext(){}

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
        this.repeatMatcherList = new ArrayList<>(2);
        this.groupEndMatchers = new GroupMatcher.GroupEndMatcher[this.groupNumber];
        this.headMatcher = MatcherChainBuilder.build(regexExp,this);
    }

    public void addRepeatMatcher(RepeatMatcher repeatMatcher){
        this.repeatMatcherList.add(repeatMatcher);
    }

    public void addGroupEndMatcher(GroupMatcher.GroupEndMatcher groupEndMatcher){
        this.groupEndMatchers[groupEndMatcher.groupId -1] = groupEndMatcher;
    }

    public void updateGroupEndMatcher(int groupId,GroupPair groupPair){
        if(groupPair == null) return;
        this.groupEndMatchers[groupId-1].updateIndex(groupPair);
    }

    public void clear(){
        for(RepeatMatcher repeatMatcher:this.repeatMatcherList) repeatMatcher.clear();
    }
}
