package org.hyperledger.identus.mediator

import fmgp.did.*
import org.hyperledger.identus.mediator.db.DidAccountStubSetup
import zio.*
import zio.test.*

object MediatorTransportManagerSpec extends ZIOSpecDefault with DidAccountStubSetup {

  private val first = TransportUtil.newTransportEmptySingleTransmission
  private val second = TransportUtil.newTransportEmptyMultiTransmissions

  override def spec = suite("MediatorTransportManagerSpec")(
    test("only registered transports can be linked to a DID") {
      for {
        ref <- ZIO.service[Ref[MediatorTransportManager]]
        empty <- ref.get
        subject = alice.asFROMTO
        ignored = empty.link(subject, first.id)
        registered = empty.registerTransport(first).registerTransport(second)
        linked = registered.link(subject, first).link(subject, second).link(subject, first)
      } yield assertTrue(
        ignored.ids.isEmpty,
        linked.ids.get(subject).contains(Set(first.id, second.id)),
        linked.transports.size == 2
      )
    },
    test("unregistering a transport removes its DID and live-mode links") {
      for {
        ref <- ZIO.service[Ref[MediatorTransportManager]]
        empty <- ref.get
        subject = alice.asFROMTO
        linked = empty.registerTransport(first).registerTransport(second)
          .link(subject, first).link(subject, second)
          .enableLiveMode(subject, first.id).enableLiveMode(subject, second.id)
        remaining = linked.unregisterTransport(first.id)
        none = remaining.unregisterTransport(second.id)
      } yield assertTrue(
        remaining.ids.get(subject).contains(Set(second.id)),
        remaining.isLiveModeEnabled(subject, second.id),
        !remaining.isLiveModeEnabled(subject, first.id),
        remaining.getLiveModeEnableConnections(subject).map(_.id) == Seq(second.id),
        none.ids.isEmpty,
        none.liveMode.isEmpty,
        none.transports.isEmpty
      )
    },
    test("live mode can be enabled and disabled per transport") {
      for {
        ref <- ZIO.service[Ref[MediatorTransportManager]]
        empty <- ref.get
        subject = bob.asFROMTO
        enabled = empty.registerTransport(first).enableLiveMode(subject, first.id)
        disabled = enabled.disableLiveMode(subject, first.id)
      } yield assertTrue(
        !empty.isLiveModeEnabled(subject, first.id),
        enabled.isLiveModeEnabled(subject, first.id),
        !disabled.isLiveModeEnabled(subject, first.id),
        disabled.getLiveModeEnableConnections(subject).isEmpty
      )
    }
  ).provideLayer(MediatorTransportManagerUtil.layerTest)
}
