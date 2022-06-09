package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.flows.ReceiveFinalityFlow;
import com.sun.istack.NotNull;


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