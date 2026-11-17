package deskapi;

import java.util.List;

/**
 * Where tickets live. Complete. The service depends only on this interface,
 * so the store behind it can change - in-memory today, a database in the SQL
 * course - without touching the service or the routes.
 */
public interface TicketRepository {

    /** Stores the ticket and returns it. */
    Ticket save(Ticket ticket);

    /** The ticket with this id, or null when there is none. */
    Ticket find(String id);

    /** Every stored ticket, oldest first. */
    List<Ticket> all();
}
