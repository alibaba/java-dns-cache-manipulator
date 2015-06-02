#!/bin/bash
# this script is based on mvn

readonly PROG=`basename $0`
readonly BASE="$(cd $(dirname "$0")/..; pwd)"

CLASSPATH="$(echo "$BASE"/dependencies/*.jar | tr ' ' :)"

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true
    #
    # Look for the Apple JDKs first to preserve the existing behaviour, and then look
    # for the new JDKs provided by Oracle.
    # 
    if [ -z "$JAVA_HOME" ] && [ -L /System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK ] ; then
     #
     # Apple JDKs
     #
     export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home
    fi
    
    if [ -z "$JAVA_HOME" ] && [ -L /System/Library/Java/JavaVirtualMachines/CurrentJDK ] ; then
     #
     # Apple JDKs
     #
     export JAVA_HOME=/System/Library/Java/JavaVirtualMachines/CurrentJDK/Contents/Home
    fi
     
    if [ -z "$JAVA_HOME" ] && [ -L "/Library/Java/JavaVirtualMachines/CurrentJDK" ] ; then
     #
     # Oracle JDKs
     #
     export JAVA_HOME=/Library/Java/JavaVirtualMachines/CurrentJDK/Contents/Home
    fi           
    
    if [ -z "$JAVA_HOME" ] && [ -x "/usr/libexec/java_home" ]; then
     #
     # Apple JDKs
     #
     export JAVA_HOME=`/usr/libexec/java_home`
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=`java-config --jre-home`
  fi
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# For Migwn, ensure paths are in UNIX format before anything is touched
if $mingw ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME="`(cd "$JAVA_HOME"; pwd)`"
  # TODO classpath?
fi

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="`which javac`"
  if [ -n "$javaExecutable" ] && ! [ "`expr \"$javaExecutable\" : '\([^ ]*\)'`" = "no" ]; then
    # readlink(1) is not available as standard on Solaris 10.
    readLink=`which readlink`
    if [ ! `expr "$readLink" : '\([^ ]*\)'` = "no" ]; then
      if $darwin ; then
        javaHome="`dirname \"$javaExecutable\"`"
        javaExecutable="`cd \"$javaHome\" && pwd -P`/javac"
      else
        javaExecutable="`readlink -f \"$javaExecutable\"`"
      fi
      javaHome="`dirname \"$javaExecutable\"`"
      javaHome=`expr "$javaHome" : '\(.*\)/bin'`
      JAVA_HOME="$javaHome"
      export JAVA_HOME
    fi
  fi
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="`which java`"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi


find_tools_jar() {
  if [ -f "$JAVA_HOME/lib/tools.jar" ]; then
    echo "$JAVA_HOME/lib/tools.jar"
  elif [ -f "$JAVA_HOME/../lib/tools.jar" ]; then
    echo "$JAVA_HOME/../lib/tools.jar"
  elif [ -f "$JAVA_HOME/../lib/tools.jar" ]; then
    echo "$JAVA_HOME/Classes/classes.jar"
  elif [ -f "$JAVA_HOME/../Classes/classes.jar" ]; then
    echo "$JAVA_HOME/../Classes/classes.jar"
  else
     echo "tools.jar/classes.jar NOT found!" 1>&2
     exit 8
  fi
}

CLASSPATH="$(find_tools_jar)":"$CLASSPATH"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

# traverses directory structure from process work directory to filesystem root
# first directory with .mvn subdirectory is considered project base directory
find_maven_basedir() {
  local basedir=$(pwd)
  local wdir=$(pwd)
  while [ "$wdir" != '/' ] ; do
    wdir=$(cd "$wdir/.."; pwd)
    if [ -d "$wdir"/.mvn ] ; then
      basedir=$wdir
      break
    fi
  done
  echo "${basedir}"
}

# concatenates all lines of a file
concat_lines() {
  if [ -f "$1" ]; then
    echo "$(tr -s '\n' ' ' < "$1")"
  fi
}


export DCM_TOOLS_AGENT_JAR=$(echo "$BASE/agent-lib"/dns-cache-manipulator*.jar)
export DCM_TOOLS_TMP_FILE="/tmp/dcm_$(date +%s)_${$}_$RANDOM.log"

cleanupWhenExit() {
    rm "$DCM_TOOLS_TMP_FILE" &> /dev/null
}
trap "cleanupWhenExit" EXIT

"$JAVACMD" -classpath $CLASSPATH com.alibaba.dcm.tool.DcmTool "$@"
