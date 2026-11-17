package deskapi;

/**
 * The JSON body of POST /tickets, for example {"title": "Printer jam"}.
 * Complete - Jackson fills this record from the request body.
 */
public record NewTicket(String title) {
}
