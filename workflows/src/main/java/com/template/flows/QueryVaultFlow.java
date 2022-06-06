//package com.template.flows;
//
//import co.paralleluniverse.fibers.Suspendable;
//import com.template.contracts.GameContract;
//import com.template.states.GameState;
//import net.corda.core.flows.*;
//import net.corda.core.identity.CordaX500Name;
//import net.corda.core.identity.Party;
//import net.corda.core.transactions.SignedTransaction;
//import net.corda.core.transactions.TransactionBuilder;
//import net.corda.core.contracts.Command;
//import net.corda.core.identity.Party;
//import net.corda.core.flows.ReceiveFinalityFlow;
//import net.corda.core.utilities.ProgressTracker;
//import net.corda.core.node.services.vault.QueryCriteria;
//import co.paralleluniverse.fibers.Suspendable;
//import com.sun.istack.NotNull;
//import net.corda.core.contracts.Command;
//import net.corda.core.contracts.ReferencedStateAndRef;
//import net.corda.core.contracts.StateAndRef;
//import net.corda.core.crypto.TransactionSignature;
//import net.corda.core.identity.AbstractParty;
//import net.corda.core.node.services.Vault;
//import net.corda.core.transactions.FilteredTransaction;
//
//import java.security.SignatureException;
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicReference;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import java.security.PublicKey;
//
//
//@StartableByRPC
//@InitiatingFlow
//public static class QueryVaultFlow extends FlowLogic<SignedTransaction>{
//
//    private final String linearId;
//    private final int move;
//
//    //public constructor
//    public QueryVaultFlow(String linearId, int move) {
//        this.linearId = linaerId;
//        this.move = move;
//    }
//
//    @Override
//    @Suspendable
//    public SignedTransaction call() throws FlowException {
//
//        // Query the vault to fetch a list of all GameState states, and filter the results based on the linearId
//        // to fetch the desired GameState states from the vault.
//        QueryCriteria.LinearStateQueryCriteria linearStateQueryCriteria =
//                new QueryCriteria.LinearStateQueryCriteria(null,
//                        Collections.singletonList(UUID.fromString(linearId)),
//                        null, Vault.StateStatus.UNCONSUMED, null);
//        List<StateAndRef<GameState>> gameStateList =  getServiceHub().getVaultService()
//                .queryBy(GameState.class, linearStateQueryCriteria).getStates();
//        if(gameStateList.size() ==0)
//            throw new FlowException("Game doesn't exist!");
//
//        GameBoard gameState = gameStateList.get(0).getState().getData();
//
////        if(gameState.getPlayer1Score() >= 3 || gameState.getPlayer2Score() >= 3){
////            throw new FlowException("This Game is Over");
////        }
//
//        // Check if the initiator is the current player
////        if(!gameState.getCurrentPlayer().equals(getOurIdentity()))
////            throw new FlowException("Please wait for your turn");
//
//        return "Move of Player 1 is : " + gameState.getMove();
//
//    }
//}