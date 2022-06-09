package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.rps.contracts.GameContract;
import com.rps.states.GameState;
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
public class PickWinnerFlow extends FlowLogic<SignedTransaction>{

    private final UniqueIdentifier gameId;
    private final Party counterparty;
    private int i;

    //public constructor
    public PickWinnerFlow(UniqueIdentifier gameId, Party counterparty) {
        this.gameId = gameId;
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

        String player1move = input.getFirstMove();
        String player2move = input.getSecondMove();
        int player1score = input.getPlayer1Score();
        int player2score = input.getPlayer2Score();


//        if (player1move == player2move) {
//               throw new FlowException("Tie. Need another round to determine winner.");
//           } else if (player1move == null) {
//               throw new FlowException("Player 2 hasn't taken his turn yet.");
//           } else if (player1move.equals("ROCK") && player2move.equals("SCISSORS")) {
//               i = player1score++;
//           } else if (player1move.equals("PAPER") && player2move.equals("ROCK")) {
//               i = player1score++;
//           } else (player1move.equals("SCISSORS") && player2move.equals("PAPER")) {
//               i = player1score++;
//           } else {
//               i = player2score++;
//           }
//
////            ––> figure this logic out. gaaaah

        GameState output = new GameState(input.getPlayer1(), input.getPlayer2(), input.getPlayer1Score(), input.getPlayer2Score(), input.getLinearId(), null, null);

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
