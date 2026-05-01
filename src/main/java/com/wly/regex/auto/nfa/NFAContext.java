package com.wly.regex.auto.nfa;

import com.wly.regex.auto.State;
import com.wly.regex.auto.StateContext;
import com.wly.regex.auto.edge.EpsilonEdge;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class NFAContext extends StateContext {

    @Override
    public State createState() {
        return State.of(++this.stateNum);
    }

    /**
     * 计算状态集合消耗某个字符后到达的状态集合
     * @param stateSet 要计算的状态集合
     * @param consumed 消耗的字符
     * @return 到达的状态集合
     */
    public Set<State> move(Set<State> stateSet,char consumed){
        // 先计算这些状态的空闭包
        Set<State> stateSetClosure = new HashSet<>();
        stateSet.forEach(state -> stateSetClosure.addAll(this.getEpsilonClosure(state)));
        // 再计算这些空闭包消耗字符后到达的状态
        Set<State> result = new HashSet<>();
        stateSetClosure.forEach(stateClosure -> {
            stateClosure.edgeList.forEach(edge -> {
                // 如果匹配的话，那么就加入
                if(edge.canMove(consumed)){
                    State targetState = edge.targetState;
                    // 如果不包含即没有访问到过则加入并加入新的空闭包
                    if(!result.contains(targetState)){
                        result.add(targetState);
                        // 计算新状态的空闭包并加入
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
        boolean[] visited = new boolean[this.stateNum];
        // 处理开始状态
        stringBuilder.append(String.format("Start:%s",this.start.printSelf())).append("\n");
        stateQueue.offer(this.start);
        visited[this.start.id-1] = true;
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
        stringBuilder.append(String.format("End:%s",this.end.printSelf())).append("\n");
        return stringBuilder.toString();
    }
}
