package deskapi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Task 1: the in-memory store behind the repository interface.
 * A LinkedHashMap keyed by id keeps lookups direct and remembers insertion
 * order, so all() can answer oldest first.
 */
public class InMemoryTicketRepository implements TicketRepository {

    private final LinkedHashMap<String, Ticket> store = new LinkedHashMap<>();

    // Task 1: store the ticket under its id and return it.
    @Override
    public Ticket save(Ticket ticket) {
        return null; // TODO
    }

    // Task 1: the ticket for the id, or null - the interface says so.
    @Override
    public Ticket find(String id) {
        return null; // TODO
    }

    // Task 1: every ticket, oldest first, as a new list.
    @Override
    public List<Ticket> all() {
        return new ArrayList<>(); // TODO: copy the map's values into the list
    }
}
