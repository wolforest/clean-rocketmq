#!/bin/sh

# Benchmark of the commitLog shards: 1 shard against 2/5/10 shards, single and multi thread:
#   sh benchmark/commitlog.sh
# The commitLogs are written to a temporary directory and are not removed afterwards.

exec sh "$(cd "$(dirname "$0")" && pwd)/runclass.sh" com.wolfmq.store.domain.commitlog.benchmark.CommitLogManagerBenchmark "$@"
