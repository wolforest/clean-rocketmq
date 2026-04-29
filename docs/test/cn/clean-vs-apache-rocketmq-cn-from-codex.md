# Clean-RocketMQ vs Apache RocketMQ 对比报告

## 1. 对比范围

本文对比对象是：

- 当前工作区中的 `clean-rocketmq`
- `apache/rocketmq` 的 `develop` 分支，访问时间为 `2026-04-29`

本文主要从以下维度展开：

- 结构
- 实现
- 交互
- 功能
- 性能
- 成熟度与适用场景

说明：

- `clean-rocketmq` 结论基于本仓库代码和文档
- 官方 RocketMQ 结论基于官方仓库 README、模块结构、`BrokerController`、`DefaultMessageStore`、`CommitLog`、`NamesrvController`、`proxy/README.md`
- 性能部分主要依据实现设计、仓库内 benchmark 和代码结构推断，不代表同机同配置下的复现实测结果

## 2. 结论先行

一句话总结：

`clean-rocketmq` 不是官方 RocketMQ 的“精简拷贝”，而是一次围绕服务端核心做的重写与分层优化。它在代码结构、组件边界、CommitLog 分片、Broker/Store 解耦上更激进；官方 RocketMQ 则在功能覆盖、生态完整度、协议支持、生产成熟度上明显更强。

更具体地说：

- 如果目标是“学习 RocketMQ 核心原理、做二次开发、继续演进服务端内核”，`clean-rocketmq` 更友好
- 如果目标是“直接获得完整的生产能力、管理能力、生态组件和多协议支持”，官方 RocketMQ 更成熟
- 如果目标是“探索 Broker 与 Store 的解耦、存算分离、分片写入”，`clean-rocketmq` 的设计更值得重点研究

## 3. 总览对比

| 维度 | clean-rocketmq | Apache RocketMQ | 对比结论 |
|---|---|---|---|
| 仓库定位 | 聚焦服务端核心重写 | 完整消息平台产品仓库 | clean 更聚焦，官方更完整 |
| 顶层模块 | `hello/domain/rpc/registry/store/broker/test` | `client/common/remoting/broker/store/namesrv/tools/proxy/controller/auth/tieredstore/...` | clean 更强调内核拆分；官方模块面更广 |
| 架构风格 | 生命周期 + 组件注册 + 领域分层 | 历史演进型大仓库，核心能力集中在大型控制器/存储类中 | clean 可读性更好；官方复杂度更高但更成熟 |
| Broker 与 Store 关系 | 既支持内嵌 Store，也支持远端 Store 适配 | Broker 默认自带存储，Proxy 提供无状态流量入口 | clean 的存算分离下沉得更深 |
| 协议入口 | Broker 内直接承载 gRPC 服务 | gRPC 更强调通过 Proxy 统一入口 | clean 路径更短；官方分层更标准 |
| 存储实现 | CommitLog/Dispatcher/CQ/Index/MQ/Timer/HA 拆域更明显 | `DefaultMessageStore` 统一组织大量存储职责 | clean 更适合理解和重构；官方更偏成熟一体化实现 |
| 事务/定时/POP | 都被拆成独立功能域 | 官方也支持，但实现分散在 Broker/Store/Proxy 多模块 | clean 业务语义更显式 |
| 性能思路 | CommitLog 分片、多目录、CPU 绑定、组件拆分 | 单主干 CommitLog + 多路径目录 + 丰富刷盘、HA、冷数据、统计体系 | clean 更偏激进优化；官方更偏全链路稳定 |
| 生态能力 | 核心功能完整，外围能力仍在演进 | 认证、控制器、Proxy、TieredStore、Dashboard、Operator、Connectors 较完整 | 官方明显更强 |
| 成熟度 | README 明确仍有主从、Raft、云原生等在继续完成 | 长期生产化项目 | 官方更稳，clean 更具演化空间 |

## 4. 结构专题

### 4.1 顶层结构

`clean-rocketmq` 在根 `pom.xml` 中只保留了 7 个主模块：

- `hello`
- `domain`
- `rpc`
- `registry`
- `store`
- `broker`
- `test`

这说明它的核心重心是：

- 把公共领域模型单独抽到 `domain`
- 把协议和 RPC 抽到 `rpc`
- 把元数据注册中心抽到 `registry`
- 把存储核心抽到 `store`
- 把对外服务和业务能力放在 `broker`

对应地，官方 RocketMQ 的根仓库除了 `broker/store/namesrv/client/remoting` 这些传统模块，还包含：

- `proxy`
- `controller`
- `auth`
- `tieredstore`
- `container`
- `openmessaging`
- `tools`
- `distribution`

这意味着两者从一开始就不是同一种“仓库角色”：

- `clean-rocketmq` 是“内核导向”
- 官方 RocketMQ 是“产品导向”

### 4.2 组件装配方式

`clean-rocketmq` 的 Broker 和 Store 都使用统一的生命周期装配模式：

- Broker 入口：`broker/src/main/java/cn/coderule/wolfmq/broker/Broker.java`
- Broker 组件装配：`broker/src/main/java/cn/coderule/wolfmq/broker/server/ComponentRegister.java`
- Store 入口：`store/src/main/java/cn/coderule/wolfmq/store/Store.java`
- Store 组件装配：`store/src/main/java/cn/coderule/wolfmq/store/server/bootstrap/ComponentRegister.java`
- Registry 入口：`registry/src/main/java/cn/coderule/wolfmq/registry/Registry.java`

这种结构的特点是：

- 启动入口很薄
- 依赖关系集中在 `ComponentRegister`
- 各功能域都可以独立 `initialize/start/shutdown`
- 便于替换实现和做分层隔离

官方 RocketMQ 也有模块边界，但核心对象更集中。典型例子：

- `BrokerController.java` 在 `develop` 分支页面显示约 `2823` 行
- `CommitLog.java` 在 `develop` 分支页面显示约 `2544` 行
- `DefaultMessageStore.java` 在 `develop` 分支页面显示约 `3272` 行

这不表示官方设计不好，而表示它经过长期演进后，把大量生产特性累积到了大核心类中。

### 4.3 结构上的直接结论

| 对比点 | clean-rocketmq | Apache RocketMQ | 结论 |
|---|---|---|---|
| 分层清晰度 | 更强 | 中等 | clean 更适合阅读和重构 |
| 模块职责边界 | 更清晰 | 更厚重 | clean 更利于解耦 |
| 历史包袱 | 更少 | 更多 | clean 更“干净” |
| 生态完整度 | 较弱 | 更强 | 官方更适合直接落地 |

## 5. 存储与性能专题

### 5.1 Store 在 clean 中是独立服务，而不是 Broker 的一个内部对象

`clean-rocketmq` 的 `Store` 是一个单独入口，并有自己的：

- `MetaBootstrap`
- `CommitLogBootstrap`
- `DispatcherBootstrap`
- `ConsumeQueueBootstrap`
- `IndexBootstrap`
- `MQBootstrap`
- `TimerBootstrap`
- `RpcBootstrap`
- `HABootstrap`

这意味着它把 RocketMQ 存储层做成了相对独立的“后端服务”。

官方 RocketMQ 中，存储能力主要被 `DefaultMessageStore` 统一组织，内部再管理：

- CommitLog
- ConsumeQueueStore
- HAService
- TimerMessageStore
- StoreCheckpoint
- ReputMessageService
- Compaction 等

结论：

- clean 的 Store 服务化倾向更强
- 官方的 Store 更偏成熟的一体化内核

### 5.2 CommitLog：clean 的最大亮点是“分片化”

`clean-rocketmq` 在 `CommitConfig` 中显式提供了：

- `enableSharding`
- `bindShardingWithCpu`
- `maxShardingNumber`
- `shardingNumber`
- `enableMultiDir`

`CommitLogBootstrap` 会创建多个 `CommitLog`，再由 `CommitLogManager` 管理。

`CommitLogManager` 的关键特征是：

- 按 shard 管理多个 `CommitLog`
- 支持根据线程编号做 shard 绑定
- 支持随机选择 shard
- 通过 `OffsetCodec` 将逻辑 offset 与 shardId 编码到统一 offset 空间中

这与官方 RocketMQ 的思路有明显差异：

- 官方 `CommitLog` 仍是单核心对象
- 官方已经支持 `MultiPathMappedFileQueue`，说明它也支持多路径目录
- 但官方没有像 clean 这样把“多个 CommitLog shard”做成一等公民

换句话说：

- 官方优化的是“一个 CommitLog 主干如何更强”
- clean 优化的是“写入路径如何并行分片”

### 5.3 多目录能力：两边都有，但 clean 更易扩展到分片场景

`clean-rocketmq` 里有 `MultiDirMappedFileQueue`：

- 每个目录一个 `DefaultMappedFileQueue`
- 全局 offset 空间统一
- 新文件默认 round-robin 分配

官方 RocketMQ 的 `CommitLog` 构造逻辑也已经支持 `MultiPathMappedFileQueue`。

因此这里的结论不是“只有 clean 支持多目录”，而是：

- 官方也支持多目录
- clean 把多目录和分片写入放在了一套更容易继续演进的抽象里

### 5.4 写入路径：clean 更强调小组件职责拆分

`clean-rocketmq` 的 `DefaultCommitLog` 只有约 `295` 行，职责相对集中：

- 分配 commit offset
- 编码消息
- 插入 MappedFile
- 交给 `flushPolicy`

而官方的 `CommitLog.java` 本身就是一个巨大的核心实现，除了基本写入，还承载了更多成熟特性、异常分支和生产场景处理。

优劣很清楚：

- clean 更容易改
- 官方更难改，但更全

### 5.5 分发路径：clean 更显式地把 CQ/Index 当作 CommitLog 事件处理器

在 clean 中：

- `DispatcherBootstrap` 初始化 `DispatchManager`
- `ConsumeQueueBootstrap` 注册 `ConsumeQueueCommitHandler`
- `IndexBootstrap` 注册 `IndexCommitHandler`

这等价于把“CommitLog 写入后如何派生 CQ/Index”做成了明确的事件分发链。

官方 RocketMQ 也有 reput/dispatch 机制，但更多被包裹在 `DefaultMessageStore` 体系中，不像 clean 这样在结构上那么显性。

### 5.6 Timer：clean 的设计更开放，但当前完成度不如官方完整

clean 的 Timer 体系有两套方向：

- 基于时间轮和日志的 `DefaultTimer`
- 预留的 `RocksdbTimer`

`TimerService` 会根据配置选择：

- 关闭 Timer 时走 `BlackHoleTimer`
- 启用 RocksDB 时走 `RocksdbTimer`
- 否则走 `DefaultTimer`

但当前代码里：

- `DefaultTimer` 已具备 `TimerLog + TimerWheel + Recover + Scanner/Adder`
- `RocksdbTimer` 仍是明显未完成的占位实现

官方 RocketMQ 在 `DefaultMessageStore` 中已经明确接入：

- `TimerMessageStore`
- `TimerMessageRocksDBStore`

因此这里的结论是：

- clean 在 Timer 架构上更开放
- 官方在 Timer 能力上更成熟、更完整

### 5.7 HA：clean 已经抽成独立子域，但 README 也承认仍在继续完成

clean 的 Store 侧有：

- `HABootstrap`
- `HAService`
- `DefaultHAServer`
- `DefaultHAClient`
- `CommitLogSynchronizer`

这说明主从同步和高可用方向不是空白。

但 README 也明确写了：

- 主从架构、存算分离“已部分完成”
- Raft 支持“未完成”

官方 RocketMQ 的 README 则直接把高可用写成产品能力，并明确提到基于 `DLedger Controller` 的容错和 HA 选项。

### 5.8 性能结论

clean README 的性能主张是：

- 支持 CommitLog 分片
- 吞吐量目标达到 `5倍+ RocketMQ`

仓库里也确实有专门 benchmark：

- `store/.../InsertBenchmarkTest.java`
- `store/.../CommitLogManagerBenchmarkTest.java`

这些 benchmark 明确在比较：

- 单 CommitLog
- 多线程单 shard
- 多线程多 shard
- 1/2/5/10 shard scaling

因此，clean 的“性能优化”不是停留在宣传层，而是已经体现在代码组织和测试方向中。

但目前仍需保持一个务实判断：

- clean 的性能优势主要来自分片并行写入路径
- 官方的性能优势来自更成熟的完整系统优化
- 如果只测 WAL 写入，clean 很可能更有优势
- 如果测完整生产场景，官方在 HA、异常恢复、运维能力、配套生态上可能更稳

## 6. 交互与部署专题

### 6.1 clean 的 Broker-Store 关系比官方更灵活

`clean-rocketmq` 的 `BrokerConfig` 同时支持：

- `enableEmbedStore`
- `enableRemoteStore`

并且 `StoreBootstrap` 会同时装配：

- `EmbedStoreBootstrap`
- `RemoteStoreBootstrap`

然后由统一的 `MQStore/TopicStore/ConsumeOffsetStore/SubscriptionStore/TimerStore` 门面决定最终访问哪一侧。

这意味着 clean 在 Broker 侧的抽象是：

- 对上层业务暴露统一 Store 接口
- 对底层既可以内嵌，也可以远程

官方 RocketMQ 的对外流量分层则主要体现在 `proxy` 模块：

- `Cluster mode`：Proxy 独立部署，Broker 持有本地存储
- `Local mode`：Proxy 与 Broker 同进程部署

二者方向相似，但切分点不同：

- 官方把“无状态入口”和“有状态 Broker”分开
- clean 把“Broker 业务层”和“Store 存储层”分开

### 6.2 clean 的 gRPC 入口在 Broker 内部，不依赖独立 Proxy

clean 的 Broker 组件注册里，明确直接注册了 `GrpcBootstrap`。

`MessageBootstrap` 又继续拆成：

- `ClientActivity`
- `RouteActivity`
- `ProducerActivity`
- `ConsumerActivity`
- `TransactionActivity`

同时还有：

- `RegisterService`
- `HeartbeatService`
- `TelemetryService`
- `TerminationService`

这说明 clean 的思路是：

- Broker 自己就是 gRPC 对外入口
- 路由、生产、消费、事务在 Broker 内做活动分派

官方 RocketMQ 的 README 和 `proxy/README.md` 明确说明：

- gRPC 是官方重点协议能力
- Proxy 是无状态流量入口
- Proxy 负责连接管理、认证鉴权、日志/追踪/治理

所以在协议入口这一层：

- clean 更短、更直接
- 官方更标准化，也更利于多协议治理

### 6.3 Registry：clean 的注册中心更轻、更接近 NameServer 的最小内核

clean 的 Registry 组件注册只保留了：

- `PropertyManager`
- `KVManager`
- `StoreManager`
- `BrokerManager`
- `RpcManager`

这说明它在元数据层面保持了“尽量轻”的策略。

官方 `NamesrvController` 规模也不算很大，相比 Broker/Store 更轻，但官方仓库还额外有 `controller` 模块承担更复杂的 HA/控制平面能力。

因此：

- clean 的 `registry` 更像一个“增强版轻量 NameServer”
- 官方则把控制能力拆分到了 NameServer 之外的更大体系里

### 6.4 路由改写：clean 已经把 embed/remote 两种路由改写明确写进代码

`RouteService` 很有代表性：

- 如果走 embed store，则把 broker 地址端口替换成当前服务端口
- 如果走 remote store，则把返回路由地址改写成外部地址列表

这段逻辑很重要，因为它说明 clean 的“存算分离”不是停留在概念，而是已经进入客户端可见的路由编排层。

官方 RocketMQ 的同类能力更多由：

- NameServer 路由
- Broker 路由数据
- Proxy 转发与地址治理

共同完成。

### 6.5 交互与部署结论

| 对比点 | clean-rocketmq | Apache RocketMQ | 结论 |
|---|---|---|---|
| gRPC 入口 | Broker 内直接承载 | 更强调 Proxy 统一入口 | clean 更短，官方更标准 |
| 存算分离位置 | Broker/Store 边界 | Proxy/Broker 边界 | clean 下沉更深 |
| 部署灵活性 | embed + remote 双模式 | local proxy + cluster proxy + traditional broker | 两者都灵活，但抽象层次不同 |
| 元数据服务 | 更轻 | 更完整 | clean 更易懂，官方能力更全 |

## 7. 功能专题

### 7.1 事务：clean 把事务当成一等领域，而不是一组零散处理器

clean 的 `TransactionBootstrap` 直接组织了：

- `PrepareService`
- `CommitService`
- `RollbackService`
- `TransactionMessageService`
- `BatchCommitService`
- `ReceiptRegistry`
- `ReceiptCleaner`
- `CheckService`
- `CheckerFactory`

这说明它对事务的建模是完整的领域拆分。

官方 RocketMQ README 明确把事务消息列为核心能力，且是“financial grade transactional message”。但在工程结构上，官方事务实现更多依附于 Broker/Store 的处理器和消息存储主链。

因此事务对比结论是：

- clean 的事务代码结构更适合学习和演化
- 官方的事务语义和生产经验更成熟

### 7.2 定时与调度：clean 目标更大，当前落地仍在持续推进

clean README 明确宣称：

- 更完善的延时、过期、周期、循环等消息调度支持

从代码结构看，它确实在往“更丰富调度”前进：

- Broker 侧有 `TimerBootstrap`
- Store 侧也有 `TimerBootstrap`
- Broker 侧分成 `TimerTaskSaver/Scanner/Scheduler/MessageProducer`
- Store 侧分成 `TimerLog/TimerWheel/Recover`

这比传统 RocketMQ 的“延时消息主要作为一种功能点”更有“独立子系统”味道。

但从完成度看：

- `DefaultTimer` 已经较完整
- `RocksdbTimer` 仍未完成
- README 也承认“更丰富的调度功能”仍在继续

因此这里要给一个平衡判断：

- clean 的调度架构 ambition 更大
- 官方的现有调度能力更成熟

### 7.3 POP/ACK/Invisible：clean 直接把消费状态机拆开了

clean 的消费者域不是一个大模块，而是拆成了：

- `PopBootstrap`
- `AckBootstrap`
- `RenewBootstrap`
- `ReviveBootstrap`

并通过 `ConsumerBootstrap` 统一装配。

`PopBootstrap` 里又继续拆成：

- `ContextBuilder`
- `QueueSelector`
- `BrokerDequeueService`
- `PopService`

`AckBootstrap` 则装配：

- `BrokerAckService`
- `InvisibleService`

这说明 clean 对 POP 消费模型的理解是显式的：

- POP
- ACK
- invisible time
- renew
- revive

这些概念都被提升成了单独对象。

官方 RocketMQ 的 POP 模式同样是核心能力，官方 Proxy 文档明确写到：

- `pop` consumption is natively supported
- normal/fifo/transaction/delay 都可经由 pop 模式支持

同时官方源码中也存在 `PopMessageProcessor`、`AckMessageProcessor` 等演进痕迹。

结论：

- clean 在代码组织上更容易讲清 POP 模型
- 官方在生产语义和兼容面上更成熟

### 7.4 协议和生态功能：official 明显更强

官方 RocketMQ README 当前仍明确列出：

- gRPC、MQTT、JMS、OpenMessaging
- authentication and authorization
- connectors
- dashboard
- operator
- streams
- event bridge

clean 当前则更聚焦：

- RocketMQ 协议兼容
- 服务端核心能力
- 事务、调度、存储、注册中心

因此这里的结论没有悬念：

- 在“消息平台”层面，官方更强
- 在“重写 RocketMQ 服务端核心”这个目标上，clean 更专注

## 8. 性能与工程权衡

### 8.1 clean 的优势

- 组件边界清晰，便于持续重构
- CommitLog 分片是明确的性能突破口
- Broker/Store 解耦天然利于存算分离演进
- gRPC 直接进 Broker，链路更短
- 事务、调度、消费状态机都被做成了清晰子域

### 8.2 clean 的风险或未完项

- 远端 Store 方向还在继续收尾，部分启动/关闭逻辑仍为空
- RocksDB Timer 仍未完成
- README 明确承认主从、Raft、云原生能力还在推进
- 性能结论虽有 benchmark 支撑，但缺少公开的同环境官方对打结果

### 8.3 官方 RocketMQ 的优势

- 功能覆盖广
- 生产经验足
- 多协议生态完整
- Proxy、Controller、TieredStore、Auth 等外围能力成熟
- HA、治理、可观测、生态工具链更完整

### 8.4 官方 RocketMQ 的代价

- 核心类体量大
- 历史演进痕迹重
- 对学习者和二次开发者不够友好
- 某些能力跨模块分布，理解成本高

## 9. 适用场景建议

适合优先研究 `clean-rocketmq` 的场景：

- 想系统学习 RocketMQ 服务端核心
- 想继续做 Broker/Store 解耦
- 想研究 CommitLog 分片和高吞吐写入
- 想在一个更干净的代码底座上继续扩展事务/调度/存算分离

适合优先采用官方 RocketMQ 的场景：

- 需要完整生产能力
- 需要成熟 HA 和外围生态
- 需要控制台、Operator、Connector、Auth、Proxy 等配套
- 需要跟官方客户端和官方运维体系紧密对齐

## 10. 最终判断

如果把两者放在同一个评价框架里，可以得出一个比较客观的判断：

- `clean-rocketmq` 的核心价值，不是“功能比官方更多”，而是“把 RocketMQ 核心做得更清楚、更易演进，并在写入路径和存算分离上往前走了一步”
- 官方 RocketMQ 的核心价值，不是“代码更优雅”，而是“作为一个长期生产项目，已经把协议、生态、治理、HA、外围产品能力做成了完整平台”

因此两者并不是简单的替代关系，而更像：

- 官方 RocketMQ：成熟平台
- clean-rocketmq：面向下一代内核形态的重写实验与工程化探索

## 11. 主要依据

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

- 官方仓库首页：<https://github.com/apache/rocketmq>
- 官方 README：<https://raw.githubusercontent.com/apache/rocketmq/develop/README.md>
- 官方根 `pom.xml`：<https://github.com/apache/rocketmq/blob/develop/pom.xml>
- `BrokerController.java`：<https://github.com/apache/rocketmq/blob/develop/broker/src/main/java/org/apache/rocketmq/broker/BrokerController.java>
- `DefaultMessageStore.java`：<https://github.com/apache/rocketmq/blob/develop/store/src/main/java/org/apache/rocketmq/store/DefaultMessageStore.java>
- `CommitLog.java`：<https://github.com/apache/rocketmq/blob/develop/store/src/main/java/org/apache/rocketmq/store/CommitLog.java>
- `NamesrvController.java`：<https://github.com/apache/rocketmq/blob/develop/namesrv/src/main/java/org/apache/rocketmq/namesrv/NamesrvController.java>
- `proxy/README.md` 页面：<https://github.com/apache/rocketmq/tree/develop/proxy>
