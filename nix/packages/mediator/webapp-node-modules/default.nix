{
  nodejs_24,
  buildNpmPackage,
}:

buildNpmPackage {
  pname = "webapp-node-modules";
  version = "latest";
  src = ./.;

  nodejs = nodejs_24;
  npmDepsHash = "sha256-0YcjHDgxoF5S9ZzufulA9KjI8S9ghoayJITQe/UVVFo=";

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
