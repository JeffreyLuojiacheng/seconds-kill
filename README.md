## 秒杀系统

### 使用技术
Springboot, SpringMVC, MyBatis, Mysql, Zookeeper, Dubbo, Redis, Lua, Rocketmq

### 整体思想

#### 限流
因为秒杀只允许少部分用户抢购成功，所以需要限制大部分的流量，只允许少部分的流量进入到后台系统中

#### 缓存
将原先查询数据的请求改为查询Redis缓存，大大减少数据库的压力

#### 削峰
将瞬时的流量转换成系统可控制的较为平缓的流量，后面的逻辑可根据系统的能力排队处理，这里利用Rocketmq做削峰处理

#### 异步
异步处理，将原先的系统压力分担为多个子系统的压力


### 整体流程图
![](https://github.com/Weiwf/seconds-kill/blob/master/file/pic/%E6%B5%81%E7%A8%8B.jpg)

如上图，整个系统的压力最大的部分是在MQ这里，只要MQ抗住了压力，后面的减库存和下订单都是可以控制的。


