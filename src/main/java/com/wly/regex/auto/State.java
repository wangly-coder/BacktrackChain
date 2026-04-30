package com.wly.regex.auto;

import com.wly.regex.auto.edge.Edge;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class State {
    public int id;
    public boolean isStart; // 是否开始状态
    public boolean isEnd; // 是否是结束状态
    public List<Edge> edgeList; // 邻接表

    static int sharedId = 1; // 状态自增id

    /**
     * 用于test使用
     * @param value 重新设置的值，一般为1
     */
    public static void resetSharedId(int value){
        sharedId = value;
    }

    public static State getNewState(){
        return State.builder().id(sharedId++).edgeList(new ArrayList<>()).build();
    }

    public void addEdge(Edge edge){
        this.edgeList.add(edge);
    }

    public String printSelf(){
        return "S" + this.id;
    }

    public String printMove(){
        StringBuilder stringBuilder = new StringBuilder();
        this.edgeList.forEach(edge ->
                stringBuilder.append(this.printSelf()).append(edge.printSelf()).append("\n")
        );
        return stringBuilder.toString();
    }
}
