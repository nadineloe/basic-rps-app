package com.rps.states;

import com.rps.contracts.MoveContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.contracts.UniqueIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;


@BelongsToContract(MoveContract.class)
public class MoveState implements ContractState {
    private UniqueIdentifier linearId = UniqueIdentifier.Companion.fromString(UUID.randomUUID().toString());
    private String move;

    public MoveState(UniqueIdentifier linearId, String move) {
        this.linearId = linearId;
        this.move = move;
    }

    public MoveState pickMove(String move) {
        return new MoveState(
                this.linearId,
                move
        );
    }

    public String getMove() { return move; }

    @NotNull
    public UniqueIdentifier getLinearId() { return this.linearId; }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return null;
    }
}