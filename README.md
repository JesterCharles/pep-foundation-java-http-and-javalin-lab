# HTTP and Javalin lab: the help desk as a JSON API

You put the help desk on the network as a small Javalin JSON API, in five linked tasks in the package `deskapi`.
You build the three layers a real service has:
- an in-memory repository behind an interface;
- a service that holds the rules;
- HTTP routes with lambda handlers.

You also give the API an honest failure path: a missing ticket answers 404 with a JSON error, and bad input answers 400.
This lab is also the required hands-on run for the Javalin routes topic.

## Quick start

The lab needs JDK 25 and Apache Maven 3.9 or later in the terminal. Troubleshooting shows how to check.

1. Open the lab project you were given in VS Code, then open its integrated terminal: **Terminal > New Terminal**.
2. Go into the project folder. Every Maven command in this lab runs there:

   ```
   cd starter
   ```

3. Before the first test run, download the lab's libraries once. This needs the internet, and prints nothing when it works:

   ```
   mvn -q dependency:go-offline
   ```

   This lab downloads Javalin 7.2.3 (as `javalin-bundle`, which brings Jackson for JSON and a simple logger) besides JUnit and the build plugins.
4. Run the tests before you change anything:

   ```
   mvn -q -o test
   ```

   **All 5 tests fail at first. That is expected: the TODOs are not done yet.**
   The output ends with `Tests run: 5, Failures: 5`, and every failure line starts with its task number, such as `Task 1:`.
   The tests start your app on a free localhost port, send real HTTP requests, and stop it again.
5. Work through the checklist below in order. After each task, run `mvn -q -o test` again and read the remaining `Task N:` lines.
6. **You are finished when `mvn -q -o test` prints nothing at all.**

```
OPEN project -> cd starter -> setup (once) -> RUN tests: 5 fail (expected)
  -> READ the "Task N:" lines -> EDIT the next task -> RUN tests again
  -> store -> service -> routes -> failure paths -> health route
  -> STOP when the test run prints nothing
```

In words: open the project, run the tests, read which tasks fail, complete one task, run the tests again, and stop when they print nothing.

## Checklist

Edit exactly three files in `starter/src/main/java/deskapi/`, one per layer. Each task is marked with a `Task N` comment in the file, and most of them also carry a `TODO`:

- [ ] Task 1: `InMemoryTicketRepository.java`, `save`, `find` and `all` (the store)
- [ ] Task 2: `TicketService.java`, `create`, `get` and `list` (the rules)
- [ ] Task 3: `DeskApi.java`, the three routes (route handlers as lambdas)
- [ ] Task 4: `DeskApi.java`, the two exception handlers (404 and 400)
- [ ] Task 5: `DeskApi.java`, the `/ping` health route (from an anonymous class to a lambda and a method reference)

A task is done when no failure line starts with its number.
Do not change anything else:
- `Ticket.java`, `NewTicket.java`, `NotFoundException.java` and `TicketRepository.java` are complete contracts the three layers share.
- The tests in `tests/deskapi/`, the build file `starter/pom.xml` and `expected/test-summary.txt` stay as they are.

## Task steps

Read the complete files first:
- `Ticket` and `NewTicket`, records that Jackson maps to JSON;
- `NotFoundException`;
- the `TicketRepository` interface.

The tests call the repository and the service directly, and speak real HTTP to the routes.

### Task 1: the in-memory repository

1. Open `InMemoryTicketRepository.java`. The `LinkedHashMap` is already there: it looks tickets up by id, and it remembers insertion order.
2. Implement the three methods:
   - `save`: put the ticket under its id, and return the ticket;
   - `find`: the map's value, or null, as the interface says;
   - `all`: a new list of the values, oldest first.
3. While it is wrong, you see lines such as `Task 1: find("T-1")`.

### Task 2: the service

1. Open `TicketService.java`. It gets the repository through its constructor and never builds one: the tests hand in their own, and the SQL course will hand in a database-backed one.
2. Implement `create`:
   - reject a null or blank title with `new IllegalArgumentException("title must not be blank")`;
   - otherwise build an open `Ticket` with the next id (`"T-" + nextNumber`, then count up), save it and return it.
3. Implement `get`: the repository's ticket, or `throw new NotFoundException("no ticket " + id)` when the repository answered null. Implement `list` as pure delegation.

### Task 3: the routes

1. Open `DeskApi.java`. Everything happens inside `Javalin.create(config -> ...)`, and each route takes a path and a handler lambda `ctx -> ...`.
2. Register `config.routes.get("/tickets", ...)`, answering the list as JSON with `ctx.json(service.list())`. The default status is 200.
3. Register `config.routes.get("/tickets/{id}", ...)`. Read the path parameter with `ctx.pathParam("id")`, and answer the service's ticket as JSON.
4. Register `config.routes.post("/tickets", ...)`:
   - read the body with `ctx.bodyAsClass(NewTicket.class)`;
   - create the ticket;
   - set `ctx.status(201)`, because Created is the honest code for a successful POST;
   - answer the new ticket as JSON.
5. Follow one request in your head: request -> matched route -> your lambda -> service -> repository -> JSON response.

<details><summary>Hint</summary>

If a route test answers 404 where you expected 200, no route matched. Check the path spelling, including the `{id}` braces.

</details>

### Task 4: the failure path

1. Run the tests first and look at task 4. Right now a missing ticket answers 500 `Server Error`, because `NotFoundException` reaches Javalin unhandled.
2. Register `config.routes.exception(NotFoundException.class, (e, ctx) -> ...)`. Set status 404, and answer `ctx.json(Map.of("error", e.getMessage()))`.
3. Register the same shape for `IllegalArgumentException`, with status 400: a blank title is the caller's mistake, not the server's.

<details><summary>Hint</summary>

If task 4 still answers 500, register the handlers inside the same `Javalin.create(config -> ...)` block as the routes.

</details>

### Task 5: modernize the health route

1. Find the `/ping` route in `DeskApi.create`. It is still written as an anonymous `new Handler() { ... }` class, the pre-lambda idiom. It also answers with the wrong field name, so monitoring sees no `status`.
2. Rewrite the registration as a lambda with the fixed body: `config.routes.get("/ping", ctx -> ctx.json(Map.of("status", "ok")))`. It uses the same interface, in one line instead of six. Run the tests.
3. Go one step further. The complete `pingHandler` method at the bottom of the class has exactly a `Handler`'s shape: one `Context` in, nothing out.
   So the lambda, which only forwards to it, can become a method reference: `config.routes.get("/ping", DeskApi::pingHandler)`. Run the tests again; the behavior is identical.

### About the test results

A passing run shows the API behaves correctly.
It does not check which Java constructs your code uses, so compare your code with each task's steps yourself.
While the tests run, everything printed goes to `starter/target/surefire-reports/deskapi.DeskApiTest-output.txt`, Javalin's log included, so a passing run stays silent.

## Try it over real HTTP (optional)

1. Start the API on port 7070 with one sample ticket. From `starter`, run:

   ```
   mvn -q -o compile exec:java
   ```

2. Open a second terminal with **Terminal > New Terminal**, and run:

   ```
   curl -i http://localhost:7070/tickets
   curl -i http://localhost:7070/tickets/T-9
   curl -i -X POST -H "Content-Type: application/json" -d "{\"title\":\"VPN drops\"}" http://localhost:7070/tickets
   ```

   On Windows, run these curl lines in Command Prompt, not PowerShell.
3. Read each response's status line against what you wired: 200, then 404 with your JSON error, then 201.
4. Stop the API with Ctrl+C in its terminal.

`expected/test-summary.txt` shows the finished result, including what curl shows against the running API.

## Using an AI assistant

If you use an AI assistant, ask it to explain, not to answer.
"Why does my unhandled exception answer 500?" teaches you the request flow; "write the routes for me" does not.
Check any explanation with curl or a test run before you trust it.

## Troubleshooting

- **`there is no POM in this directory`.** The terminal is not in `starter`. From the lab folder, run `cd starter` and try again.
- **`Address already in use` when you start the API.** An earlier run is still using port 7070. Stop it with Ctrl+C in its terminal, then start again.
- **A `println` you added does not appear during the tests.** Test output goes to `starter/target/surefire-reports/deskapi.DeskApiTest-output.txt`. To watch output live, start the API with `mvn -q -o compile exec:java` and watch that terminal.
- **`COMPILATION ERROR`.** Your code does not compile yet. Maven names the file, then `[line,column]`, then the problem. Fix that line and run the tests again.
- **`mvn: command not found`, or `'mvn' is not recognized`.** Maven's `bin` folder is not on your `PATH`. Add it, then open a new terminal.
- **`mvn -v` shows a Java version other than 25, or you see `release version 25 not supported`.** Maven uses the JDK named by `JAVA_HOME` when it is set, otherwise the `java` on your `PATH`. Set `JAVA_HOME` to your JDK 25 folder, or unset it, then open a new terminal. Check with `java -version` and `mvn -v`.
- **`Cannot access central in offline mode`, or a missing artifact.** The setup step has not finished. Run `mvn -q dependency:go-offline` once while online.

## Cleanup (optional)

Maven writes only the `starter/target` folder, and Git already ignores it.
To delete it, run `rm -rf target` from `starter`. In Windows PowerShell, run `Remove-Item -Recurse -Force target`.
The tests stop the app themselves. If you started the API yourself, stop it with Ctrl+C in its terminal.
