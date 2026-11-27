#!/bin/sh

# Run a class with the classpath of the distribution:
#   sh benchmark/runclass.sh <class> [options]

if [ $# -lt 1 ]; then
    echo "USAGE: sh runclass.sh <class> [options]"
    exit 1
fi

. "$(cd "$(dirname "$0")/.." && pwd)/bin/env.sh"

# every encode thread holds a direct buffer of maxRequestSize (130MB),
# size the direct memory by the core count
CPU_NUMBER=`getconf _NPROCESSORS_ONLN 2>/dev/null`
[ -z "$CPU_NUMBER" ] && CPU_NUMBER=8
JAVA_OPT="${JAVA_OPT} -Xms1g -Xmx1g"
JAVA_OPT="${JAVA_OPT} -XX:MaxDirectMemorySize=$((CPU_NUMBER * 300))M"

run_java "$@"
