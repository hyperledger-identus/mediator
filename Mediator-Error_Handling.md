# Error Handling


When processing any step of any protocol, if something is wrong, the mediator tries to:
- Update the record to a documented error state
- Log the error in the service logs
- Send the problem report message when appropriate

https://identity.foundation/didcomm-messaging/spec/#problem-reports

## Custom behavior table

This table defines the expected behavior of the mediator in different scenarios not covered by the specifications.

| Mediators   | Mediator       | Roadmap Mediator |
|-------------|----------------|------------------|
| Scenario G1 | G1C            | -                |
| Scenario G2 | G2B [ATL-5840] | -                |
| Scenario G3 | Fallback G2B   | [TODO] G3B       |
| Scenario G4 | G4B            | -                |
| Scenario G5 | Fallback G4B   | [TODO] G5B       |
| Scenario G6 | Fallback G4B   | G6B              |
| Scenario G7 | Fallback G4B   | [TODO] G7B       |
| Scenario G8 | G8C            | -                |
|             |                |                  |
| Scenario M1 | M1B            | -                |
| Scenario M2 | M2B            | -                |
| Scenario M3 | M3B            | -                |
| Scenario M4 | M4B            | -                |
| Scenario M5 | M5B [#145]     | -                |

### Scenarios Description

#### General Scenarios:

- **G1** - Received the exact same message (encrypted with the same ephemeral keys) (possible replay attack)
  - S1A - (default) executes the protocol as normal
  - S1B - ignore (no effect/output) 
  - S1C - Send a problem report "e.p.cryto.dejavu" ??? or "e.p.crypto.replay"
  - S1D - Reply with the exact same message (encrypted with the same ephemeral keys)

- **G2** - Receive a message for an unsupported protocol
  https://input-output.atlassian.net/browse/ATL-5840
  - G2A - ignore (no effect/output) 
  - G2B - Send a problem report `e.p.msg.unsupported`

- **G3** - Receive a message for an unsupported version of the protocol.
  - G2B - Fallback to G2
  - G3B - Send a problem report `e.p.msg.unsupported` and say what version(s) its supported

- **G4** - When an internal error or any unexpected error happens.
  - G4A - ignore (no effect/output) 
  - G4B - Send a problem report "e.p.error"

- **G5** - If the message is tampered or got any crypto errors when decoding.
  - G5A - ignore (no effect/output) 
  - G5B - (sync!) Send a problem report "e.p.crypto"

- **G6** - If the DID method is not supported (`did.peer` in this case)
  - G6A - ignore (no effect/output)
  - G6B - (sync!) e.p.did 

- **G7** - If the DID method is malformed.
  - G7A - ignore (no effect/output)
  - G7B - (sync!) e.p.did.malformed

- **G8** - When a parsing error from the decrypted message.
  - G8A - ignore (no effect/output)
  - G8B - (sync!) Send a problem report `e.p.msg` if the plaintext message is malformed 
  - G8C - Send a problem report `e.p.msg.<PIURI>` if the plaintext message is malformed or if parsing into a specific protocol's data model fails.

#### Mediator especific Scenarios:

- **M1** - Receive a pickup message from a DID not enrolled.
  - M1A - ignore (no effect/output) 
  - M1B - Send a problem report "e.p.req.not_enroll"

- **M2** - Connection to the BD is not working (at the moment of the call).
  - G4
  - M2A - ignore (no effect/output)
  - M2B - Send a problem report "e.p.me.res.storage" (maybe mention the try again later)

- **M3** - Business logic in BD is failing (got some error from the query).
  - G4
  - M3A - ignore (no effect/output)
  - M3B - Send a problem report "e.p.me.res.storage"

- **M4** - Get a Forward message to a DID that is not enrolled.
  - M4A - ignore (no effect/output)
  - M4B - Send a problem report "e.p.req.not_enroll" (the `next` DID is not enrolled)

- **M5** - LiveModeChange https://didcomm.org/messagepickup/3.0/live-delivery-change
  - Follow the protocol -> Upon receiving the `live_delivery_change` message with `live_delivery` set to `true``:
    - M5A - If the connection supports live delivery respond with a [`status`](https://didcomm.org/messagepickup/3.0/status) message.
    - M5B - If the connection is incapable of live delivery send a problem report `e.m.live-mode-not-supported`

### Considerations

In the Identus Mediator, when an issue arises, we undertake the following steps:
* Log the error.
* Dispatch a problem report as outlined in the table above.
* Record the problem report and error information in the database.
Every one of these steps should be traceable back to the initial call via an `XRequestID`, and by the MsgID (the SHA-251 hash of the encrypted message).
This procedure will enhance Level 3 support and debugging capabilities. (For [ATL-4147](https://input-output.atlassian.net/browse/ATL-4147))

[TODO optional] Would be nice to propose a list/table of new tokens to the protocol specs:
  - `e.p.me.res.storage`
  - `e.p.me.res.not_enroll`

## Problem Reports in Mediator flow

https://didcomm.org/coordinate-mediation/2.0/mediate-request

```mermaid
stateDiagram-v2
  classDef processing fill:#f00,color:white,font-weight:bold,stroke-width:2px,stroke:yellow

  [*] -->Recipient
 Recipient --> mediation_request_received:mediation request message(1)
  mediation_request_received --> request_stored :received mediation request message (2)
  request_stored -->  request_processing:{Mediation request processing} (3)
  request_processing -->Recipient: Mediation Grant message reply Sync (4)
  request_processing -->Recipient: Mediation Deny message reply Sync  (4)
  request_processing -->Recipient: Problem report message reply sync  (4)
  class request_processing processing
```

https://didcomm.org/coordinate-mediation/2.0/keylist-update
```mermaid
stateDiagram-v2
  classDef processing fill:#f00,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  classDef grant fill:#0a0,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  mediation_granted -->Recipient
 Recipient --> Key_list_update_request:key list update request message(1)
  Key_list_update_request --> request_stored :received Key list update request message (2)
  request_stored -->  request_processing:{Key list update request processing} (3)
  request_processing -->Recipient: Key list update Response message reply Sync (4)
  request_processing -->Recipient: Problem report message reply sync  (4)
  class request_processing processing
  class mediation_granted grant
```

https://didcomm.org/coordinate-mediation/2.0/keylist-query
```mermaid
stateDiagram-v2
  classDef processing fill:#f00,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  classDef grant fill:#0a0,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  mediation_granted -->Recipient
 Recipient --> Key_list_query_request:key list query request message(1)
  Key_list_query_request --> request_stored :received Key list query request message (2)
  request_stored -->  request_processing:{Key list query request processing} (3)
  request_processing -->Recipient: Key list query Response message reply Sync (4)
  request_processing -->Recipient: Problem report message reply sync  (4)
  class request_processing processing
  class mediation_granted grant
```

MessagePickup 3.0 https://didcomm.org/messagepickup/3.0/

https://didcomm.org/messagepickup/3.0/status-request
```mermaid
stateDiagram-v2
  classDef processing fill:#f00,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  classDef success fill:#0a0,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  mediation_granted --> Key_list_updated
  Key_list_updated -->Recipient
 Recipient --> status_request:status request message(1)
  status_request --> request_stored :received status request message (2)
  request_stored -->  request_processing: status request processing (3)
  request_processing -->Recipient: status Response message reply Sync (4)
  request_processing -->Recipient: Problem report message reply sync  (4)
  class request_processing processing
  class mediation_granted,Key_list_updated success
```
https://didcomm.org/messagepickup/3.0/delivery-request
```mermaid
stateDiagram-v2
  classDef processing fill:#f00,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  classDef success fill:#0a0,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  mediation_granted --> Key_list_updated
  Key_list_updated -->Recipient
 Recipient --> Key_list_query_request:Delivery request message(1)
  Key_list_query_request --> request_stored :received Delivery request message (2)
  request_stored -->  request_processing:{Delivery request processing} (3)
  request_processing -->Recipient: Delivery Response message reply Sync (4)
  request_processing -->Recipient: Problem report message reply sync  (4)
  class request_processing processing
  class mediation_granted,Key_list_updated success
```

https://didcomm.org/messagepickup/3.0/messages-received
```mermaid
stateDiagram-v2
  classDef processing fill:#f00,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  classDef success fill:#0a0,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  mediation_granted --> Key_list_updated
  Key_list_updated -->Recipient
 Recipient --> Key_list_query_request:messages-received request message(1)
  Key_list_query_request --> request_stored :messages-received request message (2)
  request_stored -->  request_processing:{messages-received request processing} (3)
  request_processing -->Recipient: messages-received Response message reply Sync (4)
  request_processing -->Recipient: Problem report message reply sync  (4)
  class request_processing processing
  class mediation_granted,Key_list_updated success
```


Sender to Recipient (Sender use the type forward message)
https://didcomm.org/routing/2.0/forward
```mermaid
stateDiagram-v2
  classDef processing fill:#f00,color:white,font-weight:bold,stroke-width:2px,stroke:yellow
  [*] --> Sender
  Sender --> forward_message_request:forward message request(1)
  forward_message_request --> request_stored :forward request message (2)
  request_stored -->  request_processing:{forward message request processing} (3)
  request_processing --> forward_message_stored: message stored (4)
  request_processing -->Sender: Problem report Signed message reply sync  (4)
  forward_message_stored --> [*]
  class request_processing processing
```
