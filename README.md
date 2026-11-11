# HTTP and Javalin lab: the help desk as a JSON API

## Goal

Put the help desk on the network: a small Javalin JSON API in four linked tasks in the package `deskapi`.
You wire the three layers a real service has - an in-memory repository behind an interface, a service with the rules, and HTTP routes with lambda handlers - and you give it an honest failure path: a missing ticket answers 404 with a JSON error, bad input answers 400.
This lab is also the required guided local run for the Javalin routes topic: its public checks are the ones that topic's objectives name.
You build and test it with the real JDK and Maven on your own computer; the tests speak real HTTP to your app on a free localhost port.

## Files

**Edit exactly three files, one per layer.** Every `TODO` comment marks its task; the test failure lines carry the same task numbers:

- `starter/src/main/java/deskapi/InMemoryTicketRepository.java` - Task 1: the store
- `starter/src/main/java/deskapi/TicketService.java` - Task 2: the rules
- `starter/src/main/java/deskapi/DeskApi.java` - Tasks 3-5: the routes, the failure paths and the health-route modernization

**Do not change anything else.** In particular:

- `Ticket.java`, `NewTicket.java`, `NotFoundException.java` and `TicketRepository.java` - complete contracts the three layers share
- `tests/deskapi/DeskApiTest.java` - the public tests your API must pass
- `starter/pom.xml` - the build is prewired
- `expected/test-summary.txt` - the finished-state reference

## Prerequisites

You need JDK 25 and Apache Maven 3.9 or later, the same setup as the earlier Java labs.
Check both in a new terminal:

```
java -version
mvn -v
```

- `java -version` must report version 25.
- `mvn -v` must report Apache Maven 3.9 or later, and its `Java version:` line must also say 25.

This lab works the same on macOS, Linux and Windows.
On Windows, the Maven commands are the same in PowerShell or Command Prompt; run the curl lines in Command Prompt (curl.exe ships with Windows 10 and later).
No Docker or other container tool is needed; the server is plain Java on localhost.

## Setup (once, needs the internet)

Open a terminal in the lab's root folder - the one that holds this README, `starter/` and `tests/` - and change into `starter` with `cd starter`; every command in this lab runs there. Then run:

```
mvn -q dependency:go-offline
```

It prints nothing when it succeeds.
This lab downloads Javalin 7.2.3 (as `javalin-bundle`, which brings Jackson for JSON and a simple logger) besides the usual JUnit and plugins.
After this step, everything else in the lab runs offline - the tests talk only to your own machine.

## Tasks

Open the lab's root folder in your IDE or editor; it holds one Maven project, `starter/pom.xml`, and if your editor asks, open or import that file as a Maven project. Whatever your editor shows, the test command below, run in a terminal in `starter`, is the result that counts.
Read the complete files first - `Ticket` and `NewTicket` (records Jackson maps to JSON), `NotFoundException`, and the `TicketRepository` interface - then work through the TODOs.
The tests call the repository and service directly, and speak real HTTP to the routes.

After each task, run the one test command from the `starter` folder:

```
mvn -q -o test
```

Every failure line starts with its task number, so you can work through the tasks in order.

### Task 1: the in-memory repository

1. Open `InMemoryTicketRepository.java`. The `LinkedHashMap` is already there: it looks tickets up by id and remembers insertion order.
2. Implement `save` (put under the ticket's id, return the ticket), `find` (the map's value, or null - the interface says so), and `all` (a new list of the values, oldest first).
3. While it is wrong, you see lines such as `Task 1: find("T-1")`.
4. Done when no line starting with `Task 1` appears.

### Task 2: the service

1. Open `TicketService.java`. It gets the repository through its constructor and never builds one - the tests hand in their own, and the SQL course will hand in a database-backed one.
2. Implement `create`: reject a null or blank title with `new IllegalArgumentException("title must not be blank")`, build an open `Ticket` with the next id (`"T-" + nextNumber`, then count up), save and return it.
3. Implement `get`: the repository's ticket, or `throw new NotFoundException("no ticket " + id)` when it answered null. Implement `list` as pure delegation.
4. Done when no line starting with `Task 2` appears.

### Task 3: the routes

1. Open `DeskApi.java`. Everything happens inside `Javalin.create(config -> ...)`; each route takes a path and a handler lambda `ctx -> ...`.
2. Register `config.routes.get("/tickets", ...)` answering the list as JSON with `ctx.json(service.list())` - 200 is the default status.
3. Register `config.routes.get("/tickets/{id}", ...)`: read the path parameter with `ctx.pathParam("id")`, answer the service's ticket as JSON.
4. Register `config.routes.post("/tickets", ...)`: read the body with `ctx.bodyAsClass(NewTicket.class)`, create the ticket, set `ctx.status(201)` - Created is the honest code for a successful POST - and answer the new ticket as JSON.
5. The tests start your app on a free port and send real requests; follow one in your head: request -> matched route -> your lambda -> service -> repository -> JSON response.
6. Done when no line starting with `Task 3` appears.

### Task 4: the failure path

1. Run the tests first and look at task 4: right now a missing ticket answers 500 `Server Error`, because `NotFoundException` reaches Javalin unhandled.
2. Register `config.routes.exception(NotFoundException.class, (e, ctx) -> ...)`: set status 404 and answer `ctx.json(Map.of("error", e.getMessage()))`.
3. Register the same shape for `IllegalArgumentException` with status 400: a blank title is the caller's mistake, not the server's.
4. Done when no line starting with `Task 4` appears and the whole run prints nothing.

### Try it over real HTTP

Start the API on port 7070 with one sample ticket: run `DeskApi.main` from the IDE, or from the `starter` folder run:

```
mvn -q -o compile exec:java
```

Then, in a second terminal (on Windows, open it as Command Prompt, not PowerShell):

```
curl -i http://localhost:7070/tickets
curl -i http://localhost:7070/tickets/T-9
curl -i -X POST -H "Content-Type: application/json" -d "{\"title\":\"VPN drops\"}" http://localhost:7070/tickets
```

Read each response's status line against what you wired: 200, 404 with your JSON error, 201.
Stop a terminal run with Ctrl+C in that terminal, and an IDE run with the IDE's Stop button.

### Task 5: modernize the health route

1. Find the `/ping` route in `DeskApi.create`: it is still written as an anonymous `new Handler() { ... }` class - the pre-lambda idiom - and it answers the wrong field name, so monitoring sees no `status`.
2. Rewrite the registration as a lambda with the fixed body: `config.routes.get("/ping", ctx -> ctx.json(Map.of("status", "ok")))`. Same interface, one line instead of six. Run the tests.
3. Go one step further: the complete `pingHandler` method at the bottom of the class has exactly a `Handler`'s shape (one `Context` in, nothing out), so the lambda that only forwards can become a method reference: `config.routes.get("/ping", DeskApi::pingHandler)`. Run the tests again - behavior identical.
4. Done when no line starting with `Task 5` appears and the whole run prints nothing.

## Using an AI assistant

If you use an AI assistant in this lab, ask it to explain, not to answer: "why does my unhandled exception answer 500?" teaches you the request flow; "write the routes for me" does not.
Verify any explanation with curl or a test run before you trust it.

## Test

The one test command, run from the `starter` folder, is:

```
mvn -q -o test
```

- Before you start, all five tests fail, and Maven ends with `Tests run: 5, Failures: 5`.
- The route tests bind a free localhost port, speak HTTP and stop the app again, run after run, offline.
- Each test lists what behavior was wrong. A passing run shows the API behaves correctly; it does not check which Java constructs your code uses, so compare your code with each task's steps yourself.
- If your code does not compile, Maven prints `COMPILATION ERROR` with the file, then `[line,column]`, then the compiler's message. Fix that line and run the test again.

## Expected output

When every task is complete, `mvn -q -o test` prints nothing and exits with status 0.
`expected/test-summary.txt` shows the full expected result, including what curl shows against the running API.

## Offline note

Only the setup step uses the network.
The test command uses `-o` (offline), and the tests talk only to your own localhost.
If it reports that an artifact is missing from the local repository, run the setup step once more while online.

## Cleanup

The lab writes only the `starter/target` folder (compiled classes and test reports). Delete it from the `starter` folder:

- macOS and Linux: `rm -rf target`
- Windows PowerShell: `Remove-Item -Recurse -Force target`

The tests stop the app themselves; if you started `DeskApi.main` yourself, stop a terminal run with Ctrl+C in that terminal, and an IDE run with the IDE's Stop button.
The Javalin, JUnit and plugin downloads stay in your local Maven repository (`~/.m2/repository` by default) so later Java labs can reuse them.

`starter/.gitignore` already keeps Maven's `starter/target` folder out of Git. Your editor and operating system may also create local files in the lab's root folder, such as `.idea/`, `.vscode/` or `.DS_Store`; they are not part of the lab, so if `git status` lists any of them, add them to a `.gitignore` file in the lab's root folder instead of committing them.

## Troubleshooting

- **`mvn: command not found`, or `'mvn' is not recognized`.** Maven's `bin` folder is not on your `PATH`. Add it, then open a new terminal.
- **`mvn -v` shows a Java version other than 25.** Maven uses the JDK named by `JAVA_HOME` when it is set, otherwise the `java` on your `PATH`. Set `JAVA_HOME` to your JDK 25 folder, or unset it, then open a new terminal.
- **`release version 25 not supported`.** Maven is running on an older JDK. Fix it as in the previous item.
- **`Cannot access central in offline mode`, or a missing artifact.** The setup step has not completed. Run `mvn -q dependency:go-offline` once while online.
- **A route test answers 404 where you expected 200.** No route matched: check the path spelling, including the `{id}` braces.
- **Task 4 still answers 500.** The exception reaches Javalin unhandled; register the handler inside the same `Javalin.create(config -> ...)` block as the routes.
- **`Address already in use` when running main.** An earlier run of DeskApi is still alive on 7070: stop a terminal run with Ctrl+C in that terminal, and an IDE run with the IDE's Stop button, then start again.
- **A `println` or log line you added does not appear during the tests.** While the tests run, everything printed - Javalin's log included - goes to `starter/target/surefire-reports/deskapi.DeskApiTest-output.txt`, so a passing run stays silent. To watch output live, start the API with `mvn -q -o compile exec:java` and watch that terminal.
