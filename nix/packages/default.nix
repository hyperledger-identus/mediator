{
  pkgs,
}:

rec {
  webapp-node-modules = pkgs.callPackage ./webapp-node-modules { };
  mediator = pkgs.callPackage ./mediator.nix { inherit webapp-node-modules; };
  mediator-dependencies = pkgs.callPackage ./mediator-dependencies.nix { };
}
