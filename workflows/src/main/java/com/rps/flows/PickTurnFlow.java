package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.rps.contracts.MoveContract;
import com.rps.states.MoveState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.utilities.ProgressTracker;

import java.util.Collections;
import java.util.List;

@StartableByRPC
@InitiatingFlow
public class PickTurnFlow extends FlowLogic<SignedTransaction> {

    private final ProgressTracker progressTracker = tracker();

    private static final ProgressTracker.Step STATE_CHECK = new ProgressTracker.Step("State check passed");
    private static final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private static final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Recording transaction") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.tracker();
        }
    };

    private static ProgressTracker tracker() {
        return new ProgressTracker(
                STATE_CHECK,
                SIGNING_TRANSACTION,
                FINALISING_TRANSACTION
        );
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    private final UniqueIdentifier gameId;
    private String move;

    public PickTurnFlow(UniqueIdentifier gameId, String move) {
        this.gameId = gameId;
        this.move = move;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {

//        StateAndRef moveStateAndRef = getServiceHub().cordaService(GameService.class).getMoveStateAndRef(gameId);
//         if(inputMoveStateAndRef.size() != 0){
//             throw new FlowException("You've already made a move.");
//         }

//        if(moveStateAndRef.get(0).getState().getData().isValid(move) == false){
//            throw new FlowException("Move isn't valid. Allowed moves are: ROCK, PAPER, SCISSOR.");
//        }

        List<StateAndRef<MoveState>> moveStateAndRef = getServiceHub().getVaultService().queryBy(MoveState.class).getStates();

        if(moveStateAndRef.size() == 0){
            StateAndRef gameInputStateAndRef = getServiceHub().cordaService(GameService.class).getGameStateAndRef(gameId);

            Command command = new Command(new MoveContract.Commands.SubmitTurn(), getOurIdentity().getOwningKey());
            MoveState moveOutput = new MoveState(gameId, move, getOurIdentity());

            TransactionBuilder builder = Helpers.ourTx(getServiceHub())
                    .addReferenceState(gameInputStateAndRef.referenced())
                    .addOutputState(moveOutput)
                    .addCommand(command);

            builder.verify(getServiceHub());
            SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(builder);

            // counterparty subflow not needed!
            return subFlow(new FinalityFlow(selfSignedTransaction, Collections.emptyList()));
        }
        else {
            if(moveStateAndRef.get(0).getState().getData().getGameId().equals(gameId)) {
                throw new FlowException("You've already made a move.");
            }
            else {
                StateAndRef gameInputStateAndRef = getServiceHub().cordaService(GameService.class).getGameStateAndRef(gameId);

                Command command = new Command(new MoveContract.Commands.SubmitTurn(), getOurIdentity().getOwningKey());
                MoveState moveOutput = new MoveState(gameId, move, getOurIdentity());

                TransactionBuilder builder = Helpers.ourTx(getServiceHub())
                        .addReferenceState(gameInputStateAndRef.referenced())
                        .addOutputState(moveOutput)
                        .addCommand(command);

                builder.verify(getServiceHub());
                SignedTransaction selfSignedTransaction = getServiceHub().signInitialTransaction(builder);

                // counterparty subflow not needed!
                return subFlow(new FinalityFlow(selfSignedTransaction, Collections.emptyList()));
            }
        }
    }
}