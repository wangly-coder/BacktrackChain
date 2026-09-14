package com.wly.regex.match.back;

import com.wly.regex.match.MatchContext;
import com.wly.regex.match.matcher.RepeatMatcher;
import com.wly.regex.match.group.GroupPair;

import java.util.*;

public class BackContext {
    public String searchString;
    public int maxIndex = -1;
    public Stack<BackPoint> backStack;
    public GroupPair[] groupPairs;
    public MatchContext matchContext;

    public BackContext(String searchString,MatchContext matchContext){
        this.backStack = new Stack<>();
        this.searchString = searchString;
        this.matchContext = matchContext;
        // 计算指针能达到的最大下标
        this.maxIndex = searchString.length() - this.matchContext.lengthInfo.minLength;
    }

    public void initGroupPairs(){
        this.groupPairs = new GroupPair[this.matchContext.groupNumber];
    }

    // 设置回溯点
    public void store(BackPoint backPoint){
        if(backPoint == null) return;
        this.backStack.push(backPoint);
    }

    /**
     * 从最近的回溯点回溯
     */
    public BackPoint restore(){
        if(this.backStack.empty()) return null;
        BackPoint backPoint = this.backStack.pop();
        backPoint.backFunc.run();
        // 恢复各层RM计数
        RepeatMatcher preRepeatMatcher = backPoint.preOrSelfRepeatMatcher;
        int i = 0;
        while(preRepeatMatcher != null) {
            preRepeatMatcher.count = backPoint.preCounts.get(i++);
            preRepeatMatcher = preRepeatMatcher.preRepeatMatcher;
        }
        return backPoint;
    }

    public void addGroupPair(int groupId, GroupPair pair){
        if(this.groupPairs == null) this.initGroupPairs();
        this.groupPairs[groupId - 1] = pair;
    }

    public void resetGroupPairs(List<Integer> groupIds){
        if(this.groupPairs == null || groupIds == null || groupIds.isEmpty()) return;
        for(Integer groupId : groupIds){
            this.groupPairs[groupId - 1] = null;
        }
    }

    public void resetGroupParis(int maxGroupId){
        if(maxGroupId < 1 || this.groupPairs == null) return;
        // 注意清除的不包括它自己
        for(int i = maxGroupId+1;i<=this.groupPairs.length;i++) this.groupPairs[i-1] = null;
    }

    // 回溯恢复捕获组数据
    public void restoreGroupPairs(Map<Integer,GroupPair> pairs){
        if(pairs == null || pairs.isEmpty()) return;
        if(this.groupPairs == null) this.initGroupPairs();
        // 恢复GroupPair数组的值和GSM和GEM保存的索引值
        for(Map.Entry<Integer,GroupPair> pair: pairs.entrySet()){
            int groupId = pair.getKey();
            GroupPair groupPair = pair.getValue();
            this.groupPairs[groupId - 1] = groupPair;
            this.matchContext.updateGroupEndMatcher(groupId,groupPair);
        }
    }

    public GroupPair getGroupPair(int groupId){
        if(this.groupPairs == null) return null;
        return this.groupPairs[groupId - 1];
    }

    public void clear(){
        this.backStack.clear();
        if(this.groupPairs != null) Arrays.fill(this.groupPairs, null);
    }
}
