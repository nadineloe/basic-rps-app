package com.template.states;

import com.template.contracts.GameContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import org.jetbrains.annotations.NotNull;
import net.corda.core.identity.AnonymousParty;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;


import java.util.Arrays;
import java.util.List;


@BelongsToContract(GameContract.class)
public class GameState implements ContractState {
    private int move;
    private final Party player1;
    private final Party player2;
    private int player1score;
    private int player2score;
    private UniqueIdentifier linearId;

    public GameState(int move, Party player1, Party player2, int player1score, int player2score, UniqueIdentifier linearId) {
        this.move = move;
        this.player1 = player1;
        this.player2 = player2;
        this.player1score = player1score;
        this.player2score = player2score;
        this.linearId = linearId;
    }

    public int getMove() {
        return move;
    }

    public Party getPlayer1() {
        return player1;
    }

    public Party getPlayer2() { return player2; }

    public int getPlayer1Score() {
        return player1score;
    }

    public int getPlayer2Score() { return player2score; }

    @NotNull @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(player1, player2);
    }

    @NotNull
    public UniqueIdentifier getLinearId() { return this.linearId; }
}