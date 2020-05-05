#!/bin/bash
# SDKMAN! with Travis
# https://objectcomputing.com/news/2019/01/07/sdkman-travis
set -eEuo pipefail

[ -z "${_source_mark_of_prepare_jdk:+dummy}" ] || return 0
export _source_mark_of_prepare_jdk=true

# shellcheck source=common.sh
source "$(dirname "$(readlink -f "$BASH_SOURCE")")/common.sh"

if [ ! -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    [ -d "$HOME/.sdkman" ] && rm -rf "$HOME/.sdkman"
    curl -s get.sdkman.io | bash || die "fail to install sdkman"
    echo sdkman_auto_answer=true >>"$HOME/.sdkman/etc/config"
fi
set +u
# shellcheck disable=SC1090
source "$HOME/.sdkman/bin/sdkman-init.sh"
set -u

jdks_install_by_sdkman=(
    7.0.262-zulu
    8.0.252-zulu

#    9.0.7-zulu
#    10.0.2-zulu
#
#    11.0.7-zulu
#    12.0.2-open
#    13.0.3-zulu
#    14.0.1-zulu
#    15.ea.20-open
)

ci_jdks=(
    6
    "${jdks_install_by_sdkman[@]}"
)

JDK6_HOME="${JDK6_HOME:-/usr/lib/jvm/java-6-openjdk-amd64}"
JDK8_HOME="$SDKMAN_CANDIDATES_DIR/java/8.0.252-zulu"
#JDK11_HOME="$SDKMAN_CANDIDATES_DIR/java/11.0.7-zulu"

installJdkBySdkman() {
    local i
    for ((i = 0; i < ${#jdks_install_by_sdkman[@]}; i++)); do
        local jdkNameOfSdkman="${jdks_install_by_sdkman[i]}"
        local jdkHomePath="$SDKMAN_CANDIDATES_DIR/java/$jdkNameOfSdkman"

        # install jdk by sdkman
        if [ ! -d "$jdkHomePath" ]; then
            set +u
            runCmd sdk install java "$jdkNameOfSdkman" || die "fail to install jdk $jdkNameOfSdkman by sdkman"
            set -u
        fi
    done

    ls -la "$SDKMAN_CANDIDATES_DIR/java/"
}
installJdkBySdkman

switch_to_jdk() {
    local javaHomeVarName="JDK${1}_HOME"
    if [ -n "${!javaHomeVarName:-}" ]; then
        export JAVA_HOME="${!javaHomeVarName}"
    else
        export JAVA_HOME="$SDKMAN_CANDIDATES_DIR/java/$1"
    fi

    [ -n "$JAVA_HOME" ] || die "jdk $1 env not found: $javaHomeVarName"
    [ -e "$JAVA_HOME" ] || die "jdk $1 not existed: $JAVA_HOME"
    [ -d "$JAVA_HOME" ] || die "jdk $1 is not directory: $JAVA_HOME"
}
