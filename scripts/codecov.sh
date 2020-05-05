#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

source ./common_build.sh
source ./prepare-jdk.sh

switch_to_jdk 8

runCmd ./mvnw clean cobertura:cobertura coveralls:report
