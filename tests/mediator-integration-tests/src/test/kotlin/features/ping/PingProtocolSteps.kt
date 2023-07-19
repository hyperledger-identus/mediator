package features.ping

import abilities.HttpListener
import common.DidcommMessageTypes
import common.EdgeAgent
import common.Ensure
import common.Environments.MEDIATOR_PEER_DID
import interactions.SendDidcommMessage
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.iohk.atala.prism.walletsdk.domain.models.Message
import net.serenitybdd.rest.SerenityRest
import net.serenitybdd.screenplay.Actor
import org.apache.http.HttpStatus.SC_OK

class PingProtocolSteps {

    @When("{actor} sends trusted ping message to mediator")
    fun iSendTrustedPingMessageToMediator(recipient: Actor) {

        val pingMessage = Message(
            piuri = DidcommMessageTypes.PING_REQUEST,
            from = EdgeAgent.peerDID,
            to = MEDIATOR_PEER_DID,
            body = """{"response_requested": true}"""
        )

        recipient.attemptsTo(
            SendDidcommMessage(pingMessage)
        )
    }

    @Then("{actor} gets trusted ping message back")
    fun recipientGetTrustedPingMessageBack(recipient: Actor) {
        val didcommResponse: Message = EdgeAgent.unpackMessage(
            HttpListener.receivedResponse()!!
        )
        val httpResponse = SerenityRest.lastResponse()

        recipient.attemptsTo(
            Ensure.that(httpResponse.statusCode).isEqualTo(SC_OK),
            Ensure.that(didcommResponse.piuri).isEqualTo(DidcommMessageTypes.PING_RESPONSE),
            Ensure.that(didcommResponse.from.toString()).isEqualTo(MEDIATOR_PEER_DID.toString()),
            Ensure.that(didcommResponse.to.toString()).isEqualTo(EdgeAgent.peerDID.toString()),
        )

    }
}
