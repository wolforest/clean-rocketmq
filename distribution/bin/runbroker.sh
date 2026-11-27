#!/bin/sh

# Launch a broker process:
#   sh runbroker.sh <main class> [args]
# Usually started by mqbroker.

. "$(cd "$(dirname "$0")" && pwd)/env.sh"

JAVA_OPT="${JAVA_OPT} -Xms1g -Xmx2g"

use_logback_config

run_java "$@"
