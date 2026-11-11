package org.hyperledger.identus.mediator

import fmgp.crypto.Curve
import zio.ConfigProvider
import zio.test.*

object CurveConfigSpec extends ZIOSpecDefault {

  private def provider(value: String) = ConfigProvider.fromMap(Map("curve" -> value))

  override def spec = suite("CurveConfigSpec")(
    test("OKP curves load by their configured names") {
      for {
        agreement <- provider("X25519").load(CurveConfig.okpCurveConfig.nested("curve"))
        authentication <- provider("Ed25519").load(CurveConfig.okpCurveConfig.nested("curve"))
        invalid <- provider("unknown").load(CurveConfig.okpCurveConfig.nested("curve")).either
      } yield assertTrue(
        agreement == Curve.X25519,
        authentication == Curve.Ed25519,
        invalid.isLeft
      )
    },
    test("EC curves load by their configured names") {
      for {
        p256 <- provider("P-256").load(CurveConfig.ecCurveConfig.nested("curve"))
        p384 <- provider("P-384").load(CurveConfig.ecCurveConfig.nested("curve"))
        p521 <- provider("P-521").load(CurveConfig.ecCurveConfig.nested("curve"))
        secp256k1 <- provider("secp256k1").load(CurveConfig.ecCurveConfig.nested("curve"))
        invalid <- provider("unknown").load(CurveConfig.ecCurveConfig.nested("curve")).either
      } yield assertTrue(
        p256 == Curve.`P-256`,
        p384 == Curve.`P-384`,
        p521 == Curve.`P-521`,
        secp256k1 == Curve.secp256k1,
        invalid.isLeft
      )
    }
  )
}
