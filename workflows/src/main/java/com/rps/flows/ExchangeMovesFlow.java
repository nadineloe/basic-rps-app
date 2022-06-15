package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.rps.states.GameState;
import com.rps.states.MoveState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.utilities.UntrustworthyData;

import java.util.List;
import java.util.stream.Collectors;

public class ExchangeMovesFlow {

    @StartableByRPC
    @InitiatingFlow
    public static class Initiator extends FlowLogic<String> {

        private final UniqueIdentifier gameId;

        public Initiator(UniqueIdentifier gameId) {
            this.gameId = gameId;
        }

        @Override
        @Suspendable
        public String call() throws FlowException {

            StateAndRef gameStateAndRef = getServiceHub().cordaService(GameService.class).getGameStateAndRef(gameId);
            GameState gameInput = (GameState) gameStateAndRef.getState().getData();

            List<AbstractParty> players = gameInput.getParticipants();
            AbstractParty counterparty = players.stream().filter(it -> it != getOurIdentity()).collect(Collectors.toList()).get(0);

            FlowSession counterpartySession = initiateFlow(counterparty);

            UntrustworthyData<StateAndRef> moveCheck = counterpartySession.sendAndReceive(StateAndRef.class, gameId);
            StateAndRef<MoveState> counterpartyMoveStateAndRef = moveCheck.unwrap( stateAndRef -> {
                assert(stateAndRef.equals(StateAndRef.class));
                return stateAndRef;
            });
            String counterpartyMove = counterpartyMoveStateAndRef.getState().getData().getMove();
            return counterpartyMove;
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<Void> {
        private FlowSession counterpartySession;
        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {

            UniqueIdentifier gameId = counterpartySession.receive(UniqueIdentifier.class).unwrap(data -> data);
            StateAndRef moveStateAndRef = getServiceHub().cordaService(GameService.class).getMoveStateAndRef(gameId);
            counterpartySession.send(moveStateAndRef);
            return null;
        }
    }
}