package com.rps.states;

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType;
import com.rps.contracts.CoffeeTokenContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.spi.AbstractResourceBundleProvider;

@BelongsToContract(CoffeeTokenContract.class)
public class CoffeeTokenType extends EvolvableTokenType {

    private UniqueIdentifier linearId = UniqueIdentifier.Companion.fromString(UUID.randomUUID().toString());
    private final AbstractParty issuer;
    private final AbstractParty owner;
    private double amount = 3.2;
    private int fractionDigits = 2;
    private final List<AbstractParty> maintainers;

    public CoffeeTokenType(UniqueIdentifier linearId, AbstractParty issuer, AbstractParty owner, double amount, int fractionDigits) {
        this.linearId = linearId;
        this.issuer = issuer;
        this.owner = owner;
        this.amount = amount;
        this.fractionDigits = fractionDigits;
        this.maintainers = new ArrayList<>();
        maintainers.add(issuer);
        maintainers.add(owner);
    }

    public CoffeeTokenType(AbstractParty issuer, AbstractParty owner) {
        this.issuer = issuer;
        this.owner = owner;
        this.maintainers = new ArrayList<>();
        maintainers.add(issuer);
        maintainers.add(owner);
    }

    public AbstractParty getIssuer() {
        return issuer;
    }

    public AbstractParty getOwner() {
        return owner;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public int getFractionDigits() {
        return this.fractionDigits;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getMaintainers() { return maintainers;}
}