{
  nodejs_24,
  stdenv,
  sbt,
  cacert,
  gnused,
  findutils,
  file,
  strip-nondeterminism,
}:

stdenv.mkDerivation {
  pname = "identus-mediator-dependencies";
  version = "latest";

  src = ./../../..;

  outputHashMode = "recursive";
  outputHashAlgo = "sha256";
  outputHash = "sha256-MaDmMdBX4zeZQRmx6rR+/L2gGZ137HpVKxVrlL/4ukg=";

  nativeBuildInputs = [
    sbt
    nodejs_24
    gnused
    findutils
    file
    strip-nondeterminism
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
    mkdir -p $SBT_DEPS/project/{.sbtboot,.boot,.ivy,.coursier}

    runHook postConfigure
  '';

  buildPhase = ''
    runHook preBuild

    sbt mediator/compile

    echo "stripping out comments containing dates"
    find $SBT_DEPS/project -name '*.properties' -type f -exec sed -i '/^#/d' {} \;

    echo "removing non-reproducible accessory files"
    find $SBT_DEPS/project -name '*.lock' -type f -print0 | xargs -r0 rm -rfv
    find $SBT_DEPS/project -name '*.log' -type f -print0 | xargs -r0 rm -rfv

    echo "fixing-up the compiler bridge and interface"
    find $SBT_DEPS/project -name 'org.scala-sbt-compiler-bridge_*' -type f -print0 | xargs -r0 strip-nondeterminism
    find $SBT_DEPS/project -name 'org.scala-sbt-compiler-interface_*' -type f -print0 | xargs -r0 strip-nondeterminism

    echo "removing runtime jar"
    find $SBT_DEPS/project -name rt.jar -delete

    echo "removing empty directories"
    find $SBT_DEPS/project -type d -empty -delete

    echo "removing scalablytyped"
    rm -rf $SBT_DEPS/project/.ivy/local/org.scalablytyped

    runHook postBuild
  '';

  installPhase = ''
    runHook preInstall

    mkdir -p $out
    cp -r $SBT_DEPS/project $out/project

    runHook postInstall
  '';
}
