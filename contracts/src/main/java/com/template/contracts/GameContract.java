package com.template.contracts;

import com.template.states.GameState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import net.corda.core.identity.Party;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;


// ************
// * Contract *
// ************
public class GameContract implements Contract {
    public static final String ID = "com.template.contracts.GameContract";

    // Our Create command.
    public static class Create implements CommandData {
    }

    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<GameContract.Create> command = requireSingleCommand(tx.getCommands(), GameContract.Create.class);

        // Constraints on the shape of the transaction.
        if (!tx.getInputs().isEmpty())
            throw new IllegalArgumentException("No inputs should be consumed when issuing a new GameState.");
        if (!(tx.getOutputs().size() == 1))
            throw new IllegalArgumentException("There should be one output state of type GameState.");

        // Game-specific constraints.
        final GameState output = tx.outputsOfType(GameState.class).get(0);
        final Party player1 = output.getPlayer1();
        final Party player2 = output.getPlayer2();
//        if (output.getMove() <= 0)
//            throw new IllegalArgumentException("The Game's value must be non-negative.");
        if (player1.equals(player2))
            throw new IllegalArgumentException("The player1 and the player2 cannot be the same entity.");

        // Constraints on the signers.
        final List<PublicKey> requiredSigners = command.getSigners();
        final List<PublicKey> expectedSigners = Arrays.asList(player1.getOwningKey(), player2.getOwningKey());
        if (requiredSigners.size() != 2)
            throw new IllegalArgumentException("There must be two signers.");
        if (!(requiredSigners.containsAll(expectedSigners)))
            throw new IllegalArgumentException("The player1 and player2 must be signers.");

    }
}