package org.pgi.paxoscoin.banking;

import org.pgi.paxoscoin.commands.Command;
import org.pgi.paxoscoin.commands.PayWageCommand;
import org.pgi.paxoscoin.commands.ReadCardCommand;
import org.pgi.paxoscoin.events.ChangedBalanceEvent;
import org.pgi.paxoscoin.events.Event;
import org.pgi.paxoscoin.events.EventLog;
import org.pgi.paxoscoin.exceptions.UnsupportedCommandException;
import org.pgi.paxoscoin.worldmodel.Employee;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * The banking backend.
 */
public class BankingBackend {

    private static final String ACCOUNT_BALANCES_FILE = "persistence/account_balances.csv";
    private static final String GLOBAL_BALANCE_FILE = "persistence/global_balance.csv";

    private static BankingBackend instance;
    
    private double globalBalance;


    /**
     * Private constructor to allow instantiation only through {@link #getInstance()}
     */
    private BankingBackend() {
        // can only be instantiated through {@link #getInstance()}
        this.globalBalance = 0d;

    }

    /**
     * Method to retrieve the banking singleton backend instance.
     *
     * @return instance The banking backend instance
     */
    public static BankingBackend getInstance() {
        if (BankingBackend.instance == null) {
            instance = new BankingBackend();
        }
        return instance;
    }

    /**
     * Handle a command (e.g. a card that is read or a wage that is payed).
     *
     * @param command The command issued to the backend
     * @throws UnsupportedCommandException in case the command is unrecognized
     */
    public void handleCommand(Command command) throws UnsupportedCommandException {
        
        if(command instanceof PayWageCommand) {
            PayWageCommand pwcommand = (PayWageCommand) command;
            pwcommand.getEmployee().getAccount().deposit(pwcommand.getAmount());
            this.globalBalance += pwcommand.getAmount();

            ChangedBalanceEvent event = new ChangedBalanceEvent(
                    pwcommand.getEmployee().getId(),
                    null, // command has no id
                    TransactionType.DEPOSIT,
                    pwcommand.getAmount());
            EventLog.persist(event);
            // TODO: create ChangedBalanceEvent

        } else if(command instanceof ReadCardCommand) {
            ReadCardCommand rccommand = (ReadCardCommand) command;
            // reject any read card commands, if the current account balance is not sufficing
            if (rccommand.getAmount() > rccommand.getCard().getEmployee().getAccount().getBalance()) {
                System.err.println(rccommand.getCard().getEmployee().getName() + " just tried to overdraw their account!");
                return;
            }
            rccommand.getCard().getEmployee().getAccount().withdraw(rccommand.getAmount());
            this.globalBalance -= rccommand.getAmount();

            ChangedBalanceEvent event = new ChangedBalanceEvent(
                    rccommand.getCard().getEmployee().getId(),
                    rccommand.getTerminal().getId(),
                    TransactionType.WITHDRAW,
                    rccommand.getAmount());
            EventLog.persist(event);
            // TODO: create ChagedBalanceEvent
        } else {
            throw new UnsupportedCommandException();
        }
        
    }

    /**
     * Gets the current global balance.
     *
     * @return the current global balance
     */
    public double getGlobalBalance() {
        return globalBalance;
    }

    /**
     * Sets the current global balance.
     */
    public void setGlobalBalance(double globalBalance) {
        this.globalBalance = globalBalance;
    }

    /**
     * Saves a checkpoint of the system state (i.e. the accounts and the current global balance) to the hard drive
     */
    public void saveCheckpoint(Map<UUID, Employee> employees) {

        // TODO: save a current checkpoint of all accounts to a file
        // the choice of format is completely up to you,
        // but has to match the restore logic in the restoreCheckpoint method

        // persist global balance
        try {
            Files.writeString(Path.of(GLOBAL_BALANCE_FILE), "" + globalBalance, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // persist accounts
        try (FileWriter writer = new FileWriter(ACCOUNT_BALANCES_FILE, StandardCharsets.UTF_8)) {
            employees.forEach((key, value) -> {
                String line = value.getId() + "," + value.getAccount().getBalance() + "\n";
                try {
                    writer.append(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restores a checkpoint that was saved with {@link #saveCheckpoint(Map)}
     */
    public void restoreCheckpoint(Map<UUID, Employee> employees) {
        // TODO: restore the last saved checkpoint

        // solution for subtask 3


        List<Event> events = EventLog.restoreEvent();

        employees.values().forEach(employee -> employee.setAccount(new Account(employee, 0)));

        events.forEach(evt -> {
            if (evt instanceof ChangedBalanceEvent event) {
                Employee employee = employees.get(event.getUserID());
                switch (event.getTransactionType()) {
                    case DEPOSIT -> {
                        employee.getAccount().deposit(event.getAmount());
                        this.globalBalance += event.getAmount();
                    }
                    case WITHDRAW -> {
                        employee.getAccount().withdraw(event.getAmount());
                        this.globalBalance -= event.getAmount();
                    }
                }
            } else {
                throw new UnsupportedOperationException("Event is not supported.");
            }
        });


        // Solution for subtask 2

        /*
        // restore global balance
        try {
            List<String> globalBalance = Files.readAllLines(Path.of(GLOBAL_BALANCE_FILE), StandardCharsets.UTF_8);
            this.globalBalance = Double.parseDouble(globalBalance.getFirst());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // restore accounts
        try {
            List<String> accounts = Files.readAllLines(Path.of(ACCOUNT_BALANCES_FILE), StandardCharsets.UTF_8);
            accounts.forEach(account -> {
                String[] data = account.split(",");
                UUID uuid = UUID.fromString(data[0]);
                double balance = Double.parseDouble(data[1]);

                Employee employee = employees.get(uuid);
                employee.setAccount(new Account(employee, balance));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
         */
    }
}
