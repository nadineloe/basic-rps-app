//package com.rps.states;
//
//import com.rps.contracts.GameContract;
//import net.corda.core.contracts.*;
//import net.corda.core.identity.AbstractParty;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//@BelongsToContract(TokenContract.class)
//public class TokenState extends EvolvableTokenType {
//
//    private String issuer;
//    private String receipient;
//    private UniqueIdentifier linearId;
//
//    public CustomTokenState(String issuer, String receipient, UniqueIdentifier linearId) {
//        this.issuer = issuer;
//        this.receipient = receipient;
//        this.linearId = linearId;
//    }
//
//    public String getIssuer() {
//        return issuer;
//    }
//
//    public String getReceipient() {
//        return receipient;
//    }
//
//    @NotNull
//    @Override
//    public UniqueIdentifier getLinearId() {
//        return linearId;
//    }
//
//    public void setIssuer(String issuer) {
//        this.issuer = issuer;
//    }
//
//    public void setReceipient(String receipient) {
//        this.receipient = receipient;
//    }
//
//    public void setLinearId(UniqueIdentifier linearId) {
//        this.linearId = linearId;
//    }

    /* This method returns a TokenPointer by using the linear Id of the state */
//    public TokenPointer<TokenState> toPointer(){
//        LinearPointer<TokenState> linearPointer = new LinearPointer<>(linearId, TokenState.class);
//        return new TokenPointer<>(linearPointer);
//    }
//}

