
# Test Mediator (Trust Ping)

## Compile and Run the Mediator on docker

```shell
docker:publishLocal # Compile and create the mediator image
docker-compose up #docker run -p 8080:8080 docker.io/hyperledgeridentus/identus-mediator:0.1.0-SNAPSHOT
```

## Run the client

```shell
scala-cli repl \
  --dependency app.fmgp::did::0.1.0-M2 \
  --dependency app.fmgp::did-imp::0.1.0-M2 \
  --dependency app.fmgp::did-method-peer::0.1.0-M2 \
  --repo https://oss.sonatype.org/content/repositories/releases
```


```scala
import zio._
import zio.json._
import fmgp.crypto._
import fmgp.did._
import fmgp.did.comm._
import fmgp.did.comm.protocol.trustping2._
import fmgp.did.method.peer._

val agent = DIDPeer2.makeAgent(
  Seq(
    OKPPrivateKey(// keyAgreement
      kty = KTY.OKP,
      crv = Curve.X25519,
      d = "Z6D8LduZgZ6LnrOHPrMTS6uU2u5Btsrk1SGs4fn8M7c",
      x = "Sr4SkIskjN_VdKTn0zkjYbhGTWArdUNE4j_DmUpnQGw",
      kid = None
    ),
    OKPPrivateKey(//keyAuthentication
      kty = KTY.OKP,
      crv = Curve.Ed25519,
      d = "INXCnxFEl0atLIIQYruHzGd5sUivMRyQOzu87qVerug",
      x = "MBjnXZxkMcoQVVL21hahWAw43RuAG-i64ipbeKKqwoA",
      kid = None
    )
  ), //   //For mac user: if you are running the mediator using docker  replace localhost with host.docker.internal 
  Seq(DIDPeerServiceEncoded(s = "http://localhost:5000/")) 
)

val mediator = "did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9rOHMtaW50LmF0YWxhcHJpc20uaW8vbWVkaWF0b3IiLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19"

val ping = TrustPingWithRequestedResponse(from = agent.id, to = TO(mediator))

val program = for {
  msg <- Operations.authEncrypt(ping.toPlaintextMessage)
  _ <- Console.printLine(msg.toJson)
} yield ()

Unsafe.unsafe { implicit unsafe => // Run side efect
  Runtime.default.unsafe
    .run(program.provide(Operations.layerDefault ++ DidPeerResolver.layer ++ ZLayer.succeed(agent)))
    .getOrThrowFiberFailure()
}
```

To send the trust ping you can do a request with curl `curl -X POST http://localhost:8080 -H 'content-type: application/didcomm-encrypted+json' -d'...'` (replace `...` with the encrypted DID Comm message you get from the code above).

To receive the reply you can have netcat running like `nc -nl -p 5000`.

Then you can decrypt reply with the follow code:

```scala

val reply = """{"ciphertext":"863YmjDPR5mYlJtTEfTUFW11wpCKxsVKpDeEWtQPAqbPlECVIDKaCkh0j_tjpSQomgQL93MjrtjuTo3OnIU2EfHDX8t2mkf84muUSmeQtfMnN_ZmRYPxKw3WG_XUGrE25m8zAcce3RClm13TK9Y3XHeM4vrrExfOBYFcIIUoT9yWPIWb6Y39fRNyvlbNuYcEbsPR3RfvecOcBQJ3YaTlpzVjdgXaICZ4oC_5aVRX3EXsIhmEfjt6lBOGmstGT3zPAsLoP8mjb9GbwOIwpbj4u2BczjMDkUsezuzHsyCeilMGziG5Zr7D64Cjbt8Pg2F11vx4sLW11U3hfjRJ0gnShyBzSAOZmrR_TVBWGaP2KOELRtY4Inp8SzP4-YG89Ie2Cu9ZJesZr4eHYe5qFbI7IZg5lALazTtKodoehFuUDk9YklW8xip5_4-yIWKvj0_j79VBoTf3NiEyRz6CmPiW3Mtx5QJwt56Gb1DkLIkgyO5p_N6mUO3wntOa-bwW4ukYCG8eGnM05ENkIOpVW6DU2b5kvfuQarlG_QRXeUPy5hSjZjDa6WM6HwrbiXCFPc6-s3iMKs7IfrezVt7lN-hUynInR9duWNQLJEbyr_-Ete7r7YJDoORLemF6VGAfihO4ut1ceYIeYpF30saz9n6avVasaDSO_mErx8HvucXpZ8CzwyIO3PBImFi_EFP3VI1JGCE3b5EwUDXbkmUKIj1QbOrt_5kpmojgGP5h9Bi5Fzca9DEJrXsB2MTrXCNMhc1vUH5btMQy3guCmERrzcKIPPFhC8LMZshUzDRU1TDFKBUsUzcPitbi2wxz1zWV9ENnpLWil6pxu0LyNfjfz83GBA","protected":"eyJlcGsiOnsia3R5IjoiT0tQIiwiY3J2IjoiWDI1NTE5IiwieCI6InE1dW1XUnQ0dE5YeWM1eUlWOG84R1Jaekc5YUZmWlhkeHYxMlIwaktNRDgifSwiYXB2IjoidWRmdmFEZFpKNTFEdFJPZm8tOW1nMXRCOTRhM3ZxRFFhaFdpZXNNZjd3VSIsInNraWQiOiJkaWQ6cGVlcjoyLkV6NkxTZ2h3U0U0Mzd3bkRFMXB0M1g2aFZEVVF6U2pzSHppbnBYM1hGdk1qUkFtN3kuVno2TWtoaDFlNUNFWVlxNkpCVWNUWjZDcDJyYW5DV1JydjdZYXgzTGU0TjU5UjZkZC5TZXlKMElqb2laRzBpTENKeklqb2lhSFIwY0hNNkx5OXJPSE10YVc1MExtRjBZV3hoY0hKcGMyMHVhVzh2YldWa2FXRjBiM0lpTENKeUlqcGJYU3dpWVNJNld5SmthV1JqYjIxdEwzWXlJbDE5IzZMU2dod1NFNDM3d25ERTFwdDNYNmhWRFVRelNqc0h6aW5wWDNYRnZNalJBbTd5IiwiYXB1IjoiWkdsa09uQmxaWEk2TWk1RmVqWk1VMmRvZDFORk5ETTNkMjVFUlRGd2RETllObWhXUkZWUmVsTnFjMGg2YVc1d1dETllSblpOYWxKQmJUZDVMbFo2TmsxcmFHZ3haVFZEUlZsWmNUWktRbFZqVkZvMlEzQXljbUZ1UTFkU2NuWTNXV0Y0TTB4bE5FNDFPVkkyWkdRdVUyVjVTakJKYW05cFdrY3dhVXhEU25wSmFtOXBZVWhTTUdOSVRUWk1lVGx5VDBoTmRHRlhOVEJNYlVZd1dWZDRhR05JU25Cak1qQjFZVmM0ZG1KWFZtdGhWMFl3WWpOSmFVeERTbmxKYW5CaVdGTjNhVmxUU1RaWGVVcHJZVmRTYW1JeU1YUk1NMWw1U1d3eE9TTTJURk5uYUhkVFJUUXpOM2R1UkVVeGNIUXpXRFpvVmtSVlVYcFRhbk5JZW1sdWNGZ3pXRVoyVFdwU1FXMDNlUSIsInR5cCI6ImFwcGxpY2F0aW9uL2RpZGNvbW0tZW5jcnlwdGVkK2pzb24iLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiYWxnIjoiRUNESC0xUFUrQTI1NktXIn0","recipients":[{"encrypted_key":"yhqQA2n2QRm5e8m15hVsRWoL4BcEooLBlROwAYenUIOOwc90jK-9uDO2qAiV8eamgU9GQZ0dNYFM2oyaRQaewnIbCIyM6nwg","header":{"kid":"did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cDovL2xvY2FsaG9zdDo1MDAwLyIsInIiOltdLCJhIjpbImRpZGNvbW0vdjIiXX0#6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y"}}],"tag":"Xm_uDGCkZVjgWc8d0dr_N2L5gN8ll_rPvLyexiiImlM","iv":"46H81iydYSwZColseMrf-g"}"""
  .fromJson[EncryptedMessage]
  .getOrElse(???)

val program2 = for {
  msg <- Operations.anonDecrypt(reply)
  _ <- Console.printLine(msg.toJson)
} yield ()

Unsafe.unsafe { implicit unsafe => // Run side efect
  Runtime.default.unsafe
    .run(program2.provide(Operations.layerDefault ++ DidPeerResolver.layer ++ ZLayer.succeed(agent)))
    .getOrThrowFiberFailure()
}
```
