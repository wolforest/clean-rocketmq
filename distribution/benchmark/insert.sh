#!/bin/sh

# Benchmark of writing messages into one commitLog (single and multi thread):
#   sh benchmark/insert.sh
# The commitLog is written to a temporary directory and is not removed afterwards.

exec sh "$(cd "$(dirname "$0")" && pwd)/runclass.sh" com.wolfmq.store.domain.commitlog.benchmark.InsertBenchmark "$@"
