package com.wly.regex.auto;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@NoArgsConstructor
public class NFA {
    public State start;
    public State end;

    public NFA(State start,State end){
        start.isStart = true;
        start.isEnd =false;
        end.isStart = false;
        end.isEnd = true;
        this.start = start;
        this.end = end;
    }

    /**
     * 以字符串的形式打印NFA
     * @return 字符串化NFA
     */
    public String printSelf(){
        StringBuilder stringBuilder = new StringBuilder();
        Queue<State> stateQueue = new LinkedList<>();
        boolean[] visited = new boolean[State.sharedId-1];
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
