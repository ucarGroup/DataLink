# DataLink
DataLink是一个满足各种异构数据源之间的实时增量同步，分布式、可扩展的数据交换平台。
# 项目背景
着眼于未来，我们的目标是打造一个平台，满足各种异构数据源之间的实时增量同步，支撑公司业务的快速发展。在充分调研的基础之上，我们发现，没有任何一款开源产品能轻易的满足我们的目标，每个产品都有其明显的短板和局限性，所以最终的选项只有"自行设计"。但自行设计并不是凭空设计，现有的数据交换平台、已有的经验、大大小小的开源产品都是我们的设计根基，与其说是自行设计，倒不如说是站在巨人的肩膀上做了一次飞跃。由此诞生了DataLink这样一个产品：
* 满足各种异构数据源之间的实时增量同步
* 平台提供统一的基础设施（高可用、动态负载、同步任务管理、插件管理、监控报警、公用业务组件等等），让设计人员专注于同步插件开发，一次投入，长久受益
* 吸收、整合业内经验，在架构模型、设计方法论、功能特性、可运维、易用性上进行全面的升级，在前瞻性和扩展性上下足功夫，满足未来5-10年内的各种同步需求

DataLink开发时间从2016年12月开始，第一版于2017年5月份上线，在神州优车集团服役到现在，基本上满足了公司所有业务线的同步需求。此次外部开源版本为去除内部依赖后的版本。
#### 目前同步规模：
* 同步数据量360亿+
* 涉及272个数据库实例之间的3208个同步映射
* 60台Worker+2台Manager机器的集群规模
# 项目介绍
名称：DataLink['deitə liŋk]<br><br>
译意： 数据链路，数据（自动）传输器<br><br>
语言： 纯java开发<br><br>
定位： 完成各种异构数据源之间的实时增量同步，一个分布式、可扩展的数据库同步系统
# 工作原理
![]()
原理描述：
* 典型管理系统架构，manager(web管理)+worker(工作节点)<br>
    a. manager负责worker的负载均衡、集群的配置管理和系统监控<br>
    b. worker核心功能是管理Task的生命周期，并配合Manager进行Re-Balance<br>
* Zookeeper：Manager的高可用需要依赖于zookeeper，另外，Task会将运行时信息注册到zookeeper
* Mysql：Datalink的运行需要依赖各种配置信息、以及在运行过程中会动态产生监控和统计数据，统一保存到Mysql中
# QuickStart
See the page for quick start: [QuickStart](https://github.com/ucarGroup/DataLink/wiki/QuickStart)
# 架构&文档
See the page for introduction: [架构&文档](https://github.com/ucarGroup/DataLink/wiki/DataLink%E6%80%BB%E4%BD%93%E6%9E%B6%E6%9E%84)
# 常见问题
See the page for FAQ: [FAQ](https://github.com/ucarGroup/DataLink/wiki/FAQ)
# 相关开源
canal：http://github.com/alibaba/canal<br>
otter：https://github.com/alibaba/otter<br>
Kafka-Connect：https://github.com/apache/kafka<br>
DataBus：https://github.com/linkedin/databus
# 问题反馈
目前有关DataLink的问题交流方式有如下几种，欢迎各位加入进行技术讨论。<br>
qq交流群： 758937055<br>
邮件交流： 845219359@qq.com<br>
报告issue：[issues](https://github.com/ucarGroup/DataLink/issues)
