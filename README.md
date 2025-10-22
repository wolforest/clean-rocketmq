# Clean-RocketMQ: 7天看懂的RocketMQ 
RocketMQ是最佳<strong style="color:#D55F5B;">高并发</strong>学习项目，其核心仅5万行，却扛下了双11，是<strong style="color:#D55F5B;">最简单</strong>的消息队列。<br />
Clean-RocketMQ是从零重写的分支，目的是成为<strong style="color:#D55F5B;">7天看懂</strong>的RocketMQ。

我们的目标是：
1. :rocket: 兼容RocketMQ5.*，完全重写
2. :heart: <strong>更简洁的代码、更清晰的分层</strong>
3. :brain: 先完成第二条，相信第二条是未来的基础

## 项目进度
这是1000天后的第一次交付，RocketMQ默认配置下的功能，基本已实现。
![项目进度](/docs/cn/img/wolfmq-progress.png "项目进度")

## 项目起因
先看个消息发送的例子吧:

| [官方实现](https://github.com/apache/rocketmq/blob/develop/broker/src/main/java/org/apache/rocketmq/broker/processor/SendMessageProcessor.java) | [Clean-RocketMQ实现](https://github.com/wolforest/clean-rocketmq/blob/main/broker/src/main/java/cn/coderule/minimq/broker/api/ProducerController.java)                                  |
|---------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| <img src="/docs/code/rocketmq-sendmsg.png" width="300">                                                                                     | <img src="/docs/code/wolfmq-sendmsg.png" width="300"> |

我们确实没做什么巨大变革，我们也是学习者，Clean-RocketMQ是我们1000天的学习笔记。

## 项目的结构
我们的项目包括开源项目和RocketMQ相关『书库』。<br />
先说开源项目，开源项目包括:
* [Clean-RocketMQ](https://github.com/wolforest/clean-rocketmq) : 完全重写，兼容RocketMQ5.*
* [RocketMQ-wolf](https://github.com/wolforest/rocketmq-wolf) : 项目分支，基于RocketMQ5.2, 生产可用
* [RocketMQ-comment](https://github.com/wolforest/rocketmq-comment) : 官方分支 + 注释

另外是我们围绕RocketMQ的一系列『结构化书库』:
![RocketMQ书库](/docs/cn/img/rocketmq-books.png "RocketMQ书库")



