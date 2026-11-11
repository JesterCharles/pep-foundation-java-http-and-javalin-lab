package deskapi;

import java.util.List;

/**
 * Task 2: the rules between the routes and the store.
 * The service gets its repository through the constructor and never creates
 * one itself - the tests (and later courses) hand in their own.
 */
public class TicketService {

    private final TicketRepository repository;
    private int nextNumber = 1;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    // Task 2: create a ticket for the title: reject a null or blank title with
    //   new IllegalArgumentException("title must not be blank")
    // then build an open ticket with the next id - "T-1", "T-2", ... -
    // save it and return it.
    public Ticket create(String title) {
        return null; // TODO
    }

    // Task 2: the ticket for the id, or
    //   throw new NotFoundException("no ticket " + id)
    // when the repository answers null.
    public Ticket get(String id) {
        return null; // TODO
    }

    // Task 2: every ticket, oldest first.
    public List<Ticket> list() {
        return null; // TODO: delegate to the repository
    }
}
