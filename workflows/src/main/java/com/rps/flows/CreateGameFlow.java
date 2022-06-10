package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.rps.contracts.GameContract;
import com.rps.states.GameState;
import com.sun.istack.NotNull;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.contracts.Command;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.contracts.UniqueIdentifier;

import java.util.Arrays;
import java.util.List;

import java.security.PublicKey;
import java.util.stream.Collectors;

public class CreateGameFlow {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<UniqueIdentifier> {
        /**
         * The progress tracker provides checkpoints indicating the progress of
         * the flow to observers.
         */
        private final ProgressTracker progressTracker = tracker();

        private static final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Creating the Game.");
        private static final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
        private static final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Recording transaction") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.tracker();
            }
        };

        private static ProgressTracker tracker() {
            return new ProgressTracker(
                    GENERATING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    FINALISING_TRANSACTION
            );
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        //private variables
        private final AbstractParty otherParty;

        //public constructor
        public Initiator(AbstractParty otherParty) {
            this.otherParty = otherParty;
        }


        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        @Override
        public UniqueIdentifier call() throws FlowException {

            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            List<AbstractParty> players = Arrays.asList(getOurIdentity(), otherParty);
            GameState output = new GameState(players);
            List<PublicKey> requiredSigners = players.stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());

            Command command = new Command(new GameContract.Commands.Create(), requiredSigners);

            TransactionBuilder builder = Helpers.ourTx(getServiceHub())
                    .addOutputState(output, GameContract.ID)
                    .addCommand(command);

            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            builder.verify(getServiceHub());
            SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(builder);
            FlowSession counterpartySession = initiateFlow(otherParty);
            subFlow(new FinalityFlow(selfSignedTransaction, Arrays.asList(counterpartySession)));

            return output.getLinearId();
        }
    }


    @InitiatedBy(Initiator.class)
    class Responder extends FlowLogic<Void> {
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