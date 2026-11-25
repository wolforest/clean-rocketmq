#!/bin/sh

error_exit ()
{
    echo "ERROR: $1 !!"
    exit 1
}

find_java_home()
{
    if [ -n "$JAVA_HOME" ]; then
        return
    fi
    case "`uname`" in
        Darwin)
            JAVA_HOME=$(/usr/libexec/java_home)
        ;;
        *)
            JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac))))
        ;;
    esac
}

find_java_home

[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=$HOME/jdk/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/java
[ ! -e "$JAVA_HOME/bin/java" ] && error_exit "Please set the JAVA_HOME variable in your environment, We need java(x64)!"

export JAVA_HOME
export JAVA="$JAVA_HOME/bin/java"
export BASE_DIR=$(dirname $0)/..
export CLASSPATH=.:${BASE_DIR}/conf:${BASE_DIR}/lib/*:${CLASSPATH}

JAVA_OPT="${JAVA_OPT} -Xms512m -Xmx512m -XX:+UseG1GC -XX:G1HeapRegionSize=16m"
JAVA_OPT="${JAVA_OPT} -XX:+AlwaysPreTouch -XX:-OmitStackTraceInFastThrow"
JAVA_OPT="${JAVA_OPT} --enable-preview"

echo "$JAVA ${JAVA_OPT} $@"
"$JAVA" ${JAVA_OPT} $@
