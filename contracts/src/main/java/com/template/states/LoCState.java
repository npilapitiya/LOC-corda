package com.template.states;

import com.template.contracts.LoCContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(LoCContract.class)
public final class LoCState implements LinearState {

    private Party buyer;
    private Party buyerBank;
    private Party seller;
    private Party sellerBank;

    private UniqueIdentifier linearID;
    private String loc;

    public LoCState(Party buyer, Party buyerBank, String loc, UniqueIdentifier linearID) {
        this.buyer = buyer;
        this.buyerBank = buyerBank;
        this.linearID = linearID;
        this.loc = loc;
    }

    @ConstructorForDeserialization
    public LoCState(Party buyer, Party buyerBank, Party seller, Party sellerBank, String loc, UniqueIdentifier linearID) {
        this.buyer = buyer;
        this.buyerBank = buyerBank;
        this.seller = seller;
        this.sellerBank = sellerBank;
        this.linearID = linearID;
        this.loc = loc;
    }

    public String getLoC() {
        return loc;
    }

    public Party getSender() {
        return buyer;
    }

    public Party getReceiver() {
        return buyerBank;
    }

    public UniqueIdentifier getLinearId() {
        return this.linearID;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(buyer, buyerBank);
    }

    public LoCState approve(Party seller, Party sellerBank) {
        LoCState newState = new LoCState(this.buyer, this.buyerBank, seller,sellerBank, this.loc, this.linearID);
        return newState;
    }

}
