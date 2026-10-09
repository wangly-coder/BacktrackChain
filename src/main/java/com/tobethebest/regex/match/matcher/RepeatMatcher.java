package com.tobethebest.regex.match.matcher;

import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.back.BackPoint;
import com.tobethebest.regex.match.Pointer;
import com.tobethebest.regex.match.group.GroupPair;

import java.util.*;

/**
 * 开放量词匹配器
 */
public abstract class RepeatMatcher extends ChainMatcher {
    public int min;
    public int max;
    public boolean greedy;
    public boolean matchEmptyString; // 是否可以匹配空串
    public ChainMatcher repeatChainHead;
    public RepeatMatcher preRepeatMatcher;
    public int count;

    public int maxGroupId; // 内部最大的捕获组id

    public void setMin(int min){
        this.min = min;
        this.matchEmptyString = min == 0;
    }

    public abstract void clear();

    /**
     * 获取以当前对象开始到最外层的RM的当前计数信息
     */
    public List<Integer> getPreCounts(){
        List<Integer> preCounts = new ArrayList<>();
        RepeatMatcher preRepeatMatcher = this;
        while(preRepeatMatcher != null){
            preCounts.add(preRepeatMatcher.count);
            preRepeatMatcher = preRepeatMatcher.preRepeatMatcher;
        }
        return preCounts;
    }

    public static class RepeatStartMatcher extends ChainMatcher {
        // GRM回溯后，该下标位置它无法匹配。因为GRM回溯就意味着它在该位置匹配完了，无需再匹配
        public int backUnMatchIndex = -1;
        // 对于贪婪匹配是需要根据它保存快照。而非贪婪匹配需要它来重新匹配，那么就需要重置为未匹配状态
        public HashSet<Integer> innerMatchedIndexSet;
        // 非贪婪匹配上下文
        public Set<Integer> emptyStrMatchedIndexSet; // 空串匹配需要额外记录
        public boolean isSkip; // 是否跳过内部匹配
        public ChainMatcher oldNextMatcher; // 记录原先的下一个匹配器，搭配isSkip一起使用。

        public RepeatEndMatcher repeatEndMatcher;

        public RepeatStartMatcher(){
            this.innerMatchedIndexSet = new HashSet<>();
        }

        @Override
        public void setNext(ChainMatcher next) {
            this.repeatEndMatcher.next = next;
        }

        // 当前索引位置是否已经匹配过了
        public boolean innerMatched(int index){
            if(this.innerMatchedIndexSet.contains(index)) return true;
            this.innerMatchedIndexSet.add(index);
            return false;
        }

        // NGRM的专有方法，当前位置的空串是否匹配过了
        public boolean emptyStrMatched(int index){
            // 如果不匹配空串，那么就应该返回true，告诉调用者空串已经用过了，不能再用了。或者Set集合包含该索引
            if(!this.repeatEndMatcher.matchEmptyString || this.emptyStrMatchedIndexSet.contains(index)) return true;
            this.emptyStrMatchedIndexSet.add(index);
            return false;
        }

        protected void beforeSkip(){
            this.isSkip = true;
            this.oldNextMatcher = this.next;
            this.next = this.repeatEndMatcher;
        }

        // 否定断言跳过内部匹配后，用于恢复初始状态的
        protected void afterSkip(){
            this.isSkip = false;
            this.next = this.oldNextMatcher;
            this.oldNextMatcher = null;
        }

        @Override
        public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
            if(!this.repeatEndMatcher.greedy) return this.notGreedyMatch(str, pointer, backContext);
            int curIndex = pointer.index;
            // 如果已经匹配过，则直接返回false
            if(this.innerMatched(curIndex)) return false;
            // 回溯后不应该再匹配，否则发生匹配-循环问题
            if(this.backUnMatchIndex == curIndex) return false;
            // 记录ZBP
            if(this.repeatEndMatcher.matchEmptyString) this.repeatEndMatcher.setBackPoint(curIndex,backContext);
            // 每次重新匹配都要重置计数值
            this.repeatEndMatcher.count = 0;
            return true;
        }

        public boolean notGreedyMatch(String str, Pointer pointer, BackContext backContext){
            int curIndex = pointer.index;
            // 可以匹配空串则匹配并记录
            if(!this.emptyStrMatched(curIndex)) {
                this.repeatEndMatcher.setBackPoint(curIndex,backContext);
                // 跳过内部匹配器
                this.beforeSkip();
                return true;
            }
            // 如果已经匹配过，则直接返回false
            if(this.innerMatched(curIndex)) return false;
            this.repeatEndMatcher.count = 0;
            return true;
        }

        @Override
        public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
            return matcherVisitor.visit(this,context);
        }

        public void clear(){
            this.backUnMatchIndex = -1;
            this.innerMatchedIndexSet.clear();
            if(this.emptyStrMatchedIndexSet != null) this.emptyStrMatchedIndexSet.clear();
        }
    }

    public static class RepeatEndMatcher extends RepeatMatcher{
        public List<Integer> groupIdList; // 内部捕获组的id集合

        public RepeatStartMatcher repeatStartMatcher;

        public void setGreedy(boolean greedy) {
            this.greedy = greedy;
            if(!greedy) this.repeatStartMatcher.emptyStrMatchedIndexSet = new HashSet<>();
        }

        public void setGroupIds(List<Integer> groupIdList) {
            if(this.greedy) this.groupIdList = groupIdList;
            this.maxGroupId = groupIdList.stream().max(Integer::compareTo).orElse(0);
        }

    // 记录回溯点
    public void setBackPoint(int index, BackContext backContext){
        boolean greedy = this.greedy;
        ChainMatcher nextMatcher = greedy ? this.next : this.repeatStartMatcher;
        RepeatMatcher preRepeatMatcher = greedy ? this.preRepeatMatcher : this;
        BackPoint backPoint = new BackPoint(index,nextMatcher,preRepeatMatcher);
        // 如果是REPEAT类型，则需要记录捕获组快照
        if(greedy){
            // 不为空则记录，否则不记录
            GroupPair[] groupPairs = backContext.groupPairs;
            if(groupPairs != null && !this.groupIdList.isEmpty()){
                backPoint.greedyRepeatGroupPairs = new HashMap<>();
                // 取出自己内部维护的相关组数据
                for(Integer groupId : this.groupIdList) {
                    backPoint.greedyRepeatGroupPairs.put(groupId, groupPairs[groupId - 1]);
                }
            }
        }
        // 定义回溯函数
        backPoint.backFunc = () -> {
            // 清空大于maxGroupId的所有组状态信息
            backContext.resetGroupParis(this.maxGroupId);
            // 如果是REPEAT那么就要处理匹配-回溯循环相关问题
            if(greedy){
                this.repeatStartMatcher.backUnMatchIndex = index;
                backPoint.preOrSelfRepeatMatcher = this.preRepeatMatcher;
                // 恢复内部各个组状态。顺序记录，顺序恢复
                backContext.restoreGroupPairs(backPoint.greedyRepeatGroupPairs);
            }
        };
        backContext.store(backPoint);
    }

        @Override
        public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
            // 如果是NGRM跳过
            if(this.repeatStartMatcher.isSkip){
                this.repeatStartMatcher.afterSkip();
                return true;
            }
            int curIndex;
            // 内层成功匹配了一次
            while(true){
                int count = ++this.count;
                // 如果已经达到了最大值，就直接退出该RM。
                if(count == this.max) return true;
                // 如果没有达到最大值但是达到了最小值则需要记录回溯
                curIndex = pointer.index;
                if(count >= this.min) {
                    this.setBackPoint(curIndex,backContext);
                    if(!this.greedy) return true;
                }
                // 如果匹配过了或者下一次匹配失败了，尝试使用上一次的匹配结果，没有则失败返回false
                if(this.repeatStartMatcher.innerMatched(curIndex) || !repeatChainHead.chainMatch(str, pointer, backContext,this)) return false;
            }
        }

        @Override
        public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
            return matcherVisitor.visit(this,context);
        }

        @Override
        public void clear() {
            this.repeatStartMatcher.clear();
        }
    }

}