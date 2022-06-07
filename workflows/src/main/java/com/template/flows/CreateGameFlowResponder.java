package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import com.template.contracts.GameContract;
import com.template.states.GameState;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.contracts.Command;
import net.corda.core.flows.ReceiveFinalityFlow;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.crypto.SecureHash;
import com.sun.istack.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import static net.corda.core.contracts.ContractsDSL.requireThat;



@InitiatedBy(CreateGameFlow.class)
class CreateGameFlowResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public CreateGameFlowResponder(FlowSession counterpartySession) {
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