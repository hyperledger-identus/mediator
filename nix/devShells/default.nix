{
  pkgs,
}:
let
  format-nix = pkgs.writeShellApplication {
    name = "format-nix";
    runtimeInputs = with pkgs; [
      findutils
      nixfmt-rfc-style
    ];
    text = ''
      find . -name "*.nix" -type f -exec nixfmt {} +
    '';
  };
  jdk17 = pkgs.javaPackages.compiler.openjdk17;
in
{
  default = pkgs.mkShell {
    packages = with pkgs; [
      docker
      git
      jdk17
      nodejs_24
      openssl
      (sbt.override { jre = jdk17; })
      # custom scripts
      format-nix
    ];

    # envs
    NODE_OPTIONS = "--openssl-legacy-provider";
  };
}
