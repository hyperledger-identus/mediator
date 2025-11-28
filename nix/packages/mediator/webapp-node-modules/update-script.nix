{
  writeShellApplication,
}:

writeShellApplication {
  name = "update-webapp-node-modules";

  runtimeInputs = [ ];

  text = ''
    # Script to update webapp-node-modules package.json and package-lock.json
    # This script should be run from the repository root

    if [ ! -f "build.sbt" ]; then
        echo "Error: This script must be run from the repository root (directory containing build.sbt)"
        exit 1
    fi

    REPO_ROOT="$(pwd)"
    NIX_DIR="$REPO_ROOT/nix/packages/mediator/webapp-node-modules"

    echo "Repository root: $REPO_ROOT"
    echo "Target directory: $NIX_DIR"

    # Run sbt compile to generate package.json
    echo "Running mediator/compile to generate package.json..."
    nix develop -c sbt clean mediator/compile

    # Check if files were generated
    GENERATED_DIR="$REPO_ROOT/webapp/target/scala-3.6.4/scalajs-bundler/main"
    if [ ! -f "$GENERATED_DIR/package.json" ]; then
        echo "Error: package.json was not generated at $GENERATED_DIR/package.json"
        exit 1
    fi

    if [ ! -f "$GENERATED_DIR/package-lock.json" ]; then
        echo "Error: package-lock.json was not generated at $GENERATED_DIR/package-lock.json"
        exit 1
    fi

    # Copy files to nix directory
    echo "Copying package.json and package-lock.json to $NIX_DIR..."
    cp "$GENERATED_DIR/package.json" "$NIX_DIR/package.json"
    cp "$GENERATED_DIR/package-lock.json" "$NIX_DIR/package-lock.json"

    echo ""
    echo "✓ Successfully updated webapp-node-modules package.json and package-lock.json"
    echo ""
    echo "Next steps:"
    echo "1. Review the changes: git diff $NIX_DIR"
    echo "2. Update the npmDepsHash in $NIX_DIR/default.nix"
    echo "   You can get the new hash by running:"
    echo "   nix build .#mediator 2>&1 | grep 'got:' | awk '{print \$2}'"
    echo "   Or set npmDepsHash to an empty string and run the build to get the correct hash."
    echo ""
  '';
}
