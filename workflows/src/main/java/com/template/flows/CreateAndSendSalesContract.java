package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.SalesContract;
import com.template.states.SalesContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.Arrays;

public class CreateAndSendSalesContract {
    @InitiatingFlow
    @StartableByRPC
    public static class CreateAndSendSalesContractInitiator extends FlowLogic<SignedTransaction> {

        private String salesContract;
        private Party buyer;

        public CreateAndSendSalesContractInitiator(String salesContract, Party buyer) {
            this.salesContract = salesContract;
            this.buyer = buyer;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            UniqueIdentifier uniqueID = new UniqueIdentifier();
            SalesContractState newStamp = new SalesContractState(this.getOurIdentity(), this.buyer, this.salesContract, uniqueID);

            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(newStamp)
                    .addCommand(new SalesContract.Commands.Send(),
                            Arrays.asList(getOurIdentity().getOwningKey(),buyer.getOwningKey()));

            txBuilder.verify(getServiceHub());

            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            FlowSession otherPartySession = initiateFlow(buyer);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession)));

            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession)));
        }
    }

    @InitiatedBy(CreateAndSendSalesContractInitiator.class)
    public static class CreateAndSendSalesContractResponder extends FlowLogic<Void>{

        private FlowSession counterpartySession;

        public CreateAndSendSalesContractResponder(FlowSession counterpartySession) {
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
