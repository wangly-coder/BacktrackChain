package com.wly.regex.auto.edge;

import com.wly.regex.auto.State;
import com.wly.regex.util.CharRange;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CharRangeEdge extends Edge{
    public CharRange charRange;
    public static CharRangeEdge of(char left,char right,State target){
        return CharRangeEdge.builder().charRange(CharRange.of(left,right)).targetState(target).build();
    }

    @Override
    public String printSelf() {
        return String.format(
                Edge.MOVE+"[%s,%s]"+Edge.MOVE+this.targetState.printSelf(),
                this.charRange.left,this.charRange.right
        );
    }
}
