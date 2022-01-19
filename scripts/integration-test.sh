#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export CI_TEST_MODE=true
export DCM_AGENT_SUPRESS_EXCEPTION_STACK=true

source ./prepare_jdk.sh
source ./common_build.sh

# default jdk 11, do build and test
switch_to_jdk 11

if [ "${1:-}" != "skipClean" ]; then
    MVN_CMD clean
fi

# Build jar action should have used package instead of install
# here use install intended to check release operations.
#
# De-activate a maven profile from command line
# https://stackoverflow.com/questions/25201430
headInfo "test with Java: $JAVA_HOME"
MVN_CMD -DperformRelease -P '!gen-sign' install

# test multi-version java home env
# shellcheck disable=SC2154
for jhome in "${java_home_var_names[@]}"; do
    # already tested by above `mvn install`
    [ "JDK11_HOME" = "$jhome" ] && continue

    export JAVA_HOME=${!jhome}

    headInfo "test with Java: $JAVA_HOME"
    MVN_CMD surefire:test
done
