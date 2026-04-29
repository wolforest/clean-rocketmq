# Clean-RocketMQ vs Apache RocketMQ Comparison Report

## 1. Scope

This report compares:

- `clean-rocketmq` in the current workspace
- `apache/rocketmq` on the `develop` branch, accessed on `2026-04-29`

The comparison covers:

- Structure
- Implementation
- Interactions
- Features
- Performance
- Maturity and recommended use cases

Notes:

- Conclusions about `clean-rocketmq` are based on the local repository and its documentation
- Conclusions about Apache RocketMQ are based on the official repository README, module layout, `BrokerController`, `DefaultMessageStore`, `CommitLog`, `NamesrvController`, and `proxy/README.md`
- The performance section is based on architecture, code paths, and benchmark harnesses in the repository. It is not a reproduced side-by-side benchmark under the same hardware and runtime environment

## 2. Executive Summary

In one sentence:

`clean-rocketmq` is not just a trimmed-down clone of RocketMQ. It is a service-core rewrite that pushes harder on code clarity, domain boundaries, CommitLog sharding, and Broker/Store decoupling, while Apache RocketMQ remains much stronger in ecosystem breadth, protocol coverage, production hardening, and platform completeness.

More concretely:

- If the goal is to study RocketMQ internals, continue refactoring the server core, or evolve storage/compute separation further, `clean-rocketmq` is easier to work with
- If the goal is to obtain a complete production messaging platform with mature HA, governance, security, tooling, and protocol support, Apache RocketMQ is the safer and more complete choice
- If the goal is to explore aggressive write-path optimization and a cleaner storage architecture, `clean-rocketmq` is especially interesting

## 3. High-Level Comparison

| Dimension | clean-rocketmq | Apache RocketMQ | Conclusion |
|---|---|---|---|
| Repository role | Focused server-core rewrite | Full messaging platform repository | clean is more focused; official RocketMQ is more complete |
| Top-level modules | `hello/domain/rpc/registry/store/broker/test` | `client/common/remoting/broker/store/namesrv/tools/proxy/controller/auth/tieredstore/...` | clean emphasizes kernel decomposition; official RocketMQ covers a much larger product surface |
| Architectural style | Lifecycle-driven assembly plus explicit domain layering | Long-evolved large repository with many responsibilities concentrated in large controller/store classes | clean is easier to read and refactor; official RocketMQ is more complex but more battle-tested |
| Broker/Store relationship | Supports both embedded Store and remote Store facades | Broker is stateful with local storage; Proxy provides a stateless traffic layer | clean pushes storage/compute separation one layer deeper |
| Protocol entry | gRPC is served directly inside Broker | gRPC is emphasized through Proxy | clean has a shorter path; official RocketMQ has a more standardized front door |
| Storage design | CommitLog/Dispatcher/CQ/Index/MQ/Timer/HA are explicit subdomains | `DefaultMessageStore` coordinates many storage concerns centrally | clean is easier for understanding and restructuring; official RocketMQ is more integrated and mature |
| Transaction/Timer/POP | Explicit domain modules | Supported too, but spread across Broker/Store/Proxy layers | clean makes the business semantics more visible |
| Performance strategy | CommitLog sharding, multi-dir queues, CPU binding, separated components | Single main CommitLog design plus multi-path support, rich flush/HA/cold-data/statistics machinery | clean is more aggressive on write-path parallelism; official RocketMQ is more balanced end-to-end |
| Ecosystem | Core messaging abilities are present, surrounding capabilities are still evolving | Auth, Controller, Proxy, TieredStore, Dashboard, Operators, Connectors and more | official RocketMQ is clearly stronger here |
| Maturity | README still marks master-slave, Raft, cloud-native evolution as ongoing | Long-running production project | official RocketMQ is safer for production breadth; clean has more room to evolve |

## 4. Structural Comparison

### 4.1 Top-level repository shape

`clean-rocketmq` keeps the root module set intentionally small:

- `hello`
- `domain`
- `rpc`
- `registry`
- `store`
- `broker`
- `test`

That makes the architectural emphasis very clear:

- common domain model in `domain`
- protocol and transport in `rpc`
- metadata and service discovery in `registry`
- storage kernel in `store`
- exposed messaging services in `broker`

Apache RocketMQ, by contrast, keeps the traditional server modules and also includes:

- `proxy`
- `controller`
- `auth`
- `tieredstore`
- `container`
- `openmessaging`
- `tools`
- `distribution`

This is the first major conclusion:

- `clean-rocketmq` is kernel-oriented
- Apache RocketMQ is product-oriented

### 4.2 Component assembly style

`clean-rocketmq` uses a very explicit lifecycle assembly pattern:

- Broker entry: `broker/.../Broker.java`
- Broker assembly: `broker/.../ComponentRegister.java`
- Store entry: `store/.../Store.java`
- Store assembly: `store/.../ComponentRegister.java`
- Registry entry: `registry/.../Registry.java`

This gives it several advantages:

- startup entry points stay thin
- dependency wiring is concentrated
- each functional domain can initialize, start, and stop independently
- implementations are easier to replace

Apache RocketMQ also has clear module boundaries, but core runtime responsibilities are much more concentrated in large classes. Current GitHub page metadata shows:

- `BrokerController.java`: about `2823` lines
- `CommitLog.java`: about `2544` lines
- `DefaultMessageStore.java`: about `3272` lines
- `NamesrvController.java`: about `285` lines

This does not mean the official design is poor. It means the official codebase has accumulated many production concerns in its core classes over time.

### 4.3 Structural conclusion

| Aspect | clean-rocketmq | Apache RocketMQ | Conclusion |
|---|---|---|---|
| Layer clarity | Stronger | Moderate | clean is easier to read and refactor |
| Responsibility boundaries | Sharper | Heavier | clean is better for decoupling work |
| Historical baggage | Lower | Higher | clean feels cleaner |
| Platform completeness | Lower | Higher | official RocketMQ is better for direct platform adoption |

## 5. Storage and Performance Comparison

### 5.1 Store is an independent service in clean

In `clean-rocketmq`, Store has its own entry point and its own bootstraps:

- `MetaBootstrap`
- `CommitLogBootstrap`
- `DispatcherBootstrap`
- `ConsumeQueueBootstrap`
- `IndexBootstrap`
- `MQBootstrap`
- `TimerBootstrap`
- `RpcBootstrap`
- `HABootstrap`

This means the storage layer is treated as a relatively independent backend service.

In Apache RocketMQ, storage is still centrally orchestrated by `DefaultMessageStore`, which manages things such as:

- CommitLog
- ConsumeQueueStore
- HAService
- TimerMessageStore
- StoreCheckpoint
- ReputMessageService
- Compaction

Conclusion:

- clean has a stronger service-oriented storage split
- official RocketMQ has a more mature integrated storage kernel

### 5.2 CommitLog sharding is the standout idea in clean

`clean-rocketmq` exposes sharding directly in `CommitConfig`:

- `enableSharding`
- `bindShardingWithCpu`
- `maxShardingNumber`
- `shardingNumber`
- `enableMultiDir`

`CommitLogBootstrap` creates multiple `CommitLog` instances and `CommitLogManager` coordinates them.

Key characteristics of `CommitLogManager`:

- manages multiple CommitLog shards
- can bind shards to thread numbering / CPU-oriented worker layout
- can choose shards randomly
- uses `OffsetCodec` to encode shard identity into a unified logical offset space

Apache RocketMQ uses a different optimization philosophy:

- its `CommitLog` remains a single central object
- it already supports `MultiPathMappedFileQueue`
- but it does not elevate multiple CommitLog shards into a first-class design in the same way clean does

So the difference is:

- official RocketMQ optimizes one strong CommitLog trunk
- clean optimizes for parallelized write-path sharding

### 5.3 Multi-directory support exists in both, but clean integrates it better with sharding

`clean-rocketmq` has `MultiDirMappedFileQueue`:

- one `DefaultMappedFileQueue` per directory
- one shared global logical offset space
- round-robin file placement by default

Apache RocketMQ also supports multi-path CommitLog directories through `MultiPathMappedFileQueue`.

So the right conclusion is not “only clean supports multi-directory storage”. The more accurate conclusion is:

- both support multi-directory layouts
- clean combines multi-directory storage with a sharded write-path model in a way that is easier to extend

### 5.4 Write path responsibilities are more concentrated in the official code, more separated in clean

`clean-rocketmq` keeps `DefaultCommitLog` relatively focused:

- assign commit offsets
- encode messages
- insert into mapped files
- delegate flush semantics to `flushPolicy`

Apache RocketMQ’s `CommitLog.java` is much larger and carries far more production branches, validation paths, and operational details.

That trade-off is straightforward:

- clean is easier to change
- official RocketMQ is harder to change but more complete

### 5.5 Dispatch path is more explicit in clean

In clean:

- `DispatcherBootstrap` initializes `DispatchManager`
- `ConsumeQueueBootstrap` registers `ConsumeQueueCommitHandler`
- `IndexBootstrap` registers `IndexCommitHandler`

This makes the post-CommitLog derivation chain very explicit.

Apache RocketMQ has reput and dispatch logic too, but it is more deeply embedded inside the `DefaultMessageStore` structure.

Conclusion:

- clean makes the CommitLog-to-CQ/Index path easier to reason about
- official RocketMQ makes the same path more integrated inside a mature store runtime

### 5.6 Timer architecture is more open in clean, but less complete today

clean currently has two timer directions:

- `DefaultTimer` based on timer log + timer wheel + recovery
- `RocksdbTimer` as a reserved path

`TimerService` can switch between:

- `BlackHoleTimer` when timer is disabled
- `RocksdbTimer` when RocksDB mode is enabled
- `DefaultTimer` otherwise

At the moment:

- `DefaultTimer` is meaningfully implemented
- `RocksdbTimer` is still a visible placeholder

Apache RocketMQ already integrates:

- `TimerMessageStore`
- `TimerMessageRocksDBStore`

So here the conclusion is:

- clean has a more open architectural direction
- official RocketMQ has a more mature timer implementation

### 5.7 HA is a separate subdomain in clean, but still evolving

The clean Store side already has:

- `HABootstrap`
- `HAService`
- `DefaultHAServer`
- `DefaultHAClient`
- `CommitLogSynchronizer`

So high availability is not absent.

But the local README still clearly marks:

- master-slave and storage/compute separation as partially completed
- Raft support as still pending

Apache RocketMQ’s README explicitly advertises HA and fault tolerance based on `DLedger Controller`.

Conclusion:

- clean is architecturally prepared for HA evolution
- official RocketMQ provides a more complete and proven HA story today

### 5.8 Performance conclusion

The clean README makes a strong performance claim:

- CommitLog sharding
- `5x+` throughput relative to RocketMQ

The repository also contains focused benchmarks:

- `InsertBenchmarkTest.java`
- `CommitLogManagerBenchmarkTest.java`

Those benchmarks compare:

- single CommitLog
- multi-threaded single shard
- multi-threaded multi-shard
- 1/2/5/10 shard scaling

So performance is not only a marketing statement in the project; it is reflected in the architecture and benchmark direction.

Still, a balanced conclusion is important:

- clean’s performance advantage is mainly rooted in sharded parallel writes
- official RocketMQ’s performance strength comes from a more mature whole-system optimization stack
- if you benchmark WAL-heavy write throughput alone, clean may have an advantage
- if you benchmark full production workloads with HA, recovery, operational behaviors, governance, and surrounding capabilities, official RocketMQ may remain stronger overall

## 6. Interaction and Deployment Comparison

### 6.1 Broker-Store relationship is more flexible in clean

`clean-rocketmq` exposes both:

- `enableEmbedStore`
- `enableRemoteStore`

and `StoreBootstrap` assembles both:

- `EmbedStoreBootstrap`
- `RemoteStoreBootstrap`

Then unified facades such as `MQStore`, `TopicStore`, `ConsumeOffsetStore`, `SubscriptionStore`, and `TimerStore` abstract away where the storage work is actually executed.

This means Broker-side logic is intentionally designed to sit above a storage abstraction.

Apache RocketMQ separates runtime responsibilities differently. The main comparison point is Proxy:

- `Cluster mode`: Proxy is independent and stateless, Broker keeps local state and storage
- `Local mode`: Proxy and Broker run in the same process

The direction is similar but the split point is different:

- official RocketMQ separates stateless ingress from stateful Broker
- clean separates Broker business logic from Store storage logic

### 6.2 gRPC lives inside Broker in clean, but is externalized through Proxy in official RocketMQ

clean registers `GrpcBootstrap` directly inside the Broker component graph.

`MessageBootstrap` then further divides runtime handling into:

- `ClientActivity`
- `RouteActivity`
- `ProducerActivity`
- `ConsumerActivity`
- `TransactionActivity`

plus channel and session services such as:

- `RegisterService`
- `HeartbeatService`
- `TelemetryService`
- `TerminationService`

So clean’s design is:

- Broker itself is the gRPC-facing server
- routing, produce, consume, and transaction flows are dispatched inside Broker

Apache RocketMQ’s Proxy documentation makes a different front-door choice:

- Proxy is a stateless traffic interface
- gRPC is implemented first on Proxy
- Proxy can evolve into a multi-protocol entry point
- in cluster mode, Proxy acts as the computing layer while Broker stays stateful with local storage

Conclusion:

- clean gives you a shorter direct path
- official RocketMQ gives you a more standardized multi-protocol front door

### 6.3 Registry in clean stays lighter and closer to a minimal NameServer kernel

clean’s Registry keeps the component list small:

- `PropertyManager`
- `KVManager`
- `StoreManager`
- `BrokerManager`
- `RpcManager`

This reflects a deliberate choice to keep metadata services light.

Apache RocketMQ’s `NamesrvController` is also much smaller than `BrokerController` or `DefaultMessageStore`, but the official ecosystem also adds `controller` and other control-plane capabilities outside NameServer itself.

So the conclusion is:

- clean registry feels like a leaner enhanced NameServer
- official RocketMQ spreads control-plane capability across a broader system

### 6.4 Route rewriting makes storage/compute separation visible in clean

`RouteService` in clean contains explicit route rewriting logic:

- in embedded-store mode, it rewrites ports to match the current server-facing address
- in remote-store mode, it rewrites route addresses to an externally provided address list

This is important because it shows that storage/compute separation in clean is not only conceptual. It already affects client-visible route orchestration.

Apache RocketMQ achieves the same kind of end-user routing outcome through a combination of:

- NameServer route metadata
- Broker addresses
- Proxy forwarding and address management

### 6.5 Interaction and deployment conclusion

| Aspect | clean-rocketmq | Apache RocketMQ | Conclusion |
|---|---|---|---|
| gRPC entry | Served directly by Broker | More strongly standardized through Proxy | clean is shorter; official RocketMQ is more structured |
| Storage/compute split point | Broker/Store boundary | Proxy/Broker boundary | clean pushes the split deeper |
| Deployment flexibility | Embedded and remote storage modes | Traditional Broker plus local Proxy plus clustered Proxy | both are flexible, but at different abstraction layers |
| Metadata service shape | Lighter | Broader control-plane ecosystem | clean is easier to understand; official RocketMQ is more complete |

## 7. Feature Comparison

### 7.1 Transactions are a first-class domain in clean

clean’s `TransactionBootstrap` assembles:

- `PrepareService`
- `CommitService`
- `RollbackService`
- `TransactionMessageService`
- `BatchCommitService`
- `ReceiptRegistry`
- `ReceiptCleaner`
- `CheckService`
- `CheckerFactory`

This gives transaction handling a very explicit domain shape.

Apache RocketMQ’s README still explicitly lists transactional messages as a core capability, including “financial grade transactional message”. The difference is mostly in code organization:

- clean makes the transaction model easier to study structurally
- official RocketMQ integrates transaction support into its larger Broker/Store runtime

### 7.2 Scheduling and delayed delivery have a larger architectural ambition in clean

clean’s README explicitly aims beyond ordinary delay messages:

- delay
- expiration
- periodic scheduling
- cycle / loop scheduling

The code structure reflects this ambition:

- Broker-side `TimerBootstrap`
- Store-side `TimerBootstrap`
- Broker-side saver/scanner/scheduler/producer roles
- Store-side timer log, timer wheel, and recovery chain

This makes scheduling look more like an independent subsystem than a single feature.

At the same time:

- `DefaultTimer` is already meaningful
- `RocksdbTimer` is still incomplete
- the README still says richer scheduling features are ongoing

So the balanced conclusion is:

- clean has a broader timer architecture ambition
- official RocketMQ has a more mature timer feature set today

### 7.3 POP, ACK, invisibility, renew, and revive are modeled more explicitly in clean

clean’s consumer domain is split into:

- `PopBootstrap`
- `AckBootstrap`
- `RenewBootstrap`
- `ReviveBootstrap`

and they are assembled by `ConsumerBootstrap`.

`PopBootstrap` itself separates:

- `ContextBuilder`
- `QueueSelector`
- `BrokerDequeueService`
- `PopService`

`AckBootstrap` assembles:

- `BrokerAckService`
- `InvisibleService`

This makes the POP consumption state machine very explicit:

- POP
- ACK
- invisibility timeout
- renew
- revive

Apache RocketMQ also treats POP as a core capability. The official Proxy documentation explicitly says:

- POP mode is natively supported in Proxy
- normal / FIFO / transaction / delay can all be supported with POP mode

The conclusion is:

- clean explains the POP model more clearly in code structure
- official RocketMQ has stronger maturity and compatibility around the same semantics

### 7.4 Ecosystem and surrounding platform features are much stronger in official RocketMQ

The official README explicitly lists:

- gRPC, MQTT, JMS, and OpenMessaging
- authentication and authorization
- connectors
- dashboard
- operator
- streams
- event bridge

clean is much more focused on:

- RocketMQ-compatible protocol handling
- service-core messaging abilities
- storage, registry, transactions, scheduling, and write-path evolution

So this conclusion is straightforward:

- official RocketMQ is much stronger as a full messaging platform
- clean is more focused as a core-server rewrite

## 8. Maturity and Engineering Trade-offs

### 8.1 Where clean is stronger

- clearer component boundaries
- easier long-term refactoring
- explicit CommitLog sharding as a performance lever
- Broker/Store decoupling that naturally supports deeper storage/compute separation
- direct gRPC ingress inside Broker
- transactions, scheduling, and consumption semantics are easier to understand as domains

### 8.2 Risks and incomplete areas in clean

- remote Store evolution is still being completed, and some lifecycle methods are still placeholders
- `RocksdbTimer` is still unfinished
- the README explicitly marks master-slave, Raft, and cloud-native directions as ongoing
- performance claims are supported by benchmark intent and architecture, but not by a public same-environment side-by-side result in this repository

### 8.3 Where official RocketMQ is stronger

- much broader feature coverage
- far more production experience
- stronger multi-protocol ecosystem
- mature Proxy, Controller, TieredStore, Auth, and surrounding platform modules
- stronger HA, observability, governance, and deployment tooling

### 8.4 The cost of official RocketMQ’s maturity

- core classes are large
- historical evolution has increased internal complexity
- the codebase is less friendly for newcomers and deep refactoring
- many capabilities are spread across modules, which raises comprehension cost

## 9. Recommended Use Cases

Choose `clean-rocketmq` first if:

- you want to study RocketMQ server internals systematically
- you want to keep evolving Broker/Store decoupling
- you want to explore CommitLog sharding and high-throughput writes
- you want a cleaner codebase for extending transactions, scheduling, or storage/compute separation

Choose Apache RocketMQ first if:

- you need complete production capabilities
- you need mature HA and ecosystem support
- you need dashboard, operator, connectors, auth, proxy, and control-plane integration
- you want to align closely with the official clients, official operational model, and surrounding tooling

## 10. Final Judgment

If both projects are evaluated within the same frame, the most balanced conclusion is:

- the value of `clean-rocketmq` is not mainly “more features than Apache RocketMQ”, but “a clearer server-core rewrite that moves further on write-path sharding and Broker/Store decoupling”
- the value of Apache RocketMQ is not mainly “cleaner code”, but “a complete and mature messaging platform with stronger ecosystem, protocol surface, governance, HA, and production confidence”

So these two repositories are not simply substitutes for each other:

- Apache RocketMQ is the mature platform
- clean-rocketmq is the cleaner core rewrite and architectural exploration

## 11. Primary Sources

### 11.1 clean-rocketmq

- `README.md`
- `pom.xml`
- `broker/src/main/java/cn/coderule/wolfmq/broker/server/ComponentRegister.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/infra/store/StoreBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/infra/embed/EmbedStoreBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/infra/remote/RemoteStoreBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/server/grpc/service/MessageBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/domain/meta/MetaBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/domain/meta/RouteService.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/domain/transaction/TransactionBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/domain/timer/TimerBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/domain/consumer/ConsumerBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/domain/consumer/pop/PopBootstrap.java`
- `broker/src/main/java/cn/coderule/wolfmq/broker/domain/consumer/ack/AckBootstrap.java`
- `store/src/main/java/cn/coderule/wolfmq/store/server/bootstrap/ComponentRegister.java`
- `store/src/main/java/cn/coderule/wolfmq/store/domain/commitlog/log/DefaultCommitLog.java`
- `store/src/main/java/cn/coderule/wolfmq/store/domain/commitlog/log/CommitLogManager.java`
- `store/src/main/java/cn/coderule/wolfmq/store/infra/file/MultiDirMappedFileQueue.java`
- `store/src/main/java/cn/coderule/wolfmq/store/domain/dispatcher/DispatcherBootstrap.java`
- `store/src/main/java/cn/coderule/wolfmq/store/domain/consumequeue/ConsumeQueueBootstrap.java`
- `store/src/main/java/cn/coderule/wolfmq/store/domain/index/IndexBootstrap.java`
- `store/src/main/java/cn/coderule/wolfmq/store/domain/timer/service/TimerService.java`
- `store/src/main/java/cn/coderule/wolfmq/store/domain/timer/wheel/DefaultTimer.java`
- `store/src/main/java/cn/coderule/wolfmq/store/domain/timer/rocksdb/RocksdbTimer.java`
- `store/src/main/java/cn/coderule/wolfmq/store/server/ha/HABootstrap.java`
- `registry/src/main/java/cn/coderule/wolfmq/registry/server/ComponentRegister.java`
- `rpc/pom.xml`

### 11.2 Apache RocketMQ

- Repository root: <https://github.com/apache/rocketmq>
- Official README: <https://raw.githubusercontent.com/apache/rocketmq/develop/README.md>
- Root `pom.xml`: <https://github.com/apache/rocketmq/blob/develop/pom.xml>
- `BrokerController.java`: <https://github.com/apache/rocketmq/blob/develop/broker/src/main/java/org/apache/rocketmq/broker/BrokerController.java>
- `DefaultMessageStore.java`: <https://github.com/apache/rocketmq/blob/develop/store/src/main/java/org/apache/rocketmq/store/DefaultMessageStore.java>
- `CommitLog.java`: <https://github.com/apache/rocketmq/blob/develop/store/src/main/java/org/apache/rocketmq/store/CommitLog.java>
- `NamesrvController.java`: <https://github.com/apache/rocketmq/blob/develop/namesrv/src/main/java/org/apache/rocketmq/namesrv/NamesrvController.java>
- `proxy/README.md` page: <https://github.com/apache/rocketmq/tree/develop/proxy>

