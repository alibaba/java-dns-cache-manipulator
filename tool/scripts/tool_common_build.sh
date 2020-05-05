#!/bin/bash
[ -z "${_source_mark_of_tool_common_build:+dummy}" ] || return 0
_source_mark_of_tool_common_build=true

set -eEuo pipefail

readonly TOOL_PROJECT_SCRIPTS_DIR="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# shellcheck source=common.sh
source "$TOOL_PROJECT_SCRIPTS_DIR/../../scripts/common_build.sh"

#################################################################################
# project common info
#################################################################################

# set project root dir to PROJECT_ROOT_DIR var
readonly TOOL_PROJECT_DIR="$(readlink -f "$TOOL_PROJECT_SCRIPTS_DIR/..")"

readonly TOOL_PROJECT_VERSION=$(grep '<version>.*</version>' "$TOOL_PROJECT_DIR/pom.xml" | awk -F'</?version>' 'NR==1{print $2}')
readonly TOOL_PROJECT_AID=$(grep '<artifactId>.*</artifactId>' "$TOOL_PROJECT_DIR/pom.xml" | awk -F'</?artifactId>' 'NR==1{print $2}')

#################################################################################
# maven operation functions
#################################################################################

readonly tool_jar="$TOOL_PROJECT_DIR/target/$TOOL_PROJECT_AID-$TOOL_PROJECT_VERSION.jar"
readonly tool_dependencies_dir="$TOOL_PROJECT_DIR/target/dependency"

mvnCopyDependencies_Necessity() {
    [ ! -e "$tool_dependencies_dir" ]
}

mvnCompileTest_Necessity() {
    [ ! -e "$TOOL_PROJECT_DIR/target/test-classes/" -o "$TOOL_PROJECT_DIR/target/test-classes/" -ot src/ ]
}

mvnBuildJar_Necessity() {
    [ ! -e "$tool_jar" -o "$tool_jar" -ot "$TOOL_PROJECT_DIR/src/" ]
}

getClasspathOfDependencies() {
    mvnCopyDependencies 1>&2

    echo "$tool_dependencies_dir"/*.jar | tr ' ' :
}

getClasspathWithoutLibJar() {
    mvnCompileTest 1>&2

    echo "$TOOL_PROJECT_DIR/target/test-classes:$(getClasspathOfDependencies)"
}

getLibJarPath() {
    mvnBuildJar 1>&2

    echo "$tool_jar"
}

getClasspath() {
    local jdk_tools_jar="$JAVA_HOME/lib/tools.jar"
    if [ ! -e "$jdk_tools_jar" ]; then
        jdk_tools_jar=
    fi

    echo "${jdk_tools_jar:+$jdk_tools_jar:}$(getLibJarPath):$(getClasspathWithoutLibJar)"
}

getJUnitTestCases() {
    (
        mvnCompileTest 1>&2

        cd "$TOOL_PROJECT_DIR/target/test-classes" &&
            find . -iname '*Test.class' | sed '
                s%^\./%%
                s/\.class$//
                s%/%.%g
            '
    )
}
