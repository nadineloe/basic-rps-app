package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.GameContract;
import com.template.states.GameState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;

import java.util.Arrays;
import java.util.List;


@StartableByRPC
@InitiatingFlow
public class PickTurnFlow extends FlowLogic<SignedTransaction>{

    private final UniqueIdentifier gameId;
    private final String move;
    private final Party counterparty;

    //public constructor
    public PickTurnFlow(UniqueIdentifier gameId, String move, Party counterparty) {
        this.gameId = gameId;
        this.move = move;
        this.counterparty = counterparty;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {

        // Query the vault to fetch a list of all AuctionState states, and filter the results based on the gameId
        // to fetch the desired GameState states from the vault. This filtered states would be used as input to the
        // transaction.
        List<StateAndRef<GameState>> gameStateAndRefs = getServiceHub().getVaultService()
                .queryBy(GameState.class).getStates();
        StateAndRef<GameState> inputStateAndRef = gameStateAndRefs.stream().filter(stateAndRef -> {
            GameState input = stateAndRef.getState().getData();
            return input.getLinearId().equals(gameId);
        }).findAny().orElseThrow(() -> new FlowException("Game Not Found"));
        GameState input = inputStateAndRef.getState().getData();


        GameState output = new GameState(input.getPlayer1(), input.getPlayer2(), input.getPlayer1Score(), input.getPlayer2Score(), input.getLinearId(), input.getFirstMove(), move);

        // Build the transaction. On successful completion of the transaction the current game state is consumed
        // and a new game state is create as an output containing the new details of the second move
        TransactionBuilder builder = new TransactionBuilder(inputStateAndRef.getState().getNotary())
                .addInputState(inputStateAndRef)
                .addOutputState(output)
                .addCommand(new GameContract.Commands.SubmitTurn(), Arrays.asList(getOurIdentity().getOwningKey()));

        // Verify the transaction
        builder.verify(getServiceHub());

        // Sign the transaction
        SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(builder);

        FlowSession counterpartySession = initiateFlow(counterparty);

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow(new FinalityFlow(selfSignedTransaction, Arrays.asList(counterpartySession)));

        }
}
