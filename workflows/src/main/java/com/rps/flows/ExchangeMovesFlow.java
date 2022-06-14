//package com.rps.flows;
//
//import co.paralleluniverse.fibers.Suspendable;
//import com.rps.states.MoveState;
//import net.corda.core.contracts.StateAndRef;
//import net.corda.core.contracts.UniqueIdentifier;
//import net.corda.core.flows.*;
//import net.corda.core.identity.AbstractParty;
//import net.corda.core.utilities.UntrustworthyData;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class ExchangeMovesFlow {
//
//    @StartableByRPC
//    @InitiatingFlow
//    public static class Initiator extends FlowLogic<Boolean> {
//
//        private final UniqueIdentifier gameId;
//
//        public Initiator(UniqueIdentifier gameId) {
//            this.gameId = gameId;
//        }
//
//        @Override
//        @Suspendable
//        public Boolean call() throws FlowException {
//
//            StateAndRef moveStateAndRef = getServiceHub().cordaService(GameService.class).getMoveStateAndRef(gameId);
//            MoveState moveInput = (MoveState) moveStateAndRef.getState().getData();
//
//            List<AbstractParty> players = moveInput.getParticipants();
//            AbstractParty otherParty = players.stream().filter(it -> it != getOurIdentity()).collect(Collectors.toList()).get(0);
//
//            FlowSession counterpartySession = initiateFlow(otherParty);
//
//            UntrustworthyData<MoveState> moveCheck = counterpartySession.sendAndReceive(MoveState.class, gameId);
//            Boolean bool = moveCheck.unwrap( msg -> {
//                assert(msg.equals(MoveState.class));
//                return true;
//            });
//            return bool;
//        }
//    }
//
//    @InitiatedBy(Initiator.class)
//    class Responder extends FlowLogic<Void> {
//        //private variable
//        private FlowSession counterpartySession;
//
//        //Constructor
//        public Responder(FlowSession counterpartySession) {
//            this.counterpartySession = counterpartySession;
//        }
//
//        @Override
//        @Suspendable
//        public Void call() throws FlowException {
//            MoveState didOtherPartyMove = counterpartySession.sendAndReceive(MoveState.class, true).unwrap(data -> data);
//            counterpartySession.send(MoveState.class);
//            return null;
//        }
//    }
//}