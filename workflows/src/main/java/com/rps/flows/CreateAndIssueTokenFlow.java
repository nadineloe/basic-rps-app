//package com.rps.flows;
//
//import co.paralleluniverse.fibers.Suspendable;
//import com.rps.contracts.CoffeeTokenContract;
//import com.rps.states.CoffeeTokenType;
//import com.rps.states.GameState;
//import com.sun.istack.NotNull;
//import net.corda.core.contracts.*;
//import net.corda.core.flows.*;
//import net.corda.core.identity.AbstractParty;
//import net.corda.core.identity.CordaX500Name;
//import net.corda.core.identity.Party;
//import net.corda.core.node.services.Vault;
//import net.corda.core.node.services.vault.QueryCriteria;
//import net.corda.core.transactions.SignedTransaction;
//import net.corda.core.transactions.TransactionBuilder;
//import com.r3.corda.lib.tokens.contracts.states.NonFungibleToken;
//import com.r3.corda.lib.tokens.workflows.flows.rpc.CreateEvolvableTokens;
//import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens;
//import com.r3.corda.lib.tokens.workflows.utilities.NonFungibleTokenBuilder;
//
//import java.security.PublicKey;
//import java.util.stream.Collectors;
//import java.util.*;
//import java.util.stream.Collectors;
//
//
///**
// * Flow to create and issue house tokens. Token SDK provides some in-built flows which could be called to Create and Issue tokens.
// * This flow should be called by the issuer of the token. The constructor takes the owner and other properties of the house as
// * input parameters, it first creates the house token onto the issuer's ledger and then issues it to the owner.
// */
//public class CreateAndIssueTokenFlow {
//
//    @StartableByRPC
//    public static class CreateToken extends FlowLogic<SignedTransaction> {
//        private final UniqueIdentifier gameId;
//
//        public CreateToken(UniqueIdentifier gameId) {
//            this.gameId = gameId;
//        }
//
//        @Suspendable
//        @Override
//        public SignedTransaction call() throws FlowException {
//
//            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"));
//
//            StateAndRef gameInputStateAndRef = getServiceHub().cordaService(GameService.class).getGameStateAndRef(gameId);
//            GameState gameInput = (GameState) gameInputStateAndRef.getState().getData();
//            List<AbstractParty> players = gameInput.getParticipants();
//            List<AbstractParty> roundWinners = gameInput.getRoundWinners();
//
//            AbstractParty winner = roundWinners.stream()
//                    .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
//                    .entrySet()
//                    .stream()
//                    .max(Comparator.comparing(Map.Entry::getValue))
//                    .get()
//                    .getKey();
//
//            AbstractParty loser = players.stream().filter(it -> it != winner.collect(Collectors.toList()).get(0);
//
//            CoffeeTokenType tokenType = new CoffeeTokenType(loser, winner);
//            TransactionState transactionState = new TransactionState((ContractState) tokenType, notary);
//            subFlow(new CreateEvolvableTokens(transactionState));
//
//            TokenPointer<CoffeeTokenType> evolvableCoffeeTokenPtr = tokenType.toPointer();
//
//            NonFungibleToken token = new NonFungibleTokenBuilder()
//                    .ofTokenType(evolvableCoffeeTokenPtr.toPointer())
//                    .issuedBy(loser)
//                    .heldBy(winner)
//                    .buildNonFungibleToken();
//
//            return subFlow(new IssueTokens(Arrays.asList(token)));
//        }
//    }
//}