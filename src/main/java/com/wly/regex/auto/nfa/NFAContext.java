package com.wly.regex.auto.nfa;

import com.wly.regex.auto.State;
import com.wly.regex.auto.StateContext;
import com.wly.regex.auto.edge.EpsilonEdge;

public class NFAContext extends StateContext {
    @Override
    public State createState() {
        return State.of(super.stateNum+1);
    }
}
