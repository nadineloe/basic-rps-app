package com.rps.contracts;

import com.rps.states.GameState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.List;

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
        GameState.class.getDeclaredField("maxRounds");
        assert (GameState.class.getDeclaredField("maxRounds").getType().equals(Integer.class));

        GameState.class.getDeclaredField("players");
        assert (GameState.class.getDeclaredField("players").getType().equals(AbstractParty.class));

        GameState.class.getDeclaredField("roundWinners");
        assert (GameState.class.getDeclaredField("roundWinners").getType().equals(AbstractParty.class));
    }
}