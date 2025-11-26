{
  mkSbtDerivation,
  nodejs_24,
}:

mkSbtDerivation {
  pname = "identus-mediator";
  version = "1.2.0-SNAPSHOT";

  src = ./../..;

  nativeBuildInputs = [ nodejs_24 ];

  depsSha256 = "sha256-zEN+9a4aylhTOpl9bDUsXrZoYC/qp6ojOzj/8znCxYA=";

  depsWarmupCommand = ''
    # sbt -Dsbt.log.format=true --debug mediator/update
    sbt mediator/update
  '';

  buildPhase = ''
    sbt -Dsbt.log.format=true --debug mediator/compile
  '';

  installPhase = ''
    touch $out
  '';
}
