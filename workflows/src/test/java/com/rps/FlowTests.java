package com.rps;

import com.google.common.collect.ImmutableList;
import com.rps.flows.*;
import com.rps.states.GameState;
import com.rps.states.MoveState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NotaryInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FlowTests {
    private MockNetwork network;
    private StartedMockNode a;
    private StartedMockNode b;
    private AbstractParty player1;
    private AbstractParty player2;
    private List<AbstractParty> players;
    List<NotaryInfo> notaryinfo = Arrays.asList();

    private NetworkParameters testNetworkParameters =
            new NetworkParameters(4,
                    notaryinfo,
                    10485760,
                    524288000,
                    java.time.Instant.now(),
                    1,
                    Collections.emptyMap());

    @Before
    public void setup() {

        network = new MockNetwork(new MockNetworkParameters().withNetworkParameters(testNetworkParameters).withCordappsForAllNodes(ImmutableList.of(
                TestCordapp.findCordapp("com.rps.contracts"),
                TestCordapp.findCordapp("com.rps.flows")))
                .withNotarySpecs(ImmutableList.of(new MockNetworkNotarySpec(CordaX500Name.parse("O=Notary,L=London,C=GB")))));
        a = network.createPartyNode(null);
        b = network.createPartyNode(null);
        player1 = a.getInfo().getLegalIdentities().get(0);
        player2 = b.getInfo().getLegalIdentities().get(0);
        players = Arrays.asList(player1, player2);
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    // ###############################################
    //          CREATE GAME FLOW
    // ###############################################

    @Test
    public void checkIfGameStateExists() {
        GameState testState = new GameState(players);
        a.startFlow(new CreateGameFlow.Initiator(player2));
        network.runNetwork();
        Vault.Page<GameState> testStatesNodeA = a.getServices().getVaultService().queryBy(GameState.class);
        Vault.Page<GameState> testStatesNodeB = b.getServices().getVaultService().queryBy(GameState.class);
        assert testStatesNodeA.getStates().size() == 1;
        assert testStatesNodeB.getStates().size() == 1;
        GameState dataA = testStatesNodeA.getStates().get(0).getState().getData();
        GameState dataB = testStatesNodeB.getStates().get(0).getState().getData();
        assert dataA.equals(dataB);
    }

    @Test
    public void checkIfCreateGameFlowExists() {
        CreateGameFlow.Initiator flow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> future = a.startFlow(flow);
        network.runNetwork();

        //successful query means the state is stored at node b's vault. Flow went through.
        GameState game = b.getServices().getVaultService().queryBy(GameState.class).getStates().get(0).getState().getData();
    }

    @Test
    public void checkIfMoveStateEmptyWhenNoTurnPicked() {
        GameState testState = new GameState(players);
        a.startFlow(new CreateGameFlow.Initiator(player2));
        network.runNetwork();
        Vault.Page<MoveState> testStatesNodeA = a.getServices().getVaultService().queryBy(MoveState.class);
        Vault.Page<MoveState> testStatesNodeB = b.getServices().getVaultService().queryBy(MoveState.class);
        assert testStatesNodeA.getStates().size() == 0;
        assert testStatesNodeB.getStates().size() == 0;
    }


    // ###############################################
    //          PICK TURN FLOW
    // ###############################################

    @Test
    public void checkIfPickTurnFlowCreatesMoveState() throws ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        PickTurnFlow flow = new PickTurnFlow(gameId, "SCISSOR");
        a.startFlow(flow);
        network.runNetwork();

        Vault.Page<MoveState> testStatesNodeA = a.getServices().getVaultService().queryBy(MoveState.class);
        Vault.Page<MoveState> testStatesNodeB = b.getServices().getVaultService().queryBy(MoveState.class);
        assert testStatesNodeA.getStates().size() == 1;
        assert testStatesNodeB.getStates().size() == 0;
    }

    @Test
    public void checkIfBothMoveStatesPresentWhenBothPlayersMoved() throws ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        PickTurnFlow pickTurnFlow = new PickTurnFlow(gameId, "ROCK");
        a.startFlow(pickTurnFlow);
        network.runNetwork();

        PickTurnFlow newPickTurnFlow = new PickTurnFlow(gameId, "PAPER");
        b.startFlow(newPickTurnFlow);
        network.runNetwork();

        Vault.Page<MoveState> testStatesNodeA = a.getServices().getVaultService().queryBy(MoveState.class);
        Vault.Page<MoveState> testStatesNodeB = b.getServices().getVaultService().queryBy(MoveState.class);
        assert testStatesNodeA.getStates().size() == 1;
        assert testStatesNodeB.getStates().size() == 1;
        MoveState dataA = testStatesNodeA.getStates().get(0).getState().getData();
        MoveState dataB = testStatesNodeB.getStates().get(0).getState().getData();
        assert dataA != dataB;
    }


    // ###############################################
    //          CHECK STATUS FLOW
    // ###############################################

    @Test
    public void checkIfMsgIsBoolean() throws FlowException, ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        CheckStatusFlow.Initiator checkStatusFlow = new CheckStatusFlow.Initiator(gameId);
        Future<Boolean> hasPlayerGoneFuture = a.startFlow(checkStatusFlow);
        network.runNetwork();
    }

    @Test
    public void checkStatusFlowShouldReturnFalseIfNoMovesHaveBeenMade() throws ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        CheckStatusFlow.Initiator flow = new CheckStatusFlow.Initiator(gameId);
        Future<Boolean> checkStatusFuture = a.startFlow(flow);
        network.runNetwork();
        assert !checkStatusFuture.get();
    }


    @Test
    public void checkStatusFlowShouldReturnFalseIfPlayer2NotReady() throws ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        PickTurnFlow pickTurnFlow = new PickTurnFlow(gameId, "ROCK");
        a.startFlow(pickTurnFlow);
        network.runNetwork();

        CheckStatusFlow.Initiator checkStatusFlow = new CheckStatusFlow.Initiator(gameId);
        Future<Boolean> checkStatusFuture = a.startFlow(checkStatusFlow);
        network.runNetwork();
        assert !checkStatusFuture.get();
    }


    @Test
    public void checkOtherPlayersMoveState() throws FlowException, ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        PickTurnFlow pickTurnFlow = new PickTurnFlow(gameId, "ROCK");
        Future<SignedTransaction> futureP1 = a.startFlow(pickTurnFlow);
        network.runNetwork();

        PickTurnFlow newPickTurnFlow = new PickTurnFlow(gameId, "PAPER");
        Future<SignedTransaction> futureP2 = b.startFlow(newPickTurnFlow);
        network.runNetwork();

        Vault.Page<MoveState> testStatesNodeA = a.getServices().getVaultService().queryBy(MoveState.class);
        Vault.Page<MoveState> testStatesNodeB = b.getServices().getVaultService().queryBy(MoveState.class);
        MoveState dataA = testStatesNodeA.getStates().get(0).getState().getData();
        MoveState dataB = testStatesNodeB.getStates().get(0).getState().getData();

        ExchangeMovesFlow.Initiator exchangeMoveFlow = new ExchangeMovesFlow.Initiator(gameId);
        Future<String> counterpartyMoveFuture = a.startFlow(exchangeMoveFlow);
        network.runNetwork();
        String counterpartyMove = counterpartyMoveFuture.get();
        System.out.println(counterpartyMove);
        assert counterpartyMove.equals(dataB.getMove());
    }

    @Test
    public void checkWinner() throws FlowException, ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        PickTurnFlow pickTurnFlow = new PickTurnFlow(gameId, "ROCK");
        Future<SignedTransaction> futureP1 = a.startFlow(pickTurnFlow);
        network.runNetwork();

        PickTurnFlow newPickTurnFlow = new PickTurnFlow(gameId, "PAPER");
        Future<SignedTransaction> futureP2 = b.startFlow(newPickTurnFlow);
        network.runNetwork();

        Vault.Page<MoveState> testStatesNodeA = a.getServices().getVaultService().queryBy(MoveState.class);
        Vault.Page<MoveState> testStatesNodeB = b.getServices().getVaultService().queryBy(MoveState.class);
        assert testStatesNodeA.getStates().size() == 1;
        assert testStatesNodeB.getStates().size() == 1;
        MoveState dataA = testStatesNodeA.getStates().get(0).getState().getData();
        MoveState dataB = testStatesNodeB.getStates().get(0).getState().getData();

        Future<AbstractParty> whoIsWinner = a.startFlow(new PickWinnerFlow.Initiator(gameId));
//        System.out.println(whoIsWinner);
        AbstractParty winner = whoIsWinner.get();
        assert winner.equals(player2);
    }
}
