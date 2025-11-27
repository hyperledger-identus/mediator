{
  nodejs_24,
  cacert,
  stdenv,
  sbt,
  callPackage,
  makeWrapper,
  customJdk,
  lib,
}:

let
  webapp-node-modules = callPackage ./webapp-node-modules { };
  mediator-sbt-dependencies = callPackage ./mediator-sbt-dependencies.nix { };
in
stdenv.mkDerivation {
  pname = "identus-mediator";
  version = "1.2.0-SNAPSHOT";

  src = lib.cleanSourceWith {
    src = lib.cleanSource ./../../..;
    filter =
      path: type:
      let
        baseName = baseNameOf path;
      in
      !(baseName == "nix" || baseName == "docs" || baseName == ".git");
  };

  nativeBuildInputs = [
    nodejs_24
    sbt
    makeWrapper
  ];

  configurePhase = ''
    runHook preConfigure

    export HOME=$TMPDIR/home

    export SSL_CERT_FILE=${cacert}/etc/ssl/certs/ca-bundle.crt
    export NODE_EXTRA_CA_CERTS="${cacert}/etc/ssl/certs/ca-bundle.crt"
    export NODE_OPTIONS=--openssl-legacy-provider

    export SBT_DEPS=$TMPDIR/sbtdeps
    export SBT_OPTS="-Dsbt.global.base=$SBT_DEPS/project/.sbtboot -Dsbt.boot.directory=$SBT_DEPS/project/.boot -Dsbt.ivy.home=$SBT_DEPS/project/.ivy $SBT_OPTS"
    export COURSIER_CACHE=$SBT_DEPS/project/.coursier

    runHook postConfigure
  '';

  buildPhase = ''
    runHook preBuild

    # Setup SBT dependencies
    mkdir -p $SBT_DEPS/project/{.sbtboot,.boot,.ivy,.coursier}
    cp -r ${mediator-sbt-dependencies}/project $SBT_DEPS
    chmod -R u+w $SBT_DEPS/project

    # Setup node_modules and related files
    mkdir -p ./webapp/target/scala-3.6.4/scalajs-bundler/main
    cp -r ${webapp-node-modules}/* ./webapp/target/scala-3.6.4/scalajs-bundler/main
    chmod -R u+w ./webapp/target/scala-3.6.4/scalajs-bundler/main

    sbt mediator/stage

    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    mkdir -p $out
    cp -r mediator/target/universal/stage/* $out/
    wrapProgram $out/bin/mediator --set JAVA_HOME "${customJdk}"

    runHook postInstall
  '';
}
