package com.rps.flows;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.identity.CordaX500Name;

public class Helpers {

    public static TransactionBuilder ourTx(ServiceHub hub) {
        return new TransactionBuilder(hub.getNetworkMapCache().getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")));
    }
}