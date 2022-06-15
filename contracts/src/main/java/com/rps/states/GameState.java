package com.rps.states;

import com.rps.contracts.GameContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


@BelongsToContract(GameContract.class)
public class GameState implements LinearState {
    private final List<AbstractParty> players;
    private UniqueIdentifier linearId = UniqueIdentifier.Companion.fromString(UUID.randomUUID().toString());
    private List<AbstractParty> roundWinners = Collections.emptyList();
    private int maxRounds;

    public GameState(List<AbstractParty> players) {
        this.players = players;
    }

    public GameState(List<AbstractParty> players, UniqueIdentifier linearId, List<AbstractParty> roundWinners, int maxRounds) {
        this.players = players;
        this.linearId = linearId;
        this.roundWinners = roundWinners;
        this.maxRounds = maxRounds;
    }


    public int getMaxRounds() { return maxRounds; };

    public List<AbstractParty> getRoundWinners() { return roundWinners; }

    @NotNull @Override
    public List<AbstractParty> getParticipants() { return players; }

    @NotNull
    public UniqueIdentifier getLinearId() { return this.linearId; }

}