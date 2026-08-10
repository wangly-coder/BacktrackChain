package com.wly.regex;

import com.wly.regex.ast.LengthMeasurer;
import com.wly.regex.ast.RegexParser;
import com.wly.regex.ast.exp.RegexExp;
import com.wly.regex.match.ChainMatcher;
import com.wly.regex.match.MatcherChainBuilder;
import com.wly.regex.match.control.MatchController;
import com.wly.regex.match.search.Pointer;
import com.wly.regex.match.RepeatMatcher;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.back.BackPoint;
import com.wly.regex.match.search.SearchResult;

import java.util.ArrayList;
import java.util.List;

public class RegexMatcher {
    protected ChainMatcher headMatcher;
    protected MatchController controller;

    public RegexMatcher(String regexString){
        this(new RegexParser(regexString));
    }

    public RegexMatcher(RegexParser parser){
        RegexExp regexExp = parser.parse();
        this.headMatcher = MatcherChainBuilder.build(regexExp);
        this.controller = new MatchController(parser);
        controller.lengthInfo = LengthMeasurer.measureLength(regexExp);
    }

    /**
     * 以指针的形式并从该下标匹配适合的目标字符串的前缀子串，匹配一次结束
     * @return 是否匹配成功
     */
    protected boolean match(String str,Pointer pointer,BackContext context) {
        // 匹配链开始匹配
        if(this.headMatcher.chainMatch(str,pointer,context)) return true;
        // 失败则回溯
        return this.backtrack(str,pointer,context);
    }

    // 回溯处理函数，直到回溯匹配成功或者没有BP后才宣告失败
    protected boolean backtrack(String str,Pointer pointer,BackContext context){
        // 失败则回溯
        BackPoint backPoint = context.restore();
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
                if(!headMatcher.chainMatch(str, pointer, context)) {
                    backPoint = context.restore();
                    continue;
                }
                // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯处理
                if(this.handleBackRepeatNest(str,pointer,context,backPoint)) return true;
                backPoint = context.restore();
            }
            // backPoint.bptype == BackPoint.BPTYPE.REPEAT
            else {
                // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯处理
                if(this.handleBackRepeatNest(str,pointer,context,backPoint)) return true;
                backPoint = context.restore();
            }
        }
        return false;
    }

    // 处理回溯时的RM嵌套
    protected boolean handleBackRepeatNest(String str,Pointer pointer,BackContext context,BackPoint backPoint){
        // 如果成功了还需要判断是否是某个RM的内部，如果是则需要进入RM回溯
        RepeatMatcher preRepeatMatcher = backPoint.preOrSelfRepeatMatcher;
        // 如果处理的是最外层的UM，其外部匹配链已经处理好了，无需再匹配
        if(preRepeatMatcher == null && backPoint.bptype == BackPoint.BPTYPE.UNION) return true;
        if(preRepeatMatcher != null) {
            // 如果回溯处理失败，那么继续回溯
            if(!preRepeatMatcher.back(str,pointer,context,backPoint)) return false;
        }
        // 达到了是最外部的RM了，直接拿取外部匹配链
        ChainMatcher nextMatcher = backPoint.nextMatcher;
        if(nextMatcher == null) return true;
        // 匹配成功则返回，否则继续回溯
        return nextMatcher.chainMatch(str, pointer, context);
    }

    /**
     * 判断给定字符串是否整个符合正则表达式，等同于正则表达式加上^$两个限定符
     * @param str 判断的目标字符串
     * @return 是否完全匹配
     */
    public boolean matchAll(String str){
        // 如果目标字符串小于理论最小长度或者大于了理论最大长度，那么绝不可能匹配成功
        int strLength = str.length();
        int maxLength = this.controller.lengthInfo.maxLength;
        if(strLength < this.controller.lengthInfo.minLength
                || (maxLength != -1 && strLength > maxLength)) return false;
        Pointer pointer = new Pointer();
        BackContext context = new BackContext();
        // 尝试了所有可能都没有匹配从下标为0开始的任何子串，那么完全失败
        if(!this.match(str,pointer,context)) return false;
        /*
        检查指针是否到了末尾。如果没有到达末尾，那么是匹配了更短的子串，如果还有BP的话需要继续回溯
        更短的子串可能是模式串需要匹配的文本长度更短，本身语义单元较少或者非贪婪匹配，也有可能是目标文本更长
         */
        while(!(pointer.index == strLength)){
           if(!this.backtrack(str,pointer,context)) return false;
        }
        return true;
    }

    /**
     * 以指针的形式从目标字符串中匹配符合模式串的子串
     * @param str 目标字符串
     * @param pointer 游标
     * @return 是否搜寻成功
     */
    public boolean match(String str,Pointer pointer){
        if(pointer == null) pointer = new Pointer();
        // 计算理论上指针匹配的起始位置能够到达的最远下标
        int strLength = str.length();
        int maxIndex = strLength - this.controller.lengthInfo.minLength;
        // 目标字符串连最小长度都达不到
        if(maxIndex < 0) return false;
        // 如果指针已经超过了最远下标，那么不用匹配了，直接返回false
        if(pointer.index > maxIndex) return false;
        BackContext backContext = new BackContext();
        while(pointer.index <= maxIndex){
            // 匹配成功直接返回
            if(this.match(str,pointer,backContext)) return true;
            // 失败则进行下一次匹配
            pointer.backward();
            pointer.bothForward();
            backContext.reset();
        }
        return false;
    }

    /**
     * 从指定下标开始从目标字符串中匹配符合模式串的子串
     * @return 是否搜寻成功
     */
    public boolean match(String str,int index){
        return this.match(str,new Pointer(index));
    }

    /**
     * 从头开始搜索目标字符串中符合模式串的子串
     * @param str 目标字符串
     * @return 可以迭代搜索的SearchResult对象
     */
    public SearchResult search(String str){
        Pointer pointer = new Pointer();
        // 将pointer保存在SearchResult中而不是RegexMatcher，提高了其线程安全性
        boolean isSearch = this.match(str,pointer);
        if(!isSearch) return null;
        if(this.controller.hasStartLimit || this.controller.hasEndLimit) return new SearchResult(null,str,pointer,true);
        return new SearchResult(this,str,pointer,false);
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
        do result.add(searchResult.getMatchedString());
        while (searchResult.next());
        return result;
    }
}
