package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.rps.contracts.MoveContract;
import com.rps.states.GameState;
import com.rps.states.MoveState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

@StartableByRPC
@InitiatingFlow
public class PickTurnFlow extends FlowLogic<SignedTransaction> {

    private final UniqueIdentifier gameId;
    private String move;

    public PickTurnFlow(UniqueIdentifier gameId, String move) {
        this.gameId = gameId;
        this.move = move;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {

        StateAndRef gameInputStateAndRef = getServiceHub().cordaService(GameService.class).getGameStateAndRef(gameId);
        GameState gameInput = (GameState) gameInputStateAndRef.getState().getData();

        List<PublicKey> requiredSigners = (List<PublicKey>) getOurIdentity().getOwningKey();

        Command command = new Command(new MoveContract.Commands.SubmitTurn(), requiredSigners);
        MoveState moveOutput = new MoveState(gameId, move);

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