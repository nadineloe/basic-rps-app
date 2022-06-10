package com.rps.contracts;

import com.rps.states.GameState;
import net.corda.core.identity.Party;
import org.junit.Test;
import java.lang.reflect.Field;

public class StateTests {

    //Mock State test check for if the state has correct parameters type
    @Test
    public void hasFieldOfCorrectType() throws NoSuchFieldException {
        GameState.class.getDeclaredField("player1");
        assert (GameState.class.getDeclaredField("player1").getType().equals(String.class));
    }

    @Test
    public void hasLinearIdFieldOfCorrectType() throws NoSuchFieldException {
        // Does the linearId field exist?
        Field linearIdField = GameState.class.getDeclaredField("linearId");
    }

    @Test
    public void gameStateStateHasFieldOfCorrectType() throws NoSuchFieldException {
        GameState.class.getDeclaredField("player1");
        assert (GameState.class.getDeclaredField("player1").getType().equals(Party.class));

        GameState.class.getDeclaredField("player2");
        assert (GameState.class.getDeclaredField("player2").getType().equals(Party.class));

        GameState.class.getDeclaredField("firstMove");
        assert (GameState.class.getDeclaredField("firstMove").getType().equals(String.class));
    }
}