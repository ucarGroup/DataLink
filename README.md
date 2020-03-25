## 项目介绍
DataLink是一个满足各种异构数据源之间的实时增量同步、离线全量同步，分布式、可扩展的数据交换平台。
#### 项目背景
着眼于未来，我们的目标是打造一个平台，满足各种异构数据源之间的实时增量同步和离线全量同步，支撑公司业务的快速发展。在充分调研的基础之上，我们发现，没有任何一款开源产品能轻易的满足我们的目标，每个产品都有其明显的短板和局限性，所以最终的选项只有"自行设计"。但自行设计并不是凭空设计，现有的数据交换平台、已有的经验、大大小小的开源产品都是我们的设计根基，与其说是自行设计，倒不如说是站在巨人的肩膀上做了一次飞跃。由此诞生了DataLink这样一个产品：
* 满足各种异构数据源之间的实时增量同步和离线全量同步
* 平台提供统一的基础设施（高可用、动态负载、同步任务管理、插件管理、监控报警、公用业务组件等等），让设计人员专注于同步插件开发，一次投入，长久受益
* 吸收、整合业内经验，在架构模型、设计方法论、功能特性、可运维、易用性上进行全面的升级，在前瞻性和扩展性上下足功夫，满足未来5-10年内的各种同步需求

DataLink开发时间从2016年12月开始，第一版于2017年5月份上线，在神州优车集团服役到现在，基本上满足了公司所有业务线的同步需求。此次外部开源版本为去除内部依赖后的版本。
#### 内部使用情况
* 支持了神州优车和瑞幸咖啡的核心业务运行
* 涉及500多个数据库实例之间的6000+个同步映射
* 100台Worker+2台Manager机器的集群规模
* 日均数据同步量TB级
#### 当前规划
整合业内更多的产品经验，打造一个全新的平台，功能丰富程度、扩展性、标准化层面更上一层楼
## 相关文章
神州优车数据交换平台的架构、建设与痛点难点详解 <br>
https://mp.weixin.qq.com/s/BVuDbS-2Ra5pIJ7oV78FBA <br>
（神州优车）大数据平台建设经验分享 <br>
https://www.cnblogs.com/ucarinc/p/12091053.html
## 总体架构
![总体架构](https://github.com/ucarGroup/DataLink/blob/master/img/0-0.%E5%85%A8%E9%87%8F%26%E5%A2%9E%E9%87%8F%E6%80%BB%E4%BD%93%E6%9E%B6%E6%9E%84.png)
## 工作原理(增量)
![基础架构](https://github.com/ucarGroup/DataLink/blob/master/img/0-0.%E6%9E%B6%E6%9E%84%E5%8E%9F%E7%90%86.png)
* 典型Master-Slave式系统架构，Manager(Web管理)+Worker(工作节点)<br>
    a. Manager负责worker的负载均衡、集群的配置管理和系统监控<br>
    b. Worker核心功能是管理Task的生命周期，并配合Manager进行Re-Balance<br>
* Zookeeper：Manager的高可用需要依赖于Zookeeper，另外，Task会将运行时信息注册到Zookeeper
* Mysql：Datalink的运行需要依赖各种配置信息、以及在运行过程中会动态产生监控和统计数据，统一保存到Mysql中
## QuickStart
See the page for quick start: [QuickStart](https://github.com/ucarGroup/DataLink/wiki/0.0_QuickStart)
## 架构&文档
See the page for introduction: [架构&文档](https://github.com/ucarGroup/DataLink/wiki/1.0_DataLink%E6%80%BB%E4%BD%93%E6%9E%B6%E6%9E%84)
## 常见问题
See the page for FAQ: [FAQ](https://github.com/ucarGroup/DataLink/wiki/FAQ)
## 类似开源
canal：http://github.com/alibaba/canal<br>
otter：https://github.com/alibaba/otter<br>
Kafka-Connect：https://github.com/apache/kafka<br>
DataBus：https://github.com/linkedin/databus
## 版本历史
当前最新版本为1.0.2-beta，版本发布历史如下：<br>
https://github.com/ucarGroup/DataLink/releases/
## 问题反馈
目前有关DataLink的问题交流方式有如下几种，欢迎各位加入进行技术讨论。<br>
qq交流群： 758937055<br>
邮件交流： tech_plat_data@ucarinc.com<br>
报告issue：[issues](https://github.com/ucarGroup/DataLink/issues)<br>
![](https://github.com/ucarGroup/DataLink/blob/master/img/DataLink%E4%BA%A4%E6%B5%81%E7%BE%A4%E7%BE%A4%E4%BA%8C%E7%BB%B4%E7%A0%81.png)
<pre name="code" class="java">
</pre>
