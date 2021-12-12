package com.template.states;

import com.template.contracts.BoLContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(BoLContract.class)
public final class BoLState implements LinearState {

    private Party buyer;
    private Party seller;
    private Party holder;
    private UniqueIdentifier linearID;
    private String bol;

    public BoLState(Party seller, Party buyer, Party holder, String bol, UniqueIdentifier linearID) {
        this.seller = seller;
        this.buyer = buyer;
        this.holder = holder;
        this.linearID = linearID;
        this.bol = bol;
    }

    public String getBoL() {
        return bol;
    }

    public Party getSender() {
        return seller;
    }

    public Party getBuyer() {
        return buyer;
    }

    public Party getHolder() {
        return holder;
    }


    public UniqueIdentifier getLinearId() {
        return this.linearID;
    }

    public BoLState changeOwner(Party holder) {
        BoLState newOwnerState = new BoLState(this.seller, this.buyer, holder, this.bol, this.linearID);
        return newOwnerState;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(seller, buyer);
    }

}
