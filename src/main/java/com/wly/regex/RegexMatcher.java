package com.wly.regex;

import com.wly.regex.ast.RegexParser;
import com.wly.regex.match.matcher.ChainMatcher;
import com.wly.regex.match.MatchContext;
import com.wly.regex.match.Pointer;
import com.wly.regex.match.matcher.RepeatMatcher;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.back.BackPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RegexMatcher {
    protected MatchContext matchContext;

    public RegexMatcher(String regexString){
        this(new RegexParser(regexString),false);
    }

    public RegexMatcher(String regexString,boolean isCloseCheck){
        this(new RegexParser(regexString),isCloseCheck);
    }

    private RegexMatcher(RegexParser parser,boolean isCloseCheck){
        if(isCloseCheck) parser.closeNestCheck();
        this.matchContext = new MatchContext(parser);
    }

    /**
     * 以指针的形式并从该下标匹配适合的目标字符串的前缀子串，匹配一次结束
     * @return 是否匹配成功
     */
    protected boolean matchOnce(String str,Pointer pointer,BackContext backContext) {
        // 匹配链开始匹配
        if(this.matchContext.headMatcher.chainMatch(str,pointer,backContext)) return true;
        // 失败则回溯
        return this.backtrack(str,pointer,backContext);
    }

    // 回溯处理函数，直到回溯匹配成功或者没有BP后才宣告失败
    protected boolean backtrack(String str,Pointer pointer,BackContext backContext){
        // 失败则回溯
        BackPoint backPoint = backContext.restore();
        while(backPoint != null) {
            // 恢复指针位置
            pointer.index = backPoint.index;
            /*
                根据不同类型进行不同处理
                始终贯穿回溯在外部，外部处理器在外部处理的原则
            */
            if(backPoint.bptype == BackPoint.BPTYPE.UNION){
                ChainMatcher headMatcher = backPoint.nextMatcher;
                // 走完该UM的这条路径，失败则继续回溯
                if(!headMatcher.chainMatch(str, pointer, backContext)) {
                    backPoint = backContext.restore();
                    continue;
                }
                // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯处理
                if(this.handleBackRepeatNest(str,pointer,backContext,backPoint)) return true;
                backPoint = backContext.restore();
            }
            // backPoint.bptype == BackPoint.BPTYPE.REPEAT
            else {
                // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯处理
                if(this.handleBackRepeatNest(str,pointer,backContext,backPoint)) return true;
                backPoint = backContext.restore();
            }
        }
        return false;
    }

    // 处理回溯时的RM嵌套
    protected boolean handleBackRepeatNest(String str,Pointer pointer,BackContext backContext,BackPoint backPoint){
        // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯
        RepeatMatcher preRepeatMatcher = backPoint.preOrSelfRepeatMatcher;
        // 如果处理的是最外层的UM，其外部匹配链已经处理好了，无需再匹配
        if(preRepeatMatcher == null && backPoint.bptype == BackPoint.BPTYPE.UNION) return true;
        if(preRepeatMatcher != null) {
            // 如果回溯处理失败，那么继续回溯
            if(!preRepeatMatcher.back(str,pointer,backContext,backPoint)) return false;
        }
        // 达到了是最外部的RM了，直接拿取外部匹配链
        ChainMatcher nextMatcher = backPoint.nextMatcher;
        if(nextMatcher == null) return true;
        // 匹配成功则返回，否则继续回溯
        return nextMatcher.chainMatch(str, pointer, backContext);
    }

    /**
     * 判断给定字符串是否整个符合正则表达式，等同于正则表达式加上^$两个限定符
     * @param str 判断的目标字符串
     * @return 是否完全匹配
     */
    public boolean matchAll(String str){
        // 如果目标字符串小于理论最小长度或者大于了理论最大长度，那么绝不可能匹配成功
        int strLength = str.length();
        int maxLength = this.matchContext.lengthInfo.maxLength;
        if(strLength < this.matchContext.lengthInfo.minLength ||
                (maxLength != -1 && strLength > maxLength)) return false;
        Pointer pointer = new Pointer();
        BackContext backContext = new BackContext(str,this.matchContext);
        boolean isMatchAll = false;
        // 尝试匹配成功
        if(this.matchOnce(str,pointer,backContext) && pointer.index == strLength) isMatchAll = true;
        else{
            /*
            检查指针是否到了末尾。如果没有到达末尾，那么是匹配了更短的子串，如果还有BP的话需要继续回溯
            更短的子串可能是模式串需要匹配的文本长度更短，本身语义单元较少或者非贪婪匹配，也有可能是目标文本更长
             */
            while(true){
                // 如果没有回溯点了，那么退出
                if(backContext.backStack.empty()) break;
                if(this.backtrack(str,pointer,backContext) && pointer.index == strLength) {
                    isMatchAll = true;
                    break;
                }
            }
        }
        // 清空缓存
        this.matchContext.clear();
        return isMatchAll;
    }

    /**
     * 以指针的形式从目标字符串中匹配符合模式串的子串
     * @param str 目标字符串
     * @param pointer 游标
     * @return 是否搜寻成功
     */
    protected boolean match(String str,Pointer pointer,BackContext backContext){
        while(pointer.index <= backContext.maxIndex){
            // 匹配成功直接返回
            if(this.matchOnce(str,pointer,backContext)){
                // 清空匹配器缓存。但未清空回溯上下文缓存，需要交给调用者处理！
                this.matchContext.clear();
                return true;
            }
            // 失败则进行下一次匹配
            pointer.backward();
            pointer.bothForward();
            // 清空上下文缓存
            backContext.clear();
            this.matchContext.clear();
        }
        return false;
    }

    /**
     * 从指定下标开始从目标字符串中匹配符合模式串的子串
     * @return 是否搜寻成功
     */
    public boolean match(String str,int index){
        return this.match(str,new Pointer(index),new BackContext(str,this.matchContext));
    }

    public boolean match(String str){
        return this.match(str,0);
    }

    /**
     * 从头开始搜索目标字符串中符合模式串的子串
     * @param str 目标字符串
     * @return 可以迭代搜索的SearchResult对象
     */
    public SearchResult search(String str){
        Pointer pointer = new Pointer();
        BackContext backContext = new BackContext(str,this.matchContext);
        if(this.match(str,pointer,backContext)){
            // 将pointer以及group等信息保存在SearchResult中而不是RegexMatcher，可复用性提高
            boolean isOnlyOnce = this.matchContext.hasStartLimit || this.matchContext.hasEndLimit;
            SearchResult searchResult = new SearchResult(this,pointer,backContext,isOnlyOnce);
            // 清空回溯上下文缓存
            backContext.clear();
            return searchResult;
        }
        return null;
    }

    /**
     * 从目标字符串中搜索所有符合模式串的子串
     * @param str 目标字符串
     * @return 所有匹配的子串集合
     */
    public List<String> searchAll(String str){
        SearchResult searchResult = this.search(str);
        if(searchResult == null) return null;
        List<String> result = new ArrayList<>();
        do result.add(searchResult.group(0));
        while (searchResult.next());
        return result;
    }

    public int groupSize(){
        return this.matchContext.groupNumber;
    }

    public Optional<Integer> getGroupId(String groupName){
        return Optional.ofNullable(this.matchContext.groupNameToIdMap.get(groupName));
    }

    public Set<String> getGroupNameSet(){
        return this.matchContext.groupNameToIdMap.keySet();
    }
}
