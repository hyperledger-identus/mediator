{
  bash,
  buildEnv,
  coreutils,
  curl,
  dockerTools,
  jdkCustom,
  mediator,
  tag ? mediator.version,
}:

let
  runtimeEnv = buildEnv {
    name = "mediator-runtime-env";
    paths = [
      coreutils
      bash
      curl
    ];
    pathsToLink = [
      "/bin"
      "/lib"
    ];
  };
in
dockerTools.buildLayeredImage {
  inherit tag;
  name = "hyperledgeridentus/identus-mediator";

  contents = [
    runtimeEnv
    mediator
  ];

  config = {
    WorkingDir = "/home/mediator";
    Cmd = [ "${mediator}/bin/mediator" ];
    ExposedPorts = {
      "8080/tcp" = { };
    };
    Env = [
      "JAVA_HOME=${jdkCustom}"
      "LANG=C.UTF-8"
      "LC_ALL=C.UTF-8"
    ];
  };
}
