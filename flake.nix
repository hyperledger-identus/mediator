{
  description = "Identus Mediator - A DID Comm v2 mediator service";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
    sbt-derivation = {
      url = "github:zaninime/sbt-derivation";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
      sbt-derivation,
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
                jdk17 = pkgs.javaPackages.compiler.openjdk17;
                sbt = prev.sbt.override { jre = jdk17; };
                mkSbtDerivation = sbtOptions: sbt-derivation.lib.mkSbtDerivation ({ pkgs = final; } // sbtOptions);
              })
            ];
          };
        in
        {
          packages = import ./nix/packages { inherit pkgs; };
          devShells = import ./nix/devShells { inherit pkgs; };
        }
      );
}
