package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.LoCContract;
import com.template.states.LoCState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;
import java.util.UUID;

public class ApproveAndSendLoC {
    @InitiatingFlow
    @StartableByRPC
    public static class ApproveAndSendLoCInitiator extends FlowLogic<SignedTransaction> {

        private String loc;
        private Party buyer;
        private Party seller;
        private Party sellerBank;
        private UniqueIdentifier locId;

        public ApproveAndSendLoCInitiator(String loc, Party buyer, Party sellerBank, Party seller, UniqueIdentifier locId) {
            this.loc = loc;
            this.buyer = buyer;
            this.seller = seller;
            this.sellerBank = sellerBank;
            this.locId = locId;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);


//            UniqueIdentifier uniqueID = new UniqueIdentifier();
//            LoCState newStamp = new LoCState(this.getOurIdentity(), this.buyerBank, this.loc, uniqueID);


            QueryCriteria.LinearStateQueryCriteria inputCriteria = new QueryCriteria.LinearStateQueryCriteria()
                    .withUuid(Arrays.asList(UUID.fromString(locId.toString())));
            
            StateAndRef locStateAndRef = getServiceHub().getVaultService().queryBy(LoCState.class, inputCriteria).getStates().get(0);

            LoCState originalLoC = (LoCState) locStateAndRef.getState().getData();

            LoCState output = originalLoC.approve(seller, sellerBank);

            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(output, LoCContract.ID)
                    .addCommand(new LoCContract.Commands.Approve(),
                            Arrays.asList(getOurIdentity().getOwningKey(),this.buyer.getOwningKey()));

            txBuilder.verify(getServiceHub());

            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            FlowSession buyerPartySession = initiateFlow(buyer);
            FlowSession sellerPartySession = initiateFlow(seller);
            FlowSession sellerBankPartySession = initiateFlow(sellerBank);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(buyerPartySession, sellerPartySession, sellerBankPartySession)));

            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(buyerPartySession, sellerPartySession, sellerBankPartySession)));
        }
    }

    @InitiatedBy(ApproveAndSendLoCInitiator.class)
    public static class ApproveAndSendLoCResponder extends FlowLogic<Void> {

        private FlowSession counterpartySession;


        public ApproveAndSendLoCResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Override
                @Suspendable
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                }
            });
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }
}
