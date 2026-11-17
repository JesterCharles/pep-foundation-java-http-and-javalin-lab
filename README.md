# HTTP and Javalin lab: the help desk as a JSON API

You put the help desk on the network as a small Javalin JSON API, in five linked tasks in the package `deskapi`.
You build the three layers a real service has:
- an in-memory repository behind an interface;
- a service that holds the rules;
- HTTP routes with lambda handlers.

You also give the API an honest failure path: a missing ticket answers 404 with a JSON error, and bad input answers 400.

The full learner instructions are in [`.evolv/instructions.md`](.evolv/instructions.md), which the lab platform shows as the lab's instructions.

## Layout

This repository follows the Exa coding-lab scaffold for Java.

| Path | What it is |
|---|---|
| `src/main/java/deskapi/Ticket.java`, `NewTicket.java`, `NotFoundException.java`, `TicketRepository.java` | Given contracts the three layers share |
| `src/main/java/deskapi/InMemoryTicketRepository.java` | Lab source (Task 1) |
| `src/main/java/deskapi/TicketService.java` | Lab source (Task 2) |
| `src/main/java/deskapi/DeskApi.java` | Lab source (Tasks 3-5) |
| `src/test/java/deskapi/DeskApiTest.java` | Visible tests |
| `.evolv/instructions.md` | Learner instructions |
| `.evolv/grade.yml` | Grading manifest, used by Run Tests and by the grader |
| `pom.xml` | Maven build: Java 17, JUnit 5.11.3 and Javalin 7 |
| `Dockerfile` | Image for local Run Tests; warms the Maven cache |
| `expected/test-summary.txt` | The result of a finished lab |

There are no held-out tests in this lab: every test is visible.

## Run the tests

On the platform, select **Run Tests**. From a terminal with JDK 17 or later and Apache Maven 3.9:

```
mvn -q test
```

All 5 tests fail until the `TODO`s are done. That is expected.
The route tests start the app on a free localhost port, send real HTTP requests, and stop it again.
