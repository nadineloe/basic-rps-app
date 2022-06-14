package com.rps.flows;

import com.rps.states.GameState;
import com.rps.states.MoveState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.TransactionSignature;
import net.corda.core.flows.FlowException;
import net.corda.core.identity.AbstractParty;
import net.corda.core.node.AppServiceHub;
import net.corda.core.node.services.CordaService;
import net.corda.core.serialization.SingletonSerializeAsToken;
import net.corda.core.transactions.FilteredTransaction;
import net.corda.core.transactions.FilteredTransactionVerificationException;

import java.util.List;


@CordaService
public class GameService extends SingletonSerializeAsToken {

    private final AppServiceHub serviceHub;

    public GameService(AppServiceHub serviceHub) {
        this.serviceHub = serviceHub;
    }

    public StateAndRef getGameStateAndRef (UniqueIdentifier gameId) throws FlowException {
        List<StateAndRef<GameState>> gameStateAndRefs = serviceHub.getVaultService()
                .queryBy(GameState.class).getStates();
        StateAndRef<GameState> inputStateAndRef = gameStateAndRefs.stream().filter(stateAndRef -> {
            GameState input = stateAndRef.getState().getData();
            return input.getLinearId().equals(gameId);
        }).findAny().orElseThrow(() -> new FlowException("Game Not Found"));
            return inputStateAndRef;
    }

    public StateAndRef getGameStateAndRefByPlayer (AbstractParty counterparty) throws FlowException {
        List<StateAndRef<GameState>> gameStateAndRefs = serviceHub.getVaultService()
                .queryBy(GameState.class).getStates();
        StateAndRef<GameState> inputStateAndRef = gameStateAndRefs.stream().filter(stateAndRef -> {
            GameState input = stateAndRef.getState().getData();
            return input.getLinearId().equals(counterparty);
        }).findAny().orElseThrow(() -> new FlowException("Game Not Found"));
        return inputStateAndRef;
    }

    public StateAndRef getMoveStateAndRef (UniqueIdentifier gameId) throws FlowException {
        List<StateAndRef<MoveState>> moves = serviceHub.getVaultService()
                .queryBy(MoveState.class).getStates();
        return moves.stream().filter(stateAndRef -> {
            MoveState moveStateInput = stateAndRef.getState().getData();
            return moveStateInput.getLinearId().equals(gameId);
        }).findAny().orElse( null);
    }

    public TransactionSignature sign(FilteredTransaction transaction) throws FilteredTransactionVerificationException {
        transaction.verify();
        boolean isValid = true;

        if(isValid){
            return serviceHub.createSignature(transaction, serviceHub.getMyInfo().getLegalIdentities().get(0).getOwningKey());
        }else{
            throw new IllegalArgumentException();
        }
    }
}
