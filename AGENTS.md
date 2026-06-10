# AGENTS.md — Identus Mediator

## Project Overview

This is the **Identus Mediator** — a DIDComm v2 mediator service built with Scala 3, ZIO, and SBT. It acts as a cloud-based routing agent that receives encrypted messages for many agents at a single endpoint and stores them for pickup, enabling offline/mobile agents to communicate asynchronously.

**SBT multi-module layout:**

- `mediator/` — JVM backend: ZIO HTTP server, DIDComm protocol handlers, MongoDB persistence, Docker packaging
- `webapp/` — Scala.js single-page application (Laminar + Waypoint) served by the backend; compiles to JS and is bundled into the mediator's static assets via `WebScalaJSBundlerPlugin`

**Key coordinates:**

- Scala 3.6.4, SBT 1.12.11 (see `build.sbt`, `project/build.properties`)
- ZIO for effects, ZIO HTTP for server, ReactiveMongo for MongoDB
- scala-did library (`app.fmgp` % `did` `0.1.0-M45`) for DIDComm primitives

## Build Commands

```bash
sbt compile                  # Compile all modules
sbt "mediator/compile"       # Compile mediator module only
sbt ~compile                 # Watch mode — recompile on file changes
sbt docker:publishLocal      # Build Docker image locally (sbt-native-packager + DockerPlugin)
```

The main class is `org.hyperledger.identus.mediator.MediatorStandalone` (configured in `build.sbt`).

SBT plugins used: `sbt-revolver` (for `reStart`), `sbt-release`, `sbt-native-packager`, `sbt-buildinfo`, `WebScalaJSBundlerPlugin`.

## Test Commands

```bash
sbt test                     # Run all tests (unit + embedded MongoDB)
sbt "mediator/test"          # Run mediator module tests only
sbt coverage test coverageReport coverageAggregate  # With coverage (as CI does)
```

**Test framework details (from `build.sbt`):**

- **munit 1.0.0** — pinned due to a framework bug (`https://github.com/scalameta/munit/issues/554`). Do NOT upgrade munit without checking this issue.
- **zio-test** — ZIO's native test framework; registered via `testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")`
- **munit-zio** (`com.github.poslegm` %% `munit-zio` `0.1.1`) — munit integration for ZIO
- **de.flapdoodle.embed.mongo** `4.14.0` — embedded MongoDB for unit tests that need a DB
- `Test / parallelExecution := false` — tests run sequentially (configured in `build.sbt`)

Test source root: `mediator/src/test/scala/`
Test suites: `protocols/` (per-protocol specs), `db/` (repo specs with embedded MongoDB).

There is no separate scalac "typecheck" step — `sbt compile` includes full Scala 3 type checking. The build also sets `-Xfatal-warnings` in `scalacOptions` (in `build.sbt`), so warnings are errors.

## Local Development

**Start MongoDB:**

```bash
docker compose up mongo       # Start MongoDB only (port 27017)
docker compose up             # Start MongoDB + mediator image (port 8080 + 27017)
```

**Run mediator backend locally (with hot-reload):**

```bash
docker compose up mongo       # Terminal 1: start MongoDB
sbt "~mediator/reStart"       # Terminal 2: start mediator with sbt-revolver (port 8080)
```

The mediator serves on port 8080 by default (`mediator.server.http.port` in `application.conf`).

**Environment variables** (set by `build.sbt` defaults and overridable):

| Variable | Purpose | Default (from `build.sbt` / `application.conf`) |
| --- | --- | --- |
| `KEY_AGREEMENT_D` | X25519 private key (key agreement) | Demo key in `build.sbt` |
| `KEY_AGREEMENT_X` | X25519 public key (key agreement) | Demo key in `build.sbt` |
| `KEY_AUTHENTICATION_D` | Ed25519 private key (authentication) | Demo key in `build.sbt` |
| `KEY_AUTHENTICATION_X` | Ed25519 public key (authentication) | Demo key in `build.sbt` |
| `SERVICE_ENDPOINTS` | Mediator endpoint URLs (`;`-separated) | `http://localhost:8080;ws://localhost:8080/ws` |
| `MONGODB_CONNECTION_STRING` | Full MongoDB URI (takes precedence) | — |
| `MONGODB_PROTOCOL` | MongoDB protocol | `mongodb` |
| `MONGODB_HOST` | MongoDB host | `localhost` |
| `MONGODB_PORT` | MongoDB port | `27017` |
| `MONGODB_USER` | MongoDB username | `admin` |
| `MONGODB_PASSWORD` | MongoDB password | `admin` |
| `MONGODB_DB_NAME` | Database name | `mediator` |
| `PORT` | HTTP server port override | `8080` |
| `ESCALATE_TO` | Problem report escalation email | `atala@iohk.io` |
| `DID_PRISM_RESOLVER` | Base URL for resolving `did:prism` DID documents | Default in `application.conf` |

## MongoDB Dependency

**Docker Compose** (`docker-compose.yml`):

- MongoDB 6.0 image with `--auth`
- Credentials: `admin` / `admin` (via `MONGO_INITDB_ROOT_USERNAME` / `MONGO_INITDB_ROOT_PASSWORD`)
- Database: `mediator`
- Initialization script: `initdb.js` (mounted into container)

**Collections and indexes** (`initdb.js`):

| Collection | Indexes | Notes |
| --- | --- | --- |
| `user.account` | `{did: 1}` (unique), `{alias: 1}` (unique, partial), `{messagesRef.hash: 1, messagesRef.recipient: 1}` | DID account registry |
| `messages` | `{ts: 1}` (TTL, partial on `message_type: "Mediator"`, 7-day expiry) | Mediator-type messages auto-expire; User-type messages persist |
| `messages.outbound` | — | Outbound message queue |

**Connection config** (`mediator/src/main/resources/application.conf`):

- `MONGODB_CONNECTION_STRING` takes precedence over individual components
- If no connection string: URI is built as `<protocol>://<userName>:<password>@<host>:<port>/<dbName>`

**Migration:** Existing users should use `migration_mediator_collection.js` to migrate the collection.

## Mediator Identity Key Generation

The mediator identity requires two OKP key pairs in JOSE (JWK) format:

- **KEY_AGREEMENT** — X25519 key pair for encryption (key agreement)
- **KEY_AUTHENTICATION** — Ed25519 key pair for signing (authentication)

**Generation process** (see `mediator-identity-key-generation.md`):

1. Generate X25519 key: `openssl genpkey -algorithm X25519 -out private_key_x25519.pem`
2. Format to JWK using `openssl pkey -noout -text` + `jq` to extract `d` (private) and `x` (public) in base64url encoding
3. Generate Ed25519 key: `openssl genpkey -algorithm Ed25519 -out private_key_ed25519.pem`
4. Format to JWK similarly for `d` and `x` fields

The keys are set via environment variables (`KEY_AGREEMENT_D`, `KEY_AGREEMENT_X`, `KEY_AUTHENTICATION_D`, `KEY_AUTHENTICATION_X`). By default, the mediator builds a `did:peer:2` DID from these keys and the service endpoints at startup. Operators can also provide an explicit DID plus `keyStore` directly in `application.conf`, which enables mediator identities such as `did:prism`.

**⚠️ Never use the demo keys from `build.sbt` in production.**

## Architecture

### Entry Point

`MediatorStandalone` (`mediator/src/main/scala/.../MediatorStandalone.scala`) — ZIO application that:

1. Loads HOCON config via `zio-config-typesafe` + `zio-config-magnolia`
2. Constructs a `did:peer:2` DID from the configured keys + endpoints, or uses an explicitly configured DID + `keyStore`
3. Wires ZIO layers: `ReactiveMongoApi` → repos (`UserAccountRepo`, `MessageItemRepo`, `OutboxMessageRepo`) → `OperatorImp` → protocol handlers
4. Starts ZIO HTTP server on configured port (default 8080)

### HTTP Routes

- **`DIDCommRoutes`** (`DIDCommRoutes.scala`) — Core DIDComm message handling:
  - `GET /ws` — WebSocket transport for DIDComm messages
  - `POST /*` — HTTP POST for encrypted/signed DIDComm messages (content-type: `application/didcomm-signed+json` or `application/didcomm-encrypted+json`)
- **`MediatorAgent`** (`MediatorAgent.scala`) — Utility and health routes:
  - `GET /health` — Health check (queries all three repo stats, returns 503 if any DB connection fails)
  - `GET /version` — Returns `MediatorBuildInfo.version`
  - `GET /did` — Returns the mediator's DID
  - `GET /invitation` — Returns an out-of-band invitation JSON
  - `GET /invitationOOB` — Returns an OOB invitation URL
  - `GET /` — Serves the webapp SPA (index.html with QR code)

### Protocol Handlers

Located in `mediator/src/main/scala/.../protocols/`:

| File | Protocol | Purpose |
| --- | --- | --- |
| `MediatorCoordinationExecuter.scala` | Coordinate-Mediation 2.0 | Mediate request/grant/deny, keylist update/query |
| `ForwardMessageExecuter.scala` | Forward | Route and store forwarded user messages |
| `PickupExecuter.scala` | MessagePickup 3.0 | Message pickup, delivery, and status |
| `DiscoverFeaturesExecuter.scala` | Discover Features | Protocol capability discovery |
| `MissingProtocolExecuter.scala` | — | Fallback for unrecognized protocol PIURIs |
| `Problems.scala` | Report Problem 2.0 | Problem report generation helpers |

### Database Layer

Located in `mediator/src/main/scala/.../db/`:

| File | Purpose |
| --- | --- |
| `ReactiveMongoApi.scala` | MongoDB connection and driver management |
| `AsyncDriverResource.scala` | ZIO-managed ReactiveMongo `AsyncDriver` lifecycle |
| `UserAccountRepo.scala` | DID account CRUD (alias, keylist, message refs) |
| `MessageItemRepo.scala` | Inbound message storage (mediator + user types) |
| `OutboxMessageRepo.scala` | Outbound message queue |
| `DataModels.scala` | BSON data models for collections |
| `BsonImplicits.scala` | BSON serialization helpers |
| `MongoHealth.scala` | MongoDB health check utilities |
| `XRequestId.scala` | Request ID tracking |

### Message Processing Flow

`AgentExecutorMediator` (`AgentExecutorMediator.scala`) is the core message processing pipeline:

1. **Receive transport** — Accept HTTP or WebSocket transport, register with `MediatorTransportManager`
2. **Decrypt** — `AgentExecutorMediator.decrypt()` unwraps encryption layers (anon/auth decrypt, signature verify)
3. **Store** — Insert received message into `MessageItemRepo` (with duplicate detection)
4. **Dispatch to protocol handler** — `ProtocolExecuter` routes to the correct `*Executer` based on PIURI
5. **Reply** — Encrypt and send response via transport or `TransportDispatcher`

### Webapp

The `webapp/` module is a Scala.js application using Laminar + Waypoint that provides the mediator invitation page with QR code. It compiles to JS, is bundled by `ScalaJSBundlerPlugin`, and served as static assets by the mediator backend. Test coverage is disabled for this module (`coverageEnabled := false`).

## Protocols and Flow Documentation

| Document | Content |
| --- | --- |
| `Mediation-Flows.md` | Sequence diagrams: accept invitation, send message through mediator, receive message |
| `Coordinate-Mediation-Protocol.md` | Coordinate-Mediation 2.0 PIURIs, roles, states, message schemas |
| `Mediator-Error_Handling.md` | Error handling patterns, problem reports, custom behavior table for edge cases |

**Supported protocols:**

- `BasicMessage 2.0` — `https://didcomm.org/basicmessage/2.0`
- `CoordinateMediation 2.0` — `https://didcomm.org/coordinate-mediation/2.0/`
- `Discover Features 2.0` — `https://didcomm.org/discover-features/2.0`
- `MessagePickup 3.0` — `https://didcomm.org/messagepickup/3.0/`
- `Report Problem 2.0` — `https://didcomm.org/report-problem/2.0/`
- `Routing 2.0` — `https://didcomm.org/routing/2.0`
- `TrustPing 2.0` — `https://didcomm.org/trust-ping/2.0/`

## CI/CD

### Unit Tests — `.github/workflows/ci.yml`

Triggers: PRs (non-gh-pages), pushes to main.
Steps: JDK 17 setup → Node.js setup → Scala.js setup → `sbt coverage test coverageReport coverageAggregate` → Coveralls upload.

### Integration Tests — `.github/workflows/integration-tests.yml`

Triggers: PRs, pushes to main, manual dispatch.
Steps: Build local Docker image (`sbt docker:publishLocal`) → `docker compose up` with health check → run external test suite (`input-output-hk/didcomm-v2-mediator-test-suite` via Gradle) → publish results.

**Integration tests require a running mediator + MongoDB instance.** They are not part of `sbt test`.

### Docker Hub Release — `.github/workflows/release-docker-hub.yml`

Triggers: Tags (`v*`), pushes to main, manual dispatch.
Steps: Build binary (`sbt "docker:stage"`) → build multi-arch Docker images (amd64/arm64 via `docker/build-push-action`) → push to `docker.io/hyperledgeridentus/identus-mediator` → trigger downstream integration repo (`hyperledger-identus/integration`).

### Other Workflows

- `scala-steward.yml` — Automated dependency updates
- `codeql.yml` — Security analysis
- `scorecard.yml` — OpenSSF Scorecard
- `sbt-dependency-submission.yml` — Dependency graph submission

## Integration Tests Note

**Integration tests require a running mediator + MongoDB instance.** They use the external DIDComm v2 mediator test suite (`input-output-hk/didcomm-v2-mediator-test-suite`), not `sbt test`.

To run integration tests locally:

```bash
sbt docker:publishLocal                          # Build local mediator image
MEDIATOR_VERSION=<version> docker compose up      # Start mediator + MongoDB
# Then clone and run the test suite per integration-tests.yml
```

Unit tests that need MongoDB use **embedded MongoDB** (`de.flapdoodle.embed.mongo`) and do not require Docker.

## General Guidelines

### Commit Conventions

- Format: Conventional Commits
- No secrets in commits
- Sign-off required (DCO)

### Quality Gates

Before submitting a PR:

1. `sbt compile` — must pass with no warnings (`-Xfatal-warnings` is set)
2. `sbt test` — all tests pass
3. `sbt coverage test coverageReport coverageAggregate` — if running coverage
