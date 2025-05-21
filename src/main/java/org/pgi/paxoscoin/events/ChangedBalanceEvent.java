package org.pgi.paxoscoin.events;

import org.pgi.paxoscoin.banking.TransactionType;

import java.time.Instant;
import java.util.UUID;

public class ChangedBalanceEvent implements Event {

    private final Instant time;
    private final UUID userID;
    private final UUID terminalID;
    private final TransactionType transactionType;
    private final double amount;

    public ChangedBalanceEvent(Instant time, UUID userID, UUID terminalID, TransactionType transactionType, double amount) {
        this.time = time;
        this.userID = userID;
        this.terminalID = terminalID;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    public ChangedBalanceEvent(UUID userID, UUID terminalID, TransactionType transactionType, double amount) {
        this.time = Instant.now();
        this.userID = userID;
        this.terminalID = terminalID;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    // GETTERS

    public UUID getUserID() {
        return userID;
    }

    public UUID getTerminalID() {
        return terminalID;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public Instant getTime() {
        return time;
    }
}
