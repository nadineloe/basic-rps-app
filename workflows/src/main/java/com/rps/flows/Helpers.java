package com.rps.flows;
import net.corda.core.identity.AbstractParty;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.identity.CordaX500Name;

public class Helpers {

    public static TransactionBuilder ourTx(ServiceHub hub) {
        return new TransactionBuilder(hub.getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")));
    }

//    public static AbstractParty whosWinning(ourselfs, otherParty, player1move, player2move) {
//        //        if (player1move == player2move) {
//        //               throw new FlowException("Tie. Need another round to determine winner.");
//        //           } else if (player1move == null) {
//        //               throw new FlowException("Player 2 hasn't taken his turn yet.");
//        //           } else if (player1move.equals("ROCK") && player2move.equals("SCISSORS")) {
//        //               return player1;
//        //           } else if (player1move.equals("PAPER") && player2move.equals("ROCK")) {
//        //               return player1
//        //           } else (player1move.equals("SCISSORS") && player2move.equals("PAPER")) {
//        //               return player1
//        //           } else {
//        //               return player2;
//        //           }
//    };
}