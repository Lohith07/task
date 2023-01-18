package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.ManagementContract;
import com.template.states.ManagementState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import java.util.Arrays;

public class TemplateFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class TemplateFlowInitiator extends FlowLogic<SignedTransaction> {

        // private variables
        private String title;
        private Party receiver;

        private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new IOU.");
        private final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
        private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
        private final Step GATHERING_SIGS = new Step("Gathering the counterparty's signature.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return CollectSignaturesFlow.Companion.tracker();
            }
        };
        private final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction.") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION);

        public void Initiator(String title, Party receiver) {
            Party sender = getOurIdentity();
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Supplier"));
            if (sender == notary) {
                this.title = title;
                this.receiver = receiver;
            }
        }

        // public constructor
        public TemplateFlowInitiator(Party receiver) {
            this.receiver = receiver;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            // Hello World message

            Party me = getOurIdentity();

            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            final Party notary = getServiceHub().getNetworkMapCache().getNotary(CordaX500Name.parse("O=Supplier"));

            final ManagementState output = new ManagementState(title, me, receiver, new UniqueIdentifier());

            final Command<ManagementContract.Commands.Create> txCommand = new Command<>(
                    new ManagementContract.Commands.Create(),
                    Arrays.asList(output.getSender().getOwningKey(), output.getReceiver().getOwningKey()));
            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(output, ManagementContract.ID)
                    .addCommand(txCommand);

            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);

            txBuilder.verify(getServiceHub());

            progressTracker.setCurrentStep(SIGNING_TRANSACTION);

            final SignedTransaction partSignedTx = getServiceHub().signInitialTransaction(txBuilder);

            progressTracker.setCurrentStep(GATHERING_SIGS);

            FlowSession otherPartySession = initiateFlow(receiver);
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(partSignedTx, Arrays.asList(otherPartySession),
                            CollectSignaturesFlow.Companion.tracker()));

            progressTracker.setCurrentStep(FINALISING_TRANSACTION);

            return subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(otherPartySession)));
        }
    }

    @InitiatedBy(TemplateFlowInitiator.class)
    public static class TemplateFlowResponder extends FlowLogic<Void> {
        // private variable
        private FlowSession counterpartySession;

        // Constructor
        public TemplateFlowResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public Void call() throws FlowException {
            SignedTransaction signedTransaction = subFlow(new SignTransactionFlow(counterpartySession) {
                @Suspendable
                @Override
                protected void checkTransaction(SignedTransaction stx) throws FlowException {
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        require.using("This must be an asset transaction.", output instanceof ManagementState);
                        ManagementState title = (ManagementState) output;
                        require.using("I won't accept transaction without title", title.getTitle() == null);
                        return null;
                    });
                }
            });
            // Stored the transaction into data base.
            subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
            return null;
        }
    }

}