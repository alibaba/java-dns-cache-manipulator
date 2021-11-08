#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export CI_TEST_MODE=true

source ./prepare-jdk.sh
source ./common_build.sh

# default jdk 11, do build and test
switch_to_jdk 11

if [ "${1:-}" != "skipClean" ]; then
    mvnClean

    mvnBuildJar

    mvnCopyDependencies
fi

# test multi-version java home env
# shellcheck disable=SC2154
for jhome in "${java_home_var_names[@]}"; do
    export JAVA_HOME=${!jhome}

    headInfo "test with Java: $JAVA_HOME"
    runCmd ../library/scripts/run-junit.sh skipClean
    runCmd ../tool/scripts/run-junit.sh skipClean
done
