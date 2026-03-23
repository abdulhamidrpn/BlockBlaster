#!/usr/bin/env sh
##############################################################################
## Gradle start up script for UN*X
##############################################################################
set -e
DIRNAME=$(cd "$(dirname "$0")" && pwd)
APP_BASE_NAME=$(basename "$0")
APP_HOME="$DIRNAME"
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Use JAVA_HOME if set
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
