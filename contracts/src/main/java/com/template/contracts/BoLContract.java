package com.template.contracts;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

public final class BoLContract implements Contract {

    public static final String ID = "com.template.contracts.BoLContract";

    @Override
    public void verify(LedgerTransaction tx) {

    }

    public interface Commands extends CommandData {
        class Send implements BoLContract.Commands {
        }
    }
}
