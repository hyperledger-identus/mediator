package org.hyperledger.identus.mediator

import zio.test.*

object DataBaseConfigSpec extends ZIOSpecDefault {

  private def config(connectionString: Option[String], port: Option[String]) =
    DataBaseConfig(connectionString, "mongodb", "localhost", port, "admin", "secret", "mediator")

  override def spec = suite("DataBaseConfigSpec")(
    test("an explicit connection string takes precedence and masks credentials for display") {
      val scheme = "mongodb"
      val user = "alice"
      val password = "password"
      val endpoint = "example.org:27017/custom"
      val connection = s"$scheme://$user:$password@$endpoint"
      val value = config(Some(connection), Some("27018"))
      assertTrue(
        value.finalConnectionString == connection,
        value.displayConnectionString == s"$scheme://***:***@$endpoint"
      )
    },
    test("a connection string is built from components with a port") {
      val value = config(None, Some("27017"))
      assertTrue(
        value.finalConnectionString.endsWith("admin:secret@localhost:27017/mediator"),
        value.displayConnectionString.endsWith("admin:******@localhost:27017/mediator")
      )
    },
    test("an absent or empty port is omitted from the connection string") {
      val absent = config(None, None)
      val empty = config(None, Some(""))
      assertTrue(
        absent.finalConnectionString.endsWith("admin:secret@localhost/mediator"),
        empty.finalConnectionString == absent.finalConnectionString,
        absent.displayConnectionString.endsWith("admin:******@localhost/mediator")
      )
    }
  )
}
