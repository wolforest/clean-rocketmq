# Clean-RocketMQ: 7天看懂的RocketMQ 
RocketMQ是最佳<strong style="color:#D55F5B;">高并发</strong>学习项目，其核心仅5万行，却扛下了双11，是<strong style="color:#D55F5B;">最简单</strong>的消息队列。<br />
Clean-RocketMQ是从零重写的分支，目的是成为<strong style="color:#D55F5B;">7天看懂</strong>的RocketMQ。

我们的目标是：
1. :rocket: 兼容RocketMQ5.*，完全重写
2. :heart: <strong>更简洁的代码、更清晰的分层</strong>
3. :brain: 先完成第二条，相信第二条是未来的基础

### 项目进度
这是1000天后的第一次交付，RocketMQ默认配置下的功能，基本已实现。
![项目进度](/docs/cn/img/wolfmq-progress.png "项目进度")

## 项目起因
我们确实没做什么，我们也是学习者，Clean-RocketMQ是我们1000天的学习笔记。<br />
我们借用了Uncle Bob的Clean概念，希望这里成为Clean概念的实践场。<br />
Clean是目标、方向，『7天看懂』是我们的第一个里程碑。

### RocketMQ的价值
相比pulsar、kafka超百万行的规模，RocketMQ5万行的极简架构，是更佳的学习起点。<br />
如下是我们想深入学习的知识点，每一个小点都可深挖成一本书。
![RocketMQ书库](/docs/cn/img/rocketmq-books.png "RocketMQ书库")

### 为什么重写RocketMQ
先看个消息发送的例子吧:

| [官方实现](https://github.com/apache/rocketmq/blob/develop/broker/src/main/java/org/apache/rocketmq/broker/processor/SendMessageProcessor.java) | [Clean-RocketMQ实现](https://github.com/wolforest/clean-rocketmq/blob/main/broker/src/main/java/cn/coderule/minimq/broker/api/ProducerController.java)                                  |
|---------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| <img src="/docs/code/rocketmq-sendmsg.png" width="300">                                                                                     | <img src="/docs/code/wolfmq-sendmsg.png" width="300"> |

### 阅读路线图
![阅读地图](/docs/cn/img/learn-map.jpg "阅读路线图")


## 开源商业模式
我们坦白这是个商业化项目：
* 代码开源免费，大家共同学习
* 服务收费：3年10本深挖RocketMQ的电子书 + 知识社区

先说开源项目，开源项目包括:
* [Clean-RocketMQ](https://github.com/wolforest/clean-rocketmq) : 完全重写，兼容RocketMQ5.*
* [RocketMQ-wolf](https://github.com/wolforest/rocketmq-wolf) : 项目分支，基于RocketMQ5.2, 生产可用
* [RocketMQ-comment](https://github.com/wolforest/rocketmq-comment) : 官方分支 + 注释





