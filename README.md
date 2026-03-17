# Clean-RocketMQ: 下一代、高性能、事务型消息队列
<img src="/docs/architecture/next-generation-mq.jpg" alt="下一代消息队列" />

Clean-RocketMQ源于、并兼容RocketMQ, 提供:
1. **更高性能：** 5倍+性能提升(mac上测试) 
2. **更全功能：** 存算分离、事务、调度、云原生、...
3. **简洁代码：** 借鉴DDD、简洁思想，从零重写。

RocketMQ核心不超过5万行，是个极其优秀的消息队列。<br />
基于RocketMQ，提高性能、增加功能，并非难事。<br />
真正有挑战的是: 如何保证代码的简洁与可维护。

对抗复杂是软件的核心，AI时代也不会改变。<br />


## 架构
<img src="/docs/architecture/clean-rocketmq.jpg" alt="架构" />



<!-- 

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
