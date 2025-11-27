{
  pkgs,
}:

rec {
  mediator = pkgs.callPackage ./mediator { };
  mediator-docker = pkgs.callPackage ./mediator-docker.nix { inherit mediator; };
}
