{
  pkgs,
}:

rec {
  mediator = pkgs.callPackage ./mediator { };

  mediator-docker =
    if pkgs.stdenv.hostPlatform.system == "aarch64-darwin" then
      mediator-docker-linux-arm64
    else
      mediator-docker-linux-amd64;

  mediator-docker-linux-amd64 = pkgs.pkgsCross.gnu64.callPackage ./mediator-docker.nix {
    mediator = pkgs.pkgsCross.gnu64.callPackage ./mediator { };
  };

  mediator-docker-linux-arm64 =
    pkgs.pkgsCross.aarch64-multiplatform.callPackage ./mediator-docker.nix
      {
        mediator = pkgs.pkgsCross.aarch64-multiplatform.callPackage ./mediator { };
      };
}
