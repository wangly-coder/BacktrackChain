package com.wly.regex.auto.edge;

import com.wly.regex.auto.State;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class EpsilonEdge extends Edge{
    public static EpsilonEdge of(State targetState){
        return EpsilonEdge.builder().targetState(targetState).build();
    }

    public static final String SIGN = "ε";

    @Override
    public boolean canMove(char c) {
        return false;
    }

    @Override
    public String printSelf() {
        return Edge.MOVE+EpsilonEdge.SIGN+Edge.MOVE+this.targetState.printSelf();
    }
}
