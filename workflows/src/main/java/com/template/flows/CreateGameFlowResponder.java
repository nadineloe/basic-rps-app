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
                // Custom Logic to validate transaction.
            }
        });
        subFlow(new ReceiveFinalityFlow(counterpartySession));
        return null;
    }
}
//public class IOUFlowResponder extends FlowLogic<Void> {
//    private final FlowSession otherPartySession;
//
//    public IOUFlowResponder(FlowSession otherPartySession) {
//        this.otherPartySession = otherPartySession;
//    }
//
//    @Suspendable
//    @Override
//    public Void call() throws FlowException {
//        class SignTxFlow extends SignTransactionFlow {
//            private SignTxFlow(FlowSession otherPartySession, ProgressTracker progressTracker) {
//                super(otherPartySession, progressTracker);
//            }
//
//            @Override
//            protected void checkTransaction(SignedTransaction stx) {
//                requireThat(require -> {
//                    ContractState output = stx.getTx().getOutputs().get(0).getData();
//                    require.using("This must be a Game transaction.", output instanceof IOUState);
//                    IOUState iou = (IOUState) output;
////                    require.using("The inputs value cannot be over 3.", iou.getMove() < 4);
////                    require.using("Move cannot be anything else than Rock, Paper or Scissors.", iou.getMove() == "Scissor" ||  iou.getMove() == "Paper" || iou.getMove() == "Rock")
//                    return subFlow(new ReceiveFinalityFlow(otherPartySession));
////                    return null;
//                });
//            }
//        }
//
//        subFlow(new SignTxFlow(otherPartySession, SignTransactionFlow.Companion.tracker()));
//
//        return null;
//    }
//}