# Nix Packages

This directory contains Nix package definitions for the Identus Mediator.

## Outputs

### Packages

- **`mediator`** - Main mediator application built with Scala/sbt. Includes webapp frontend compiled via Scala.js. The build uses cached sbt dependencies and npm modules for reproducibility.

- **`mediator-docker`** - Docker image containing the mediator application with minimal runtime dependencies (coreutils, bash, curl). Tagged with the package version by default.

- **`mediator-docker-latest`** - Same as `mediator-docker` but tagged as `latest`.

- **`mediator-docker-cross-linux-amd64`** - Cross-compiled Docker image for Linux AMD64.

- **`mediator-docker-cross-linux-arm64`** - Cross-compiled Docker image for Linux ARM64.

**Note:** Docker images and cross-compilation are only available on Linux hosts.

## Usage

```bash
# Build the mediator application
nix build .#mediator

# Build Docker image (Linux only)
nix build .#mediator-docker

# Build cross-platform Docker images (Linux only)
# These create Docker images for specific architectures regardless of your host architecture
nix build .#mediator-docker-cross-linux-amd64
nix build .#mediator-docker-cross-linux-arm64

# Build for different system (requires remote or emulated builder for cross-building)
nix build .#mediator --system x86_64-linux
nix build .#mediator --system aarch64-linux

# Load Docker image
docker load < result

# Enter development shell
nix develop
```

## Limitations

### Building on macOS

Docker images cannot be built natively on macOS, and cross-compilation is not yet supported in this repository yet.
To build Docker images or Linux-specific packages from macOS, you need to configure a Linux remote builder.

Nix will automatically delegate builds to the remote builder when you specify a different system architecture:

For setup instructions, see the [official Nix documentation on distributed builds](https://nixos.org/manual/nix/stable/advanced-topics/distributed-builds.html).
