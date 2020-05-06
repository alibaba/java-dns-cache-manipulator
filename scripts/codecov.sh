#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./common_build.sh skipClean
source ./prepare-jdk.sh

switch_to_jdk 8

cd "$ROOT_PROJECT_DIR"
runCmd ./mvnw -Pgen-code-cov clean package cobertura:cobertura coveralls:report
