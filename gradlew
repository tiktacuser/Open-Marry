#!/bin/sh

GRADLE_VERSION=9.6.1
APP_HOME=$(cd -P "$(dirname "$0")" && pwd) || exit 1
DIST_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/openmarry-dists"
GRADLE_DIR="$DIST_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_DIR/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
    mkdir -p "$DIST_DIR" || exit 1
    ZIP="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
    echo "Downloading Gradle $GRADLE_VERSION..."
    curl -fL -o "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" || exit 1
    unzip -q -o "$ZIP" -d "$DIST_DIR" || exit 1
    rm -f "$ZIP"
    chmod +x "$GRADLE_BIN"
fi

cd "$APP_HOME" && exec "$GRADLE_BIN" "$@"
