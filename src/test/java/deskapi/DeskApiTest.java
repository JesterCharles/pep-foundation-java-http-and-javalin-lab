package deskapi;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Public tests for the HTTP and Javalin lab. The repository and service tests
 * call the classes directly; the route tests start the real app on a free
 * localhost port and speak real HTTP to it with Java's own HttpClient.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class DeskApiTest {

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
    void task1Repository() {
        InMemoryTicketRepository repository = new InMemoryTicketRepository();
        Ticket jam = new Ticket("T-1", "Printer jam", true);
        Ticket vpn = new Ticket("T-2", "VPN drops", true);
        assertAll(
                () -> assertEquals(jam, repository.save(jam), "Task 1: save returns the stored ticket"),
                () -> assertEquals(vpn, repository.save(vpn), "Task 1: save returns the stored ticket"),
                () -> assertEquals(jam, repository.find("T-1"), "Task 1: find(\"T-1\")"),
                () -> assertNull(repository.find("T-9"),
                        "Task 1: find of an unknown id answers null, as the interface says"),
                () -> assertEquals(List.of(jam, vpn), repository.all(), "Task 1: all(), oldest first"));
    }

    @Test
    void task2Service() {
        TicketService service = new TicketService(new InMemoryTicketRepository());
        Ticket first = service.create("Printer jam");
        Ticket second = service.create("VPN drops");
        assertNotNull(first, "Task 2: create must return the new ticket, not null");
        assertAll(
                () -> assertEquals("T-1", first.id(), "Task 2: the first id is T-1"),
                () -> assertEquals("T-2", second.id(), "Task 2: the ids count up"),
                () -> assertEquals("Printer jam", first.title(), "Task 2: the title is kept"),
                () -> assertTrue(first.open(), "Task 2: a new ticket is open"),
                () -> assertEquals(first, service.get("T-1"), "Task 2: get finds a saved ticket"),
                () -> assertEquals(List.of(first, second), service.list(), "Task 2: list, oldest first"));
        NotFoundException missing = assertThrows(NotFoundException.class, () -> service.get("T-9"),
                "Task 2: an unknown id must raise NotFoundException");
        assertEquals("no ticket T-9", missing.getMessage(), "Task 2: the message names the id");
        IllegalArgumentException blank = assertThrows(IllegalArgumentException.class,
                () -> service.create("  "), "Task 2: a blank title must be rejected");
        assertEquals("title must not be blank", blank.getMessage(), "Task 2: the rejection message");
    }

    @Test
    void task3Routes() throws Exception {
        Javalin app = DeskApi.create(new TicketService(new InMemoryTicketRepository()));
        app.start(0);
        try {
            int port = app.port();
            HttpResponse<String> created = post(port, "/tickets", "{\"title\":\"Printer jam\"}");
            assertEquals(201, created.statusCode(),
                    "Task 3: POST /tickets answers 201 Created, not " + created.statusCode());
            JsonNode body = JSON.readTree(created.body());
            assertAll(
                    () -> assertEquals("T-1", body.get("id").asText(), "Task 3: the response carries the new id"),
                    () -> assertEquals("Printer jam", body.get("title").asText(),
                            "Task 3: the response carries the title"),
                    () -> assertTrue(body.get("open").asBoolean(), "Task 3: a new ticket is open"));
            HttpResponse<String> one = get(port, "/tickets/T-1");
            assertEquals(200, one.statusCode(), "Task 3: GET /tickets/{id} answers 200 for a real id");
            assertEquals("T-1", JSON.readTree(one.body()).get("id").asText(),
                    "Task 3: GET /tickets/T-1 returns that ticket");
            HttpResponse<String> all = get(port, "/tickets");
            assertEquals(200, all.statusCode(), "Task 3: GET /tickets answers 200");
            JsonNode list = JSON.readTree(all.body());
            assertAll(
                    () -> assertTrue(list.isArray(), "Task 3: GET /tickets returns a JSON array"),
                    () -> assertEquals(1, list.size(), "Task 3: the array holds the one created ticket"),
                    () -> assertTrue(all.headers().firstValue("Content-Type").orElse("")
                                    .startsWith("application/json"),
                            "Task 3: the list response is served as application/json"));
        } finally {
            app.stop();
        }
    }

    @Test
    void task5Ping() throws Exception {
        Javalin app = DeskApi.create(new TicketService(new InMemoryTicketRepository()));
        app.start(0);
        try {
            HttpResponse<String> ping = get(app.port(), "/ping");
            assertEquals(200, ping.statusCode(), "Task 5: GET /ping answers 200");
            JsonNode body = JSON.readTree(ping.body());
            assertEquals("ok", body.get("status") == null ? null : body.get("status").asText(),
                    "Task 5: the body is {\"status\":\"ok\"} - the old handler misspells the field");
        } finally {
            app.stop();
        }
    }

    @Test
    void task4FailurePaths() throws Exception {
        Javalin app = DeskApi.create(new TicketService(new InMemoryTicketRepository()));
        app.start(0);
        try {
            int port = app.port();
            HttpResponse<String> missing = get(port, "/tickets/T-9");
            assertEquals(404, missing.statusCode(),
                    "Task 4: an unknown id is 404 Not Found, not " + missing.statusCode());
            assertEquals("no ticket T-9", errorField(missing.body()),
                    "Task 4: the 404 body is JSON with an error field");
            HttpResponse<String> blank = post(port, "/tickets", "{\"title\":\"  \"}");
            assertEquals(400, blank.statusCode(),
                    "Task 4: a blank title is the caller's mistake: 400, not " + blank.statusCode());
            assertEquals("title must not be blank", errorField(blank.body()),
                    "Task 4: the 400 body is JSON with an error field");
        } finally {
            app.stop();
        }
    }
}
