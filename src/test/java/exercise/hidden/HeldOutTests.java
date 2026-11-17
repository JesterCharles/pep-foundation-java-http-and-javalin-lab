package exercise.hidden;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import exercise.DeskApi;
import exercise.InMemoryTicketRepository;
import exercise.NotFoundException;
import exercise.TicketService;
import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

/** Held-out tests: graded on submit, never shown to the learner. The edge cases of each task. */
class HeldOutTests {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    private static HttpResponse<String> get(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> post(int port, String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /** The "error" field of a JSON body, or null when the body is not that JSON. */
    private static String errorField(String body) {
        try {
            JsonNode node = JSON.readTree(body).get("error");
            return node == null ? null : node.asText();
        } catch (Exception notJson) {
            return null;
        }
    }

    @Test
    void task1FindUnknownId() {
        InMemoryTicketRepository repository = new InMemoryTicketRepository();
        assertNull(repository.find("T-9"), "Task 1: find of an unknown id answers null, as the interface says");
    }

    @Test
    void task2ServiceMessages() {
        TicketService service = new TicketService(new InMemoryTicketRepository());
        NotFoundException missing = assertThrows(NotFoundException.class, () -> service.get("T-9"),
                "Task 2: an unknown id must raise NotFoundException");
        assertEquals("no ticket T-9", missing.getMessage(), "Task 2: the message names the id");
        IllegalArgumentException blank = assertThrows(IllegalArgumentException.class,
                () -> service.create("  "), "Task 2: a blank title must be rejected");
        assertEquals("title must not be blank", blank.getMessage(), "Task 2: the rejection message");
    }

    @Test
    void task3ListIsJson() throws Exception {
        Javalin app = DeskApi.create(new TicketService(new InMemoryTicketRepository()));
        app.start(0);
        try {
            HttpResponse<String> all = get(app.port(), "/tickets");
            assertTrue(all.headers().firstValue("Content-Type").orElse("").startsWith("application/json"),
                    "Task 3: the list response is served as application/json");
        } finally {
            app.stop();
        }
    }

    @Test
    void task4BadRequestBody() throws Exception {
        Javalin app = DeskApi.create(new TicketService(new InMemoryTicketRepository()));
        app.start(0);
        try {
            HttpResponse<String> blank = post(app.port(), "/tickets", "{\"title\":\"  \"}");
            assertEquals("title must not be blank", errorField(blank.body()),
                    "Task 4: the 400 body is JSON with an error field");
        } finally {
            app.stop();
        }
    }
}
