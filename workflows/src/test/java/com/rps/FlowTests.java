package com.rps;

import com.google.common.collect.ImmutableList;
import com.rps.flows.*;
import com.rps.states.GameState;

import com.rps.states.MoveState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;

import net.corda.core.node.NetworkParameters;
import net.corda.core.node.NotaryInfo;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
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

//    private NetworkParameters testNetworkParameters =
//            new NetworkParameters(4, Collections.emptyList(),
//                    10485760, 10485760 * 50, Instant.now(), 1,
//                    Collections.emptyMap());

    @Before
    public void setup() {

        network = new MockNetwork(new MockNetworkParameters().withCordappsForAllNodes(ImmutableList.of(
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
    public void checkIfMoveStateExists() throws ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        Vault.Page<MoveState> testStatesNodeA = a.getServices().getVaultService().queryBy(MoveState.class);
        Vault.Page<MoveState> testStatesNodeB = b.getServices().getVaultService().queryBy(MoveState.class);
        assert testStatesNodeA.getStates().size() == 1;
        assert testStatesNodeB.getStates().size() == 1;
        MoveState dataA = testStatesNodeA.getStates().get(0).getState().getData();
        MoveState dataB = testStatesNodeB.getStates().get(0).getState().getData();
        assert dataA.equals(dataB);
    }

    @Test
    public void playerCanOnlyMoveOnce() {
        GameState testState = new GameState(players);
        a.startFlow(new CreateGameFlow.Initiator(player2));
        network.runNetwork();
        Vault.Page<MoveState> testStatesNodeA = a.getServices().getVaultService().queryBy(MoveState.class);
        Vault.Page<MoveState> testStatesNodeB = b.getServices().getVaultService().queryBy(MoveState.class);
        assert testStatesNodeA.getStates().size() == 0;
        assert testStatesNodeB.getStates().size() == 0;
    }

    @Test
    public void checkIfMsgIsBoolean() throws FlowException, ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        AskOtherPartyFlow.Initiator askOtherPartyFlow = new AskOtherPartyFlow.Initiator(gameId);
        Future<Boolean> hasPlayerGoneFuture = a.startFlow(askOtherPartyFlow);
        network.runNetwork();
    }

    @Test
    public void checkOtherPlayersMoveState() throws FlowException, ExecutionException, InterruptedException {
        CreateGameFlow.Initiator createGameFlow = new CreateGameFlow.Initiator(player2);
        Future<UniqueIdentifier> createGameFuture = a.startFlow(createGameFlow);
        network.runNetwork();
        UniqueIdentifier gameId = createGameFuture.get();

        ExchangeMovesFlow.Initiator exchangeMoveFlow = new ExchangeMovesFlow.Initiator(gameId);
        Future<String> counterpartyMove = a.startFlow(exchangeMoveFlow);
        network.runNetwork();
    }

//    @Test
//    public void checkIfPickTurnFlowExists() {
//        CreateGameFlow.Initiator flow = new CreateGameFlow.Initiator(player2);
//        Future<UniqueIdentifier> future = a.startFlow(flow);
//        network.runNetwork();
//
//        PickTurnFlow.Initiator flow = new PickTurnFlow.Initiator(gameId);
//        Future<SignedTransaction> future = a.startFlow(flow);
//        network.runNetwork();
//
//        //successful query means the state is stored at node b's vault. Flow went through.
//        QueryCriteria inputCriteria = new QueryCriteria.VaultQueryCriteria().withStatus(Vault.StateStatus.UNCONSUMED);
//        GameState state = b.getServices().getVaultService().queryBy(GameState.class, inputCriteria)
//                .getStates().get(0).getState().getData();
//    }
}
