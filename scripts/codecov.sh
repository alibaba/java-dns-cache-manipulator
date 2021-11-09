#!/bin/bash
set -eEuo pipefail
cd "$(dirname "$(readlink -f "$0")")"

export DCM_AGENT_SUPRESS_EXCEPTION_STACK=true

source ./common_build.sh
source ./prepare-jdk.sh

cd "$ROOT_PROJECT_DIR"

switch_to_jdk 11
MVN_CMD -Pgen-code-cov clean test jacoco:report

switch_to_jdk 8
# use -Dmaven.main.skip option fix below problem of jacoco-maven-plugin:report :
#
# [WARNING] Classes in bundle 'Java Dns Cache Manipulator(DCM) Lib' do not match with execution data.
#           For report generation the same class files must be used as at runtime.
# [WARNING] Execution data for class com/alibaba/xxx/Yyy does not match.
MVN_CMD -Pgen-code-cov -Dmaven.main.skip test jacoco:report coveralls:report
