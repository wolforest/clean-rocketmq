# Clean-RocketMQ
<img src="/docs/cn/img/architecture/next-generation-mq.jpg" alt="下一代消息队列" />

**Clean-RocketMQ 是最快的事务型消息队列。**<br />
1. **兼容RocketMQ：** 可完全或部分替换RocketMQ。 
2. **高性能：** 支持CommitLog分片，5倍+RocketMQ吞吐量提升。
3. **事务型：** 分布式事务(TCC)支持，保障消息发送及调度的一致性。
4. **更完善：** 更完善的延时、过期、周期、循环等消息调度支持。
5. **存算分离：** 更合理的领域层与存储层切分, 也是云原生的基础。
6. **CLEAN：** 引入微框架、DDD思想, 易读易懂。

> Clean-RocketMQ是RocketMQ的传承与优化。<br />
> RocketMQ核心不足5万行，在其基础上提升性能、扩展功能，并不困难。<br />
> 真正的挑战在于:  &nbsp;&nbsp; **如何持续保持代码的简洁与可维护?**

**兄弟项目:**
* [RocketMQ-wolf](https://github.com/wolforest/rocketmq-wolf) : 重构分支, 生产可用，10W+优化，已维护1200+天。
* [RocketMQ-comment](https://github.com/wolforest/rocketmq-comment) : 官方分支 + 纯注释版。
* [Clean-RocketMQ](https://github.com/wolforest/clean-rocketmq) : 从零重写，RocketMQ的传承与进化。

## 项目进度
* [x] 核心功能(pub/sub、顺序/延时消息、事务、注册中心、RPC)
* [x] CommitLog分片
* [ ] 主从架构、存算分离(已部分完成)
* [ ] 更丰富的调度、事务功能
* [ ] Raft支持、云原生

## 做AI时代的软件人
AI已扑面而来，避无可避。好在软件还是软件 —— 手写或是Vibe。<br />
软件服务真实业务，也依然是软件人的立身之本, AI时代也不例外。

与RocketMQ已磨合1200多天，并用400天从零实现了Clean-RocketMQ。<br />
我们也准备好了，再走10年、20年、35年 ...

# 社区
AI时代，软件人并不轻松: 内卷、年龄危机、AI冲击.... <br />
当然焦虑没有用，拥抱AI, 持续体系化学习才有可能应对时代的挑战。

社区是高效的学习加速器，有兴趣的可以加入我们：
* [RocketMQ学习圈](https://wx.zsxq.com/group/28882125582281): 学透RocketMQ, 掌握高并发。
* [软件读书会](https://wx.zsxq.com/group/28851142528481): 每周一本，从技术到能力。

### RocketMQ学习圈

### 软件读书会
<img src="/docs/cn/img/architecture/software-reading-community.png" alt="软件读书会" />


## 更多信息
**Clean-RocketMQ:**
* [Clean-RocketMQ架构](/docs/cn/architecture/logical.md)

**图书目录及进度:**
* [RocketMQ源码解析与优化(上)](/docs/book/rocketmq-analyze.md)
* [RocketMQ源码解析与优化(下)](/docs/book/rocketmq-refactor.md)

<!-- 

## 更多信息

AI时代充满不确定性。开源、读书、写作，是为数不多、可积累、可复利的确定性:
* 参与顶级开源项目，提高解决方案能力 
* 读书沉淀知识体系，提高智能应用能力 
* 写作锻炼表达能力，提高智能协作能力

AI时代，软件人并不轻松: 内卷、年龄危机、AI冲击.... <br />

AI时代，软件依然是软件，仍需解决真实的业务问题。<br />
而软件背后的人，必须为其负责 —— 无论是手写，还是 Vibe。

这不只是一个项目，它是我们的兄弟<br />
是愿意并肩走很久、走很远的兄弟。<br />
也希望有更多的软件人一起同行，走得更久、更远。

但焦虑没有价值，真正能依赖的，只有软件与我们自己:
* 做出有价值的软件
* 持续提升自身能力

## 架构
<img src="/docs/architecture/clean-rocketmq.jpg" alt="架构" />

Clean-RocketMQ是RocketMQ的升级版, 架构上差异不大，只是将存算分离下沉到了存储层。

<img src="/docs/architecture/cloud-rocketmq.jpg" alt="云原生" />
## 开源 & 商业化
开源我们是认真的，商业化是我们的策略。<br />
代码开源 + 商业化运营，我们希望把技术创业这条路走通、走好、走长久...

RocketMQ核心不超过5万行，是最简单、最合适的学习项目。<br />
基于RocketMQ，我们搭建了真并发的学习社区，另外Clean-RocketMQ的商业模式也在探索中。

### 高并发学习社区
如下图所示，我们提供：
* 基于RocketMQ, 体系化学习高并发。
* 读书、交流、写作，闭环式学习。

![社区](/docs/architecture/community.png "社区")


AI降低了知识获取的难度，也提高了知识人的生存下线。<br />
**终身学习**是知识人愿意、或不愿意都要面对的**要求**。

不过读懂代码只是第一步，读书、交流必不可少。<br />
写作输出是进阶学习与扩大影响力的重要手段。

我们先学为敬：
* 输出RocketMQ相关知识体系，源码理解、方案改进、专题思考...
* 输出技术类图书，阅读思考、及沉淀百科化软件知识体系。
* 期待你的交流、并参与软件知识体系的搭建。

代码能力是本分，技术管理者也逃不开。<br />
读书、交流必不可少，写作输出、分享，是

这确实是我们重写RocketMQ的真实原因。<br />
要有能力造出AI做不出来的东西，未来才属于我们。

软件高手的比例不会超过10%，尤其在中国。<br />
AI也没有强大到可生成万物，技术人依然有大量的机会...



### 高性能 + 全功能 的消息队列
先说高性能，Kafka/Pulsar性能强悍，功能限于Pub/Sub。<br />
Clean-RocketMQ改进了WAL机制，性能可以达到Kafka的量级。

再说全功能，RocketMQ基于多队列架构，很容易扩展出如下功能。
<img src="/docs/architecture/rocketmq-feature.jpg" width="500" alt="特性" />

最后云原生，借助云的存储能力，轻松实现高可用，<br />
还可以有效地平衡性能与成本，为商业化铺平了道路。<br />
如下图所示，云原生后，broker/NameServer都可以实现无状态化。

-->
