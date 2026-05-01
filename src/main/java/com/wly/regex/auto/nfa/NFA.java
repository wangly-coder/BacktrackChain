package com.wly.regex.auto.nfa;

import com.wly.regex.auto.State;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class NFA {
    public NFAContext nfaContext;

    public NFA(State start, State end, NFAContext nfaContext) {
        this.nfaContext = nfaContext;
        this.nfaContext.setStart(start);
        this.nfaContext.setEnd(end);
    }

    /**
     * 判断目标字符串是否匹配
     * @param target 匹配的目标字符串
     * @return 匹配是否成功
     */
    public boolean match(String target){
        // 加入开始状态
        Set<State> transferedSet = new HashSet<>();
        transferedSet.add(this.nfaContext.start);
        // 逐个字符消耗直到结束
        for(int i=0;i<target.length();i++) transferedSet = this.nfaContext.move(transferedSet,target.charAt(i));
        // 如果最终的状态集合包含了结束状态，即视为成功
        return transferedSet.contains(this.nfaContext.end);
    }

    public String printSelf() {
        return this.nfaContext.printSelf();
    }
}
