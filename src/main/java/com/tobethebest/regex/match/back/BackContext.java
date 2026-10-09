package com.tobethebest.regex.match.back;

import com.tobethebest.regex.match.MatchContext;
import com.tobethebest.regex.match.Pointer;
import com.tobethebest.regex.match.matcher.RepeatMatcher;
import com.tobethebest.regex.match.group.GroupPair;

import java.util.*;

public class BackContext {
    public String searchString;
    public int maxIndex;
    public ArrayDeque<BackPoint> backStack;
    public GroupPair[] groupPairs;
    public MatchContext matchContext;

    public BackContext(String searchString,MatchContext matchContext){
        this.backStack = new ArrayDeque<>();
        this.searchString = searchString;
        this.matchContext = matchContext;
        // 计算指针能达到的最大下标
        this.maxIndex = searchString.length() - this.matchContext.lengthInfo.minLength;
    }

    public void initGroupPairs(){
        this.groupPairs = new GroupPair[this.matchContext.groupNumber];
    }

    public void removeBackPoints(int removeCount){
        if(removeCount <= 0) return;
        for(int i = 0;i<removeCount;i++) this.backStack.pop();
    }

    // 设置回溯点
    public void store(BackPoint backPoint){
        if(backPoint == null) return;
        this.backStack.push(backPoint);
    }

    /**
     * 从最近的回溯点回溯
     */
    public BackPoint restore(Pointer pointer){
        if(this.backStack.isEmpty()) return null;
        BackPoint backPoint = this.backStack.pop();
        // 恢复指针位置
        pointer.index = backPoint.index;
        // 先进行回溯处理再进行各个状态恢复。对于RM来说需要调整preRM的指向
        if(backPoint.backFunc != null) backPoint.backFunc.run();
        // 恢复各层RM计数
        RepeatMatcher preRepeatMatcher = backPoint.preOrSelfRepeatMatcher;
        int i = 0;
        while(preRepeatMatcher != null) {
            preRepeatMatcher.count = backPoint.preCounts.get(i++);
            preRepeatMatcher = preRepeatMatcher.preRepeatMatcher;
        }
        backPoint.preCounts = null;
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
        for(int i = maxGroupId;i<this.groupPairs.length;i++) this.groupPairs[i] = null;
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
