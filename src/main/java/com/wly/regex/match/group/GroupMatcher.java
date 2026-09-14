package com.wly.regex.match.group;

import com.wly.regex.match.Pointer;
import com.wly.regex.match.back.BackContext;
import com.wly.regex.match.matcher.ChainMatcher;

public class GroupMatcher{

    public static class GroupStartMatcher extends ChainMatcher {
        public int startIndex;
        public GroupEndMatcher groupEndMatcher;

        public GroupStartMatcher() {}

        @Override
        public String toString() {
            return String.format("GroupStartMatcher:%d",this.groupEndMatcher.groupId);
        }

        @Override
        public String printSelf(){
            return String.format("[GroupStart:%d]",this.groupEndMatcher.groupId);
        }

        // 在MatcherChainBuilder中GSM作为返回值，那么当它设置下一个Matcher时，实际上就是GEM设置
        @Override
        public void setNext(ChainMatcher next) {
            this.groupEndMatcher.next = next;
        }

        @Override
        public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
            this.startIndex = pointer.index;
            return true;
        }

        public void clear(){
            this.startIndex = 0;
        }
    }

    public static class GroupEndMatcher extends ChainMatcher {
        public int groupId;
        public int endIndex;
        public GroupStartMatcher groupStartMatcher;

        public GroupEndMatcher(int groupId,GroupStartMatcher groupStartMatcher) {
            this.groupId = groupId;
            this.groupStartMatcher = groupStartMatcher;
        }

        @Override
        public String toString() {
            return String.format("GroupEndMatcher:%d",this.groupId);
        }

        @Override
        public String printSelf(){
            return String.format("[GroupEnd:%d]",this.groupId);
        }

        public void updateIndex(GroupPair groupPair){
            this.endIndex = groupPair.endIndex;
            this.groupStartMatcher.startIndex = groupPair.startIndex;
        }

        @Override
        public boolean doMatch(String str, Pointer pointer, BackContext backContext) {
            this.endIndex = pointer.index;
            // 往回溯容器中写入Pair
            backContext.addGroupPair(this.groupId,GroupPair.of(this.groupStartMatcher.startIndex,this.endIndex));
            return true;
        }

        public void clear(){
            this.endIndex = 0;
            this.groupStartMatcher.clear();
        }
    }
}
