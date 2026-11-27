#!/bin/sh

# Launch a server process (registry):
#   sh runserver.sh <main class> [args]
# Usually started by mqregistry.

. "$(cd "$(dirname "$0")" && pwd)/env.sh"

# the registry only keeps route metadata, a small fixed heap is enough
JAVA_OPT="${JAVA_OPT} -Xms512m -Xmx512m"

use_logback_config

run_java "$@"
