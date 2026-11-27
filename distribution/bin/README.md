### Prerequisites

JDK 21 — the dependencies are compiled with `--enable-preview`,
a release runs only on the JDK it was compiled with (checked by the scripts).

Build the release from the project root (the distribution needs the whole reactor):

  `mvn clean package -DskipTests`

### Operating system tuning

Before deploying brokers on Linux it is highly recommended to run **os.sh**,
which tunes the operating system for the mmap based store (open file limits,
`vm.max_map_count`, page cache write back).

  `sudo sh bin/os.sh`

Notes:

* Linux only, run it once with root permission
* idempotent, settings are persisted in `/etc/sysctl.d/99-wolfmq.conf` and
  `/etc/security/limits.conf`
* the parameter settings are reference values, tune them for the host

### Operating system tuning

The store maps commitLog files into memory (1GB each). Before deploying brokers on
Linux it is recommended to check the following settings, reference values:

* `vm.max_map_count = 655360` (many mapped files)
* `vm.swappiness = 1`
* `vm.dirty_background_ratio = 40`, `vm.dirty_ratio = 40`
* open files: `ulimit -n 655350`

### Start registry

* Unix platform

  `nohup sh bin/mqregistry &`

  specify the directory of the config files (default: `$WOLFMQ_HOME/conf`)

  `nohup sh bin/mqregistry -p /path/to/conf &`

### Start broker

* Unix platform

  `nohup sh bin/mqbroker &`

  specify the directory of the config files (default: `$WOLFMQ_HOME/conf`)

  `nohup sh bin/mqbroker -p /path/to/conf &`

### Shutdown

  `sh bin/mqshutdown registry`
  `sh bin/mqshutdown broker`

SIGTERM is sent first, the process gets 30 seconds
(`WOLFMQ_SHUTDOWN_TIMEOUT`) to shut down gracefully, then it is killed.

### Configuration

The config directory holds the yml files, only the properties to override need to
be listed (an unknown property makes the process fail to start):

* `broker.yml` — broker and embedded store configuration
* `store.yml` — store configuration, also used by the embedded store
* `registry.yml` — registry configuration (not read by the registry yet, it starts
  with the built-in defaults)

### Logs

The logs are written to `$WOLFMQ_LOG_DIR` (default: `$HOME/logs/wolfmq`), the console
only shows WARN and ERROR:

* `<process>.log` — rolling, 128MB × 10 days, 2GB total
* `gc_<pid>_<timestamp>.log` — gc log

Change with environment variables before starting:

* `WOLFMQ_LOG_DIR`, `WOLFMQ_LOG_LEVEL` (INFO), `WOLFMQ_CONSOLE_LEVEL` (WARN)
* run in the foreground with INFO on the console: `WOLFMQ_CONSOLE_LEVEL=INFO sh bin/mqbroker`

### JVM options

The defaults can be overridden or extended without editing the scripts:

  `JAVA_OPT_EXT="-Xmx4g -XX:MaxDirectMemorySize=8g" nohup sh bin/mqbroker &`

Note: every message encoding thread holds a direct buffer of `maxRequestSize`
(130MB), size the direct memory accordingly.

### Benchmark

  `sh benchmark/insert.sh` — write throughput of one commitLog, single/multi thread
  `sh benchmark/commitlog.sh` — commitLog shards: 1 against 2/5/10 shards

### Troubleshooting

* port already in use: `lsof -nP -iTCP:<port> -sTCP:LISTEN`, then `sh bin/mqshutdown <name>`
* wrong java: the release needs JDK 21, check `java -version` of the resolved
  `$JAVA_HOME` (the scripts print it on mismatch) and set `JAVA_HOME` accordingly
