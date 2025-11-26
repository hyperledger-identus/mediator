{
  nodejs_24,
  cacert,
  webapp-node-modules,
  stdenv,
  sbt,
  callPackage,
}:

let
  dependencies = callPackage ./mediator-dependencies.nix { };
in
stdenv.mkDerivation {
  pname = "identus-mediator";
  version = "1.2.0-SNAPSHOT";

  src = ./../..;

  nativeBuildInputs = [
    nodejs_24
    sbt
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

    mkdir -p $SBT_DEPS/project
    cp -r ${dependencies}/project $SBT_DEPS
    chmod +w $SBT_DEPS/project/.boot

    runHook postConfigure
  '';

  buildPhase = ''
    runHook preBuild

    mkdir -p ./webapp/target/scala-3.6.4/scalajs-bundler/main
    cp -r ${webapp-node-modules}/* ./webapp/target/scala-3.6.4/scalajs-bundler/main
    sbt -debug mediator/compile

    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    touch $out

    runHook postInstall
  '';
}
