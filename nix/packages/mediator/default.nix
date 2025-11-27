{
  cacert,
  callPackage,
  gawk,
  jdkCustom,
  lib,
  makeWrapper,
  nodejs_24,
  sbt,
  stdenv,
  writeShellScriptBin,
}:

let
  webapp-node-modules = callPackage ./webapp-node-modules { };
  mediator-sbt-dependencies = callPackage ./mediator-sbt-dependencies.nix { };
  version = lib.removeSuffix "\n" (builtins.readFile ./version);
in
stdenv.mkDerivation {
  pname = "identus-mediator";
  inherit version;

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
    wrapProgram $out/bin/mediator \
      --set JAVA_HOME "${jdkCustom}" \
      --prefix PATH : ${gawk}/bin

    runHook postInstall
  '';

  passthru = {
    updateVersion = writeShellScriptBin "update-mediator-version" ''
      set -euo pipefail

      # Determine project root
      if [ -f "build.sbt" ]; then
        PROJECT_ROOT="$(pwd)"
      else
        echo "Error: Must run from project root (where build.sbt is located)" >&2
        exit 1
      fi

      VERSION_FILE="$PROJECT_ROOT/nix/packages/mediator/version"

      echo "Fetching version from sbt..."

      # Set up Java environment for sbt
      export JAVA_HOME="${jdkCustom}"
      export PATH="${jdkCustom}/bin:${sbt}/bin:$PATH"

      # Run sbt and extract version, removing ANSI codes and extra whitespace
      VERSION=$(sbt -no-colors 'print mediator/version' 2>&1 | grep -v '^\[' | tail -n1 | tr -d '[:space:]')

      if [ -z "$VERSION" ]; then
        echo "Error: Failed to get version from sbt" >&2
        exit 1
      fi

      echo "Updating version file to: $VERSION"
      echo "$VERSION" > "$VERSION_FILE"
      echo "Version file updated successfully at: $VERSION_FILE"
    '';
  };
}
