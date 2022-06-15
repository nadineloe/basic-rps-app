package com.rps.states;

import com.rps.contracts.GameContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    @ConstructorForDeserialization
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GameState gameState = (GameState) o;

        if (maxRounds != gameState.maxRounds) return false;
        if (!players.equals(gameState.players)) return false;
        if (!linearId.equals(gameState.linearId)) return false;
        return Objects.equals(roundWinners, gameState.roundWinners);
    }

    @Override
    public int hashCode() {
        int result = players.hashCode();
        result = 31 * result + linearId.hashCode();
        result = 31 * result + (roundWinners != null ? roundWinners.hashCode() : 0);
        result = 31 * result + maxRounds;
        return result;
    }
}