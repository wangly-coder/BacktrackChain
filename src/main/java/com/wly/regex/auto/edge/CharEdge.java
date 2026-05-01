package com.wly.regex.auto.edge;

import com.wly.regex.auto.State;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CharEdge extends Edge{
    public char charValue;
    public static CharEdge of(char c, State target){
        return CharEdge.builder().charValue(c).targetState(target).build();
    }

    @Override
    public boolean canMove(char c) {
        return charValue == c;
    }

    @Override
    public String printSelf() {
        return String.format(Edge.MOVE+"[%s]"+Edge.MOVE+this.targetState.printSelf()
                ,this.charValue);
    }
}
