#!/bin/sh

# Shared environment of all wolfmq scripts: java, home directory, classpath and
# the JVM launcher. It is sourced by the other scripts, do not run it directly.

# wolfmq and its dependencies are compiled with --enable-preview, so a release
# can only run on the JDK it was compiled with.
# Keep it in sync with java.version of the root pom.
JAVA_MAJOR_VERSION_REQUIRED=21

error_exit ()
{
    echo "ERROR: $1 !!" >&2
    exit 1
}

find_java_home()
{
    if [ -e "$JAVA_HOME/bin/java" ]; then
        return
    fi

    case "`uname`" in
        Darwin)
            JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
        ;;
        *)
            JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac) 2>/dev/null)))
        ;;
    esac

    # sdkman, jenv and some package managers put java on PATH without setting JAVA_HOME
    if [ ! -e "$JAVA_HOME/bin/java" ]; then
        JAVA_ON_PATH=`command -v java 2>/dev/null`
        if [ -n "$JAVA_ON_PATH" ]; then
            JAVA_ON_PATH=`readlink -f "$JAVA_ON_PATH" 2>/dev/null || echo "$JAVA_ON_PATH"`
            JAVA_HOME=`dirname $(dirname "$JAVA_ON_PATH")`
        fi
    fi

    [ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=$HOME/jdk/java
    [ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/java
    [ -e "$JAVA_HOME/bin/java" ] || error_exit "Please set the JAVA_HOME variable in your environment, wolfmq needs JDK $JAVA_MAJOR_VERSION_REQUIRED!"
}

check_java_version()
{
    JAVA_VERSION=`"$JAVA_HOME/bin/java" -version 2>&1 | head -1 | cut -d'"' -f2`
    JAVA_MAJOR_VERSION=`echo "$JAVA_VERSION" | cut -d. -f1 | sed 's/[^0-9].*//'`

    [ "$JAVA_MAJOR_VERSION" = "$JAVA_MAJOR_VERSION_REQUIRED" ] || error_exit "wolfmq needs JDK $JAVA_MAJOR_VERSION_REQUIRED, but ${JAVA_HOME:-JAVA_HOME} is java ${JAVA_VERSION:-unknown}. Please set JAVA_HOME to JDK $JAVA_MAJOR_VERSION_REQUIRED."
}

find_java_home
check_java_version

export JAVA_HOME
export JAVA="$JAVA_HOME/bin/java"

# the launcher scripts resolve WOLFMQ_HOME themselves, runserver.sh/runbroker.sh are found under bin/
if [ -z "$WOLFMQ_HOME" ]; then
    WOLFMQ_HOME=`cd "$(dirname "$0")/.." && pwd`
fi
export WOLFMQ_HOME

export BASE_DIR="$WOLFMQ_HOME"
# only add the classpath once, env.sh is sourced by launcher and run script
case ":${CLASSPATH}:" in
    *":${BASE_DIR}/conf:"*) ;;
    *) export CLASSPATH=".:${BASE_DIR}/conf:${BASE_DIR}/lib/*:${CLASSPATH}" ;;
esac

# logs of the processes: $WOLFMQ_LOG_DIR/<process>.log and the gc logs
WOLFMQ_LOG_DIR="${WOLFMQ_LOG_DIR:-${HOME}/logs/wolfmq}"
export WOLFMQ_LOG_DIR

#===========================================================================================
# JVM Configuration
#===========================================================================================
# JAVA_OPT_EXT is appended last, so that it can override the options below.
JAVA_OPT="${JAVA_OPT} -XX:+UseG1GC -XX:G1HeapRegionSize=16m"
JAVA_OPT="${JAVA_OPT} -XX:MetaspaceSize=128m -XX:+AlwaysPreTouch -XX:-OmitStackTraceInFastThrow"

# the dependency jars are compiled with --enable-preview
JAVA_OPT="${JAVA_OPT} --enable-preview"

# gc log of the unified JVM logging
JAVA_OPT="${JAVA_OPT} -Xlog:gc*:file=${WOLFMQ_LOG_DIR}/gc_%p_%t.log:time,tags:filecount=5,filesize=30M"

# log to $WOLFMQ_LOG_DIR/$WOLFMQ_LOG_NAME.log when conf/logback.xml exists,
# the console only shows WARN and ERROR, set WOLFMQ_CONSOLE_LEVEL to change it
use_logback_config()
{
    if [ -f "${WOLFMQ_HOME}/conf/logback.xml" ]; then
        JAVA_OPT="${JAVA_OPT} -DWOLFMQ_LOG_DIR=${WOLFMQ_LOG_DIR}"
        JAVA_OPT="${JAVA_OPT} -DWOLFMQ_LOG_NAME=${WOLFMQ_LOG_NAME:-wolfmq}"
        JAVA_OPT="${JAVA_OPT} -DWOLFMQ_LOG_LEVEL=${WOLFMQ_LOG_LEVEL:-INFO}"
        JAVA_OPT="${JAVA_OPT} -DWOLFMQ_CONSOLE_LEVEL=${WOLFMQ_CONSOLE_LEVEL:-WARN}"
        JAVA_OPT="${JAVA_OPT} -Dlogback.configurationFile=${WOLFMQ_HOME}/conf/logback.xml"
    fi
}

# run the main class given as first argument, this shell is replaced by the JVM
# so that signals (e.g. mqshutdown) reach the JVM directly
run_java()
{
    [ -d "$WOLFMQ_LOG_DIR" ] || mkdir -p "$WOLFMQ_LOG_DIR" || error_exit "Can not create log directory: ${WOLFMQ_LOG_DIR}"

    echo "$JAVA ${JAVA_OPT} ${JAVA_OPT_EXT} -cp ${CLASSPATH} $*"
    exec "$JAVA" ${JAVA_OPT} ${JAVA_OPT_EXT} -cp "${CLASSPATH}" "$@"
}
