package com.template.states;

import com.template.contracts.SalesContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(SalesContract.class)
public final class SalesContractState implements LinearState {

    private final Party seller;
    private final Party buyer;
    private final String salesContract;
    //    private Date date;
//    private String buyerName;
//    private String buyerContact;
//    private String buyerAddress;
//    private String sellerName;
//    private String sellerContact;
//    private String sellerAddress;
//    private String itemDesc;
//    private String quantity;
//    private String unitPrice;
//    private String amount;
    private UniqueIdentifier linearID;

    public SalesContractState(Party seller, Party buyer, String salesContract, UniqueIdentifier linearID) {
        this.seller = seller;
        this.buyer = buyer;
        this.salesContract = salesContract;
        this.linearID = linearID;
    }

    public String getSalesContract() {
        return salesContract;
    }

    public Party getSender() {
        return seller;
    }

    public Party getBuyer() {
        return buyer;
    }

    public UniqueIdentifier getLinearId() {
        return this.linearID;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(seller, buyer);
    }

}
