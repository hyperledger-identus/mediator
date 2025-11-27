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
in
{
  default = pkgs.mkShell {
    packages = with pkgs; [
      customJdk
      docker
      git
      nodejs_24
      openssl
      sbt
      # custom scripts
      format-nix
    ];

    # envs
    NODE_OPTIONS = "--openssl-legacy-provider";
  };
}
