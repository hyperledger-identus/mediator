{
  nodejs_24,
  buildNpmPackage,
}:

buildNpmPackage {
  version = "1.0.0";
  pname = "scalajs-node-modules";
  src = ./.;

  nodejs = nodejs_24;
  npmDepsHash = "sha256-Vn3KeIxQhaKn+7nR0vfqWZeNZNLBa0k9NEUrmQ+Oh4I=";

  dontNpmBuild = true;

  installPhase = ''
    runHook preInstall

    mkdir -p $out
    cp -r node_modules $out/
    cp package.json $out/package.json
    cp package-lock.json $out/package-lock.json

    runHook postInstall
  '';
}
