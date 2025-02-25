export default {
    branches: [
        'main',
        '+([0-9])?(.{+([0-9]),x}).x',
        { name: 'beta/*', prerelease: 'rc' }
    ],
    plugins: [
        ['@semantic-release/commit-analyzer', {
            "preset": "conventionalcommits"
        }],
        '@semantic-release/release-notes-generator',
        ["@semantic-release/changelog", {
            "changelogFile": "CHANGELOG.md"
        }],
        ["@semantic-release/exec", {
            "prepareCmd": "echo ${nextRelease.version} > .release-version"
        }],
        ["@semantic-release/exec", {
            "prepareCmd": "npm version ${nextRelease.version} --git-tag-version false"
        }],
        ["@semantic-release/exec", {
            "prepareCmd": "NODE_OPTIONS=--openssl-legacy-provider sbt -J-Xmx5120m  \"release release-version ${nextRelease.version} with-defaults\""
        }],
        ["@semantic-release/exec", {
            "prepareCmd": "docker buildx build --platform=linux/arm64,linux/amd64 --push -t docker.io/hyperledgeridentus/identus-mediator:${nextRelease.version} ./mediator/target/docker/stage"
        }],
        ["@semantic-release/git", {
            "assets": [
                "version.sbt",
                "CHANGELOG.md",
                "DEPENDENCIES.md",
                "package.json",
                "package-lock.json",
            ],
            "message": "chore(release): cut mediator ${nextRelease.version} release\n\n${nextRelease.notes}\n\nSigned-off-by: Hyperledger Bot <hyperledger-bot@hyperledger.org>"
        }],
        ["semantic-release-slack-bot", {
            "notifyOnSuccess": true,
            "notifyOnFail": true,
            "markdownReleaseNotes": true,
            "onSuccessTemplate": {
                "text": "A new version of Identus Mediator successfully released!\nVersion: `$npm_package_version`\nTag: $repo_url/releases/tag/v$npm_package_version\n\nRelease notes:\n$release_notes"
            }
        }]
    ],
    tagFormat: "v${version}"
}
