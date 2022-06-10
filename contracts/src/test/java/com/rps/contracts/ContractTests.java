//package com.rps.contracts;
//
//import com.rps.states.GameState;
//import net.corda.core.identity.CordaX500Name;
//import net.corda.testing.core.TestIdentity;
//import net.corda.testing.node.MockServices;
//import org.junit.Test;
//
//import java.util.Arrays;
//
//import static net.corda.testing.node.NodeTestUtils.ledger;
//
//
//public class ContractTests {
//    private final MockServices ledgerServices = new MockServices(Arrays.asList("com.rps"));
//    TestIdentity alice = new TestIdentity(new CordaX500Name("Alice",  "TestLand",  "US"));
//    TestIdentity bob = new TestIdentity(new CordaX500Name("Alice",  "TestLand",  "US"));
//
//    @Test
//    public void issuerAndRecipientCannotHaveSameEmail() {
//        GameState state = new GameState(alice.getParty(),bob.getParty());
//        ledger(ledgerServices, l -> {
//            l.transaction(tx -> {
//                tx.input(GameContract.ID, state);
//                tx.output(GameContract.ID, state);
//                tx.command(alice.getPublicKey(), new GameContract.Commands.Create());
//                return tx.fails(); //fails because of having inputs
//            });
//            l.transaction(tx -> {
//                tx.output(GameContract.ID, state);
//                tx.command(alice.getPublicKey(), new GameContract.Commands.Create());
//                return tx.verifies();
//            });
//            return null;
//        });
//    }
//}