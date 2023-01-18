package com.template.contracts;

import com.template.states.ManagementState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class ManagementContract implements Contract {

    public static final String ID = "com.template.contracts.ManagementContract";

    @Override
    public void verify(LedgerTransaction tx) {

        final CommandData commandData = tx.getCommands().get(0).getValue();

        if (commandData instanceof Commands.Create) {
            // Retrieve the output state of the transaction
            ManagementState output = tx.outputsOfType(ManagementState.class).get(0);
            CommandWithParties<ManagementContract.Commands> command = requireSingleCommand(tx.getCommands(),
                    ManagementContract.Commands.class);

            requireThat(require -> {
                require.using("Sender must be required singer.",
                        command.getSigners().contains(output.getSender().getOwningKey()));
                require.using("Only one output states should be created.",
                        tx.getOutputs().size() == 1);
                final ManagementState out = tx.outputsOfType(ManagementState.class).get(0);
                require.using("The lender and the borrower cannot be the same entity.",
                        !out.getSender().equals(out.getReceiver()));
                require.using("Receiver must be required singer.",
                        command.getSigners().contains(output.getReceiver().getOwningKey()));
                return null;
            });
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        // In our hello-world app, We will only have one command.
        class Create implements Commands {
        }

        class TransferAsset implements Commands {
        }
    }
}