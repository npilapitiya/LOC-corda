package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public final class LoCContract implements Contract {

    public static final String ID = "com.template.contracts.LoCContract";

    @Override
    public void verify(LedgerTransaction tx) {

    }

    public interface Commands extends CommandData {
        class Send implements LoCContract.Commands {
        }
        class Approve implements LoCContract.Commands {
        }
    }
}
