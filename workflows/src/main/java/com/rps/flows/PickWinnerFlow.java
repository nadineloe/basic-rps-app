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

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PickWinnerFlow {

    @StartableByRPC
    @InitiatingFlow
    public class Initiator extends FlowLogic<SignedTransaction>{

        private final UniqueIdentifier gameId;
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
            AbstractParty counterparty = players.stream().filter(it -> it != getOurIdentity()).collect(Collectors.toList()).get(0);
            List<PublicKey> requiredSigners = players.stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());

            Boolean haveBothPlayersGone = subFlow(new AskOtherPartyFlow.Initiator(gameId));

            if (haveBothPlayersGone) {
                String myMove = moveInput.getMove();
                String counterpartyMove = subFlow(new ExchangeMovesFlow.Initiator(gameId));

                AbstractParty winner = getServiceHub().cordaService(GameService.class).getWinner(myMove, counterpartyMove, counterparty, getOurIdentity());

                TransactionBuilder builder = Helpers.ourTx(getServiceHub())
                        .addInputState(gameStateAndRef)
                        .addOutputState(output)
                        .addCommand(new GameContract.Commands.SubmitTurn(), requiredSigners);

                builder.verify(getServiceHub());
                SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(builder);

                FlowSession counterpartySession = initiateFlow(counterparty);
                List<FlowSession> sessions = Collections.singletonList(counterpartySession);
                SignedTransaction fullySignedTransaction = subFlow(new CollectSignaturesFlow(selfSignedTransaction, sessions));
                subFlow(new FinalityFlow(fullySignedTransaction, sessions));

                return subFlow(new FinalityFlow(selfSignedTransaction, Arrays.asList(counterpartySession)));
            }
            else {
                throw new FlowException("Both Players need to pick their weapons first.");
            }
            }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<Void> {
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