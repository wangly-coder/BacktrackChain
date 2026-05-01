package com.wly.regex.auto;

import com.wly.regex.auto.edge.Edge;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class State {
    @EqualsAndHashCode.Include
    public int id;
    public boolean isStart; // 是否开始状态
    public boolean isEnd; // 是否是结束状态
    public List<Edge> edgeList; // 邻接表

    public static State of(int id){
        return new State(id,false,false,new ArrayList<>());
    }

    public String toString(){
        return this.printSelf();
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
