package com.wly.regex.auto.nfa;

import com.google.common.collect.Lists;
import com.wly.regex.auto.State;
import com.wly.regex.auto.edge.EpsilonEdge;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class NFA {
    public NFAContext nfaContext;

    public NFA(State start, State end, NFAContext nfaContext) {
        this.nfaContext = nfaContext;
        this.nfaContext.setStart(start);
        this.nfaContext.setEnd(end);
    }

    /**
     * 计算状态集合消耗某个字符后到达的状态集合，不计算空闭包
     * @param stateSet 要计算的状态集合
     * @param consumed 消耗的字符
     * @return 到达的状态集合
     */
    public Set<State> move(Set<State> stateSet,char consumed){
        // 先计算这些状态的空闭包
        Set<State> stateSetClosure = new HashSet<>();
        stateSet.forEach(state -> stateSetClosure.addAll(this.getEpsilonClosure(state)));
        Set<State> result = new HashSet<>();
        stateSetClosure.forEach(stateClosure -> {
            stateClosure.edgeList.forEach(edge -> {
                // 如果匹配的话，那么就加入
                if(edge.canMove(consumed)){
                    State targetState = edge.targetState;
                    // 如果没有包含，那么加入并计算空边
                    if(!result.contains(targetState)){
                        result.add(targetState);
                        result.addAll(this.getEpsilonClosure(targetState));
                    }
                }
            });
        });
        return result;
    }

    /**
     * 计算某个状态的空闭包
     * @param stated 要计算的目标状态
     * @return 目标状态的空闭包集合
     */
    public Set<State> getEpsilonClosure(State stated){
        Set<State> result = new HashSet<>();
        Queue<State> stateQueue = new LinkedList<>(); // 准备访问队列
        result.add(stated);
        stateQueue.offer(stated);
        while(!stateQueue.isEmpty()){
            State state = stateQueue.poll();
            // 加入计数器相关机制做过滤处理
            state.edgeList.forEach(edge -> {
                // 转移字符为空则需要计算空闭包
                if(edge instanceof EpsilonEdge){
                    State targetState = edge.targetState;
                    // 如果没有访问过就加入预备队列中
                    if(!result.contains(targetState)){
                        stateQueue.offer(targetState);
                        result.add(targetState);
                    }
                }
            });
        }
        return result;
    }

    /**`
     * 以字符串的形式打印NFA
     * @return 字符串化NFA
     */
    public String printSelf(){
        StringBuilder stringBuilder = new StringBuilder();
        Queue<State> stateQueue = new LinkedList<>();
        boolean[] visited = new boolean[this.nfaContext.stateNum];
        // 处理开始状态
        stringBuilder.append(String.format("Start:%s",this.nfaContext.start.printSelf())).append("\n");
        stateQueue.offer(this.nfaContext.start);
        visited[this.nfaContext.start.id-1] = true;
        // 打印转移状态信息
        while(!stateQueue.isEmpty()){
            State state = stateQueue.poll();
            stringBuilder.append(state.printMove());
            // 将能访问到的状态加入到新状态中
            state.edgeList.forEach(edge -> {
                // 没有访问过才加入
                if(!visited[edge.targetState.id-1]) {
                    stateQueue.offer(edge.targetState);
                    visited[edge.targetState.id-1] = true;
                }
            });
        }
        // 处理结束状态
        stringBuilder.append(String.format("End:%s",this.nfaContext.end.printSelf())).append("\n");
        return stringBuilder.toString();
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
        for(int i=0;i<target.length();i++) transferedSet = this.move(transferedSet,target.charAt(i));
        // 如果最终的状态集合包含了结束状态，即视为成功
        return transferedSet.contains(this.nfaContext.end);
    }
}
