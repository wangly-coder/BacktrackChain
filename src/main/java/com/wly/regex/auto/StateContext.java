package com.wly.regex.auto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 状态容器，NFA/DFA形成之前存储着相关的所有状态信息
 * 代理NFA/DFA做核心操作
 */
public abstract class StateContext {
    public State start;
    public State end;
    public int stateNum;
    public List<State> stateList;
    public HashMap<Integer,State> integerStateHashMap;

    public StateContext(){
        this.stateList = new ArrayList<>();
        this.integerStateHashMap = new HashMap<>();
    }

    public void setStart(State start) {
        start.isStart = true;
        start.isEnd = false;
        this.start = start;
    }

    public void setEnd(State end) {
        end.isEnd = true;
        end.isStart = false;
        this.end = end;
    }

    public State getState(int id){return this.integerStateHashMap.get(id);}

    public abstract State createState();

    public State getNewState(){
        State state = this.createState();
        this.stateList.add(state);
        this.integerStateHashMap.put(state.id,state);
        return state;
    }
}
