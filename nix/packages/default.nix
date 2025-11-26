{
  pkgs,
}:

{
  mediator = pkgs.runCommand "mediator" { } ''
    echo "todo" > $out
  '';
}
