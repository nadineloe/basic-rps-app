package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.rps.states.MoveState;
import net.corda.core.flows.FlowSession;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.utilities.UntrustworthyData;

import java.util.List;
import java.util.stream.Collectors;

public class AskOtherPartyFlow {

    @StartableByRPC
    @InitiatingFlow
    public static class Initiator extends FlowLogic<Boolean> {

        private final UniqueIdentifier gameId;

        public Initiator(UniqueIdentifier gameId) {
            this.gameId = gameId;
        }

        @Override
        @Suspendable
        public Boolean call() throws FlowException {

            StateAndRef moveStateAndRef = getServiceHub().cordaService(GameService.class).getMoveStateAndRef(gameId);
            MoveState moveInput = (MoveState) moveStateAndRef.getState().getData();

            List<AbstractParty> players = moveInput.getParticipants();
            AbstractParty otherParty = players.stream().filter(it -> it != getOurIdentity()).collect(Collectors.toList()).get(0);

            FlowSession session = initiateFlow(otherParty);

            UntrustworthyData<Boolean> moveCheck = session.sendAndReceive(Boolean.class, gameId);
            Boolean didOtherPartyMove = moveCheck.unwrap( msg -> {
                assert(msg.getClass().isInstance(Boolean.class));
                return msg;
            });
            return didOtherPartyMove;
        }
    }

    @InitiatedBy(Initiator.class)
    public class Responder extends FlowLogic<Void> {
        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            // receiving the gameId from player1
            UniqueIdentifier gameId = counterpartySession.receive(UniqueIdentifier.class).unwrap(data -> data);
            //UniqueIdentifier didBothPartiesMove = counterpartySession.sendAndReceive(UniqueIdentifier.class, true).unwrap(data -> data);

            // using that gameId to check for MoveState in vault
            StateAndRef moveStateAndRef = getServiceHub().cordaService(GameService.class).getMoveStateAndRef(gameId);
            MoveState moveInput = (MoveState) moveStateAndRef.getState().getData();

            if (moveInput != null) {
                counterpartySession.send(true);
            }

            return null;
        }
    }
}