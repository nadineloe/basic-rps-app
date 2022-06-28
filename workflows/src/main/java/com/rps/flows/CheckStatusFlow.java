package com.rps.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.rps.states.GameState;
import com.rps.states.MoveState;
import net.corda.core.flows.FlowSession;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.utilities.UntrustworthyData;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
* CheckStatusFlow returns if player 2 has already made a move
 */
public class CheckStatusFlow {

    @StartableByRPC
    @InitiatingFlow
    public static class Initiator extends FlowLogic<Boolean> {

        private final UniqueIdentifier gameId;

        private AbstractParty counterparty;

        public Initiator(UniqueIdentifier gameId) {
            this.gameId = gameId;
        }

        @Override
        @Suspendable
        public Boolean call() throws FlowException {

            List<StateAndRef<MoveState>> inputMoveStateAndRef = getServiceHub().getVaultService().queryBy(MoveState.class).getStates();
            if (inputMoveStateAndRef.size() != 0) {
                StateAndRef gameInputStateAndRef = getServiceHub().cordaService(GameService.class).getGameStateAndRef(gameId);
                GameState gameInput = (GameState) gameInputStateAndRef.getState().getData();

                List<AbstractParty> players = gameInput.getParticipants();

                this.counterparty = players.stream().filter(player -> !player.equals(getOurIdentity())).collect(Collectors.toList()).get(0);

                FlowSession session = initiateFlow(counterparty);
                UntrustworthyData<Boolean> moveCheck = session.sendAndReceive(Boolean.class, gameId);
                return moveCheck.unwrap(hasOtherPlayerGone -> hasOtherPlayerGone);
            }
                return false;
        }
    }

    @InitiatedBy(Initiator.class)
    public static class Responder extends FlowLogic<Void> {
        //private variable
        private final FlowSession counterpartySession;
        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            // receiving the gameId from player1
            UniqueIdentifier gameId = counterpartySession.receive(UniqueIdentifier.class).unwrap(data -> data);

            List<StateAndRef<MoveState>> inputMoveStateAndRef = getServiceHub().getVaultService().queryBy(MoveState.class).getStates();
            if(inputMoveStateAndRef.size() != 0){
                counterpartySession.send(true);
            } else {
                counterpartySession.send(false);
            }
            return null;
        }
    }
}