package deskapi;

/**
 * One ticket as the API sends and stores it. Complete - a record fits here:
 * plain immutable data with generated accessors that Jackson turns into JSON
 * fields (id, title, open).
 */
public record Ticket(String id, String title, boolean open) {
}
