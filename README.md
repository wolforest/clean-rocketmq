# Clean-RocketMQ: 7天能看懂的RocketMQ 
RocketMQ是<strong style="color:#D55F5B;">最简单的</strong>、支持TCC的高性能消息队列，是最佳的<strong style="color:#D55F5B;">高并发学习项目</strong>。<br />
Clean-RocketMQ是从零重写的分支，是与RocketMQ磨合1000多天后的第一次交付.

我们的目标是：
1. :rocket: 兼容RocketMQ5.*协议，完全重写
2. :heart: <strong>更简洁的代码、更清晰的分层</strong>
3. :brain: 先完成第二条，相信第二条是未来的基础

## 项目进度
RocketMQ默认配置下的功能，基本已实现。
![项目进度](/docs/cn/img/wolfmq-progress.png "项目进度")

## 项目起因
RocketMQ是个宝库，核心代码约5万行，却实现众多功能，还扛下了双11的交易洪峰。
> pulsar和kafka代码量都在百万以上，从学习的角度，RocketMQ确实更佳。
> RocketMQ的5W行，是我们重写完后得出的数据。

Clean-RocketMQ就是让这5万行的阅读体验更佳，举个例子让大家理解一下,
下面的截图是发送消息代码，左边是官方实现，右边是Clean-RocketMQ实现。


| 官方实现 | Clean-RocketMQ |
|------|------|
| <img src="/docs/code/rocketmq-sendmsg.png" width="300"> | <img src="/docs/code/wolfmq-sendmsg.png" width="300"> |

## 项目的结构
我们的项目包括开源项目和RocketMQ相关『书库』。<br />
先说开源项目，开源项目包括:
* [Clean-RocketMQ](https://github.com/wolforest/clean-rocketmq) : 完全重写，兼容RocketMQ5.*
* [RocketMQ-wolf](https://github.com/wolforest/rocketmq-wolf) : 项目分支，基于RocketMQ5.2, 生产可用
* [RocketMQ-comment](https://github.com/wolforest/rocketmq-comment) : 官方分支 + 注释

另外是我们围绕RocketMQ的一系列『结构化书库』:
![RocketMQ书库](/docs/cn/img/rocketmq-books.png "RocketMQ书库")



