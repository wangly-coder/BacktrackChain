package com.wly.regex.auto.edge;

import com.wly.regex.auto.State;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class Edge {
    public State targetState;

    public static final String MOVE = " --> ";

    public abstract boolean canMove(char c);

    public abstract String printSelf();
}
