package com.template.states;

import com.template.contracts.GameContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.contracts.UniqueIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


@BelongsToContract(GameContract.class)
public class GameState implements ContractState {
    private final Party player1;
    private final Party player2;
    private int player1score;
    private int player2score;
    private UniqueIdentifier linearId;
    private String firstMove;
    private String secondMove;

    public GameState(Party player1, Party player2, int player1score, int player2score, UniqueIdentifier linearId, String firstMove, String secondMove) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1score = player1score;
        this.player2score = player2score;
        this.linearId = linearId;
        this.firstMove = firstMove;
        this.secondMove = secondMove;
    }

    public String getFirstMove() {
        return firstMove;
    }

    public String getSecondMove() {
        return secondMove;
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

//    public GameState returnNewScoreAfterRound(Party player1, Party player2){
//        GameState newGameState = new GameState(this.player1, this.player2, me, competitor, false, this.linearId, 0, 0, "Rock", Status.GAME_IN_PROGRESS);
//        return newGameState;
//        if(player1Score >= 3 || player2Score >= 3){
//            GameState newGameState = new GameState(this.player1, this.player2, me, competitor, !this.isPlayer1Turn, this.linearId, player1Score, player2Score, Status.GAME_OVER);
//            return newGameState;
//        } else {
//            GameState newGameState = new GameState(this.player1, this.player2, me, competitor, this.isPlayer1Turn, this.linearId, player1Score, player2Score , Status.GAME_IN_PROGRESS);
//            return newGameState;
//        }
}