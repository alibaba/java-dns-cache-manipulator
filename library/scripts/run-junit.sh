#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./lib_common_build.sh

class_path="$(getClasspath)"
junit_test_cases="$(getJUnitTestCases)"
# shellcheck disable=SC2086
runCmd "${JAVA_CMD[@]}" -cp "$class_path" \
    org.junit.runner.JUnitCore $junit_test_cases
