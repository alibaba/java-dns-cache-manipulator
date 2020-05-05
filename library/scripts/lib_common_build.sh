#!/bin/bash
[ -z "${_source_mark_of_lib_common_build:+dummy}" ] || return 0
_source_mark_of_lib_common_build=true

set -eEuo pipefail

readonly LIB_PROJECT_SCRIPTS_DIR="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# shellcheck source=common.sh
source "$LIB_PROJECT_SCRIPTS_DIR/../../scripts/common_build.sh"

#################################################################################
# project common info
#################################################################################

# set project root dir to PROJECT_ROOT_DIR var
readonly LIB_PROJECT_DIR="$(readlink -f "$LIB_PROJECT_SCRIPTS_DIR/..")"

readonly LIB_PROJECT_VERSION=$(grep '<version>.*</version>' "$LIB_PROJECT_DIR/pom.xml" | awk -F'</?version>' 'NR==1{print $2}')
readonly LIB_PROJECT_AID=$(grep '<artifactId>.*</artifactId>' "$LIB_PROJECT_DIR/pom.xml" | awk -F'</?artifactId>' 'NR==1{print $2}')

#################################################################################
# maven operation functions
#################################################################################

readonly lib_jar="$LIB_PROJECT_DIR/target/$LIB_PROJECT_AID-$LIB_PROJECT_VERSION.jar"
readonly lib_dependencies_dir="$LIB_PROJECT_DIR/target/dependency"

mvnCopyDependencies_Necessity() {
    [ ! -e "$lib_dependencies_dir" ]
}

mvnCompileTest_Necessity() {
    [ ! -e "$LIB_PROJECT_DIR/target/test-classes/" -o "$LIB_PROJECT_DIR/target/test-classes/" -ot src/ ]
}

mvnBuildJar_Necessity() {
    [ ! -e "$lib_jar" -o "$lib_jar" -ot "$LIB_PROJECT_DIR/src/" ]
}

getClasspathOfDependencies() {
    mvnCopyDependencies 1>&2

    echo "$lib_dependencies_dir"/*.jar | tr ' ' :
}

getClasspathWithoutLibJar() {
    mvnCompileTest 1>&2

    echo "$LIB_PROJECT_DIR/target/test-classes:$(getClasspathOfDependencies)"
}

getLibJarPath() {
    mvnBuildJar 1>&2

    echo "$lib_jar"
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

        cd "$LIB_PROJECT_DIR/target/test-classes" &&
            find . -iname '*Test.class' | sed '
                s%^\./%%
                s/\.class$//
                s%/%.%g
            '
    )
}
