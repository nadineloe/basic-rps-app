package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.rps.contracts.GameContract;
import com.rps.states.GameState;
import com.rps.states.MoveState;
import com.sun.istack.NotNull;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PickWinnerFlow {

    @StartableByRPC
    @InitiatingFlow
    public class Initiator extends FlowLogic<SignedTransaction>{

        private final UniqueIdentifier gameId;

        //public constructor
        public Initiator(UniqueIdentifier gameId) {
            this.gameId = gameId;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {

            StateAndRef gameStateAndRef = getServiceHub().cordaService(GameService.class).getGameStateAndRef(gameId);
            GameState gameInput = (GameState) gameStateAndRef.getState().getData();

            StateAndRef moveStateAndRef = getServiceHub().cordaService(GameService.class).getMoveStateAndRef(gameId);
            MoveState moveInput = (MoveState) moveStateAndRef.getState().getData();

            List<AbstractParty> players = gameInput.getParticipants();
            AbstractParty otherParty = players.stream().filter(it -> it != getOurIdentity()).collect(Collectors.toList()).get(0);

            String player1move = moveInput.getMove();


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


            TransactionBuilder builder = Helpers.ourTx(getServiceHub())
                    .addInputState(gameStateAndRef)
//                    .addOutputState(output)
                    .addCommand(new GameContract.Commands.SubmitTurn(), Arrays.asList(getOurIdentity().getOwningKey()));

            builder.verify(getServiceHub());
            SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(builder);

            FlowSession counterpartySession = initiateFlow(otherParty);

            return subFlow(new FinalityFlow(selfSignedTransaction, Arrays.asList(counterpartySession)));

            }
    }

    @InitiatedBy(Initiator.class)
    class Responder extends FlowLogic<Void> {
        private FlowSession counterpartySession;
        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            subFlow(new SignTransactionFlow(counterpartySession) {
                @Override
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                }
            });
            subFlow(new ReceiveFinalityFlow(counterpartySession));
            return null;
        }
    }
}