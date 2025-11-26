{
  pkgs,
}:

{
  scalajs-node-modules = pkgs.callPackage ./scalajs-node-modules { };
  mediator = pkgs.callPackage ./mediator.nix { };
}
