#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export CI_TEST_MODE=true

source ./prepare-jdk.sh
source ./common_build.sh "${1:-}"

# default jdk 8, do build and test
switch_to_jdk 8
headInfo "test with Java: $JAVA_HOME"
# run junit test
runCmd ../library/scripts/run-junit.sh skipClean
runCmd ../tool/scripts/run-junit.sh skipClean

# test multi-version java home env
# shellcheck disable=SC2154
for jdk in "${ci_jdks[@]}"; do
    switch_to_jdk "$jdk"

    headInfo "test with Java: $JAVA_HOME"
    runCmd ../library/scripts/run-junit.sh skipClean
    runCmd ../tool/scripts/run-junit.sh skipClean
done
