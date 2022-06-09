package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.ContractState;
import com.rps.contracts.GameContract;
import com.rps.states.GameState;
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



@InitiatedBy(PickTurnFlow.class)
class PickTurnFlowResponder extends FlowLogic<Void> {
    //private variable
    private FlowSession counterpartySession;

    //Constructor
    public PickTurnFlowResponder(FlowSession counterpartySession) {
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