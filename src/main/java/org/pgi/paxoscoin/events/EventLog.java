package org.pgi.paxoscoin.events;

import org.pgi.paxoscoin.banking.TransactionType;
import org.pgi.paxoscoin.commands.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class EventLog {

    private static final String CHANGED_BALANCE_EVENT_NAME = "CHANGED_BALANCE";

    private static final String GLOBAL_BALANCE_FILE = "persistence/eventLogs.csv";

    public static void reset() {
        try {
            Files.writeString(Path.of(GLOBAL_BALANCE_FILE), "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void persist(Event evt) {
        if (evt instanceof ChangedBalanceEvent event) {
            try {
                String str = CHANGED_BALANCE_EVENT_NAME + "," +
                        event.getTime() + "," +
                        event.getUserID() + "," +
                        event.getTerminalID() + "," +
                        event.getTransactionType() + "," +
                        event.getAmount() + "\n";
                Files.writeString(Path.of(GLOBAL_BALANCE_FILE), str, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException("Event is not supported.");
        }
    }

    public static List<Event> restoreEvent() {
        List<Event> events = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Path.of(GLOBAL_BALANCE_FILE));
            lines.forEach(line -> {
                String[] parts = line.split(",");
                if (parts[1].equals(CHANGED_BALANCE_EVENT_NAME)) {
                    events.add(new ChangedBalanceEvent(
                            Instant.parse(parts[1]),
                            UUID.fromString(parts[2]),
                            UUID.fromString(parts[3]),
                            TransactionType.valueOf(parts[4]),
                            Double.parseDouble(parts[5])));
                } else {
                    throw new UnsupportedOperationException("Event is not supported.");
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        events.sort(Comparator.comparing(Event::getTime));
        return events;
    }
}
