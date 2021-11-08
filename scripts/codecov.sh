#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./common_build.sh
source ./prepare-jdk.sh

switch_to_jdk 11

cd "$ROOT_PROJECT_DIR"
runCmd ./mvnw -Pgen-code-cov clean test jacoco:report coveralls:report
