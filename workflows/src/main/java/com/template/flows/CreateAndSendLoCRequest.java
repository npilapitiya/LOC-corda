package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.LoCContract;
import com.template.states.LoCState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;

public class CreateAndSendLoCRequest {
    @InitiatingFlow
    @StartableByRPC
    public static class CreateAndSendLoCRequestInitiator extends FlowLogic<SignedTransaction> {

        private String locRequest;
        private Party buyerBank;

        public CreateAndSendLoCRequestInitiator(String locRequest, Party buyerBank) {
            this.locRequest = locRequest;
            this.buyerBank = buyerBank;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            UniqueIdentifier uniqueID = new UniqueIdentifier();
            LoCState newStamp = new LoCState(this.getOurIdentity(), this.buyerBank, this.locRequest, uniqueID);

            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(newStamp)
                    .addCommand(new LoCContract.Commands.Send(),
                            Arrays.asList(getOurIdentity().getOwningKey(),buyerBank.getOwningKey()));

            txBuilder.verify(getServiceHub());

            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            FlowSession otherPartySession = initiateFlow(buyerBank);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession)));

            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession)));
        }
    }

    @InitiatedBy(CreateAndSendLoCRequestInitiator.class)
    public static class CreateAndSendLoCRequestResponder extends FlowLogic<Void>{

        private FlowSession counterpartySession;


        public CreateAndSendLoCRequestResponder(FlowSession counterpartySession) {
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
