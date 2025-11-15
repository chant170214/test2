#!/bin/sh

DIR="$(cd "$(dirname "$0")" && pwd)"
GRADLE_WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
  echo "Gradle wrapper JAR not found. Please generate the wrapper." >&2
  exit 1
fi

exec java -jar "$GRADLE_WRAPPER_JAR" "$@"
