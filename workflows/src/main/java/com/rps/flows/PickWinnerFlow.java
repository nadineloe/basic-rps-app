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
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.contracts.UniqueIdentifier;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PickWinnerFlow {

    @StartableByRPC
    @InitiatingFlow
    public static class Initiator extends FlowLogic<AbstractParty>{

        private final ProgressTracker progressTracker = tracker();

        private static final ProgressTracker.Step RETRIEVING_STATES = new ProgressTracker.Step("Retrieving GameState and MoveState from vault");
        private static final ProgressTracker.Step CHECKSTATUS_FLOW = new ProgressTracker.Step("Running CheckStatus flow");
        private static final ProgressTracker.Step EXCHANGING_MOVES = new ProgressTracker.Step("Exchanging players moves");
        private static final ProgressTracker.Step  CHECKING_WINNER = new ProgressTracker.Step("Checking who's winning");
        private static final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
        private static final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Recording transaction") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.tracker();
            }
        };

        private static ProgressTracker tracker() {
            return new ProgressTracker(
                    RETRIEVING_STATES,
                    CHECKSTATUS_FLOW,
                    EXCHANGING_MOVES,
                    CHECKING_WINNER,
                    SIGNING_TRANSACTION,
                    FINALISING_TRANSACTION
            );
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        private final UniqueIdentifier gameId;
        public Initiator(UniqueIdentifier gameId) {
            this.gameId = gameId;
        }

        @Override
        @Suspendable
        public AbstractParty call() throws FlowException {

            progressTracker.setCurrentStep(RETRIEVING_STATES);
            StateAndRef gameStateAndRef = getServiceHub().cordaService(GameService.class).getGameStateAndRef(gameId);
            GameState gameInput = (GameState) gameStateAndRef.getState().getData();

            StateAndRef moveStateAndRef = getServiceHub().cordaService(GameService.class).getMoveStateAndRef(gameId);
            MoveState moveInput = (MoveState) moveStateAndRef.getState().getData();

            List<AbstractParty> players = gameInput.getParticipants();
            AbstractParty counterparty = players.stream().filter(it -> it != getOurIdentity()).collect(Collectors.toList()).get(0);
            List<PublicKey> requiredSigners = players.stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());

            progressTracker.setCurrentStep(CHECKSTATUS_FLOW);
            Boolean haveBothPlayersGone = subFlow(new CheckStatusFlow.Initiator(gameId));

            if (haveBothPlayersGone) {
                String myMove = moveInput.getMove();

                progressTracker.setCurrentStep(EXCHANGING_MOVES);
                String counterpartyMove = subFlow(new ExchangeMovesFlow.Initiator(gameId));

                progressTracker.setCurrentStep(CHECKING_WINNER);
                AbstractParty winner = getServiceHub().cordaService(GameService.class).getWinner(myMove, counterpartyMove, counterparty, getOurIdentity());
                GameState gameOutput = new GameState(players, gameId, Arrays.asList(winner));

                TransactionBuilder builder = Helpers.ourTx(getServiceHub())
                        .addInputState(gameStateAndRef)
                        .addInputState(moveStateAndRef)
                        .addOutputState(gameOutput)
                        .addCommand(new GameContract.Commands.EndGame(), requiredSigners);


                // Verify that the transaction is valid.
                builder.verify(getServiceHub());

                // Sign the transaction.
                final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(builder);

                // Send the state to the counterparty, and receive it back with their signature.
                FlowSession otherPartySession = initiateFlow(counterparty);
                final SignedTransaction fullySignedTx = subFlow(
                        new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession)));

                // Notarise and record the transaction in both parties' vaults.
                SignedTransaction result = subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession)));

//                return result + " Winner is: " + winner + "!";
                return winner;
            }
            else {
                throw new FlowException("Both Players need to pick their weapons first.");
            }

        }
    }

    @InitiatedBy(PickWinnerFlow.Initiator.class)
    public static class Responder extends FlowLogic<Void> {
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            subFlow(new SignTransactionFlow(counterpartySession) {
                @Override
                protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
//                requireThat(require -> {
//                    ContractState output = stx.getTx().getOutputs().get(0).getData();
//                    GameState game = (GameState) output;
//                    require.using("The inputs value cannot be over 3.", game.getMove() < 4);
//                    return subFlow(new ReceiveFinalityFlow(otherPartySession));
////                    return null;
//                }
                }
            });
            subFlow(new ReceiveFinalityFlow(counterpartySession));
            return null;
        }
    }
}