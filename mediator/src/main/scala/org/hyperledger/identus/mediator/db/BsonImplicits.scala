package org.hyperledger.identus.mediator.db

import fmgp.crypto.*
import fmgp.did.*
import fmgp.did.comm.*
import fmgp.did.comm.extension.*
import fmgp.util.*
import reactivemongo.api.bson.*
import zio.json.*
import zio.json.ast.Json

import scala.util.*

given BSONWriter[DIDSubject] with {
  import DIDSubject.*
  def writeTry(obj: DIDSubject): Try[BSONValue] = Try(BSONString(obj.string))
}

given BSONReader[DIDSubject] with {
  def readTry(bson: BSONValue): Try[DIDSubject] = bson.asTry[String].map(v => DIDSubject(v))
}

given BSONWriter[DID] with {
  import DID.*
  def writeTry(obj: DID): Try[BSONValue] = {
    println("_" * 100)
    println(obj.did)
    println("^" * 100)
    Try(BSONString(obj.did))
  }
}

given BSONReader[DID] with {
  def readTry(bson: BSONValue): Try[DID] = bson.asTry[String].map(v => DIDSubject(v))
}

given BSONWriter[APV] with {
  import APV.*
  def writeTry(obj: APV): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[APV] with {
  def readTry(bson: BSONValue): Try[APV] = bson.asTry[String].map(v => APV(v))
}

given BSONWriter[APU] with {
  import APU.*
  def writeTry(obj: APU): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[APU] with {
  def readTry(bson: BSONValue): Try[APU] = bson.asTry[String].map(v => APU(v))
}

given BSONWriter[CipherText] with {
  import CipherText.*
  def writeTry(obj: CipherText): Try[BSONValue] = Try(BSONString(obj.value))
}

given BSONReader[CipherText] with {
  def readTry(bson: BSONValue): Try[CipherText] = bson.asTry[String].map(v => CipherText(v))
}

given BSONWriter[TAG] with {
  import TAG.*
  def writeTry(obj: TAG): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[TAG] with {
  def readTry(bson: BSONValue): Try[TAG] = bson.asTry[String].map(v => TAG(v))
}
given BSONWriter[IV] with {
  import IV.*
  def writeTry(obj: IV): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[IV] with {
  def readTry(bson: BSONValue): Try[IV] = bson.asTry[String].map(v => IV(v))
}

given BSONWriter[Base64Obj[ProtectedHeader]] with {
  import Base64Obj.*
  def writeTry(obj: Base64Obj[ProtectedHeader]): Try[BSONValue] = {
    val protectedHeader: String = (obj.obj, obj.original) match {
      case (_, Some(op)) => op.urlBase64
      case (p, None)     => obj.base64url
    }
    Try(BSONString(protectedHeader))
  }
}
given BSONReader[Base64Obj[ProtectedHeader]] with {
  def readTry(bson: BSONValue): Try[Base64Obj[ProtectedHeader]] =
    bson
      .asTry[String]
      .flatMap { v =>
        s""""$v"""".fromJson[Base64Obj[ProtectedHeader]] match // TODO with a new methods from ScalaDid
          case Left(value)  => Failure(RuntimeException(value))
          case Right(value) => Try(value)
      }
}

given BSONWriter[Base64] with {
  import Base64.*
  def writeTry(obj: Base64): Try[BSONValue] = Try(BSONString(obj.urlBase64))
}

given BSONReader[Base64] with {
  import Base64.*
  def readTry(bson: BSONValue): Try[Base64] = bson.asTry[String].map(v => Base64.fromBase64url(v))
}

given BSONDocumentWriter[Recipient] =
  Macros.writer[Recipient]
given BSONDocumentReader[Recipient] =
  Macros.reader[Recipient]

given BSONWriter[VerificationMethodReferenced] with {
  def writeTry(obj: VerificationMethodReferenced): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[VerificationMethodReferenced] with {
  def readTry(bson: BSONValue): Try[VerificationMethodReferenced] =
    bson.asTry[String].map(v => VerificationMethodReferenced(v))
}

given BSONDocumentWriter[RecipientHeader] =
  Macros.writer[RecipientHeader]
given BSONDocumentReader[RecipientHeader] =
  Macros.reader[RecipientHeader]

given BSONWriter[KTY] with {
  def writeTry(obj: KTY): Try[BSONValue] = Try(BSONString(obj.toString()))
}
given BSONReader[KTY] with {
  def readTry(bson: BSONValue): Try[KTY] = bson.asTry[String].map(v => KTY.valueOf(v))
}

given BSONWriter[Curve] with {
  def writeTry(obj: Curve): Try[BSONValue] = Try(BSONString(obj.toString()))
}
given BSONReader[Curve] with {
  def readTry(bson: BSONValue): Try[Curve] = bson.asTry[String].map(v => Curve.valueOf(v))
}

given BSONWriter[ENCAlgorithm] with {
  def writeTry(obj: ENCAlgorithm): Try[BSONValue] = Try(BSONString(obj.toString()))
}
given BSONReader[ENCAlgorithm] with {
  def readTry(bson: BSONValue): Try[ENCAlgorithm] = bson.asTry[String].map(v => ENCAlgorithm.valueOf(v))
}

given BSONWriter[KWAlgorithm] with {
  def writeTry(obj: KWAlgorithm): Try[BSONValue] = Try(BSONString(obj.toString()))
}
given BSONReader[KWAlgorithm] with {
  def readTry(bson: BSONValue): Try[KWAlgorithm] = bson.asTry[String].map(v => KWAlgorithm.valueOf(v))
}
given BSONWriter[MediaTypes] with {
  def writeTry(obj: MediaTypes): Try[BSONValue] = Try(BSONString(obj.toString()))
}
given BSONReader[MediaTypes] with {
  def readTry(bson: BSONValue): Try[MediaTypes] = bson.asTry[String].map(v => MediaTypes.valueOf(v))
}

given BSONDocumentWriter[PublicKey] with {
  override def writeTry(obj: PublicKey): Try[BSONDocument] =
    obj match {
      case key: ECPublicKey =>
        Try(
          BSONDocument(
            "kty" -> key.kty,
            "crv" -> key.crv,
            "x" -> key.x,
            "y" -> key.y
          )
        )
      case key: OKPPublicKey =>
        Try(
          BSONDocument(
            "kty" -> key.kty,
            "crv" -> key.crv,
            "x" -> key.x
          )
        )
      case null => Failure(new RuntimeException("PublicKey cannot be null"))
    }
}
given BSONDocumentReader[PublicKey] with {

  override def readDocument(doc: BSONDocument): Try[PublicKey] =
    doc.get("kty").get.asTry[KTY] match
      case Failure(exception) => Failure(exception)
      case Success(KTY.OKP) =>
        (
          doc.getAsTry[Curve]("crv"),
          doc.getAsTry[String]("x"),
        ) match
          case (Success(crv), Success(x)) =>
            Success(
              OKPPublicKey(
                kty = KTY.OKP,
                crv = crv.asOKPCurve,
                x = x,
                kid = doc.getAsOpt[String]("kid").orNull
              )
            )
          case (Failure(ex), _) => Failure(ex)
          case (_, Failure(ex)) => Failure(ex)
      case Success(KTY.EC) =>
        (
          doc.getAsTry[Curve]("crv"),
          doc.getAsTry[String]("x"),
          doc.getAsTry[String]("y"),
        ) match
          case (Success(crv), Success(x), Success(y)) =>
            Success(
              ECPublicKey(
                kty = KTY.EC,
                crv = crv.asECCurve,
                x = x,
                y = y,
                kid = doc.getAsOpt[String]("kid").orNull
              )
            )
          case (Failure(ex), _, _) => Failure(ex)
          case (_, Failure(ex), _) => Failure(ex)
          case (_, _, Failure(ex)) => Failure(ex)

}

given BSONDocumentWriter[AnonProtectedHeader] =
  Macros.writer[AnonProtectedHeader]
given BSONDocumentReader[AnonProtectedHeader] =
  Macros.reader[AnonProtectedHeader]

given BSONDocumentWriter[AuthProtectedHeader] =
  Macros.writer[AuthProtectedHeader]
given BSONDocumentReader[AuthProtectedHeader] =
  Macros.reader[AuthProtectedHeader]

given BSONDocumentWriter[ProtectedHeader] =
  Macros.writer[ProtectedHeader] // TODO FIX The encoder for ProtectedHeader MUST not have the field "className"
given BSONDocumentReader[ProtectedHeader] =
  Macros.reader[ProtectedHeader]

given BSONDocumentWriter[EncryptedMessage] with {
  val aux = Macros.writer[EncryptedMessageGeneric]
  override def writeTry(obj: EncryptedMessage): Try[BSONDocument] =
    obj match {
      case msg: EncryptedMessageGeneric => aux.writeTry(msg) // Success(msg): Try[reactivemongo.api.bson.BSONDocument]
      case _                            => Failure(RuntimeException("Only support EncryptedMessageGeneric"))
    }

}
given BSONDocumentReader[EncryptedMessage] with {
  val aux = Macros.reader[EncryptedMessageGeneric]
  override def readDocument(doc: BSONDocument): Try[EncryptedMessage] =
    aux.readDocument(doc)
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

given BSONWriter[Payload] with {
  import Payload.*
  def writeTry(obj: Payload): Try[BSONValue] = Try(BSONString(obj.base64url))
}
given BSONReader[Payload] with {
  def readTry(bson: BSONValue): Try[Payload] = bson.asTry[String].map(v => Payload.fromBase64url(v))
}

given BSONWriter[JWAAlgorithm] with {
  def writeTry(obj: JWAAlgorithm): Try[BSONValue] = Try(BSONString(obj.toString()))
}
given BSONReader[JWAAlgorithm] with {
  def readTry(bson: BSONValue): Try[JWAAlgorithm] = bson.asTry[String].map(v => JWAAlgorithm.valueOf(v))
}

given BSONDocumentWriter[SignProtectedHeader] = Macros.writer[SignProtectedHeader]
given BSONDocumentReader[SignProtectedHeader] = Macros.reader[SignProtectedHeader]

given given_BSONWriter_Base64Obj_SignProtectedHeader: BSONWriter[Base64Obj[SignProtectedHeader]] with {
  import Base64Obj.*
  def writeTry(obj: Base64Obj[SignProtectedHeader]): Try[BSONValue] = {
    val protectedHeader: String = (obj.obj, obj.original) match {
      case (_, Some(op)) => op.urlBase64
      case (p, None)     => obj.base64url
    }
    Try(BSONString(protectedHeader))
  }
}
given given_BSONReader_Base64Obj_SignProtectedHeader: BSONReader[Base64Obj[SignProtectedHeader]] with {
  def readTry(bson: BSONValue): Try[Base64Obj[SignProtectedHeader]] =
    bson
      .asTry[String]
      .flatMap { v =>
        s""""$v"""".fromJson[Base64Obj[SignProtectedHeader]] match // TODO with a new methods from ScalaDid
          case Left(value)  => Failure(RuntimeException(value))
          case Right(value) => Try(value)
      }
}

given BSONWriter[SignatureJWM] with {
  import SignatureJWM.*
  def writeTry(obj: SignatureJWM): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[SignatureJWM] with {
  def readTry(bson: BSONValue): Try[SignatureJWM] = bson.asTry[String].map(v => SignatureJWM(v))
}

given BSONDocumentWriter[JWMHeader] = Macros.writer[JWMHeader]
given BSONDocumentReader[JWMHeader] = Macros.reader[JWMHeader]

given BSONDocumentWriter[JWMSignatureObj] = Macros.writer[JWMSignatureObj]
given BSONDocumentReader[JWMSignatureObj] = Macros.reader[JWMSignatureObj]

given BSONDocumentWriter[SignedMessage] = Macros.writer[SignedMessage]
given BSONDocumentReader[SignedMessage] = Macros.reader[SignedMessage]

given BSONDocumentWriter[SignedMessage | EncryptedMessage] with {
  override def writeTry(obj: SignedMessage | EncryptedMessage): Try[BSONDocument] =
    obj match {
      case msg: EncryptedMessage => given_BSONDocumentWriter_EncryptedMessage.writeTry(msg)
      case msg: SignedMessage    => given_BSONDocumentWriter_SignedMessage.writeTry(msg)
    }
}
given BSONDocumentReader[SignedMessage | EncryptedMessage] with {
  override def readDocument(doc: BSONDocument): Try[SignedMessage | EncryptedMessage] =
    given_BSONDocumentReader_EncryptedMessage
      .readDocument(doc)
      .orElse(
        given_BSONDocumentReader_SignedMessage
          .readDocument(doc)
      )
}

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

given BSONWriter[MsgID] with {
  import MsgID.*
  def writeTry(obj: MsgID): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[MsgID] with {
  def readTry(bson: BSONValue): Try[MsgID] = bson.asTry[String].map(v => MsgID(v))
}

given BSONWriter[PIURI] with {
  import PIURI.*
  def writeTry(obj: PIURI): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[PIURI] with {
  def readTry(bson: BSONValue): Try[PIURI] = bson.asTry[String].map(v => PIURI(v))
}

given BSONWriter[TO] with {
  import TO.*
  def writeTry(obj: TO): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[TO] with {
  def readTry(bson: BSONValue): Try[TO] = bson.asTry[String].map(v => TO(v))
}

given BSONWriter[FROM] with {
  import FROM.*
  def writeTry(obj: FROM): Try[BSONValue] = Try(BSONString(obj.value))
}
given BSONReader[FROM] with {
  def readTry(bson: BSONValue): Try[FROM] = bson.asTry[String].map(v => FROM(v))
}

//JSON_RFC7159
def sequenceTrys[T](trySequence: Seq[? <: Try[? <: T]]): Try[Seq[T]] = {
  trySequence.foldLeft(Try(Seq.empty[T])) { (acc, tryElement) =>
    acc.flatMap(accSeq => tryElement.map(success => accSeq :+ success))
  }
}

def toBSON(j: Json): Try[BSONValue] = j match
  case Json.Obj(fields)       => sequenceTrys(fields.map(e => toBSON(e._2).map(e2 => (e._1, e2)))).map(BSONDocument(_))
  case Json.Arr(elements)     => sequenceTrys(elements.map(toBSON(_))).map(BSONArray(_))
  case Json.Bool(value)       => Try(BSONBoolean(value))
  case Json.Str(value)        => Try(BSONString(value))
  case Json.Num(value)        => BSONDecimal.fromBigDecimal(value)
  case zio.json.ast.Json.Null => Try(BSONNull)

def toJson(b: BSONValue): Try[Json] = b match
  case doc: BSONDocument =>
    sequenceTrys(doc.toMap.toSeq.map(e => toJson(e._2).map(e2 => (e._1, e2)))).map(Json.Obj(_*))
  case array: BSONArray => sequenceTrys(array.values.map(toJson(_))).map(Json.Arr(_*))
  case e: BSONDouble    => e.toDouble.map(Json.Num(_))
  case e: BSONInteger   => e.toDouble.map(Json.Num(_))
  case e: BSONLong      => e.toDouble.map(Json.Num(_))
  case e: BSONDecimal   => e.toDouble.map(Json.Num(_))
  case e: BSONString    => Try(Json.Str(e.value))
  case e: BSONBoolean   => Try(Json.Bool(e.value))
  case BSONUndefined    => Try(Json.Null)
  case BSONNull         => Try(Json.Null)
  // case _: BSONBinary =>
  // case _: BSONDateTime     =>
  // case _: BSONRegex        =>
  // case _: BSONJavaScript   =>
  // case _: BSONSymbol       =>
  // case _: BSONJavaScriptWS =>
  // case BSONMinKey =>
  // case BSONMaxKey =>
  case _ => ??? // FIXME

given BSONWriter[JSON_RFC7159] with {

  def writeTry(obj: JSON_RFC7159): Try[BSONDocument] =
    sequenceTrys(obj.fields.map(e => toBSON(e._2).map(e2 => (e._1, e2)))).map(BSONDocument(_))
}
given BSONReader[JSON_RFC7159] with {
  def readTry(bson: BSONValue): Try[JSON_RFC7159] =
    bson.asTry[BSONDocument].flatMap { doc =>
      sequenceTrys(doc.toMap.toSeq.map(e => toJson(e._2).map(e2 => (e._1, e2)))).map(Json.Obj(_*))
    }
}

given BSONWriter[Json] with {
  def writeTry(obj: Json): Try[BSONValue] = toBSON(obj)
}
given BSONReader[Json] with {
  def readTry(bson: BSONValue): Try[Json] = toJson(bson)
}

given BSONDocumentWriter[AttachmentDataJWS] = Macros.writer[AttachmentDataJWS]
given BSONDocumentReader[AttachmentDataJWS] = Macros.reader[AttachmentDataJWS]
given BSONDocumentWriter[AttachmentDataLinks] = Macros.writer[AttachmentDataLinks]
given BSONDocumentReader[AttachmentDataLinks] = Macros.reader[AttachmentDataLinks]
given BSONDocumentWriter[AttachmentDataBase64] = Macros.writer[AttachmentDataBase64]
given BSONDocumentReader[AttachmentDataBase64] = Macros.reader[AttachmentDataBase64]
given BSONDocumentWriter[AttachmentDataJson] = Macros.writer[AttachmentDataJson]
given BSONDocumentReader[AttachmentDataJson] = Macros.reader[AttachmentDataJson]
given BSONDocumentWriter[AttachmentDataAny] = Macros.writer[AttachmentDataAny]
given BSONDocumentReader[AttachmentDataAny] = Macros.reader[AttachmentDataAny]
given BSONDocumentWriter[AttachmentData] = Macros.writer[AttachmentData]
given BSONDocumentReader[AttachmentData] = Macros.reader[AttachmentData]
given BSONDocumentWriter[Attachment] = Macros.writer[Attachment]
given BSONDocumentReader[Attachment] = Macros.reader[Attachment]

given BSONWriter[ReturnRoute] with {
  def writeTry(obj: ReturnRoute): Try[BSONValue] = Try(BSONString(obj.toString()))
}
given BSONReader[ReturnRoute] with {
  def readTry(bson: BSONValue): Try[ReturnRoute] = bson.asTry[String].map(v => ReturnRoute.valueOf(v))
}

given BSONDocumentWriter[L10nInline] = Macros.writer[L10nInline]
given BSONDocumentReader[L10nInline] = Macros.reader[L10nInline]

given BSONDocumentWriter[L10n] = Macros.writer[L10n]
given BSONDocumentReader[L10n] = Macros.reader[L10n]

given BSONWriter[SenderOrder] with {
  import SenderOrder.*
  def writeTry(obj: SenderOrder): Try[BSONInteger] = Try(BSONInteger(obj.value))
}
given BSONReader[SenderOrder] with {
  def readTry(bson: BSONValue): Try[SenderOrder] = bson.asTry[BSONInteger].flatMap(_.asInt.map(SenderOrder(_)))
}

given BSONWriter[SentCount] with {
  import SentCount.*
  def writeTry(obj: SentCount): Try[BSONInteger] = Try(BSONInteger(obj.value))
}
given BSONReader[SentCount] with {
  def readTry(bson: BSONValue): Try[SentCount] = bson.asTry[BSONInteger].flatMap(_.asInt.map(SentCount(_)))
}

given BSONDocumentWriter[ReceivedOrdersElement] = Macros.writer[ReceivedOrdersElement]
given BSONDocumentReader[ReceivedOrdersElement] = Macros.reader[ReceivedOrdersElement]

// fmgp.did.comm.extension.AdvancedSequencingpackage.SenderOrder

given BSONDocumentWriter[PlaintextMessage] with {
  val aux = Macros.writer[PlaintextMessageClass]
  override def writeTry(obj: PlaintextMessage): Try[BSONDocument] =
    obj match {
      case msg: PlaintextMessageClass => aux.writeTry(msg) // Success(msg): Try[reactivemongo.api.bson.BSONDocument]
      case _                          => Failure(RuntimeException("Only support PlaintextMessageClass"))
    }

}
given BSONDocumentReader[PlaintextMessage] with {
  val aux = Macros.reader[PlaintextMessageClass]
  override def readDocument(doc: BSONDocument): Try[PlaintextMessage] =
    aux.readDocument(doc)
}
