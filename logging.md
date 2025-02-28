# Logging

We want traceability from HTTP calls.
- Each HTTP call needs to generate a call ID (preferably on the proxy). The ID must be within the scope of the ZIO application. So, every log will mention this ID. This ID must return to the user as an HTTP header.
- In case of errors and problem reports, the user can use those IDs to get support.

A user can receive Level 3 support during the logging retention policies. The user needs to provide the value in the header `X-Request-Id` to get this support.

## Annotates

We have special annotations in the log so there is traceability between the different logs. 
Here is the list of annotations and their meaning that we currently have:

- `request-id` - Is the HTTP header `X-Request-Id` from the caller.
- `msg_sha256` - Is the Hash (sha256) of the DID Comm message

## Code

To have a concise code, we have created a middleware that modifies the annotations in the scope before each execution of that endpoint and includes the trace ID of the request.
See code in file `TraceIdMiddleware` (`mediator/src/main/scala/org/hyperledger/identus/mediator/TraceIdMiddleware.scala`).

## Logging Backend

We use the Simple Logging Facade for Java (SLF4J) API to call the logging backend determined at runtime.

### Logging Pattern

`%d{yyyy-MM-dd_HH:mm:ss.SSS} %highlight(%-5level) %cyan(%logger{5}@L%line:[%-4.30thread]) {%mdc} - %msg%xException%n`

- `%d{yyyy-MM-dd_HH:mm:ss.SSS}` is the date/timestamp of the log in the human-readable way
- `%highlight(%-5level)` the log level
- `%cyan(%logger{5}@L%line:[%-4.30thread])`
  - `%cyan` - Is just modify the color to make easy to read
  - `%logger{5}` - class name that originated the log
  - `@L%line` - line of the code that originated the log
  - `%-4.30thread` - the id of the thread. The ZIO fiber name
- `%mdc` - the mapped diagnostic context [MDC](https://logback.qos.ch/manual/layouts.html#mdc)
- `%msg` - the log message
- `%xException` - exception info
