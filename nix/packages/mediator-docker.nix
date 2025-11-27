{
  pkgs,
  dockerTools,
  mediator,
  jdkCustom,
  coreutils,
  bash,
  curl,
}:

let
  runtimeEnv = pkgs.buildEnv {
    name = "mediator-runtime-env";
    paths = [
      jdkCustom
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
  name = "hyperledgeridentus/identus-mediator";
  tag = "latest";

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
    ];
    Healthcheck = {
      Test = [
        "CMD"
        "curl"
        "--fail"
        "http://localhost:8080/health"
      ];
      Interval = 10000000000; # 10 seconds in nanoseconds
      Timeout = 5000000000; # 5 seconds in nanoseconds
      Retries = 10;
    };
  };
}
