package com.rps.contracts;

import com.rps.states.rpsState;
import org.junit.Test;

public class StateTests {

    //Mock State test check for if the state has correct parameters type
    @Test
    public void hasFieldOfCorrectType() throws NoSuchFieldException {
        rpsState.class.getDeclaredField("msg");
        assert (rpsState.class.getDeclaredField("msg").getType().equals(String.class));
    }
}