#!/bin/bash
[ -z "${_source_mark_of_common_build:+dummy}" ] || return 0
_source_mark_of_common_build=true

set -eEuo pipefail

readonly ROOT_SCRIPTS_DIR="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# shellcheck source=common.sh
source "$ROOT_SCRIPTS_DIR/common.sh"

#################################################################################
# root project common info
#################################################################################

# set project root dir to PROJECT_ROOT_DIR var
readonly ROOT_PROJECT_DIR="$(readlink -f "$ROOT_SCRIPTS_DIR/..")"

readonly ROOT_PROJECT_VERSION=$(grep '<version>.*</version>' "$ROOT_PROJECT_DIR/pom.xml" | awk -F'</?version>' 'NR==1{print $2}')
readonly ROOT_PROJECT_AID=$(grep '<artifactId>.*</artifactId>' "$ROOT_PROJECT_DIR/pom.xml" | awk -F'</?artifactId>' 'NR==1{print $2}')

# set env variable ENABLE_JAVA_RUN_DEBUG to enable java debug mode
readonly -a JAVA_CMD=(
    "$JAVA_HOME/bin/java" -Xmx128m -Xms128m -server -ea -Duser.language=en -Duser.country=US
    ${ENABLE_JAVA_RUN_VERBOSE_CLASS+ -verbose:class }
    ${ENABLE_JAVA_RUN_DEBUG+ -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 }
)

readonly -a MVN_CMD=(
    "$ROOT_PROJECT_DIR/mvnw" -V
)

#################################################################################
# maven operation functions
#################################################################################

mvnClean() {
    checkNecessityForCallerFunction || return 0

    (
        cd "$ROOT_PROJECT_DIR"
        runCmd "${MVN_CMD[@]}" clean || die "fail to mvn clean!"
    )
}

mvnBuildJar() {
    checkNecessityForCallerFunction || return 0

    (
        cd "$ROOT_PROJECT_DIR"
        if [ -n "${CI_TEST_MODE+YES}" ]; then
            # Build jar action should have used package instead of install
            # here use install intended to check release operations.
            #
            # De-activate a maven profile from command line
            # https://stackoverflow.com/questions/25201430
            runCmd "${MVN_CMD[@]}" install -DperformRelease -P '!gen-sign' || die "fail to build jar!"
        else
            runCmd "${MVN_CMD[@]}" package -Dmaven.test.skip=true || die "fail to build jar!"
        fi
    )
}

mvnCompileTest() {
    checkNecessityForCallerFunction || return 0

    (
        cd "$ROOT_PROJECT_DIR"
        runCmd "${MVN_CMD[@]}" test-compile || die "fail to compile test!"
    )
}

mvnCopyDependencies() {
    checkNecessityForCallerFunction || return 0

    (
        cd "$ROOT_PROJECT_DIR"
        # https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html
        runCmd "${MVN_CMD[@]}" dependency:copy-dependencies -DincludeScope=test -DexcludeArtifactIds=jsr305,spotbugs-annotations || die "fail to mvn copy-dependencies!"
    )
}

#################################################################################
# maven actions
#################################################################################

if [ "${1:-}" != "skipClean" ]; then
    mvnClean
fi
