## Start Broker

* Unix platform

  `nohup sh bin/mqbroker &`

  specify the directory of config files (default: `$WOLFMQ_HOME/conf`)

  `nohup sh bin/mqbroker -p /path/to/conf &`

## Shutdown Broker

  `sh bin/mqshutdown broker`

## Start Registry

* Unix platform

  `nohup sh bin/mqregistry &`

  specify the directory of config files (default: `$WOLFMQ_HOME/conf`)

  `nohup sh bin/mqregistry -p /path/to/conf &`

## Shutdown Registry

  `sh bin/mqshutdown registry`
