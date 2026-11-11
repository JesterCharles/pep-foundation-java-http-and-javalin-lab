package deskapi;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Map;

/**
 * HTTP and Javalin lab: the help desk as a small JSON API.
 * Tasks 3 and 4 register the routes and the failure paths inside
 * Javalin.create(config -> ...). The handlers are lambdas: each route takes a
 * Context -> behavior function. A request then travels: HTTP request ->
 * matched route -> handler lambda -> service -> repository, and the response
 * carries a status code and JSON.
 * main is complete: it starts the API on port 7070 so you can try it with
 * curl or a browser.
 */
public class DeskApi {

    // Tasks 3 and 4: build the Javalin app for this service.
    public static Javalin create(TicketService service) {
        return Javalin.create(config -> {

            // TODO Task 3: register the routes, each handler a lambda:
            //   config.routes.get("/tickets", ctx -> ...)
            //     -> status 200 (the default) and the ticket list as JSON: ctx.json(...)
            //   config.routes.get("/tickets/{id}", ctx -> ...)
            //     -> the one ticket as JSON; read the path parameter with
            //        ctx.pathParam("id")
            //   config.routes.post("/tickets", ctx -> ...)
            //     -> read the body with ctx.bodyAsClass(NewTicket.class),
            //        create the ticket, set ctx.status(201) (Created) and
            //        answer the new ticket as JSON

            // Task 5 (modernize the handler): this health route still uses the
            // old anonymous-class form - and it answers the wrong field name.
            // First rewrite it as a lambda with the fixed body, run the tests,
            // then go one step further: the complete pingHandler method below
            // has exactly a Handler's shape, so the whole lambda can become
            // the method reference DeskApi::pingHandler.
            config.routes.get("/ping", new Handler() {
                @Override
                public void handle(Context ctx) {
                    ctx.json(Map.of("statsu", "ok"));
                }
            });

            // TODO Task 4: map the two failures to honest responses:
            //   config.routes.exception(NotFoundException.class, (e, ctx) -> ...)
            //     -> status 404 and the JSON body Map.of("error", e.getMessage())
            //   config.routes.exception(IllegalArgumentException.class, (e, ctx) -> ...)
            //     -> status 400 and the JSON body Map.of("error", e.getMessage())
            // Without these, Javalin answers 500 for both - run the tests once
            // before and once after this task and compare.

        });
    }

    // Complete: the modern body for the health route. Its shape - one Context
    // in, nothing out - is exactly Handler's single abstract method, which is
    // what makes the method reference in Task 5 possible.
    static void pingHandler(Context ctx) {
        ctx.json(Map.of("status", "ok"));
    }

    public static void main(String[] args) {
        TicketService service = new TicketService(new InMemoryTicketRepository());
        service.create("Printer jam");
        create(service).start(7070);
        System.out.println("Desk API on http://localhost:7070/tickets - stop it with Ctrl+C");
    }
}
