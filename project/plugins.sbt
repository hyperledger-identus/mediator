val scalaJSVersion = sys.env.getOrElse("SCALAJS_VERSION", "1.20.1")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")

libraryDependencies += "org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0"

// addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.1.0") //we can now update 1.2.0!
// [error] (update) found version conflict(s) in library dependencies; some are suspected to be binary incompatible:
// [error]
// [error] 	* org.scala-lang.modules:scala-java8-compat_2.12:1.0.0 (early-semver) is selected over 0.8.0
// [error] 	    +- org.scalablytyped.converter:sbt-converter:1.0.0-beta34 (sbtVersion=1.0, scalaVersion=2.12) (depends on 1.0.0)
// [error] 	    +- com.typesafe.akka:akka-actor_2.12:2.5.17           (depends on 0.8.0)

/** scalajs-bundler https://scalacenter.github.io/scalajs-bundler/getting-started.html
  * enablePlugins(ScalaJSBundlerPlugin)
  *
  * You need to have npm installed on your system.
  */
//addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.21.1")

// GRPC
//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
//addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.19")
//libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.12"
////https://mvnrepository.com/artifact/com.thesamet.scalapb.grpcweb/scalapb-grpcweb
//libraryDependencies += "com.thesamet.scalapb.grpcweb" %% "scalapb-grpcweb-code-gen" % "0.6.4"

//https://scalablytyped.org/docs/plugin
//https://github.com/ScalablyTyped/Converter/releases
resolvers += Resolver.bintrayRepo("oyvindberg", "converter")
resolvers += MavenRepository("sonatype-s01-snapshots", "https://s01.oss.sonatype.org/content/repositories/snapshots")
addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta44")

// Utils Buildinfo
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")

// CI
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4") // sbt> dependencyUpdates

// TEST COVERAGE
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.3.1") // Needs scala version 3.3.0

// Revolver use for command 'reStart' (like the command 'run' but run on the backgroun by forking the app from sbt)
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

// https://zio.dev/howto/migrate/zio-2.x-migration-guide%20v0.9.31
//sbt "scalafixEnable; scalafixAll github:zio/zio/Zio2Upgrade?sha=series/2.x"
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.12.1")

// Deploy demo
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.2.0")
addSbtPlugin("com.github.sbt" % "sbt-gzip" % "2.0.0")

// Release
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")

// Github Packages
if (sys.env.get("GITHUB_TOKEN").isDefined) {
  // The reason for this is that the plugin needs the variable to be defined. We don't want to have that requirement.
  libraryDependencies += {
    val log = sLog.value
    log.info(s"Adding plugin sbt-dependency-tree since env GITHUB_TOKEN is defined.")

    val dependency = "com.codecommit" % "sbt-github-packages" % "0.5.3"
    val sbtV = (pluginCrossBuild / sbtBinaryVersion).value
    val scalaV = (update / scalaBinaryVersion).value
    Defaults.sbtPluginExtra(dependency, sbtV, scalaV)
  }
} else libraryDependencies ++= Seq[ModuleID]()

// PUBLISH
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.9.0")

// Native Packager
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")
