package com.wly.regex.match.matcher;

import com.wly.regex.match.MatcherChainPrinter;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.back.BackPoint;
import com.wly.regex.match.back.Backer;
import com.wly.regex.match.Pointer;
import com.wly.regex.match.group.GroupPair;

import java.util.*;

public class RepeatMatcher extends ChainMatcher implements Backer {
    public ChainMatcher repeatChainHead;
    private int min;
    public int max;
    public boolean greedy;
    public int count; // 当前计数值
    public RepeatMatcher preRepeatMatcher;
    public String name; // Repeat-m-n形式，m表示外层有几个RM，n表示是该层第几个RM

    // GRM回溯后，该下标位置它无法匹配。因为GRM回溯就意味着它在该位置匹配完了，无需再匹配
    public int backUnMatchIndex = -1;

    // 对于从某个下标开始内部匹配的位置，后续无需重新匹配
    public Set<Integer> innerMatchedIndexSet;

    // 非贪婪匹配中，空串匹配需要额外处理
    public Set<Integer> emptyStrMatchedIndexSet;

    // 捕获组相关上下文
    // 对于贪婪匹配是需要根据它保存快照。而非贪婪匹配需要它来重新匹配，那么就需要重置为未匹配状态
    public List<Integer> groupIdList; // 内部捕获组的id集合
    public int maxGroupId; // 内部最大的捕获组id

    public RepeatMatcher(){
        this.innerMatchedIndexSet = new HashSet<>();
    }

    public void setMin(int min) {
        this.min = min;
        this.matchEmptyString = min == 0;
    }

    public void setGreedy(boolean greedy) {
        this.greedy = greedy;
        if(!greedy) this.emptyStrMatchedIndexSet = new HashSet<>();
    }

    public void setGroupIds(List<Integer> groupIdList) {
        this.groupIdList = groupIdList;
        this.maxGroupId = groupIdList.stream().max(Integer::compareTo).orElse(0);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String printSelf(){
        return String.format("[%s:[Pre:%s,Count:{min:%d,max:%d,greedy:%s},Chain:{%s}]]",this.name,this.preRepeatMatcher == null ? "null" : this.preRepeatMatcher.name,
                this.min,this.max,this.greedy, MatcherChainPrinter.printChain(this.repeatChainHead));
    }

    // 创建回溯点
    public BackPoint createBackPoint(int index,BackContext backContext){
        BackPoint.BPTYPE bptype = this.greedy ? BackPoint.BPTYPE.REPEAT : BackPoint.BPTYPE.NG_REPEAT;
        BackPoint backPoint = new BackPoint(bptype,this.next,index,this);
        // 如果是REPEAT类型，则需要记录捕获组快照
        if(bptype == BackPoint.BPTYPE.REPEAT){
            // 不为空则记录，否则不记录
            GroupPair[] groupPairs = backContext.groupPairs;
            if(groupPairs != null && !this.groupIdList.isEmpty()){
                backPoint.repeatGroupPairs = new HashMap<>();
                // 取出自己内部维护的相关组数据
                for(Integer groupId : this.groupIdList) {
                    backPoint.repeatGroupPairs.put(groupId, groupPairs[groupId - 1]);
                }
            }
        }
        // 定义回溯函数
        backPoint.backFunc = () -> {
            // 清空大于maxGroupId的所有组状态信息
            backContext.resetGroupParis(this.maxGroupId);
            // 如果是REPEAT那么就要处理匹配-回溯循环相关问题
            if(bptype == BackPoint.BPTYPE.REPEAT){
                this.backUnMatchIndex = index;
                backPoint.preOrSelfRepeatMatcher = this.preRepeatMatcher;
                // 恢复内部各个组状态。顺序记录，顺序恢复
                backContext.restoreGroupPairs(backPoint.repeatGroupPairs);
            }
        };
        return backPoint;
    }

    // 记录回溯点
    public void storeBackPoint(int index, BackContext backContext){
        BackPoint backPoint = this.createBackPoint(index,backContext);
        backContext.store(backPoint);
    }

    public void storeBackPoint(BackPoint backPoint,BackContext backContext){
        if(backPoint == null) return;
        backContext.store(backPoint);
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
        if(!this.matchEmptyString || this.emptyStrMatchedIndexSet.contains(index)) return true;
        this.emptyStrMatchedIndexSet.add(index);
        return false;
    }

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

    /**
     * 重置RM避免匹配-回溯循环相关的变量，避免对下一次匹配产生影响
     */
    public void clear(){
        this.backUnMatchIndex = -1;
        this.innerMatchedIndexSet.clear();
    }

    @Override
    public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
        // 非贪婪匹配
        if(!this.greedy) return this.notGreedyMatch(str,pointer,backContext);
        int curIndex = pointer.index;
        // 如果已经匹配过，则直接返回false
        if(this.innerMatched(curIndex)) return false;
        // 回溯后不应该再匹配，否则发生匹配-循环问题
        if(this.backUnMatchIndex == curIndex) return false;
        // 每次调用该方法都是从头开始匹配。这里需要清空回溯导致的计数缓存
        this.count = 0;
        // 记录ZBP
        if(this.matchEmptyString) this.storeBackPoint(curIndex,backContext);
        // 开始循环匹配
        while(this.count < this.max || this.max == -1){
            if(!this.repeatChainHead.chainMatch(str,pointer,backContext)) return false;
            // 如果已经达到了最大值，就直接退出该RM
            if(++this.count == this.max) return true;
            // 如果没有达到最大值但是达到了最小值则需要记录回溯
            if(this.count >= this.min) this.storeBackPoint(pointer.index,backContext);
        }
        throw new RuntimeException("不应该出现的异常，可能是RepeatMatcher的min和max设置问题！");
    }

    @Override
    public boolean back(String str, Pointer pointer, BackContext backContext,BackPoint backPoint) {
        // 非贪婪回退
        if(!this.greedy) return this.notGreedyBack(str,pointer,backContext,backPoint);
        // 这里的nextMatcher是当前RM的内部RM的next
        ChainMatcher nextMatcher = backPoint.nextMatcher;
        // 内部RM走完，走当前RM的内部匹配链，如果是Union类型，直接通过，因为Union把外部的匹配链也走完了
        if(backPoint.bptype != BackPoint.BPTYPE.UNION
                && nextMatcher != null
                && !nextMatcher.chainMatch(str, pointer, backContext)) return false;
        // 内层成功匹配了一次
        while(this.count < this.max || this.max == -1){
            // 如果已经达到了最大值，就直接退出该RM。
            if(++this.count == this.max) break;
            // 如果没有达到最大值但是达到了最小值则需要记录回溯
            if(this.count >= this.min) this.storeBackPoint(pointer.index,backContext);
            // 如果匹配过了或者下一次匹配失败了，尝试使用上一次的匹配结果，没有则失败返回false
            if(this.innerMatched(pointer.index) || !repeatChainHead.chainMatch(str, pointer, backContext)) {
                return false;
            }
        }
        // 更新BP的nextMatcher为当前RM的外部Matcher，以便上一层使用
        backPoint.nextMatcher = this.next;
        // 如果当前已经是最外层RM了，那么交给全局上下文处理
        if(this.preRepeatMatcher == null) return true;
        // 不是最外层，交给上一层RM处理
        return this.preRepeatMatcher.back(str,pointer,backContext,backPoint);
    }

    /**
     * 非贪婪匹配，核心就是尽可能少的匹配，达到最低次数后就退出，并记录回溯点
     */
    public boolean notGreedyMatch(String str, Pointer pointer, BackContext backContext){
        // 每次调用该方法都是从头匹配
        this.count = 0;
        int curIndex = pointer.index;
        // 如果最小次数为0，需要记录一次
        // 避免自循环，不能在同一地方多次记录ZBP。记过了就返回失败，并尝试匹配
        if(!this.emptyStrMatched(curIndex)) {
            this.storeBackPoint(curIndex,backContext);
            return true;
        }
        // 开始循环匹配
        while(this.count != this.max){
            // 匹配过了或者匹配失败直接返回false
            if(this.innerMatched(pointer.index) || !this.repeatChainHead.chainMatch(str,pointer,backContext)) {
                return false;
            }
            // 如果已经达到了最小值就退出
            if(++this.count >= this.min) {
                // 如果没有达到最大值就需要记录回溯点。不能用小于，考虑-1的情况
                if(this.count != this.max) this.storeBackPoint(pointer.index,backContext);
                return true;
            }
        }
        throw new RuntimeException("不应该出现的异常，可能是RepeatMatcher的min和max设置问题！");
    }

    /**
     * 非贪婪匹配回退
     */
    public boolean notGreedyBack(String str, Pointer pointer, BackContext backContext,BackPoint backPoint){
        // 如果当前NGRM是记录回溯点的NGRM
        if(this == backPoint.preOrSelfRepeatMatcher && backPoint.bptype == BackPoint.BPTYPE.NG_REPEAT){
            // 如果该位置匹配过或者下一次匹配失败则返回false
            if(this.innerMatched(pointer.index) || !this.repeatChainHead.chainMatch(str,pointer,backContext)) return false;
            // 自己记录的说明count>=min，只需要再匹配一次即可。如果没有达到最大值就需要记录回溯点。不能用小于，考虑-1的情况
            if(++this.count != this.max) this.storeBackPoint(pointer.index,backContext);
            // 交给上一层RM调用back()
        }
        // 不是记录回溯点的NGRM，那么需要处理外部匹配链
        else {
            // 这里的nextMatcher是当前RM的内部RM的next
            ChainMatcher nextMatcher = backPoint.nextMatcher;
            // 内部RM走完，走当前RM的内部匹配链，如果是Union类型，直接通过，因为Union把外部的匹配链也走完了
            if(backPoint.bptype != BackPoint.BPTYPE.UNION
                    && nextMatcher != null
                    && !nextMatcher.chainMatch(str, pointer, backContext)) return false;
            while(this.count != max){
                // 内层成功匹配了一次即可跳出，因为是非贪婪匹配。
                // 而且只有没有达到最大值才要记录回溯点，否则直接后续匹配。不能用小于，考虑-1的情况
                if(++this.count >= this.min) {
                    if(this.count != this.max) this.storeBackPoint(pointer.index,backContext);
                    break;
                }
                // 如果该位置匹配过或者下一次匹配失败返回false
                if(this.innerMatched(pointer.index) || !repeatChainHead.chainMatch(str, pointer, backContext)) {
                    return false;
                }
            }
        }
        // 更新BP的nextMatcher为当前RM的外部Matcher，以便上一层使用
        backPoint.nextMatcher = this.next;
        // 如果当前已经是最外层RM了，那么交给全局上下文处理
        if(this.preRepeatMatcher == null) return true;
        // 不是最外层，交给上一层RM处理
        return this.preRepeatMatcher.back(str,pointer,backContext,backPoint);
    }
}
