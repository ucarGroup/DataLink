/*
MySQL
Database : ucar_datalink
*/
CREATE DATABASE IF NOT EXISTS `ucar_datalink`
  DEFAULT CHARACTER SET utf8
  COLLATE utf8_general_ci;

USE `ucar_datalink`;
-- ----------------------------
-- Table structure for `t_dl_group`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_group`;
CREATE TABLE `t_dl_group` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键id',
  `group_name`  VARCHAR(50)         NOT NULL
  COMMENT '分组名称',
  `group_desc`  VARCHAR(100)        NOT NULL
  COMMENT '分组描述',
  `create_time` DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time` DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '分组配置表';

-- ----------------------------
-- Table structure for `t_dl_interceptor`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_interceptor`;
CREATE TABLE `t_dl_interceptor` (
  `id`                  BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键id',
  `interceptor_name`    VARCHAR(30)         NOT NULL
  COMMENT '拦截器名称',
  `interceptor_desc`    VARCHAR(100)        NOT NULL
  COMMENT '拦截器描述',
  `interceptor_type`    VARCHAR(15)         NOT NULL
  COMMENT '拦截器类型',
  `interceptor_content` TEXT                NOT NULL
  COMMENT '拦截器内容',
  `create_time`         DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time`         DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '拦截器配置表';

-- ----------------------------
-- Table structure for `t_dl_media`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_media`;
CREATE TABLE `t_dl_media` (
  `id`              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `media_namespace` VARCHAR(100)        NOT NULL
  COMMENT '介质命名空间',
  `media_name`      VARCHAR(100)        NOT NULL
  COMMENT '介质名称',
  `media_source_id` BIGINT(20)          NOT NULL
  COMMENT '介质所属数据源ID',
  `create_time`     DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time`     DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `media_uidx1` (`media_namespace`, `media_name`, `media_source_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '介质定义表';

-- ----------------------------
-- Table structure for `t_dl_media_mapping`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_media_mapping`;
CREATE TABLE `t_dl_media_mapping` (
  `id`                     BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `task_id`                BIGINT(20) UNSIGNED NOT NULL
  COMMENT '任务ID',
  `source_media_id`        BIGINT(20) UNSIGNED NOT NULL
  COMMENT '源介质ID',
  `target_media_source_id` BIGINT(20) UNSIGNED NOT NULL
  COMMENT '目标数据源ID',
  `target_media_name`      VARCHAR(100)                 DEFAULT NULL
  COMMENT '目标介质名称',
  `target_media_namespace` VARCHAR(50)                  DEFAULT NULL
  COMMENT '目标介质命名空间',
  `parameter`              TEXT                NOT NULL
  COMMENT '映射参数',
  `column_mapping_mode`    VARCHAR(10)                  DEFAULT NULL
  COMMENT '列映射模式',
  `write_weight`           BIGINT(20)          NOT NULL
  COMMENT '同步权重优先级',
  `is_valid`               TINYINT(1)          NOT NULL DEFAULT '1'
  COMMENT '是否有效',
  `interceptor_id`         BIGINT(20) UNSIGNED          DEFAULT NULL
  COMMENT '拦截器ID',
  `create_time`            DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time`            DATETIME            NOT NULL
  COMMENT '修改时间',
  `join_column`            VARCHAR(20)                  DEFAULT NULL
  COMMENT '多表聚合列名',
  `es_use_prefix`          TINYINT(1)          NOT NULL DEFAULT '1'
  COMMENT '同步到es时列名是否加表前缀',
  `geo_position_conf`      VARCHAR(5000)                DEFAULT NULL
  COMMENT 'es地理位置合并配置',
  `skip_ids`               VARCHAR(200)                 DEFAULT NULL
  COMMENT '要跳过的主键ids',
  `es_routing`             VARCHAR(100)                 DEFAULT NULL
  COMMENT 'esRouting字段',
  `es_routing_ignore`      VARCHAR(5)                   DEFAULT NULL
  COMMENT '如果routing字段值不存在，写入的数据是否可忽略写入,true：可以忽略，false：不可以忽略',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx1_mediamapping` (`task_id`, `source_media_id`, `target_media_source_id`, `target_media_name`),
  KEY `mapping_index_taskId` (`task_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '同步映射配置表';

-- ----------------------------
-- Table structure for `t_dl_media_mapping_column`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_media_mapping_column`;
CREATE TABLE `t_dl_media_mapping_column` (
  `id`               BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `source_column`    VARCHAR(200)                 DEFAULT NULL
  COMMENT '源列名称',
  `target_column`    VARCHAR(200)                 DEFAULT NULL
  COMMENT '目标列名称',
  `media_mapping_id` BIGINT(20)          NOT NULL
  COMMENT '所属同步映射ID',
  `create_time`      DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time`      DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_media_mapping_id` (`media_mapping_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '列映射配置表';

-- ----------------------------
-- Table structure for `t_dl_media_source`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_media_source`;
CREATE TABLE `t_dl_media_source` (
  `id`           BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `ms_name`      VARCHAR(50)         NOT NULL
  COMMENT '数据源名称',
  `ms_type`      VARCHAR(20)         NOT NULL
  COMMENT '数据源类型',
  `ms_desc`      VARCHAR(50)         NOT NULL
  COMMENT '数据源描述',
  `ms_parameter` TEXT                NOT NULL
  COMMENT '数据源参数',
  `create_time`  DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time`  DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_ms_name` (`ms_name`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '介质源定义表';

-- ----------------------------
-- Table structure for `t_dl_menu`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_menu`;
CREATE TABLE `t_dl_menu` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `code`        VARCHAR(20)         NOT NULL
  COMMENT '编码',
  `name`        VARCHAR(50)         NOT NULL
  COMMENT '名称',
  `parent_code` VARCHAR(20)         NOT NULL
  COMMENT '父节点编码',
  `type`        VARCHAR(20)         NOT NULL
  COMMENT '菜单类型',
  `url`         VARCHAR(200)        NOT NULL
  COMMENT '路径',
  `icon`        VARCHAR(20)         NOT NULL
  COMMENT '标识',
  `create_time` DATETIME            NOT NULL
  COMMENT '新建时间',
  `modify_time` DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '菜单表';


-- ----------------------------
-- Table structure for `t_dl_meta_mapping`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_meta_mapping`;
CREATE TABLE `t_dl_meta_mapping` (
  `id`                       BIGINT(20) NOT NULL AUTO_INCREMENT
  COMMENT '表id，主键',
  `src_media_source_type`    VARCHAR(20)         DEFAULT NULL
  COMMENT '源端类型',
  `target_media_source_type` VARCHAR(20)         DEFAULT NULL
  COMMENT '目标端类型',
  `src_mapping_type`         VARCHAR(50)         DEFAULT NULL
  COMMENT '源端字段类型',
  `target_mapping_type`      VARCHAR(50)         DEFAULT NULL
  COMMENT '目标端字段类型',
  `create_time`              DATETIME            DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `modify_time`              DATETIME            DEFAULT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `META_MAPPING_COMBINATION_INDEX` (`src_media_source_type`, `target_media_source_type`, `src_mapping_type`, `target_mapping_type`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '元数据映射表';

-- ----------------------------
-- Table structure for `t_dl_monitor`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_monitor`;
CREATE TABLE `t_dl_monitor` (
  `id`             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `resource_id`    BIGINT(20)          NOT NULL
  COMMENT '监控项目ID',
  `is_effective`   TINYINT(1)          NOT NULL
  COMMENT '是否有效',
  `threshold`      INT(11)             NOT NULL
  COMMENT '阀值',
  `interval_time`  BIGINT(20)          NOT NULL
  COMMENT '报警间隔时间',
  `receive_people` VARCHAR(100)                 DEFAULT NULL
  COMMENT '报警接收人',
  `monitor_type`   TINYINT(1)          NOT NULL
  COMMENT '监控类型',
  `create_time`    DATETIME                     DEFAULT NULL
  COMMENT '创建时间',
  `modify_time`    DATETIME                     DEFAULT NULL
  COMMENT '修改时间',
  `monitor_range`  VARCHAR(50)                  DEFAULT NULL
  COMMENT '监控时间范围',
  `monitor_cat`    TINYINT(1)          NOT NULL
  COMMENT '监控类型',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_task_id_type_cat` (`resource_id`, `monitor_type`, `monitor_cat`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '监控配置表';


-- ----------------------------
-- Table structure for `t_dl_role`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_role`;
CREATE TABLE `t_dl_role` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `code`        VARCHAR(20)         NOT NULL
  COMMENT '编码',
  `name`        VARCHAR(50)         NOT NULL
  COMMENT '名称',
  `create_time` DATETIME            NOT NULL
  COMMENT '新建时间',
  `modify_time` DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx_code` (`code`) USING BTREE
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '角色表';


-- ----------------------------
-- Table structure for `t_dl_role_authority`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_role_authority`;
CREATE TABLE `t_dl_role_authority` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `menu_id`     BIGINT(20)          NOT NULL
  COMMENT '菜单id',
  `role_id`     BIGINT(20)          NOT NULL
  COMMENT '角色id',
  `create_time` DATETIME            NOT NULL
  COMMENT '新建时间',
  `modify_time` DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '角色菜单权限表';


-- ----------------------------
-- Table structure for `t_dl_sys_properties`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_sys_properties`;
CREATE TABLE `t_dl_sys_properties` (
  `id`               BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `properties_key`   VARCHAR(30)         NOT NULL
  COMMENT '参数key',
  `properties_value` VARCHAR(50)         NOT NULL
  COMMENT '参数值',
  `create_time`      DATETIME            NOT NULL
  COMMENT '新建时间',
  `modify_time`      DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_properties_key` (`properties_key`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '系统参数表';


-- ----------------------------
-- Table structure for `t_dl_task`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task`;
CREATE TABLE `t_dl_task` (
  `id`                     BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `task_name`              VARCHAR(100)        NOT NULL
  COMMENT '任务名称',
  `task_desc`              VARCHAR(50)         NOT NULL
  COMMENT '任务描述',
  `task_parameter`         TEXT                NOT NULL
  COMMENT '任务参数',
  `reader_media_source_id` BIGINT(20) UNSIGNED NOT NULL
  COMMENT 'Reader关联的mediasourceid (冗余)',
  `task_reader_parameter`  TEXT                NOT NULL
  COMMENT 'TaskReader参数',
  `task_writer_parameter`  TEXT                NOT NULL
  COMMENT 'TaskWriter参数',
  `create_time`            DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time`            TIMESTAMP(6)        NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP (6)
  COMMENT '修改时间',
  `group_id`               BIGINT(20) UNSIGNED NOT NULL
  COMMENT '所属组ID',
  `target_state`           VARCHAR(20)         NOT NULL
  COMMENT '目标状态',
  `is_delete`              VARCHAR(5)          NOT NULL DEFAULT 'false'
  COMMENT '是否已删除',
  `task_type`              VARCHAR(20)         NOT NULL
  COMMENT '任务类型',
  `leader_task_id`         BIGINT(20) UNSIGNED          DEFAULT NULL
  COMMENT 'leader task id',
  `is_leader_task`         CHAR(1)             NOT NULL DEFAULT '0'
  COMMENT '是否是leader task',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_task_name` (`task_name`),
  KEY `idx_reader_media_source_id` (`reader_media_source_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '同步任务配置表';


-- ----------------------------
-- Table structure for `t_dl_task_delaytime`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task_delaytime`;
CREATE TABLE `t_dl_task_delaytime` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `delay_time`  BIGINT(20)          NOT NULL
  COMMENT '延迟时间',
  `create_time` DATETIME            NOT NULL
  COMMENT '创建时间',
  `task_id`     BIGINT(20)          NOT NULL
  COMMENT '任务id',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_create_time` (`create_time`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '同步任务数据延迟记录表';


-- ----------------------------
-- Table structure for `t_dl_task_exception`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task_exception`;
CREATE TABLE `t_dl_task_exception` (
  `id`               BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `task_id`          BIGINT(20)          NOT NULL
  COMMENT '任务id',
  `worker_id`        BIGINT(20)          NOT NULL
  COMMENT '机器id',
  `exception_detail` MEDIUMTEXT COMMENT '异常信息',
  `create_time`      DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id_create_time` (`task_id`, `create_time`) USING BTREE
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '同步任务异常统计表';

-- ----------------------------
-- Table structure for `t_dl_task_position`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task_position`;
CREATE TABLE `t_dl_task_position` (
  `id`            BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `task_id`       BIGINT(20)          NOT NULL
  COMMENT '任务id',
  `task_position` TEXT COMMENT '任务消费位点',
  `create_time`   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `modify_time`   DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_task_id` (`task_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '同步任务消费位点统计表';


-- ----------------------------
-- Table structure for `t_dl_task_statistic`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_task_statistic`;
CREATE TABLE `t_dl_task_statistic` (
  `id`                          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `task_id`                     BIGINT(20)          NOT NULL
  COMMENT '任务id',
  `records_per_minute`          BIGINT(20)          NOT NULL DEFAULT '0'
  COMMENT '每分钟同步条数',
  `size_per_minute`             BIGINT(20)          NOT NULL DEFAULT '0'
  COMMENT '每分钟同步流量',
  `write_time_per_record`       DECIMAL(20, 2)      NOT NULL DEFAULT '0.00'
  COMMENT '写入平均耗时',
  `exceptions_per_minute`       BIGINT(20)          NOT NULL DEFAULT '0'
  COMMENT '每分钟同步异常个数',
  `create_time`                 DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `read_write_count_per_minute` BIGINT(20)          NOT NULL DEFAULT '0'
  COMMENT '每分钟读写次数',
  PRIMARY KEY (`id`),
  KEY `idx_task_id_create_time` (`task_id`, `create_time`) USING BTREE,
  KEY `idx_create_time` (`create_time`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '同步任务性能统计表';


-- ----------------------------
-- Table structure for `t_dl_taskstatus_mismatch_log`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_taskstatus_mismatch_log`;
CREATE TABLE `t_dl_taskstatus_mismatch_log` (
  `id`            BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键id',
  `task_id`       BIGINT(20) UNSIGNED NOT NULL
  COMMENT '同步任务id',
  `worker_id`     BIGINT(20) UNSIGNED NOT NULL
  COMMENT '所属机器id',
  `action_type`   VARCHAR(20)         NOT NULL
  COMMENT '操作类型',
  `local_status`  TEXT COMMENT '本地状态',
  `remote_status` TEXT COMMENT '远程状态',
  `create_time`   DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time`   DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '任务状态冲突记录表';

-- ----------------------------
-- Table structure for `t_dl_user`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_user`;
CREATE TABLE `t_dl_user` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `user_name`   VARCHAR(50)         NOT NULL
  COMMENT '用户名称',
  `ucar_email`  VARCHAR(50)                  DEFAULT NULL
  COMMENT '集团邮箱',
  `phone`       VARCHAR(20)         NOT NULL
  COMMENT '手机号',
  `create_time` DATETIME            NOT NULL
  COMMENT '新建时间',
  `modify_time` DATETIME            NOT NULL
  COMMENT '修改时间',
  `is_alarm`    VARCHAR(5)          NOT NULL DEFAULT 'false'
  COMMENT '是否发送报警',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_idx_email` (`ucar_email`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '用户表';

-- ----------------------------
-- Table structure for `t_dl_user_role`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_user_role`;
CREATE TABLE `t_dl_user_role` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `user_id`     BIGINT(20)          NOT NULL
  COMMENT '用户id',
  `role_id`     BIGINT(20)          NOT NULL
  COMMENT '角色id',
  `create_time` DATETIME            NOT NULL
  COMMENT '新建时间',
  `modify_time` DATETIME            NOT NULL
  COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '用户角色关系表';

-- ----------------------------
-- Table structure for `t_dl_worker`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_worker`;
CREATE TABLE `t_dl_worker` (
  `id`             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `worker_name`    VARCHAR(50)         NOT NULL
  COMMENT 'Worker名称',
  `worker_desc`    VARCHAR(50)         NOT NULL
  COMMENT 'Worker描述',
  `worker_address` VARCHAR(50)         NOT NULL
  COMMENT 'Worker网络地址',
  `create_time`    DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time`    DATETIME            NOT NULL
  COMMENT '修改时间',
  `group_id`       BIGINT(20) UNSIGNED NOT NULL
  COMMENT '所属组ID',
  `rest_port`      INT(10) UNSIGNED    NOT NULL DEFAULT '8083'
  COMMENT 'rest调用端口',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = 'Worker信息配置表';


-- ----------------------------
-- Table structure for `t_dl_worker_jvm_state`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_worker_jvm_state`;
CREATE TABLE `t_dl_worker_jvm_state` (
  `id`                              BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键id',
  `worker_id`                       BIGINT(20)          NOT NULL
  COMMENT 'worker主键id',
  `host`                            VARCHAR(20)         NOT NULL
  COMMENT 'worker机器ip',
  `old_mem_used`                    BIGINT(20)                   DEFAULT NULL
  COMMENT '老年代内存使用大小',
  `old_mem_max`                     BIGINT(20)                   DEFAULT NULL
  COMMENT '老年代最大堆内存',
  `young_mem_max`                   BIGINT(20)                   DEFAULT NULL
  COMMENT '新生代内存大小',
  `young_mem_used`                  BIGINT(20)                   DEFAULT NULL
  COMMENT '新生代内存使用',
  `create_time`                     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `interval_old_collection_count`   BIGINT(20)                   DEFAULT NULL
  COMMENT '老年代一分钟内的垃圾回收次数',
  `interval_young_collection_count` BIGINT(20)                   DEFAULT NULL
  COMMENT '新生代一分钟内的垃圾回收次数',
  `interval_old_collection_time`    BIGINT(20)                   DEFAULT '0'
  COMMENT '老年代一分钟内的垃圾回收时间',
  `interval_young_collection_time`  BIGINT(20)                   DEFAULT '0'
  COMMENT '新生代一分钟内垃圾回收时间',
  `current_thread_count`            BIGINT(20)                   DEFAULT '0'
  COMMENT '当前线程数',
  PRIMARY KEY (`id`),
  KEY `IDX_COLLECTTIME` (`create_time`),
  KEY `IDX_WORKERID` (`worker_id`) USING BTREE
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = 'worker机器JVM状态统计';

-- ----------------------------
-- Table structure for `t_dl_worker_system_state`
-- ----------------------------
DROP TABLE IF EXISTS `t_dl_worker_system_state`;
CREATE TABLE `t_dl_worker_system_state` (
  `id`                       BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键id',
  `worker_id`                BIGINT(20)          NOT NULL
  COMMENT 'worker主键id`',
  `host`                     VARCHAR(20)         NOT NULL
  COMMENT 'worker机器ip',
  `load_average`             DECIMAL(20, 2)      NOT NULL DEFAULT '0.00'
  COMMENT '一分钟平均负载',
  `user_cpu_utilization`     DECIMAL(20, 2)      NOT NULL DEFAULT '0.00'
  COMMENT '用户CPU使用率',
  `sys_cpu_utilization`      DECIMAL(20, 2)      NOT NULL DEFAULT '0.00'
  COMMENT '系统CPU使用率',
  `incoming_network_traffic` BIGINT(20)          NOT NULL DEFAULT '0'
  COMMENT '一分钟内接收的字节数',
  `outgoing_network_traffic` BIGINT(20)          NOT NULL DEFAULT '0'
  COMMENT '一分钟内发送的字节数',
  `tcp_current_estab`        BIGINT(20)          NOT NULL DEFAULT '0'
  COMMENT '当前TCP连接数',
  `create_time`              TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `IDX_COLLECTTIME` (`create_time`),
  KEY `IDX_WORKERID` (`worker_id`) USING BTREE
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = 'worker机器系统状态统计';


/*Table structure for table `t_dl_audit_log` */
DROP TABLE IF EXISTS `t_dl_audit_log`;
CREATE TABLE `t_dl_audit_log` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT(20)          NOT NULL,
  `menu_code`   VARCHAR(20)         NOT NULL
  COMMENT '操作模块编码',
  `oper_type`   VARCHAR(1)          NOT NULL DEFAULT ''
  COMMENT '操作类型。1新增，2修改，3删除',
  `oper_time`   DATETIME            NOT NULL,
  `oper_key`    BIGINT(20)          NOT NULL
  COMMENT '操作资源key',
  `oper_name`   VARCHAR(200)        NOT NULL DEFAULT ''
  COMMENT '操作资源名称',
  `oper_record` MEDIUMTEXT          NOT NULL
  COMMENT '变更后的记录',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '操作审计表';


/*Table structure for table `t_dl_task_decorate` */
DROP TABLE IF EXISTS `t_dl_task_decorate`;
CREATE TABLE `t_dl_task_decorate` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `task_id`     BIGINT(20)          NOT NULL
  COMMENT '任务id',
  `table_name`  VARCHAR(200)        NOT NULL
  COMMENT '表名称',
  `remark`      VARCHAR(200)                 DEFAULT NULL
  COMMENT '备注',
  `create_time` TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `modify_time` TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '修改时间',
  `statement`   VARCHAR(500)        NOT NULL
  COMMENT '补录数据主键表达式',
  `deleted`     VARCHAR(5)          NOT NULL DEFAULT 'false'
  COMMENT '删除',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT = '数据补录表';


/*Table structure for table `t_dl_task_decorate_detail` */
DROP TABLE IF EXISTS `t_dl_task_decorate_detail`;
CREATE TABLE `t_dl_task_decorate_detail` (
  `id`           BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `decorate_id`  BIGINT(20)          NOT NULL
  COMMENT '任务id',
  `status`       SMALLINT(6)         NOT NULL DEFAULT '0'
  COMMENT '状态（0-新建,1-补录中,2-成功,3-失败）',
  `create_time`  TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '创建时间',
  `update_time`  TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP
  COMMENT '修改时间',
  `executed_log` TEXT COMMENT '提示信息',
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT = '数据补录明細表';


/*Table structure for table `t_dl_task_shadow` */
DROP TABLE IF EXISTS `t_dl_task_shadow`;
CREATE TABLE `t_dl_task_shadow` (
  `id`          BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `task_id`     BIGINT(20) UNSIGNED NOT NULL
  COMMENT '任务ID',
  `state`       VARCHAR(20)         NOT NULL
  COMMENT '状态',
  `parameter`   VARCHAR(500)        NOT NULL
  COMMENT '参数',
  `create_time` DATETIME            NOT NULL
  COMMENT '创建时间',
  `modify_time` DATETIME            NOT NULL
  COMMENT '修改时间',
  `note`        VARCHAR(200)                 DEFAULT NULL
  COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`) USING BTREE
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '影子任务表';


/*Table structure for table `t_dl_task_trace` */
DROP TABLE IF EXISTS `t_dl_task_trace`;
CREATE TABLE `t_dl_task_trace` (
  `id`         BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT
  COMMENT '主键',
  `task_id`    BIGINT(20)          NOT NULL
  COMMENT '任务id',
  `worker_id`  BIGINT(20)                   DEFAULT NULL
  COMMENT '机器id',
  `group_id`   BIGINT(20)          NOT NULL
  COMMENT '分组id',
  `start_time` DATETIME            NOT NULL
  COMMENT '开始时间',
  `end_time`   DATETIME            NOT NULL
  COMMENT '结束时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id_start_time_end_time` (`task_id`, `start_time`, `end_time`) USING BTREE
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8
  COMMENT = '任务轨迹监控表';


/*Table structure for table `t_dl_flinker_job_schedule` */

DROP TABLE IF EXISTS `t_dl_flinker_job_schedule`;

CREATE TABLE `t_dl_flinker_job_schedule` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '定时表主键',
  `job_id` bigint(20) DEFAULT NULL COMMENT '任务id job表id',
  `schedule_name` varchar(255) DEFAULT NULL,
  `cron_expression` varchar(128) DEFAULT NULL COMMENT 'quartz 运行时间规则表达式',
  `is_retry` tinyint(3) DEFAULT '0' COMMENT '是否重试 0 不重试 1 重试',
  `retry_number` int(11) DEFAULT NULL COMMENT '最大重试次数 不超过5次 高频率的不重试',
  `retry_interval` int(11) DEFAULT NULL COMMENT '每次重试间隔（秒） 不少于60秒 最大 900s',
  `max_running_time` bigint(20) DEFAULT NULL COMMENT '最大运行时间 超过时间 则抛出异常/报警 -1 为不限制',
  `online_state` tinyint(1) DEFAULT NULL COMMENT '任务状态,0:未上线,1:已上线,2:已下线',
  `is_delete` tinyint(1) DEFAULT NULL COMMENT '是否删除 0 未删除 1 已删除 （此为逻辑删除）',
  `remark` varchar(255) DEFAULT NULL COMMENT '定时备注',
  `creator_id` bigint(20) DEFAULT NULL COMMENT '创建定时任务的人员id',
  `creator_name` varchar(255) DEFAULT NULL COMMENT '创建定时任务的人员名称',
  `schedule_state` tinyint(1) DEFAULT NULL COMMENT '定时任务状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifie_time` datetime DEFAULT NULL COMMENT '最后修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='Flinker任务运行定时表';

/*Table structure for table `t_dl_flinker_job_config` */

DROP TABLE IF EXISTS `t_dl_flinker_job_config`;

CREATE TABLE `t_dl_flinker_job_config` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '任务id，主键自增长',
  `job_name` varchar(200) NOT NULL COMMENT '任务名称，名称不可以重复',
  `job_desc` varchar(200) DEFAULT NULL COMMENT '任务描述',
  `job_content` mediumtext COMMENT '任务内容',
  `job_media_name` varchar(5000) DEFAULT NULL COMMENT '参与同步的介质名称，多个表之间用逗号分隔',
  `job_media_target_name` varchar(5000) DEFAULT NULL COMMENT '目标表名',
  `job_src_media_source_id` bigint(20) DEFAULT NULL COMMENT '源端MediaSourceId',
  `job_target_media_source_id` bigint(20) DEFAULT NULL COMMENT '目标端MediaSourceId',
  `timing_transfer_type` varchar(20) DEFAULT NULL COMMENT 'FULL – 每次全量迁移,INCREMENT – 每次增量迁移',
  `timing_expression` varchar(100) DEFAULT NULL COMMENT 'corn表达式',
  `timing_yn` tinyint(1) DEFAULT NULL COMMENT '是否定时任务',
  `timing_on_yn` tinyint(1) DEFAULT NULL COMMENT '是否开启定时任务',
  `timing_parameter` varchar(4000) DEFAULT NULL COMMENT '定时任务的参数信息',
  `timing_target_worker` varchar(100) DEFAULT NULL COMMENT '执行定时任务的目标机器ip',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_delete` tinyint(1) DEFAULT NULL COMMENT '任务是否被删除',
  `apply_id` bigint(20) DEFAULT NULL COMMENT '申请id',
  `execute_state` varchar(100) DEFAULT 'UNEXECUTE' COMMENT '执行状态',
  `applyNo` varchar(25) DEFAULT NULL COMMENT '申请单号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_job_name_index` (`job_name`),
  KEY `apply_id_index` (`apply_id`) COMMENT '申请id索引',
  KEY `index_src_media_id` (`job_src_media_source_id`),
  KEY `index_dest_media_id` (`job_target_media_source_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8252 DEFAULT CHARSET=utf8 COMMENT='Flinker任务配置表';

/*Table structure for table `t_dl_flinker_job_execution` */

DROP TABLE IF EXISTS `t_dl_flinker_job_execution`;

CREATE TABLE `t_dl_flinker_job_execution` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键，自增类型',
  `job_id` bigint(20) unsigned NOT NULL COMMENT '任务id',
  `worker_address` varchar(100) DEFAULT NULL COMMENT '所在节点',
  `pid` int(10) DEFAULT NULL COMMENT '进程编号',
  `start_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间(创建索引)',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `state` varchar(100) DEFAULT NULL COMMENT '运行状态',
  `byte_speed_per_second` bigint(20) DEFAULT NULL COMMENT '任务平均流量',
  `record_speed_per_second` bigint(20) DEFAULT NULL COMMENT '记录写入速度',
  `total_error_records` bigint(20) DEFAULT NULL COMMENT '同步失败总数',
  `total_record` bigint(20) DEFAULT NULL COMMENT '同步总数',
  `wait_reader_time` decimal(50,10) DEFAULT NULL COMMENT '等待读的时间',
  `wait_writer_time` decimal(50,10) DEFAULT NULL COMMENT '等待写的时间',
  `percentage` decimal(50,10) DEFAULT NULL COMMENT '完成百分比',
  `exception` text COMMENT '异常信息',
  `job_queue_execution_id` bigint(20) DEFAULT NULL COMMENT '所属队列的运行id',
  `task_communication_info` mediumtext COMMENT '任务详细信息',
  `original_configuration` mediumtext COMMENT '此次运行的job镜像',
  PRIMARY KEY (`id`),
  KEY `indx_job_id` (`job_id`,`state`)
) ENGINE=InnoDB AUTO_INCREMENT=2813367 DEFAULT CHARSET=utf8 COMMENT='Flinker任务运行历史表';

/*Table structure for table `t_dl_flinker_job_queue` */

DROP TABLE IF EXISTS `t_dl_flinker_job_queue`;

CREATE TABLE `t_dl_flinker_job_queue` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '表主键',
  `job_name` varchar(255) NOT NULL COMMENT 'job名称',
  `queue_id` bigint(20) unsigned NOT NULL COMMENT '关联的队列id',
  `table_name` varchar(255) NOT NULL COMMENT 'job对应的表名称',
  `job_state` varchar(255) NOT NULL COMMENT 'job状态',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=241 DEFAULT CHARSET=utf8 COMMENT='Flinker job队列表';


/*Table structure for table `t_dl_flinker_job_queue_info` */

DROP TABLE IF EXISTS `t_dl_flinker_job_queue_info`;

CREATE TABLE `t_dl_flinker_job_queue_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '定时表主键',
  `queue_name` varchar(255) NOT NULL COMMENT 'job对列的名称',
  `mail` varchar(128) DEFAULT NULL COMMENT '创建job人的邮箱',
  `queue_state` varchar(20) DEFAULT NULL COMMENT '队列状态',
  `fail_to_stop` varchar(20) DEFAULT NULL COMMENT '任务失败终止运行',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `INDEX_queue_name` (`queue_name`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8 COMMENT='Flinker job队列表';

/*Table structure for table `t_dl_flinker_job_run_queue` */

DROP TABLE IF EXISTS `t_dl_flinker_job_run_queue`;

CREATE TABLE `t_dl_flinker_job_run_queue` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键，自增类型',
  `job_id_list` varchar(2000) NOT NULL COMMENT 'job id列表用逗号分隔',
  `current_process_id` varchar(200) DEFAULT NULL COMMENT '当前正在运行的id列表，[job_id]-[job_execution_id],... 这种格式',
  `queue_state` varchar(20) DEFAULT NULL COMMENT '整个队列的运行状态，未执行，有错误，执行中，执行完',
  `job_count` int(11) DEFAULT NULL COMMENT '整个队列的job数量',
  `success_list` varchar(200) DEFAULT NULL COMMENT '已经执行完的job数量',
  `failure_list` varchar(200) DEFAULT NULL COMMENT '已执行失败的job数量',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '修改时间',
  `top_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Flinker job运行队列表';


-- ----------------------------
-- 脚本初始化
-- ----------------------------
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001000000', '集群管理', '000000000', 'NODE', '', 'fa-cloud', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001001000', '分组管理', '001000000', 'LEAF', '/group/groupList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001001001', '跳转分组列表', '001001000', 'ACTION', '/group/initGroup', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001001002', '跳转新增页面', '001001000', 'ACTION', '/group/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001001003', '分组新增', '001001000', 'ACTION', '/group/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001001004', '跳转修改页面', '001001000', 'ACTION', '/group/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001001005', '分组修改', '001001000', 'ACTION', '/group/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001001006', '分组删除', '001001000', 'ACTION', '/group/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002000', '机器管理', '001000000', 'LEAF', '/worker/workerList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002001', '跳转机器列表', '001002000', 'ACTION', '/worker/initWorker', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002002', '跳转新增页面', '001002000', 'ACTION', '/worker/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002003', '机器新增', '001002000', 'ACTION', '/worker/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002004', '跳转修改页面', '001002000', 'ACTION', '/worker/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002005', '机器修改', '001002000', 'ACTION', '/worker/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002006', '机器删除', '001002000', 'ACTION', '/worker/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002000000', '介质管理', '000000000', 'NODE', '', 'fa-folder-open', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001000', 'RDBMS', '002000000', 'LEAF', '/mediaSource/mediaSourceList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001001', '跳转RDBMS列表', '002001000', 'ACTION', '/mediaSource/initMediaSource', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001002', '跳转新增RDBMS页面', '002001000', 'ACTION', '/mediaSource/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001003', '新增RDBMS', '002001000', 'ACTION', '/mediaSource/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001004', '跳转修改RDBMS页面', '002001000', 'ACTION', '/mediaSource/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001005', '修改RDBMS', '002001000', 'ACTION', '/mediaSource/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001006', '删除RDBMS', '002001000', 'ACTION', '/mediaSource/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001007', '验证数据源', '002001000', 'ACTION', '/mediaSource/checkDbContection', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002000', 'HBase', '002000000', 'LEAF', '/hbase/hbaseList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003000', 'ElasticSearch', '002000000', 'LEAF', '/es/esList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('003000000', '同步管理', '000000000', 'NODE', '', 'fa-exchange', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('003002000', '脚本检测', '003000000', 'LEAF', '/sync/relation/toCheckSql', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('003003000', '同步检测', '003000000', 'LEAF', '/sync/relation/show', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004000000', '增量任务', '000000000', 'NODE', '', 'fa-link', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010000', 'Task管理', '004000000', 'LEAF', '', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010100', 'MysqlTask', '004010000', 'LEAF', '/mysqlTask/mysqlTaskList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010101', '跳转新增MysqlTask页面', '004010100', 'ACTION', '/mysqlTask/toAddMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010102', '新增MysqlTask', '004010100', 'ACTION', '/mysqlTask/doAddMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010103', '跳转修改MysqlTask页面', '004010100', 'ACTION', '/mysqlTask/toUpdateMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010104', '修改MysqlTask', '004010100', 'ACTION', '/mysqlTask/doUpdateMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010105', '删除MysqlTask', '004010100', 'ACTION', '/mysqlTask/deleteMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010106', '暂停运行MysqlTask', '004010100', 'ACTION', '/mysqlTask/pauseMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010107', '恢复运行MysqlTask', '004010100', 'ACTION', '/mysqlTask/resumeMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010108', '重启MysqlTask', '004010100', 'ACTION', '/mysqlTask/toRestartMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010109', '执行重启MysqlTask', '004010100', 'ACTION', '/mysqlTask/doRestartMysqlTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010110', '跳转MysqlTask列表', '004010100', 'ACTION', '/mysqlTask/mysqlTaskDatas', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010200', 'HBaseTask', '004010000', 'LEAF', '/hbaseTask/hbaseTaskList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004020000', '映射管理', '004000000', 'LEAF', '/mediaMapping/mediaSourceList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004020100', '跳转映射列表', '004020000', 'ACTION', '/mediaMapping/initMediaMapping', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004020200', '跳转新增映射页面', '004020000', 'ACTION', '/mediaMapping/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004020300', '新增映射', '004020000', 'ACTION', '/mediaMapping/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004020400', '跳转修改映射页面', '004020000', 'ACTION', '/mediaMapping/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004020500', '修改映射', '004020000', 'ACTION', '/mediaMapping/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004020600', '删除映射', '004020000', 'ACTION', '/mediaMapping/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006000000', '监控管理', '000000000', 'NODE', '', 'fa-eye', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007000000', '系统管理', '000000000', 'NODE', '', 'fa-cogs', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007001000', '用户管理', '007000000', 'LEAF', '/user/userList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007001001', '跳转用户列表', '007001000', 'ACTION', '/user/initUser', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007001002', '跳转新增用户页面', '007001000', 'ACTION', '/user/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007001003', '新增用户', '007001000', 'ACTION', '/user/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007001004', '跳转修改用户页面', '007001000', 'ACTION', '/user/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007001005', '修改用户', '007001000', 'ACTION', '/user/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007001006', '删除用户', '007001000', 'ACTION', '/user/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007002000', '菜单管理', '007000000', 'LEAF', '/menu/menuList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007002001', '跳转菜单列表', '007002000', 'ACTION', '/menu/initMenu', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007002002', '跳转新增菜单页面', '007002000', 'ACTION', '/menu/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007002003', '新增菜单', '007002000', 'ACTION', '/menu/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007002004', '跳转修改菜单页面', '007002000', 'ACTION', '/menu/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007002005', '修改菜单', '007002000', 'ACTION', '/menu/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007002006', '删除菜单', '007002000', 'ACTION', '/menu/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001000', '监控配置', '006000000', 'LEAF', '/monitor/monitorList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006002000', '任务监控', '006000000', 'LEAF', '/taskMonitor/taskMonitorList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003000', '角色管理', '007000000', 'LEAF', '/role/roleList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003001', '跳转角色列表', '007003000', 'ACTION', '/role/initRole', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003002', '跳转新增角色页面', '007003000', 'ACTION', '/role/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003003', '新增角色', '007003000', 'ACTION', '/role/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003004', '跳转修改角色页面', '007003000', 'ACTION', '/role/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003005', '修改角色', '007003000', 'ACTION', '/role/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003006', '删除角色', '007003000', 'ACTION', '/role/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004000', 'ZooKeeper', '002000000', 'LEAF', '/zk/zkList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004001', '跳转zk列表', '002004000', 'ACTION', '/zk/initZk', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004002', '跳转新增zk页面', '002004000', 'ACTION', '/zk/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004003', '新增zk', '002004000', 'ACTION', '/zk/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004004', '跳转修改zk页面', '002004000', 'ACTION', '/zk/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004005', '修改zk', '002004000', 'ACTION', '/zk/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004006', '删除zk', '002004000', 'ACTION', '/zk/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004007', '验证zk', '002004000', 'ACTION', '/zk/checkZk', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004030000', '拦截器管理', '004000000', 'LEAF', '/interceptor/interceptorList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004030100', '跳转拦截器列表', '004030000', 'ACTION', '/interceptor/initInterceptor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004030200', '跳转新增拦截器页面', '004030000', 'ACTION', '/interceptor/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004030300', '新增拦截器', '004030000', 'ACTION', '/interceptor/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004030400', '跳转修改拦截器页面', '004030000', 'ACTION', '/interceptor/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004030500', '修改拦截器', '004030000', 'ACTION', '/interceptor/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004030600', '删除拦截器', '004030000', 'ACTION', '/interceptor/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002006000', 'SDDL', '002000000', 'LEAF', '/sddl/sddlList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002007000', 'HDFS', '002000000', 'LEAF', '/hdfs/hdfsList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002007001', '跳转HDFS列表', '002007000', 'ACTION', '/hdfs/initHDFSList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002001', '跳转HBase列表', '002002000', 'ACTION', '/hbase/initHBase', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002002', '跳转新增HBase页面', '002002000', 'ACTION', '/hbase/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002003', '新增HBase', '002002000', 'ACTION', '/hbase/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002004', '跳转修改HBase页面', '002002000', 'ACTION', '/hbase/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002005', '修改HBase', '002002000', 'ACTION', '	/hbase/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002006', '删除HBase', '002002000', 'ACTION', '/hbase/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('003002001', '执行脚本检测', '003002000', 'ACTION', '/sync/relation/checkSql', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('003003001', '获取同步检测树', '003003000', 'ACTION', '/sync/relation/getTrees', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002008000', 'Meta Mapping', '002000000', 'LEAF', '/metaMapping/metaMappingList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002007', '跳转logback修改', '001002000', 'ACTION', '/worker/toEditLogback', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002008', '保存logback修改', '001002000', 'ACTION', '/worker/doEditLogback', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002009', '重启机器', '001002000', 'ACTION', '/worker/restartWorker', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002010', '查看机器监控', '001002000', 'ACTION', '/worker/toWorkerMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002011', '查询机器JVM监控', '001002000', 'ACTION', '/worker/doSearchJvmMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002001008', 'ReloadRDBMS数据源', '002001000', 'ACTION', '/mediaSource/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003001', '跳转ES列表', '002003000', 'ACTION', '/es/initEs', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003002', '跳转ES新增', '002003000', 'ACTION', '/es/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003003', '保存ES新增', '002003000', 'ACTION', '/es/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003004', '跳转ES修改', '002003000', 'ACTION', '/es/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003005', '保存ES修改', '002003000', 'ACTION', '/es/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003006', '删除ES', '002003000', 'ACTION', '/es/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003007', '验证ES', '002003000', 'ACTION', '/es/checkEs', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002003008', 'ReloadES', '002003000', 'ACTION', '/es/toReloadES', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010111', '跳转task迁组', '004010100', 'ACTION', '/task/toGroupMigrate', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010112', '执行task迁组', '004010100', 'ACTION', '/task/doGroupMigrate', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010201', '跳转HBaseTask列表', '004010200', 'ACTION', '/hbaseTask/initHbaseTaskList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010202', '跳转新增HBaseTask页面', '004010200', 'ACTION', '/hbaseTask/toAddHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010203', '保存新增HBaseTask', '004010200', 'ACTION', '/hbaseTask/doAddHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010204', '跳转修改HBaseTask页面', '004010200', 'ACTION', '/hbaseTask/toUpdateHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010205', '保存修改HBaseTask', '004010200', 'ACTION', '/hbaseTask/doUpdateHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010206', '删除HBaseTask', '004010200', 'ACTION', '/hbaseTask/deleteHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010207', '暂停运行HBaseTask', '004010200', 'ACTION', '/hbaseTask/pauseHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010208', '恢复运行HBaseTask', '004010200', 'ACTION', '/hbaseTask/resumeHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004010209', '重启HBaseTask', '004010200', 'ACTION', '/hbaseTask/restartHbaseTask', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004020700', '映射数据校验', '004020000', 'ACTION', '/mediaMapping/dataCheck', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001001', '监控配置列表', '006001000', 'ACTION', '/monitor/initMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001002', '新增监控配置', '006001000', 'ACTION', '/monitor/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001003', '保存新增监控配置', '006001000', 'ACTION', '/monitor/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001004', '修改监控配置', '006001000', 'ACTION', '/monitor/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001005', '保存修改监控配置', '006001000', 'ACTION', '/monitor/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001006', '删除监控配置', '006001000', 'ACTION', '/monitor/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001007', '开启监控', '006001000', 'ACTION', '/monitor/doStart', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001008', '暂停监控', '006001000', 'ACTION', '/monitor/doPause', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001009', '开启所有监控', '006001000', 'ACTION', '/monitor/doAllStart', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001010', '停止所有监控', '006001000', 'ACTION', '/monitor/doAllStop', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006001011', '查看当前任务异常', '006001000', 'ACTION', '/taskMonitor/getException', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006002001', '任务监控列表', '006002000', 'ACTION', '/taskMonitor/initTaskMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006002002', '查看任务监控指标曲线', '006002000', 'ACTION', '/taskMonitor/toTaskStatistic', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006002003', '查询任务监控指标曲线', '006002000', 'ACTION', '/taskMonitor/doSearchTaskStatistic', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006002004', '查看任务监控异常列表', '006002000', 'ACTION', '/taskMonitor/toTaskException', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006002005', '加载任务监控异常列表', '006002000', 'ACTION', '/taskMonitor/initTaskException', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('006002006', '查看任务历史异常', '006002000', 'ACTION', '/taskMonitor/showException', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003007', '加载权限信息', '007003000', 'ACTION', '/role/initAuthority', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007003008', '保存权限信息', '007003000', 'ACTION', '/role/doEditAuthority', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002004008', 'ReloadZK', '002004000', 'ACTION', '/es/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('004030700', 'Reload拦截器', '004030000', 'ACTION', '/interceptor/toReload', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002006001', '跳转SDDL列表', '002006000', 'ACTION', '/sddl/initSddl', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002006002', '跳转SDDL新增', '002006000', 'ACTION', '/sddl/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002006003', '保存SDDL新增', '002006000', 'ACTION', '/sddl/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002006004', '跳转SDDL修改', '002006000', 'ACTION', '/sddl/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002006005', '保存SDDL修改', '002006000', 'ACTION', '/sddl/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002006006', '删除SDDL', '002006000', 'ACTION', '/sddl/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002006007', 'ReloadSDDL', '002006000', 'ACTION', '/sddl/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002007002', '跳转HDFS新增', '002007000', 'ACTION', '/hdfs/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002007003', '保存HDFS新增', '002007000', 'ACTION', '/hdfs/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002007004', '跳转HDFS修改', '002007000', 'ACTION', '/hdfs/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002007005', '保存HDFS修改', '002007000', 'ACTION', '/hdfs/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002007006', '删除HDFS', '002007000', 'ACTION', '/hdfs/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002007007', 'ReloadHDFS', '002007000', 'ACTION', '/hdfs/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002007', '验证HBase', '002002000', 'ACTION', '/hbase/checkHBase', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002002008', 'ReloadHBase', '002002000', 'ACTION', '/hbase/toReloadDB', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002008001', '跳转MetaMapping列表', '002008000', 'ACTION', '/metaMapping/initMapping', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002008002', '跳转MetaMapping新增', '002008000', 'ACTION', '/metaMapping/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002008003', '保存MetaMapping新增', '002008000', 'ACTION', '/metaMapping/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002008004', '跳转MetaMapping修改', '002008000', 'ACTION', '/metaMapping/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002008005', '保存MetaMapping修改', '002008000', 'ACTION', '/metaMapping/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002008006', '删除MetaMapping', '002008000', 'ACTION', '/metaMapping/doDelete', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('002008007', 'ReloadMetaMapping', '002008000', 'ACTION', '/metaMapping/doReload', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('001002012', '查询机器系统监控', '001002000', 'ACTION', '/worker/doSearchSystemMonitor', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007005000', '系统参数', '007000000', 'LEAF', '/sysProperties/propertieList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007005001', '跳转系统参数列表', '007005000', 'ACTION', '/sysProperties/intPropertiesList', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007005002', '跳转新增系统参数页面	', '007005000', 'ACTION', '/sysProperties/toAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007005003', '新增系统参数', '007005000', 'ACTION', '/sysProperties/doAdd', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007005004', '跳转修改系统参数页面', '007005000', 'ACTION', '/sysProperties/toEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007005005', '修改系统参数', '007005000', 'ACTION', '/sysProperties/doEdit', '', now(), now());
INSERT INTO `t_dl_menu` (`code`, `name`, `parent_code`, `type`, `url`, `icon`, `create_time`, `modify_time`)
VALUES ('007005006', '删除系统参数	', '007005000', 'ACTION', '/sysProperties/doDelete', '', now(), now());
INSERT INTO t_dl_menu (CODE, NAME, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('008002000', '审计日志', '007000000', 'LEAF', '/auditLog/auditLogList', '', now(), now());
INSERT INTO t_dl_menu (CODE, NAME, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('008002000', '审计日志', '007000000', 'LEAF', '/auditLog/auditLogList', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010400', '数据补录', '004010000', 'LEAF', '/decorate/toList', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010401', '跳转数据补录新增页面', '004010400', 'LEAF', '/decorate/toAddDecorate', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010402', '启动数据补录', '004010400', 'LEAF', '/decorate/start', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010403', '跳转数据补录修改页面', '004010400', 'LEAF', '/decorate/toUpdateDecorate', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010404', '删除数据补录', '004010400', 'LEAF', '/decorate/deleteDecorate', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010405', '跳转数据补录明细历史页面', '004010400', 'LEAF', '/decorate/toHistory', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010406', '新增数据补录', '004010400', 'LEAF', '/decorate/doAddDecorate', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010407', '修改数据补录', '004010400', 'LEAF', '/decorate/doUpdateDecorate', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010408', '查询数据补录明细历史', '004010400', 'LEAF', '/decorate/doHistory', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010409', '查询数据补录列表', '004010400', 'LEAF', '/decorate/queryDecorate', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010113', '跳转影子位点列表页面', '004010100', 'ACTION', '/shadow/toShadowList', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010114', '影子位点列表查询', '004010100', 'ACTION', '/shadow/doShadowList', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010115', '跳转影子位点添加页面', '004010100', 'ACTION', '/shadow/toAddShadow', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010116', '添加影子位点', '004010100', 'ACTION', '/shadow/doAdd', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('006002005', '跳转任务轨迹列表页面', '006002000', 'ACTION', '/taskMonitor/toTaskTrace', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('006002006', '任务轨迹列表查询', '006002000', 'ACTION', '/taskMonitor/initTaskTrace', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('001001007', '强制reBalance', '001001000', 'ACTION', '/group/doReBalance', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010000', 'Kudu', '002000000', 'LEAF', '/kudu/kuduList', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010001', '跳转Kudu列表', '002010000', 'ACTION', '/kudu/intkudu', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010002', '跳转新增页面', '002010000', 'ACTION', '/kudu/toAdd', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010003', '新增kudu', '002010000', 'ACTION', '/kudu/doAdd', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010004', '跳转修改页面', '002010000', 'ACTION', '/kudu/toEdit', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010005', '修改kudu', '002010000', 'ACTION', '/kudu/doEdit', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010006', '删除kudu', '002010000', 'ACTION', '/kudu/doDelete', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010007', '验证Kudu', '002010000', 'ACTION', '/kudu/checkKudu', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002010008', 'ReloadKudu', '002010000', 'ACTION', '/kudu/toReloadDB', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002011000', 'Kafka', '002000000', 'LEAF', '/kafka/kafkaList', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002011001', '跳转新增kafka页面', '002011000', 'ACTION', '/kafka/toAdd', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002011002', '新增kafka', '002011000', 'ACTION', '/kafka/doAdd', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002011003', '跳转修改kafka页面', '002011000', 'ACTION', '/kafka/toEdit', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002011004', '修改kafka', '002011000', 'ACTION', '/kafka/doEdit', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002011005', '删除kafka', '002011000', 'ACTION', '/kafka/doDelete', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002011006', 'Reload Kafka', '002011000', 'ACTION', '/kafka/toReloadDB', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004020800', '查看映射', '004020000', 'ACTION', '/mediaMapping/toView', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('002001009', '查看表结构', '002001000', 'ACTION', '/mediaSource/toGetTableStructure', '', now(), now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005000000','全量任务','000000000','NODE','','fa-arrows-alt',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001000','Job配置管理','005000000','LEAF','/jobConfig/jobList','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005002000','Job运行管理','005000000','LEAF','/jobExecution/jobList','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003000','Job运行队列','005000000','LEAF','/jobQueue/toJobQueue','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005004000','Job定时任务','005000000','LEAF','/jobSchedule/ScheduleList','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001001','跳转Job配置列表','005001000','ACTION','/jobConfig/initJob','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001002','跳转Job新增','005001000','ACTION','/jobConfig/toAdd','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001003','报存Job新增','005001000','ACTION','/jobConfig/doAdd','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001004','跳转Job快速新增','005001000','ACTION','/jobConfig/toFastAdd','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001005','保存Job快速新增','005001000','ACTION','/jobConfig/doFastAdd','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001006','跳转Job修改','005001000','ACTION','/jobConfig/toEdit','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001007','保存Job修改','005001000','ACTION','/jobConfig/doEdit','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001008','删除Job','005001000','ACTION','/jobConfig/doDelete','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001009','跳转启动并获取运行的datax机器列表','005001000','ACTION','/jobConfig/works','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001010','启动Job','005001000','ACTION','/jobConfig/doStart','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001011','跳转Job历史页面','005001000','ACTION','/jobConfig/toHistory','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001012','加载Job历史列表','005001000','ACTION','/jobConfig/doHistory','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001013','展示Job的MD5信息','005001000','ACTION','/jobConfig/doJobConfigMD5Info','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001014','跳转重新加载数据源','005001000','ACTION','/jobConfig/toReloadJob','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001015','执行重新加载数据源','005001000','ACTION','/jobConfig/doReoloadJob','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005001016','加入Job队列','005001000','ACTION','/jobConfig/doAddJobRunQueue','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005002001','跳转Job运行列表','005002000','ACTION','/jobExecution/initJob','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005002002','查看Job运行异常','005002000','ACTION','/jobExecution/doStat','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005002003','停止Job','005002000','ACTION','/jobExecution/doStop','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005002004','强制停止Job','005002000','ACTION','/jobExecution/doForceStop','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005002005','废弃Job','005002000','ACTION','/jobExecution/doDiscard','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003001','跳转Job运行队列列表','005003000','ACTION','/jobRunQueue/initJobQueue','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003002','删除Job运行队列','005003000','ACTION','/jobRunQueue/doDeleteJobQueue','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003003','跳转查看Job运行队列配置信息','005003000','ACTION','/jobRunQueue/toJobRunQueueConfigInfo','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003004','加载Job运行队列配置信息','005003000','ACTION','/jobRunQueue/initJobQueueConfigInfo','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003005','跳转Job运行队列修改','005003000','ACTION','/jobRunQueue/toEdit','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003006','保存Job运行队列修改','005003000','ACTION','/jobRunQueue/doUpdateInitStateJobQueue','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003007','跳转Job运行详情','005003000','ACTION','/jobRunQueue/toExecutionInfo','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003008','加载Job运行详情列表','005003000','ACTION','/jobRunQueue/initJobQueueExecutionInfo','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003009','置顶Job','005003000','ACTION','/jobRunQueue/doTop','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003010','启动或关闭Job','005003000','ACTION','/jobRunQueue/doUpdateState','',now(),now() );
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003011','开启或关闭Job队列','005003000','ACTION','/jobRunQueue/doOpenOrCloseQueue','',now(),now());
insert into t_dl_menu (code,name,parent_code,type,url,icon,create_time,modify_time ) values('005003012','再次执行Job','005003000','ACTION','/jobRunQueue/doUpdateJobRunQueueById','',now(),now());


INSERT INTO `t_dl_sys_properties` (`properties_key`, `properties_value`, `create_time`, `modify_time`) VALUES
  ('multiplexingReadGlobal', 'false', now(), now());

INSERT INTO `t_dl_role` (`id`, `code`, `name`, `create_time`, `modify_time`) VALUES (1, 'SUPER', '超级管理员', now(), now());
INSERT INTO `t_dl_role` (`id`, `code`, `name`, `create_time`, `modify_time`)
VALUES (2, 'ORDINARY', '普通用户', now(), now());

INSERT INTO `t_dl_user` (`id`, `user_name`, `ucar_email`, `phone`, `create_time`, `modify_time`, `is_alarm`)
VALUES (1, 'admin', 'admin', '18800000000', now(), now(), '1');

INSERT INTO `t_dl_user_role` (`id`, `user_id`, `role_id`, `create_time`, `modify_time`) VALUES (1, 1, 1, now(), now());
