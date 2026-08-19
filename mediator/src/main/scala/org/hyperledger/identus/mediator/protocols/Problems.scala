package org.hyperledger.identus.mediator.protocols

import com.typesafe.config.{Config, ConfigFactory}
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.protocol.reportproblem2.*

object Problems {
  val config: Config = ConfigFactory.load()
  val email: Option[String] = Option(config.getString("mediator.problem.report.escalateTo"))

  def unsupportedProtocolType(
      to: Set[TO],
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
  ) = ProblemReport(
    to = to,
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroFail("msg", "unsupported"),
    comment = None,
    args = None,
    escalate_to = email,
  )

  def unsupportedProtocolRole(
      to: TO,
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
  ) = ProblemReport(
    to = Set(to),
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroFail("msg", "unsupported"),
    comment = None,
    args = None,
    escalate_to = email,
  )

  def protocolNotImplemented(
      to: TO,
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
  ) = ProblemReport(
    to = Set(to),
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroFail("msg", "unsupported"),
    comment = None,
    args = None,
    escalate_to = email,
  )

  def liveModeNotSupported(
      to: TO,
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
  ) = ProblemReport(
    to = Set(to),
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroUndo("live-mode-not-supported"),
    comment = Some("Connection does not support Live Delivery"),
    args = None,
    escalate_to = email,
  )

  def storageError(
      to: Set[TO],
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
  ) = ProblemReport(
    to = to,
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroFail("me", "res", "storage"),
    comment = None,
    args = None,
    escalate_to = email,
  )

  def dejavuError(
      to: Set[TO],
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
  ) = ProblemReport(
    to = to,
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroFail("crypto", "message", "dejavu"),
    comment = None,
    args = None,
    escalate_to = email,
  )

  def notEnroledError(
      to: Option[TO],
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
      didNotEnrolled: DIDSubject
  ) = ProblemReport(
    to = to.toSet,
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroFail("req", "not_enroll"),
    comment = Some("The DID '{1}' is not enroled."),
    args = Some(Seq(didNotEnrolled.did)),
    escalate_to = email,
  )

  def malformedError(
      to: Set[TO],
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
      comment: String
  ) = ProblemReport(
    to = to,
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroFail("msg", piuri.value),
    comment = Some(comment),
    args = None,
    escalate_to = email,
  )

  def decryptFail(
      from: FROM,
      comment: String
  ) = ProblemReport(
    to = Set.empty,
    from = from,
    pthid = MsgID("?"), // TODO
    ack = None,
    code = ProblemCode.ErroFail("msg"),
    comment = Some(comment),
    args = None,
    escalate_to = email,
  )

  def resolutionError(
      to: Set[TO],
      from: FROM,
      pthid: MsgID,
      piuri: PIURI,
      comment: String
  ) = ProblemReport(
    to = to,
    from = from,
    pthid = pthid,
    ack = None,
    code = ProblemCode.ErroFail("me", "res", "resolver"),
    comment = Some(comment),
    args = None,
    escalate_to = email,
  )

}
