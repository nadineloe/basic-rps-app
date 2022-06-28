package com.rps.states;

import com.rps.contracts.MoveContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@BelongsToContract(MoveContract.class)
public class MoveState implements ContractState {
    private UniqueIdentifier gameId = UniqueIdentifier.Companion.fromString(UUID.randomUUID().toString());
    private String move;
    private AbstractParty player;

    public MoveState(UniqueIdentifier gameId, String move, AbstractParty player) {
        this.gameId = gameId;
        this.move = move;
        this.player = player;
    }

    public MoveState pickMove(String move) {
        return new MoveState(
                this.gameId,
                move,
                this.player
        );
    }

    @CordaSerializable
    public enum MoveChoices {
        ROCK,
        PAPER,
        SCISSOR;
    }

    public boolean isValid(String move) {
        List choices = Arrays.asList(MoveChoices.values());
        if(choices.contains(MoveChoices.valueOf(move))){
            return true;
        } else {
            return false;
        }
    }

    public String getMove() { return move; }

    @NotNull
    public UniqueIdentifier getGameId() { return this.gameId; }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(player);
    }
}