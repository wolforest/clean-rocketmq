# Clean-RocketMQ: 7天看懂的RocketMQ 
RocketMQ是最佳<strong style="color:#D55F5B;">高并发</strong>学习项目，其核心仅5万行，却扛下了双11，是<strong style="color:#D55F5B;">最简单</strong>的消息队列。<br />
Clean-RocketMQ是从零重写的分支，第一步是成为<strong style="color:#D55F5B;">7天看懂</strong>的RocketMQ。

### 项目目标：
1. :rocket: 兼容RocketMQ5.*，完全重写
2. :heart: <strong>更简洁的代码、更清晰的分层</strong>
3. :brain: 先完成第二条，相信第二条是未来的基础

### 项目进度
这是1000天后的第一次交付，RocketMQ默认配置下的功能，基本已实现。
![项目进度](/docs/cn/img/wolfmq-progress.png "项目进度")

## 项目起因
我们确实也只是学习者，Clean-RocketMQ是我们1000天的学习笔记。<br />
Bob大大叔的Clean是极高的标准，是我们的目标、是我们的方向。<br />
这里便是Clean的实践场，『7天看懂』是我们的第一个里程碑。

### RocketMQ的价值
相比pulsar、kafka超百万行的规模，RocketMQ5的万行，是更好的学习起点。<br />
但扛下双11的绝对是大成之作，而我们的文字笔记，可以形成十多本的『<strong style="color:#D55F5B;">书库</strong>』。
![RocketMQ书库](/docs/cn/img/rocketmq-books.png "RocketMQ书库")

### 重写RocketMQ
为什么重写呢，先看个消息发送的例子：

| [官方实现](https://github.com/apache/rocketmq/blob/develop/broker/src/main/java/org/apache/rocketmq/broker/processor/SendMessageProcessor.java) | [Clean-RocketMQ实现](https://github.com/wolforest/clean-rocketmq/blob/main/broker/src/main/java/cn/coderule/minimq/broker/api/ProducerController.java)                                  |
|---------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| <img src="/docs/code/rocketmq-sendmsg.png" width="300">                                                                                     | <img src="/docs/code/wolfmq-sendmsg.png" width="300"> |

确实，我们没做什么，只是优化了代码和分层。<br />
我们借用了Bob大叔Clean这个极高的标准，作为我们的目标和方向。<br />
我们知道300天的重写只是第一步，但我们相信代码质量、相信未来会来。 <br />
我们也希望成为RocketMQ的2.0，以代码质量驱动的未来分支。

1000天中我们与RocketMQ的缘分为如下三个项目：
* [Clean-RocketMQ](https://github.com/wolforest/clean-rocketmq) : 完全重写，兼容RocketMQ5.*
* [RocketMQ-wolf](https://github.com/wolforest/rocketmq-wolf) : 重构分支，兼容RocketMQ5.2, 生产可用
* [RocketMQ-comment](https://github.com/wolforest/rocketmq-comment) : 官方分支 + 注释

### 7天路线图
不熟悉RocketMQ的同学，可以参考下面的路线图
![阅读地图](/docs/cn/img/learn-map.jpg "阅读路线图")


## 2小时看懂
我们试着多个角度从RocketMQ中获取养分：
* 源码线
* 问题线
* 知识线

### RocketMQ500问
问题驱动是AI时代的要求，提出一个好问题并不容易，不过我们愿尽力一试，<br />
为什么是500问呢？这可能是我们这个小团队的一个边界。 <br />
技术是一张大网，从任何一点出发，可以到达任何终点。<br />
我们希望做问题的种子，在社区的滋润下长成5000问、50000问、...

### 系列书库
> 左手代码，右手诗； 不是目标，是要求。

文字和代码都是工程师的基本功。能把逻辑写(讲)清楚，是团队合作的基础。<br />
我们借着学习的机会，把我们的学习笔记，结构化成书、成册。<br />
过程其实是蛮痛的，但写着写着，就把自己写明白了。<br />
希望有更多的小伙伴参与进来，感受『代码』+ 『文字』的双重痛苦与双重收获。<br />

当然，罗马并非一日建成，我们需要一个计划

### 深入学习
> 能速成的只有入门，掌握一门技术需要时间和耐心。

工程技术没有秘密，书、文章都是传播的载体，但不能忽视的是『交流』。<br />
很多先进的技术都是以『交流』的形式传递的。

另外，对于工程技术而言，『手感』亦是极为重要。<br />
经历过远离一线的技术Leader瞎指挥的人，都深有体会。

技术的世界里，没有银弹。<br />
学无止境，读书、交流、代码、... 

加入我们，互相学习、共同进步



