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
import net.corda.core.contracts.UniqueIdentifier;

import java.util.UUID;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.security.PublicKey;



@InitiatingFlow
@StartableByRPC
public class CreateGameFlow extends FlowLogic<UniqueIdentifier> {
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
    private final int move;
    private final Party otherParty;

    //public constructor
    public CreateGameFlow(int move, Party otherParty) {
        this.move = move;
        this.otherParty = otherParty;
//        this.round = round;
    }


    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    @Override
    public UniqueIdentifier call() throws FlowException {
// We retrieve the notary identity from the network map.
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);


// We create the transaction components.
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);

        GameState outputState = new GameState(move, getOurIdentity(), otherParty, 0, 0, new UniqueIdentifier(null, UUID.randomUUID()));
        List<PublicKey> requiredSigners = Arrays.asList(getOurIdentity().getOwningKey(), otherParty.getOwningKey());
        Command command = new Command<>(new GameContract.Create(), requiredSigners);

// We create a transaction builder and add the components.
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(outputState, GameContract.ID)
                .addCommand(command);


        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
// Verifying the transaction.
        txBuilder.verify(getServiceHub());

// Signing the transaction.
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        // Creating a session with the other party.
        FlowSession otherPartySession = initiateFlow(otherParty);

        // Obtaining the counterparty's signature.
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTx, Arrays.asList(otherPartySession), CollectSignaturesFlow.tracker()));


        // Finalising the transaction.
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        subFlow(new FinalityFlow(fullySignedTx));

        return outputState.getLinearId();
    }
}
