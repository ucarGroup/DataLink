/*
MySQL
Database : ucar_datalink
*/
CREATE DATABASE IF NOT EXISTS `ucar_datalink` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

USE `ucar_datalink`;
-- ----------------------------
-- Table structure for `t_dl_group`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_group`;
CREATE TABLE `t_dl_group` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `group_name` varchar(50) NOT NULL COMMENT '分组名称',
  `group_desc` varchar(100) NOT NULL COMMENT '分组描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='分组配置表';

-- ----------------------------
-- Table structure for `t_dl_interceptor`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_interceptor`;
CREATE TABLE `t_dl_interceptor` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `interceptor_name` varchar(30) NOT NULL COMMENT '拦截器名称',
  `interceptor_desc` varchar(100) NOT NULL COMMENT '拦截器描述',
  `interceptor_type` varchar(15) NOT NULL COMMENT '拦截器类型',
  `interceptor_content` text NOT NULL COMMENT '拦截器内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='拦截器配置表';

-- ----------------------------
-- Table structure for `t_dl_media`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_media`;
CREATE TABLE `t_dl_media` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `media_namespace` varchar(100) NOT NULL COMMENT '介质命名空间',
  `media_name` varchar(100) NOT NULL COMMENT '介质名称',
  `media_source_id` bigint(20) NOT NULL COMMENT '介质所属数据源ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `media_uidx1` (`media_namespace`,`media_name`,`media_source_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='介质定义表';

-- ----------------------------
-- Table structure for `t_dl_media_mapping`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_media_mapping`;
CREATE TABLE `t_dl_media_mapping` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint(20) unsigned NOT NULL COMMENT '任务ID',
  `source_media_id` bigint(20) unsigned NOT NULL COMMENT '源介质ID',
  `target_media_source_id` bigint(20) unsigned NOT NULL COMMENT '目标数据源ID',
  `target_media_name` varchar(100) DEFAULT NULL COMMENT '目标介质名称',
  `target_media_namespace` varchar(50) DEFAULT NULL COMMENT '目标介质命名空间',
  `parameter` text NOT NULL COMMENT '映射参数',
  `column_mapping_mode` varchar(10) DEFAULT NULL COMMENT '列映射模式',
  `write_weight` bigint(20) NOT NULL COMMENT '同步权重优先级',
  `is_valid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否有效',
  `interceptor_id` bigint(20) unsigned DEFAULT NULL COMMENT '拦截器ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  `join_column` varchar(20) DEFAULT NULL COMMENT '多表聚合列名',
  `es_use_prefix` tinyint(1) NOT NULL DEFAULT '1' COMMENT '同步到es时列名是否加表前缀',
  `geo_position_conf` varchar(5000) DEFAULT NULL COMMENT 'es地理位置合并配置',
  `skip_ids` varchar(200) DEFAULT NULL COMMENT '要跳过的主键ids',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx1_mediamapping` (`task_id`,`source_media_id`,`target_media_source_id`),
  KEY `mapping_index_taskId` (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='同步映射配置表';

-- ----------------------------
-- Table structure for `t_dl_media_mapping_column`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_media_mapping_column`;
CREATE TABLE `t_dl_media_mapping_column` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `source_column` varchar(200) DEFAULT NULL COMMENT '源列名称',
  `target_column` varchar(200) DEFAULT NULL COMMENT '目标列名称',
  `media_mapping_id` bigint(20) NOT NULL COMMENT '所属同步映射ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_media_mapping_id` (`media_mapping_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='列映射配置表';

-- ----------------------------
-- Table structure for `t_dl_media_source`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_media_source`;
CREATE TABLE `t_dl_media_source` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `ms_name` varchar(50) NOT NULL COMMENT '数据源名称',
  `ms_type` varchar(20) NOT NULL COMMENT '数据源类型',
  `ms_desc` varchar(50) NOT NULL COMMENT '数据源描述',
  `ms_parameter` text NOT NULL COMMENT '数据源参数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_ms_name` (`ms_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='介质源定义表';

-- ----------------------------
-- Table structure for `t_dl_menu`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_menu`;
CREATE TABLE `t_dl_menu` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(20) NOT NULL COMMENT '编码',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `parent_code` varchar(20) NOT NULL COMMENT '父节点编码',
  `type` varchar(20) NOT NULL COMMENT '菜单类型',
  `url` varchar(200) NOT NULL COMMENT '路径',
  `icon` varchar(20) NOT NULL COMMENT '标识',
  `create_time` datetime NOT NULL COMMENT '新建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='菜单表';


-- ----------------------------
-- Table structure for `t_dl_meta_mapping`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_meta_mapping`;
CREATE TABLE `t_dl_meta_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '表id，主键',
  `src_media_source_type` varchar(20) DEFAULT NULL COMMENT '源端类型',
  `target_media_source_type` varchar(20) DEFAULT NULL COMMENT '目标端类型',
  `src_mapping_type` varchar(50) DEFAULT NULL COMMENT '源端字段类型',
  `target_mapping_type` varchar(50) DEFAULT NULL COMMENT '目标端字段类型',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `META_MAPPING_COMBINATION_INDEX` (`src_media_source_type`,`target_media_source_type`,`src_mapping_type`,`target_mapping_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='元数据映射表';

-- ----------------------------
-- Table structure for `t_dl_monitor`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_monitor`;
CREATE TABLE `t_dl_monitor` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `resource_id` bigint(20) NOT NULL COMMENT '监控项目ID',
  `is_effective` tinyint(1) NOT NULL COMMENT '是否有效',
  `threshold` int(11) NOT NULL COMMENT '阀值',
  `interval_time` bigint(20) NOT NULL COMMENT '报警间隔时间',
  `receive_people` varchar(100) DEFAULT NULL COMMENT '报警接收人',
  `monitor_type` tinyint(1) NOT NULL COMMENT '监控类型',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '修改时间',
  `monitor_range` varchar(50) DEFAULT NULL COMMENT '监控时间范围',
  `monitor_cat` tinyint(1) NOT NULL COMMENT '监控类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_task_id_type_cat` (`resource_id`,`monitor_type`,`monitor_cat`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='监控配置表';


-- ----------------------------
-- Table structure for `t_dl_role`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_role`;
CREATE TABLE `t_dl_role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(20) NOT NULL COMMENT '编码',
  `name` varchar(50) NOT NULL COMMENT '名称',
  `create_time` datetime NOT NULL COMMENT '新建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx_code` (`code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='角色表';


-- ----------------------------
-- Table structure for `t_dl_role_authority`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_role_authority`;
CREATE TABLE `t_dl_role_authority` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `create_time` datetime NOT NULL COMMENT '新建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='角色菜单权限表';


-- ----------------------------
-- Table structure for `t_dl_sys_properties`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_sys_properties`;
CREATE TABLE `t_dl_sys_properties` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `properties_key` varchar(20) NOT NULL COMMENT '参数key',
  `properties_value` varchar(50) NOT NULL COMMENT '参数值',
  `create_time` datetime NOT NULL COMMENT '新建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_properties_key` (`properties_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='系统参数表';


-- ----------------------------
-- Table structure for `t_dl_task`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task`;
CREATE TABLE `t_dl_task` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_desc` varchar(50) NOT NULL COMMENT '任务描述',
  `task_parameter` text NOT NULL COMMENT '任务参数',
  `reader_media_source_id` bigint(20) unsigned NOT NULL COMMENT 'Reader关联的mediasourceid (冗余)',
  `task_reader_parameter` text NOT NULL COMMENT 'TaskReader参数',
  `task_writer_parameter` text NOT NULL COMMENT 'TaskWriter参数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '修改时间',
  `group_id` bigint(20) unsigned NOT NULL COMMENT '所属组ID',
  `target_state` varchar(20) NOT NULL COMMENT '目标状态',
  `is_delete` varchar(5) NOT NULL DEFAULT 'false' COMMENT '是否已删除',
  `task_type` varchar(20) NOT NULL COMMENT '任务类型',
  `leader_task_id` bigint(20) unsigned DEFAULT NULL COMMENT 'leader task id',
  `is_leader_task` char(1) NOT NULL DEFAULT '0' COMMENT '是否是leader task',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_task_name` (`task_name`),
  KEY `idx_reader_media_source_id` (`reader_media_source_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='同步任务配置表';


-- ----------------------------
-- Table structure for `t_dl_task_delaytime`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task_delaytime`;
CREATE TABLE `t_dl_task_delaytime` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `delay_time` bigint(20) NOT NULL COMMENT '延迟时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `task_id` bigint(20) NOT NULL COMMENT '任务id',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='同步任务数据延迟记录表';


-- ----------------------------
-- Table structure for `t_dl_task_exception`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task_exception`;
CREATE TABLE `t_dl_task_exception` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint(20) NOT NULL COMMENT '任务id',
  `worker_id` bigint(20) NOT NULL COMMENT '机器id',
  `exception_detail` mediumtext COMMENT '异常信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id_create_time` (`task_id`,`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='同步任务异常统计表';

-- ----------------------------
-- Table structure for `t_dl_task_position`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task_position`;
CREATE TABLE `t_dl_task_position` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint(20) NOT NULL COMMENT '任务id',
  `task_position` text COMMENT '任务消费位点',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_task_id` (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='同步任务消费位点统计表';


-- ----------------------------
-- Table structure for `t_dl_task_statistic`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task_statistic`;
CREATE TABLE `t_dl_task_statistic` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` bigint(20) NOT NULL COMMENT '任务id',
  `records_per_minute` bigint(20) NOT NULL DEFAULT '0' COMMENT '每分钟同步条数',
  `size_per_minute` bigint(20) NOT NULL DEFAULT '0' COMMENT '每分钟同步流量',
  `write_time_per_record` decimal(20,2) NOT NULL DEFAULT '0.00' COMMENT '写入平均耗时',
  `exceptions_per_minute` bigint(20) NOT NULL DEFAULT '0' COMMENT '每分钟同步异常个数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_write_count_per_minute` bigint(20) NOT NULL DEFAULT '0' COMMENT '每分钟读写次数',
  PRIMARY KEY (`id`),
  KEY `idx_task_id_create_time` (`task_id`,`create_time`) USING BTREE,
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='同步任务性能统计表';


-- ----------------------------
-- Table structure for `t_dl_taskstatus_mismatch_log`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_taskstatus_mismatch_log`;
CREATE TABLE `t_dl_taskstatus_mismatch_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `task_id` bigint(20) unsigned NOT NULL COMMENT '同步任务id',
  `worker_id` bigint(20) unsigned NOT NULL COMMENT '所属机器id',
  `action_type` varchar(20) NOT NULL COMMENT '操作类型',
  `local_status` text COMMENT '本地状态',
  `remote_status` text COMMENT '远程状态',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='任务状态冲突记录表';

-- ----------------------------
-- Table structure for `t_dl_user`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_user`;
CREATE TABLE `t_dl_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_name` varchar(50) NOT NULL COMMENT '用户名称',
  `ucar_email` varchar(50) DEFAULT NULL COMMENT '集团邮箱',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `create_time` datetime NOT NULL COMMENT '新建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  `is_alarm` varchar(5) NOT NULL DEFAULT 'false' COMMENT '是否发送报警',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx_email` (`ucar_email`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户表';

-- ----------------------------
-- Table structure for `t_dl_user_role`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_user_role`;
CREATE TABLE `t_dl_user_role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `create_time` datetime NOT NULL COMMENT '新建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='用户角色关系表';

-- ----------------------------
-- Table structure for `t_dl_worker`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_worker`;
CREATE TABLE `t_dl_worker` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `worker_name` varchar(50) NOT NULL COMMENT 'Worker名称',
  `worker_desc` varchar(50) NOT NULL COMMENT 'Worker描述',
  `worker_address` varchar(50) NOT NULL COMMENT 'Worker网络地址',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '修改时间',
  `group_id` bigint(20) unsigned NOT NULL COMMENT '所属组ID',
  `rest_port` int(10) unsigned NOT NULL DEFAULT '8083' COMMENT 'rest调用端口',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Worker信息配置表';


-- ----------------------------
-- Table structure for `t_dl_worker_jvm_state`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_worker_jvm_state`;
CREATE TABLE `t_dl_worker_jvm_state` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `worker_id` bigint(20) NOT NULL COMMENT 'worker主键id',
  `host` varchar(20) NOT NULL COMMENT 'worker机器ip',
  `old_mem_used` bigint(20) DEFAULT NULL COMMENT '老年代内存使用大小',
  `old_mem_max` bigint(20) DEFAULT NULL COMMENT '老年代最大堆内存',
  `young_mem_max` bigint(20) DEFAULT NULL COMMENT '新生代内存大小',
  `young_mem_used` bigint(20) DEFAULT NULL COMMENT '新生代内存使用',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `interval_old_collection_count` bigint(20) DEFAULT NULL COMMENT '老年代一分钟内的垃圾回收次数',
  `interval_young_collection_count` bigint(20) DEFAULT NULL COMMENT '新生代一分钟内的垃圾回收次数',
  `interval_old_collection_time` bigint(20) DEFAULT '0' COMMENT '老年代一分钟内的垃圾回收时间',
  `interval_young_collection_time` bigint(20) DEFAULT '0' COMMENT '新生代一分钟内垃圾回收时间',
  `current_thread_count` bigint(20) DEFAULT '0' COMMENT '当前线程数',
  PRIMARY KEY (`id`),
  KEY `IDX_COLLECTTIME` (`create_time`),
  KEY `IDX_WORKERID` (`worker_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='worker机器JVM状态统计';

-- ----------------------------
-- Table structure for `t_dl_worker_system_state`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_worker_system_state`;
CREATE TABLE `t_dl_worker_system_state` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `worker_id` bigint(20) NOT NULL COMMENT 'worker主键id`',
  `host` varchar(20) NOT NULL COMMENT 'worker机器ip',
  `load_average` decimal(20,2) NOT NULL DEFAULT '0.00' COMMENT '一分钟平均负载',
  `user_cpu_utilization` decimal(20,2) NOT NULL DEFAULT '0.00' COMMENT '用户CPU使用率',
  `sys_cpu_utilization` decimal(20,2) NOT NULL DEFAULT '0.00' COMMENT '系统CPU使用率',
  `incoming_network_traffic` bigint(20) NOT NULL DEFAULT '0' COMMENT '一分钟内接收的字节数',
  `outgoing_network_traffic` bigint(20) NOT NULL DEFAULT '0' COMMENT '一分钟内发送的字节数',
  `tcp_current_estab` bigint(20) NOT NULL DEFAULT '0' COMMENT '当前TCP连接数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `IDX_COLLECTTIME` (`create_time`),
  KEY `IDX_WORKERID` (`worker_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='worker机器系统状态统计';

-- ----------------------------
-- 脚本初始化
-- ----------------------------
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001000000', '集群管理', '000000000', 'NODE', '', 'fa-cloud', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001001000', '分组管理', '001000000', 'LEAF', '/group/groupList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001001001', '跳转分组列表', '001001000', 'ACTION', '/group/initGroup', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001001002', '跳转新增页面', '001001000', 'ACTION', '/group/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001001003', '分组新增', '001001000', 'ACTION', '/group/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001001004', '跳转修改页面', '001001000', 'ACTION', '/group/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001001005', '分组修改', '001001000', 'ACTION', '/group/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001001006', '分组删除', '001001000', 'ACTION', '/group/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002000', '机器管理', '001000000', 'LEAF', '/worker/workerList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002001', '跳转机器列表', '001002000', 'ACTION', '/worker/initWorker', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002002', '跳转新增页面', '001002000', 'ACTION', '/worker/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002003', '机器新增', '001002000', 'ACTION', '/worker/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002004', '跳转修改页面', '001002000', 'ACTION', '/worker/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002005', '机器修改', '001002000', 'ACTION', '/worker/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002006', '机器删除', '001002000', 'ACTION', '/worker/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002000000', '介质管理', '000000000', 'NODE', '', 'fa-folder-open', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001000', 'RDBMS', '002000000', 'LEAF', '/mediaSource/mediaSourceList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001001', '跳转RDBMS列表', '002001000', 'ACTION', '/mediaSource/initMediaSource', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001002', '跳转新增RDBMS页面', '002001000', 'ACTION', '/mediaSource/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001003', '新增RDBMS', '002001000', 'ACTION', '/mediaSource/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001004', '跳转修改RDBMS页面', '002001000', 'ACTION', '/mediaSource/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001005', '修改RDBMS', '002001000', 'ACTION', '/mediaSource/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001006', '删除RDBMS', '002001000', 'ACTION', '/mediaSource/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001007', '验证数据源', '002001000', 'ACTION', '/mediaSource/checkDbContection', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002000', 'HBase', '002000000', 'LEAF', '/hbase/hbaseList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003000', 'ElasticSearch', '002000000', 'LEAF', '/es/esList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('003000000', '同步管理', '000000000', 'NODE', '', 'fa-exchange', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('003002000', '脚本检测', '003000000', 'LEAF', '/sync/relation/toCheckSql', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('003003000', '同步检测', '003000000', 'LEAF', '/sync/relation/show', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004000000', '增量任务', '000000000', 'NODE', '', 'fa-link', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010000', 'Task管理', '004000000', 'LEAF', '', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010100', 'MysqlTask', '004010000', 'LEAF', '/mysqlTask/mysqlTaskList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010101', '跳转新增MysqlTask页面', '004010100', 'ACTION', '/mysqlTask/toAddMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010102', '新增MysqlTask', '004010100', 'ACTION', '/mysqlTask/doAddMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010103', '跳转修改MysqlTask页面', '004010100', 'ACTION', '/mysqlTask/toUpdateMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010104', '修改MysqlTask', '004010100', 'ACTION', '/mysqlTask/doUpdateMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010105', '删除MysqlTask', '004010100', 'ACTION', '/mysqlTask/deleteMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010106', '暂停运行MysqlTask', '004010100', 'ACTION', '/mysqlTask/pauseMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010107', '恢复运行MysqlTask', '004010100', 'ACTION', '/mysqlTask/resumeMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010108', '重启MysqlTask', '004010100', 'ACTION', '/mysqlTask/toRestartMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010109', '执行重启MysqlTask', '004010100', 'ACTION', '/mysqlTask/doRestartMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010110', '跳转MysqlTask列表', '004010100', 'ACTION', '/mysqlTask/mysqlTaskDatas', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010200', 'HBaseTask', '004010000', 'LEAF', '/hbaseTask/hbaseTaskList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004020000', '映射管理', '004000000', 'LEAF', '/mediaMapping/mediaSourceList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004020100', '跳转映射列表', '004020000', 'ACTION', '/mediaMapping/initMediaMapping', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004020200', '跳转新增映射页面', '004020000', 'ACTION', '/mediaMapping/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004020300', '新增映射', '004020000', 'ACTION', '/mediaMapping/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004020400', '跳转修改映射页面', '004020000', 'ACTION', '/mediaMapping/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004020500', '修改映射', '004020000', 'ACTION', '/mediaMapping/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004020600', '删除映射', '004020000', 'ACTION', '/mediaMapping/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006000000', '监控管理', '000000000', 'NODE', '', 'fa-eye', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007000000', '系统管理', '000000000', 'NODE', '', 'fa-cogs', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007001000', '用户管理', '007000000', 'LEAF', '/user/userList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007001001', '跳转用户列表', '007001000', 'ACTION', '/user/initUser', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007001002', '跳转新增用户页面', '007001000', 'ACTION', '/user/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007001003', '新增用户', '007001000', 'ACTION', '/user/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007001004', '跳转修改用户页面', '007001000', 'ACTION', '/user/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007001005', '修改用户', '007001000', 'ACTION', '/user/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007001006', '删除用户', '007001000', 'ACTION', '/user/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007002000', '菜单管理', '007000000', 'LEAF', '/menu/menuList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007002001', '跳转菜单列表', '007002000', 'ACTION', '/menu/initMenu', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007002002', '跳转新增菜单页面', '007002000', 'ACTION', '/menu/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007002003', '新增菜单', '007002000', 'ACTION', '/menu/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007002004', '跳转修改菜单页面', '007002000', 'ACTION', '/menu/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007002005', '修改菜单', '007002000', 'ACTION', '/menu/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007002006', '删除菜单', '007002000', 'ACTION', '/menu/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001000', '监控配置', '006000000', 'LEAF', '/monitor/monitorList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006002000', '任务监控', '006000000', 'LEAF', '/taskMonitor/taskMonitorList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003000', '角色管理', '007000000', 'LEAF', '/role/roleList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003001', '跳转角色列表', '007003000', 'ACTION', '/role/initRole', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003002', '跳转新增角色页面', '007003000', 'ACTION', '/role/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003003', '新增角色', '007003000', 'ACTION', '/role/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003004', '跳转修改角色页面', '007003000', 'ACTION', '/role/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003005', '修改角色', '007003000', 'ACTION', '/role/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003006', '删除角色', '007003000', 'ACTION', '/role/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004000', 'ZooKeeper', '002000000', 'LEAF', '/zk/zkList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004001', '跳转zk列表', '002004000', 'ACTION', '/zk/initZk', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004002', '跳转新增zk页面', '002004000', 'ACTION', '/zk/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004003', '新增zk', '002004000', 'ACTION', '/zk/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004004', '跳转修改zk页面', '002004000', 'ACTION', '/zk/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004005', '修改zk', '002004000', 'ACTION', '/zk/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004006', '删除zk', '002004000', 'ACTION', '/zk/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004007', '验证zk', '002004000', 'ACTION', '/zk/checkZk', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004030000', '拦截器管理', '004000000', 'LEAF', '/interceptor/interceptorList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004030100', '跳转拦截器列表', '004030000', 'ACTION', '/interceptor/initInterceptor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004030200', '跳转新增拦截器页面', '004030000', 'ACTION', '/interceptor/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004030300', '新增拦截器', '004030000', 'ACTION', '/interceptor/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004030400', '跳转修改拦截器页面', '004030000', 'ACTION', '/interceptor/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004030500', '修改拦截器', '004030000', 'ACTION', '/interceptor/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004030600', '删除拦截器', '004030000', 'ACTION', '/interceptor/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002006000', 'SDDL', '002000000', 'LEAF', '/sddl/sddlList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002007000', 'HDFS', '002000000', 'LEAF', '/hdfs/hdfsList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002007001', '跳转HDFS列表', '002007000', 'ACTION', '/hdfs/initHDFSList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002001', '跳转HBase列表', '002002000', 'ACTION', '/hbase/initHBase', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002002', '跳转新增HBase页面', '002002000', 'ACTION', '/hbase/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002003', '新增HBase', '002002000', 'ACTION', '/hbase/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002004', '跳转修改HBase页面', '002002000', 'ACTION', '/hbase/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002005', '修改HBase', '002002000', 'ACTION', '	/hbase/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002006', '删除HBase', '002002000', 'ACTION', '/hbase/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('003002001', '执行脚本检测', '003002000', 'ACTION', '/sync/relation/checkSql', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('003003001', '获取同步检测树', '003003000', 'ACTION', '/sync/relation/getTrees', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002008000', 'Meta Mapping', '002000000', 'LEAF', '/metaMapping/metaMappingList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002007', '跳转logback修改', '001002000', 'ACTION', '/worker/toEditLogback', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002008', '保存logback修改', '001002000', 'ACTION', '/worker/doEditLogback', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002009', '重启机器', '001002000', 'ACTION', '/worker/restartWorker', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002010', '查看机器监控', '001002000', 'ACTION', '/worker/toWorkerMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002011', '查询机器JVM监控', '001002000', 'ACTION', '/worker/doSearchJvmMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002001008', 'ReloadRDBMS数据源', '002001000', 'ACTION', '/mediaSource/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003001', '跳转ES列表', '002003000', 'ACTION', '/es/initEs', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003002', '跳转ES新增', '002003000', 'ACTION', '/es/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003003', '保存ES新增', '002003000', 'ACTION', '/es/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003004', '跳转ES修改', '002003000', 'ACTION', '/es/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003005', '保存ES修改', '002003000', 'ACTION', '/es/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003006', '删除ES', '002003000', 'ACTION', '/es/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003007', '验证ES', '002003000', 'ACTION', '/es/checkEs', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002003008', 'ReloadES', '002003000', 'ACTION', '/es/toReloadES', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010111', '跳转task迁组', '004010100', 'ACTION', '/task/toGroupMigrate', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010112', '执行task迁组', '004010100', 'ACTION', '/task/doGroupMigrate', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010201', '跳转HBaseTask列表', '004010200', 'ACTION', '/hbaseTask/initHbaseTaskList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010202', '跳转新增HBaseTask页面', '004010200', 'ACTION', '/hbaseTask/toAddHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010203', '保存新增HBaseTask', '004010200', 'ACTION', '/hbaseTask/doAddHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010204', '跳转修改HBaseTask页面', '004010200', 'ACTION', '/hbaseTask/toUpdateHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010205', '保存修改HBaseTask', '004010200', 'ACTION', '/hbaseTask/doUpdateHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010206', '删除HBaseTask', '004010200', 'ACTION', '/hbaseTask/deleteHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010207', '暂停运行HBaseTask', '004010200', 'ACTION', '/hbaseTask/pauseHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010208', '恢复运行HBaseTask', '004010200', 'ACTION', '/hbaseTask/resumeHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004010209', '重启HBaseTask', '004010200', 'ACTION', '/hbaseTask/restartHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004020700', '映射数据校验', '004020000', 'ACTION', '/mediaMapping/dataCheck', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001001', '监控配置列表', '006001000', 'ACTION', '/monitor/initMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001002', '新增监控配置', '006001000', 'ACTION', '/monitor/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001003', '保存新增监控配置', '006001000', 'ACTION', '/monitor/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001004', '修改监控配置', '006001000', 'ACTION', '/monitor/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001005', '保存修改监控配置', '006001000', 'ACTION', '/monitor/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001006', '删除监控配置', '006001000', 'ACTION', '/monitor/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001007', '开启监控', '006001000', 'ACTION', '/monitor/doStart', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001008', '暂停监控', '006001000', 'ACTION', '/monitor/doPause', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001009', '开启所有监控', '006001000', 'ACTION', '/monitor/doAllStart', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001010', '停止所有监控', '006001000', 'ACTION', '/monitor/doAllStop', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006001011', '查看当前任务异常', '006001000', 'ACTION', '/taskMonitor/getException', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006002001', '任务监控列表', '006002000', 'ACTION', '/taskMonitor/initTaskMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006002002', '查看任务监控指标曲线', '006002000', 'ACTION', '/taskMonitor/toTaskStatistic', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006002003', '查询任务监控指标曲线', '006002000', 'ACTION', '/taskMonitor/doSearchTaskStatistic', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006002004', '查看任务监控异常列表', '006002000', 'ACTION', '/taskMonitor/toTaskException', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006002005', '加载任务监控异常列表', '006002000', 'ACTION', '/taskMonitor/initTaskException', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('006002006', '查看任务历史异常', '006002000', 'ACTION', '/taskMonitor/showException', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003007', '加载权限信息', '007003000', 'ACTION', '/role/initAuthority', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007003008', '保存权限信息', '007003000', 'ACTION', '/role/doEditAuthority', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002004008', 'ReloadZK', '002004000', 'ACTION', '/es/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('004030700', 'Reload拦截器', '004030000', 'ACTION', '/interceptor/toReload', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002006001', '跳转SDDL列表', '002006000', 'ACTION', '/sddl/initSddl', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002006002', '跳转SDDL新增', '002006000', 'ACTION', '/sddl/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002006003', '保存SDDL新增', '002006000', 'ACTION', '/sddl/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002006004', '跳转SDDL修改', '002006000', 'ACTION', '/sddl/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002006005', '保存SDDL修改', '002006000', 'ACTION', '/sddl/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002006006', '删除SDDL', '002006000', 'ACTION', '/sddl/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002006007', 'ReloadSDDL', '002006000', 'ACTION', '/sddl/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002007002', '跳转HDFS新增', '002007000', 'ACTION', '/hdfs/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002007003', '保存HDFS新增', '002007000', 'ACTION', '/hdfs/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002007004', '跳转HDFS修改', '002007000', 'ACTION', '/hdfs/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002007005', '保存HDFS修改', '002007000', 'ACTION', '/hdfs/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002007006', '删除HDFS', '002007000', 'ACTION', '/hdfs/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002007007', 'ReloadHDFS', '002007000', 'ACTION', '/hdfs/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002007', '验证HBase', '002002000', 'ACTION', '/hbase/checkHBase', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002002008', 'ReloadHBase', '002002000', 'ACTION', '/hbase/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002008001', '跳转MetaMapping列表', '002008000', 'ACTION', '/metaMapping/initMapping', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002008002', '跳转MetaMapping新增', '002008000', 'ACTION', '/metaMapping/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002008003', '保存MetaMapping新增', '002008000', 'ACTION', '/metaMapping/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002008004', '跳转MetaMapping修改', '002008000', 'ACTION', '/metaMapping/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002008005', '保存MetaMapping修改', '002008000', 'ACTION', '/metaMapping/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002008006', '删除MetaMapping', '002008000', 'ACTION', '/metaMapping/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('002008007', 'ReloadMetaMapping', '002008000', 'ACTION', '/metaMapping/doReload', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('001002012', '查询机器系统监控', '001002000', 'ACTION', '/worker/doSearchSystemMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007005000', '系统参数', '007000000', 'LEAF', '/sysProperties/propertieList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007005001', '跳转系统参数列表', '007005000', 'ACTION', '/sysProperties/intPropertiesList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007005002', '跳转新增系统参数页面	', '007005000', 'ACTION', '/sysProperties/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007005003', '新增系统参数', '007005000', 'ACTION', '/sysProperties/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007005004', '跳转修改系统参数页面', '007005000', 'ACTION', '/sysProperties/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007005005', '修改系统参数', '007005000', 'ACTION', '/sysProperties/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`) VALUES ('007005006', '删除系统参数	', '007005000', 'ACTION', '/sysProperties/doDelete', '', now(), now());


INSERT INTO `t_dl_role` (`id`, `code`, `name`, `create_time`, `modify_time`) VALUES (1, 'SUPER', '超级管理员', now(), now());
INSERT INTO `t_dl_role` (`id`, `code`, `name`, `create_time`, `modify_time`) VALUES (2, 'ORDINARY', '普通用户', now(), now());

INSERT INTO `t_dl_user` (`id`, `user_name`, `ucar_email`, `phone`, `create_time`, `modify_time`, `is_alarm`) VALUES (1, 'admin', 'admin', '18800000000', now(), now(), '1');

INSERT INTO `t_dl_user_role` (`id`, `user_id`, `role_id`, `create_time`, `modify_time`) VALUES (1, 1, 1, now(), now());

