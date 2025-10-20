# Clean-RocketMQ: 7天能看懂的RocketMQ 
RocketMQ是<strong style="color:#D55F5B;">最简单的</strong>高性能消息队列，也是唯一支持TCC的事务型队列。<br />
RocketMQ核心代码约5万行，是最佳的<strong style="color:#D55F5B;">高并发学习项目</strong>, 不过其历史包袱不小。<br />
700多天亲密接触后，我们从零重写，Clean-RocketMQ是300天的第一次交付.

我们的目标是：
1. :rocket: 完全兼容RocketMQ5.*协议
2. :heart: <strong>更简洁的代码、更清晰的分层</strong>
3. :brain: 先完成第二，相信第二是未来的基础

## 当前项目进度
RocketMQ默认配置下的功能，基本已实现。
![项目进度](/docs/cn/img/wolfmq-progress.png "项目进度")


## 项目的结构
我们的项目包括开源项目和RocketMQ相关『书库』。<br />
先说开源项目，开源项目包括:
* [Clean-RocketMQ](https://github.com/wolforest/clean-rocketmq) : 完全重写，兼容RocketMQ5.*
* [RocketMQ-wolf](https://github.com/wolforest/rocketmq-wolf) : 项目分支，基于RocketMQ5.2, 生产可用
* [RocketMQ-comment](https://github.com/wolforest/rocketmq-comment) : 官方分支 + 注释

另外是我们围绕RocketMQ的一系列『结构化书库』:
![RocketMQ书库](/docs/cn/img/rocketmq-books.png "RocketMQ书库")



