{
  description = "Identus Mediator - A DID Comm v2 mediator service";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachSystem
      [
        "x86_64-linux"
        "aarch64-darwin"
      ]
      (
        system:
        let
          pkgs = import nixpkgs {
            inherit system;
            config.allowUnfree = true;
            overlays = [
              (final: prev: rec {
                jdkCustom =
                  if prev.stdenv.isLinux then
                    prev.javaPackages.compiler.openjdk17-bootstrap.override { gtkSupport = false; }
                  else if prev.stdenv.isDarwin then
                    prev.javaPackages.compiler.openjdk17-bootstrap.override { swingSupport = false; }
                  else
                    prev.javaPackages.compiler.openjdk17-bootstrap;
                sbt = prev.sbt.override { jre = jdkCustom; };
              })
            ];
          };
          version = self.shortRev or self.dirtyShortRev or "dev";
        in
        {
          packages = import ./nix/packages { inherit pkgs version; };
          devShells = import ./nix/devShells { inherit pkgs; };
          checks = {
            inherit (self.packages.${system}) mediator mediator-docker;
          };
        }
      );
}
