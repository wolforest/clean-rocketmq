# Clean-RocketMQ
Clean-RocketMQ是一个RocketMQ从零重写的版本。
Clean-RocketMQ完全兼容RocketMQ5.*版本，是RocketMQ的传承与优化版本。

### Clean-RocketMQ特征:
1. **更简洁的代码：** 借鉴DDD、简洁架构思想，从零重写。
2. **更好的性能：** 高性能模式追平Kafka(Mac下2~5倍的pub性能).
3. **更全的功能：** 基于多队列架构，扩展出更多事务、调度相关队列功能。
4. **存算分离：** 事务、调度等业务逻辑无状态化，存储及HA下沉。
5. **云原生：** 云提供了强大的存储能力，后期将尝试云原化。


## 架构


