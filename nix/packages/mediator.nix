{
  mkSbtDerivation,
  nodejs_24,
  cacert,
  webapp-node-modules,
}:

mkSbtDerivation {
  pname = "identus-mediator";
  version = "1.2.0-SNAPSHOT";

  src = ./../..;

  nativeBuildInputs = [ nodejs_24 ];

  depsSha256 = "";

  depsWarmupCommand = ''
    export HOME=$TMPDIR/home
    export SSL_CERT_FILE=${cacert}/etc/ssl/certs/ca-bundle.crt
    export NODE_EXTRA_CA_CERTS=${cacert}/etc/ssl/certs/ca-bundle.crt
    export NODE_OPTIONS=--openssl-legacy-provider

    sbt webapp/update
    sbt mediator/update
  '';

  buildPhase = ''
    export HOME=$TMPDIR/home
    export SSL_CERT_FILE=${cacert}/etc/ssl/certs/ca-bundle.crt
    export NODE_EXTRA_CA_CERTS=${cacert}/etc/ssl/certs/ca-bundle.crt
    export NODE_OPTIONS=--openssl-legacy-provider

    mkdir -p ./webapp/target/scala-3.6.4/scalajs-bundler/main
    cp -r ${webapp-node-modules}/* ./webapp/target/scala-3.6.4/scalajs-bundler/main
    ls -aoh ./webapp/target/scala-3.6.4/scalajs-bundler/main
    pwd
    ls -aoh

    sbt mediator/compile
  '';

  installPhase = ''
    touch $out
  '';
}
