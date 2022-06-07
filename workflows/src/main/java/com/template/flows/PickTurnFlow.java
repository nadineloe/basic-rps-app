package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.GameContract;
import com.template.states.GameState;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.contracts.Command;
import net.corda.core.identity.Party;
import net.corda.core.flows.ReceiveFinalityFlow;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.node.services.vault.QueryCriteria;
import co.paralleluniverse.fibers.Suspendable;
import com.sun.istack.NotNull;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ReferencedStateAndRef;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.identity.AbstractParty;
import net.corda.core.node.services.Vault;
import net.corda.core.transactions.FilteredTransaction;
import net.corda.core.contracts.UniqueIdentifier;

import java.security.SignatureException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

import java.security.PublicKey;


@StartableByRPC
@InitiatingFlow
public class PickTurnFlow extends FlowLogic<String>{

    private final UniqueIdentifier gameId;
    private final String move;

    //public constructor
    public PickTurnFlow(UniqueIdentifier gameId, String move) {
        this.gameId = gameId;
        this.move = move;
    }

    @Override
    @Suspendable
    public String call() throws FlowException {

        // Query the vault to fetch a list of all AuctionState states, and filter the results based on the gameId
        // to fetch the desired AuctionState states from the vault. This filtered states would be used as input to the
        // transaction.
        List<StateAndRef<GameState>> gameStateAndRefs = getServiceHub().getVaultService()
                .queryBy(GameState.class).getStates();
        StateAndRef<GameState> gameStateAndRef = gameStateAndRefs.stream().filter(stateAndRef -> {
            GameState input = stateAndRef.getState().getData();
            return input.getLinearId().equals(gameId);
        }).findAny().orElseThrow(() -> new FlowException("Game Not Found"));
        GameState input = gameStateAndRef.getState().getData();

        // String lastMove = gameState.getMove();
        // Party player1 = gameState.getPlayer1();
        // Party player2 = gameState.getPlayer2();
        // UniqueIdentifier linearId = gameState.getLinearId();



        //Creating the output
        Party counterparty = (getOurIdentity().equals(input.getPlayer1()))? input.getPlayer2() : input.getPlayer1();


        // ProposalState output = new GameState(input.getPlayer1(), input.getPlayer2(), getOurIdentity(), counterparty, input.getLinearId(), input.getMove(), move);
        

        GameState output = new GameState(null, player1, player2, 1, 0, linearId);

//        if(gameState.getMove()!=null) {
//            if(lastMove == move){
//                throw new FlowException("Tie. Need another round to determine winner.");
//            }else {
//                if(lastMove.equals("Rock") && move.equals("Paper")){
//                    GameState outputGameState = new GameState(null, player1, player2, 1, 0, linearId);
//                }
//                else{
//                    GameState outputGameState = new GameState(null, player1, player2, 0, 1, linearId);
//                }
//            }
//            return GameState outputGameState;
//        }

//        return "Player's last move is " + lastMove;


        // Create a new TransactionBuilder object.
        TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addInputState(currentGame.get(0))
                .addOutputState(outputGameState)
                .addCommand(new GameContract.Commands.Create(), Arrays.asList(gameState.getPlayer1().getOwningKey(),
                        gameState.getPlayer2().getOwningKey()));

        transactionBuilder.verify(getServiceHub());
//
        // Step 5. Verify and sign it with our KeyPair.
        transactionBuilder.verify(getServiceHub());
        final SignedTransaction ptx = getServiceHub().signInitialTransaction(transactionBuilder);
//
//
        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        List<Party> otherParties = outputGameState.getParticipants().stream().map(el -> (Party) el).collect(Collectors.toList());
        otherParties.remove(getOurIdentity());
        List<FlowSession> sessions = otherParties.stream().map(el -> initiateFlow(el)).collect(Collectors.toList());

        SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, sessions));

        // Step 7. Assuming no exceptions, we can now finalise the transaction
        return subFlow(new FinalityFlow(stx, sessions));
    }
}
