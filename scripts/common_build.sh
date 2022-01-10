#!/bin/bash
set -eEuo pipefail

[ -z "${__source_guard_E2EB46EC_DEB8_4818_8D4E_F425BDF4A275:+dummy}" ] || return 0
__source_guard_E2EB46EC_DEB8_4818_8D4E_F425BDF4A275="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")"

# shellcheck source=common.sh
source "$__source_guard_E2EB46EC_DEB8_4818_8D4E_F425BDF4A275/common.sh"

#################################################################################
# root project common info
#################################################################################

# set project root dir to PROJECT_ROOT_DIR var
readonly ROOT_PROJECT_DIR="$(readlink -f "$__source_guard_E2EB46EC_DEB8_4818_8D4E_F425BDF4A275/..")"

readonly ROOT_PROJECT_VERSION=$(grep '<version>.*</version>' "$ROOT_PROJECT_DIR/pom.xml" | awk -F'</?version>' 'NR==1{print $2}')
readonly ROOT_PROJECT_AID=$(grep '<artifactId>.*</artifactId>' "$ROOT_PROJECT_DIR/pom.xml" | awk -F'</?artifactId>' 'NR==1{print $2}')

#################################################################################
# java operation functions
#################################################################################

__getJavaVersion() {
    "$JAVA_HOME/bin/java" -version 2>&1 | awk -F\" '/ version "/{print $2}'
}

# set env variable ENABLE_JAVA_RUN_DEBUG to enable java debug mode

JAVA_CMD() {
    local additionalOptionsForJava12Plus
    versionGreatThanEq $(__getJavaVersion) 12 && additionalOptionsForJava12Plus=(
        --add-opens java.base/java.net=ALL-UNNAMED
        --add-opens java.base/sun.net=ALL-UNNAMED
    )

    logAndRun "$JAVA_HOME/bin/java" -Xmx128m -Xms128m -server -ea -Duser.language=en -Duser.country=US \
    ${ENABLE_JAVA_RUN_VERBOSE_CLASS+ -verbose:class} \
    ${ENABLE_JAVA_RUN_DEBUG+ -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005} \
    ${additionalOptionsForJava12Plus[@]:+"${additionalOptionsForJava12Plus[@]}"} \
    "$@"
}

#################################################################################
# maven operation functions
#################################################################################


MVN_CMD() {
    logAndRun "$ROOT_PROJECT_DIR/mvnw" -V --no-transfer-progress "$@"
}

mvnClean() {
    checkNecessityForCallerFunction || return 0

    (
        cd "$ROOT_PROJECT_DIR"
        MVN_CMD clean || die "fail to mvn clean!"
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
            MVN_CMD install -DperformRelease -P '!gen-sign' || die "fail to build jar!"
        else
            MVN_CMD install -Dmaven.test.skip=true || die "fail to build jar!"
        fi
    )
}

mvnCompileTest() {
    checkNecessityForCallerFunction || return 0

    (
        cd "$ROOT_PROJECT_DIR"
        MVN_CMD test-compile || die "fail to compile test!"
    )
}

mvnCopyDependencies() {
    checkNecessityForCallerFunction || return 0

    (
        cd "$ROOT_PROJECT_DIR"
        # https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html
        MVN_CMD dependency:copy-dependencies -DincludeScope=test -DexcludeArtifactIds=jsr305,spotbugs-annotations || die "fail to mvn copy-dependencies!"
    )
}

extractFirstElementValueFromPom() {
    (($# == 2)) || die "${FUNCNAME[0]} need only 2 arguments, actual arguments: $*"

    local element=$1
    local pom_file=$2
    grep \<"$element"'>.*</'"$element"\> "$pom_file" | awk -F'</?'"$element"\> 'NR==1 {print $2}'
}
