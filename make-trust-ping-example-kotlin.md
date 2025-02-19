# Test Mediator (Trust Ping)


Start the mediator from repo using sbt 
```sbt
 sbt mediator/run  
```
Using  Docker 
***if you have mac os comment the the line
 #network_mode: host in docker-compose.yaml***

```shell
docker:publishLocal # Compile and create the mediator image
docker-compose up #docker run -p 8080:8080 docker.io/hyperledgeridentus/identus-mediator:0.1.0-SNAPSHOT
```


### Required Dependencies  for e.g. add to your build.gradle.kts

```kotlin
dependencies {
    implementation("org.didcommx:didcomm:0.3.0")
    implementation("org.didcommx:peerdid:0.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.1")
    implementation("io.ktor:ktor-client-okhttp:2.3.1")}
```kotlin


### Create a Listener.kt for listening to mediator reply. for e.g. copy the below code in Listener.kt

```kotlin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

abstract class Listener {

    private var receivedResponse: String? = null

    fun route(application: Application) {
        application.routing {
            post("/") {
                val json = call.receiveText()
                receivedResponse = json
                call.respond(HttpStatusCode.OK, "Data received")
            }
        }
    }

    fun startListening(port: Int = 9999) {
        embeddedServer(Netty, port = 9999, module = {route(this)}).start(wait = false)
    }

    fun receivedResponse(): String? {
        return receivedResponse
    }

}

```kotlin

### Create a DidAgent.kt  for e.g. copy the below code in DidAgent.kt

```kotlin
import com.nimbusds.jose.jwk.OctetKeyPair
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.diddoc.DIDDocResolver
import org.didcommx.didcomm.secret.Secret
import org.didcommx.didcomm.secret.SecretResolverInMemory
import org.didcommx.peerdid.VerificationMaterialFormatPeerDID
import org.didcommx.peerdid.VerificationMaterialPeerDID
import org.didcommx.peerdid.VerificationMethodTypePeerDID
import org.didcommx.peerdid.core.*

data class DidId(val value: String)

interface DidAgent {
    val did: DidId
    val jwkForKeyAgreement: List<OctetKeyPair>
    val jwkForKeyAuthentication: List<OctetKeyPair>
}


data class PeerDID(
    override val did: DidId,
    override val jwkForKeyAgreement: List<OctetKeyPair>,
    override val jwkForKeyAuthentication: List<OctetKeyPair>,
) : DidAgent, Listener() {
    val didDocument: String
        get() = org.didcommx.peerdid.resolvePeerDID(did.value, VerificationMaterialFormatPeerDID.JWK)

    fun getSecretResolverInMemory(): SecretResolverInMemory {

        fun validateRawKeyLength(key: ByteArray) {
            // for all supported key types now (ED25519 and X25510) the expected size is 32
            if (key.size != 32)
                throw IllegalArgumentException("Invalid key $key")
        }

        fun createMultibaseEncnumbasis(key: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>): String {
            val decodedKey = when (key.format) {
                VerificationMaterialFormatPeerDID.BASE58 -> fromBase58(key.value.toString())
                VerificationMaterialFormatPeerDID.MULTIBASE -> fromMulticodec(fromBase58Multibase(key.value.toString()).second).second
                VerificationMaterialFormatPeerDID.JWK -> fromJwk(key)
            }
            validateRawKeyLength(decodedKey)
            return toBase58Multibase(toMulticodec(decodedKey, key.type))
        }

        val keyAgreement =
            AgentPeerService.keyAgreemenFromPublicJWK(this.jwkForKeyAgreement.first()) // TODO Fix first()
        val keyAuthentication =
            AgentPeerService.keyAuthenticationFromPublicJWK(this.jwkForKeyAuthentication.first()) // TODO Fix first()

        val keyIdAgreement = createMultibaseEncnumbasis(keyAgreement).drop(1)
        val keyIdAuthentication = createMultibaseEncnumbasis(keyAuthentication).drop(1)

        val secretKeyAgreement = Secret(
            "${this.did.value}#$keyIdAgreement",
            VerificationMethodType.JSON_WEB_KEY_2020,
            VerificationMaterial(VerificationMaterialFormat.JWK, this.jwkForKeyAgreement.first().toJSONString())
        )
        val secretKeyAuthentication = Secret(
            "${this.did.value}#$keyIdAuthentication",
            VerificationMethodType.JSON_WEB_KEY_2020,
            VerificationMaterial(VerificationMaterialFormat.JWK, this.jwkForKeyAuthentication.first().toJSONString())
        )

        return SecretResolverInMemory(
            mutableMapOf(
                "${this.did.value}#$keyIdAgreement" to secretKeyAgreement,
                "${this.did.value}#$keyIdAuthentication" to secretKeyAuthentication,
            ).toMap()
        )
    }

    fun getDidDocResolverInMemory(): DIDDocResolver {
        return DIDDocResolverPeerDID()
    }

    fun getServiceEndpoint(): String {
        return DIDDocResolverPeerDID().resolve(this.did.value).get().didCommServices.map { it.serviceEndpoint }.first()
    }
}

```kotlin

### Create an AgentPeerService.kt, for e.g. copy the below code in AgentPeerService.kt

```kotlin
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.OctetKeyPair
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator
import org.didcommx.peerdid.VerificationMaterialFormatPeerDID
import org.didcommx.peerdid.VerificationMaterialPeerDID
import org.didcommx.peerdid.VerificationMethodTypeAgreement
import org.didcommx.peerdid.VerificationMethodTypeAuthentication

object AgentPeerService {

    fun createServiceJson(serviceEndpoint: String): String {
        return """
        {
            "type": "DIDCommMessaging",
            "serviceEndpoint": "$serviceEndpoint",
            "routingKeys": [],
            "accept": ["didcomm/v2"]
        }
    """
    }

    fun makeNewJwkKeyX25519(): OctetKeyPair = OctetKeyPairGenerator(Curve.X25519).generate()
    fun makeNewJwkKeyEd25519(): OctetKeyPair = OctetKeyPairGenerator(Curve.Ed25519).generate()
    fun keyAgreemenFromPublicJWK(key: OctetKeyPair): VerificationMaterialPeerDID<VerificationMethodTypeAgreement> =
        VerificationMaterialPeerDID(
            VerificationMaterialFormatPeerDID.JWK,
            key.toPublicJWK(),
            VerificationMethodTypeAgreement.JSON_WEB_KEY_2020
        )

    fun keyAuthenticationFromPublicJWK(key: OctetKeyPair): VerificationMaterialPeerDID<VerificationMethodTypeAuthentication> =
        VerificationMaterialPeerDID(
            VerificationMaterialFormatPeerDID.JWK,
            key.toPublicJWK(),
            VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020
        )

    fun makePeerDid(
        jwkForKeyAgreement: OctetKeyPair = makeNewJwkKeyX25519(),
        jwkForKeyAuthentication: OctetKeyPair = makeNewJwkKeyEd25519(),
        serviceEndpoint: String? = null
    ): PeerDID {
        println("**************************jwkForKeyAgreement***********************************************************")
        println(jwkForKeyAgreement)
        println("**************************jwkForKeyAgreement***********************************************************")
        println("************************************jwkForKeyAuthentication*************************************************")
        println(jwkForKeyAuthentication)
        println("************************************jwkForKeyAuthentication*************************************************")

        val did = org.didcommx.peerdid.createPeerDIDNumalgo2(
            listOf(keyAgreemenFromPublicJWK(jwkForKeyAgreement)),
            listOf(keyAuthenticationFromPublicJWK(jwkForKeyAuthentication)),
            serviceEndpoint?.let { createServiceJson(serviceEndpoint) }
        )
        return PeerDID(DidId(did), listOf(jwkForKeyAgreement), listOf(jwkForKeyAuthentication))
    }

    fun makeAgent(
        serviceEndpoint: String? = null
    ): PeerDID {
        return makePeerDid(
            makeNewJwkKeyX25519(),
            makeNewJwkKeyEd25519(),
            serviceEndpoint = serviceEndpoint
        )
    }

    fun makeAgentFromPeerDid(
        did: String
    ): PeerDID {
        return PeerDID(DidId(did), listOf(), listOf())
    }
}

```kotlin 

### Create a DidDocResolver.kt for e.g. copy the below code in DidDocResolver.kt

```kotlin
import org.didcommx.didcomm.common.VerificationMaterial
import org.didcommx.didcomm.common.VerificationMaterialFormat
import org.didcommx.didcomm.common.VerificationMethodType
import org.didcommx.didcomm.diddoc.DIDCommService
import org.didcommx.didcomm.diddoc.DIDDoc
import org.didcommx.didcomm.diddoc.DIDDocResolver
import org.didcommx.didcomm.diddoc.VerificationMethod
import org.didcommx.didcomm.utils.toJson
import org.didcommx.peerdid.DIDCommServicePeerDID
import org.didcommx.peerdid.DIDDocPeerDID
import org.didcommx.peerdid.VerificationMaterialFormatPeerDID
import org.didcommx.peerdid.resolvePeerDID
import java.util.*

class DIDDocResolverPeerDID : DIDDocResolver {

    override fun resolve(did: String): Optional<DIDDoc> {
        val didDocJson = resolvePeerDID(did, format = VerificationMaterialFormatPeerDID.JWK)
        val didDoc = DIDDocPeerDID.fromJson(didDocJson)
        return Optional.ofNullable(
            DIDDoc(
                did = did,
                keyAgreements = didDoc.agreementKids,
                authentications = didDoc.authenticationKids,
                verificationMethods = (didDoc.authentication + didDoc.keyAgreement).map {
                    VerificationMethod(
                        id = it.id,
                        type = VerificationMethodType.JSON_WEB_KEY_2020,
                        controller = it.controller,
                        verificationMaterial = VerificationMaterial(
                            format = VerificationMaterialFormat.JWK,
                            value = toJson(it.verMaterial.value)
                        )
                    )
                },
                didCommServices = didDoc.service?.mapNotNull {
                    when (it) {
                        is DIDCommServicePeerDID ->
                            DIDCommService(
                                id = it.id,
                                serviceEndpoint = it.serviceEndpoint,
                                routingKeys = it.routingKeys,
                                accept = it.accept
                            )

                        else -> null
                    }
                }
                    ?: emptyList()
            )
        )
    }
}
```kotlin



### This is your Main.kt  for e.g. copy the below code in Main.kt

```kotlin
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.didcommx.didcomm.DIDComm
import org.didcommx.didcomm.message.Message
import org.didcommx.didcomm.model.PackEncryptedParams

fun main(args: Array<String>) {

    //For mac user: if you are running the mediator using docker  replace localhost with host.docker.internal
    // serviceEndpoint = "http://host.docker.internal:9999/"
    val alice = AgentPeerService.makeAgent(serviceEndpoint = "http://localhost:9999/")
    println(alice.did.value)
    println(alice.didDocument)

    val mediatorDID =
        "did:peer:2.Ez6LSghwSE437wnDE1pt3X6hVDUQzSjsHzinpX3XFvMjRAm7y.Vz6Mkhh1e5CEYYq6JBUcTZ6Cp2ranCWRrv7Yax3Le4N59R6dd.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9rOHMtaW50LmF0YWxhcHJpc20uaW8vbWVkaWF0b3IiLCJyIjpbXSwiYSI6WyJkaWRjb21tL3YyIl19"

    val mediator = AgentPeerService.makeAgentFromPeerDid(mediatorDID)
    val mediatorServiceEndpoint = "http://localhost:8080"  // mediator.getServiceEndpoint()

    val didComm = DIDComm(mediator.getDidDocResolverInMemory(), alice.getSecretResolverInMemory())

    val messageTrustPing = Message.builder(
        id = "1234567890",
        body = mapOf("response_requested" to true),
        type = "https://didcomm.org/trust-ping/2.0/ping"
    ).from(alice.did.value)
        .to(listOf(mediator.did.value))
        .build()
    val messageMediationRequest = Message.builder(
        id = "1234567890",
        body = mapOf(),
        type = "https://didcomm.org/coordinate-mediation/2.0/mediate-request"
    ).from(alice.did.value)
        .to(listOf(mediator.did.value))
        .build()

    //Encrypt Message
    val packResult = didComm.packEncrypted(
        PackEncryptedParams.builder(messageMediationRequest, mediatorDID)
            .from(alice.did.value)
            .build()
    )
    println("*************************************************************************************")
    println(packResult.packedMessage)
    println("*************************************************************************************")

    // Start the listener in a separate coroutine
    runBlocking {
        val completion = CompletableDeferred<Unit>()

        val clientListener = launch(Dispatchers.Default) {
            alice.startListening(9999)
        }

        clientListener.join()
        launch(Dispatchers.Default) {

            val request = Request.Builder().url(mediatorServiceEndpoint)
                .addHeader("content-type", "application/didcomm-encrypted+json")
                .post(packResult.packedMessage.toRequestBody()).build()
            val response =  OkHttpClient().newCall(request).execute()

            println("Client Received: $response")
            println("Client Received: ${response.body?.string()}")

            val json = alice.receivedResponse()
            println("Received a reply to the http endpoint of client: $json")

            response.close()
            completion.complete(Unit)
        }

        completion.await()

        // Stop the listener
        clientListener.cancel()
    }


}
```kotlin
