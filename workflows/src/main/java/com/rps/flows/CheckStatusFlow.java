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

/*
* CheckStatusFlow returns if player 2 has already made a move
 */
public class CheckStatusFlow {

    @StartableByRPC
    @InitiatingFlow
    public static class Initiator extends FlowLogic<AbstractParty> {

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

                for (int i = 0; i < players.size()-1; i++) {
                    if (players.get(i) != getOurIdentity()) {
                        counterparty = players.get(i);
                    }
                }

                FlowSession session = initiateFlow(counterparty);
                UntrustworthyData<Boolean> moveCheck = session.sendAndReceive(Boolean.class, gameId);
                Boolean test = moveCheck.unwrap(hasOtherPlayerGone -> {
                    assert (hasOtherPlayerGone.getClass().isInstance(Boolean.class));
                    return (Boolean) hasOtherPlayerGone;
                });
                return test;
            } else {
                return false;
            }
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