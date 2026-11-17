package deskapi;

/**
 * Thrown when a ticket id does not exist. Complete. It is unchecked
 * (extends RuntimeException): a missing resource is an expected request
 * outcome the API layer maps to a response, not a contract every method
 * signature should carry.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
