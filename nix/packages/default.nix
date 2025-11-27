{
  pkgs,
  version,
}:

let
  mediator = pkgs.callPackage ./mediator { inherit version; };
  nativeDockerOutputs = pkgs.lib.optionalAttrs (!pkgs.stdenv.isDarwin) rec {
    mediator-docker = pkgs.callPackage ./mediator-docker.nix { inherit mediator; };
    mediator-docker-latest = mediator-docker.override { tag = "latest"; };
  };
  crossDockerOutputs =
    let
      mkCrossImage =
        platform:
        pkgs.pkgsCross.${platform}.callPackage ./mediator-docker.nix {
          mediator = pkgs.pkgsCross.gnu64.callPackage ./mediator { inherit version; };
        };
    in
    {
      mediator-docker-cross-linux-amd64 = mkCrossImage "gnu64";
      mediator-docker-cross-linux-arm64 = mkCrossImage "aarch64-multiplatform";
    };
in
{
  inherit mediator;
}
// nativeDockerOutputs
// crossDockerOutputs
