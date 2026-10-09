package com.tobethebest.regex.match.assertion;

import com.tobethebest.regex.ast.LengthMeasurer;
import com.tobethebest.regex.ast.exp.LookaroundExp;
import com.tobethebest.regex.match.MatcherVisitor;
import com.tobethebest.regex.match.Pointer;
import com.tobethebest.regex.match.back.BackContext;
import com.tobethebest.regex.match.back.BackPoint;
import com.tobethebest.regex.match.matcher.ChainMatcher;

import  java.lang.Math;

public class LookaroundMatcher extends ChainMatcher{
        public LookaroundExp.LookaroundType lookaroundType;
        public LengthMeasurer.LengthInfo lengthInfo;
        public ChainMatcher lookaroundChainHead;

        protected int startIndex; // 用于LEM恢复指针下标
        protected int preBackStackSize; // 记录匹配前的回溯栈大小，用于成功后删除。用于肯定断言

        public LookaroundMatcher(LookaroundExp.LookaroundType lookaroundType, LengthMeasurer.LengthInfo lengthInfo,ChainMatcher lookaroundChainHead) {
            this.lookaroundType = lookaroundType;
            this.lengthInfo = lengthInfo;
            this.lookaroundChainHead = lookaroundChainHead;
        }

        @Override
        public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
            int curIndex = pointer.index;
            this.startIndex = curIndex;
            this.preBackStackSize = backContext.backStack.size();
            BackPoint backPoint;
            ChainMatcher nextMatcher;
            switch (this.lookaroundType){
                case RIGHT_POSITIVE:
                    // 判断剩余长度是否满足最小值要求
                    if(str.length() - curIndex < this.lengthInfo.minLength) return false;
                    // 循环内部匹配，失败则使用内部产生的回溯点，直到用完或者匹配成功
                    nextMatcher = this.lookaroundChainHead;
                    while(true) {
                        if(nextMatcher.chainMatch(str,pointer,backContext)) {
                            // 成功丢弃回溯点
                            backContext.removeBackPoints(backContext.backStack.size() - this.preBackStackSize);
                            // 恢复指针
                            pointer.index = startIndex;
                            return true;
                        }
                        if(preBackStackSize == backContext.backStack.size()) return false;
                        backPoint = backContext.restore(pointer);
                        nextMatcher = backPoint.nextMatcher;
                    }
                case RIGHT_NEGATIVE:
                    // 如果剩余长度不满足最小长度，那么一定不匹配，
                    if(str.length() - curIndex < this.lengthInfo.minLength) return true;
                    // 循环内部匹配，失败则回溯，直到耗尽所有内部产生的回溯点才算成功
                    nextMatcher = this.lookaroundChainHead;
                    while(true) {
                        if(nextMatcher.chainMatch(str,pointer,backContext)) {
                            // 抛弃所有回溯点
                            backContext.removeBackPoints(backContext.backStack.size() - preBackStackSize);
                            return false;
                        }
                        if(preBackStackSize == backContext.backStack.size()){
                            // 恢复指针
                            pointer.index = startIndex;
                            return true;
                        }
                        backPoint = backContext.restore(pointer);
                        nextMatcher = backPoint.nextMatcher;
                    }
                case LEFT_POSITIVE:
                    // 先检查匹配过的长度是否满足最小要求
                    int maxIndex = curIndex - this.lengthInfo.minLength;
                    if(maxIndex < 0) return false;
                    // 确定最小下标
                    int minIndex = this.lengthInfo.maxLength == -1 ? 0 : Math.max(curIndex - this.lengthInfo.maxLength,0);
                    // 满足则进行匹配，找到最远匹配下标并从该下标（最短长度）开始匹配
                    nextMatcher = this.lookaroundChainHead;
                    for(int i = maxIndex; i >= minIndex ;i--) {
                        pointer.index = i;
                        while(true) {
                            while(nextMatcher != null && nextMatcher.doMatch(str,pointer,backContext) && pointer.index <= startIndex){
                                nextMatcher = nextMatcher.next;
                            }
                            // 如果成功
                            if(nextMatcher == null && pointer.index == startIndex) {
                                // 丢弃内部回溯点
                                backContext.removeBackPoints(backContext.backStack.size() - preBackStackSize);
                                return true;
                            }
                            if(preBackStackSize == backContext.backStack.size()) break;
                            backPoint = backContext.restore(pointer);
                            nextMatcher = backPoint.nextMatcher;
                        }
                    }
                    return false;
                case LEFT_NEGATIVE:
                    // 先检查匹配过的长度是否满足最小要求
                    maxIndex = curIndex - this.lengthInfo.minLength;
                    if(maxIndex < 0) return true;
                    // 确定最小下标
                    minIndex = this.lengthInfo.maxLength == -1 ? 0 : Math.max(curIndex - this.lengthInfo.maxLength,0);
                    // 满足则进行匹配，找到最远匹配下标并从该下标（最短长度）开始匹配
                    nextMatcher = this.lookaroundChainHead;
                    for(int i = maxIndex; i >= minIndex ;i--) {
                        pointer.index = i;
                        while (true) {
                            while (nextMatcher != null && nextMatcher.doMatch(str, pointer, backContext) && pointer.index <= startIndex) {
                                nextMatcher = nextMatcher.next;
                            }
                            // 如果成功
                            if (nextMatcher == null && pointer.index == startIndex) {
                                // 丢弃内部回溯点
                                backContext.removeBackPoints(backContext.backStack.size() - preBackStackSize);
                                return false;
                            }
                            if (preBackStackSize == backContext.backStack.size()) break;
                            backPoint = backContext.restore(pointer);
                            nextMatcher = backPoint.nextMatcher;
                        }
                    }
                    pointer.index = startIndex;
                    return true;
            }
            throw new RuntimeException(String.format("不应该出现的错误。未知的环视类型%s",this.lookaroundType));
        }

        @Override
        public <R, C> R accept(MatcherVisitor<R, C> matcherVisitor, C context) {
            return matcherVisitor.visit(this,context);
        }
}