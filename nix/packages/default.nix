{
  pkgs,
  version,
}:

rec {
  mediator = pkgs.callPackage ./mediator { inherit version; };

  mediator-docker =
    if pkgs.stdenv.hostPlatform.system == "aarch64-darwin" then
      mediator-docker-linux-arm64
    else
      mediator-docker-linux-amd64;

  mediator-docker-latest = mediator-docker.override { tag = "latest"; };

  mediator-docker-linux-amd64 = pkgs.pkgsCross.gnu64.callPackage ./mediator-docker.nix {
    mediator = pkgs.pkgsCross.gnu64.callPackage ./mediator { inherit version; };
  };

  mediator-docker-linux-arm64 =
    pkgs.pkgsCross.aarch64-multiplatform.callPackage ./mediator-docker.nix
      {
        mediator = pkgs.pkgsCross.aarch64-multiplatform.callPackage ./mediator { inherit version; };
      };
}
