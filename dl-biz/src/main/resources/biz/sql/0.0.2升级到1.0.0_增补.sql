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


ALTER TABLE `t_dl_media_mapping` DROP INDEX `idx1_mediamapping`;
ALTER TABLE `t_dl_media_mapping` ADD CONSTRAINT `idx1_mediamapping` UNIQUE (`task_id`, `source_media_id`, `target_media_source_id`, `target_media_name`);
ALTER TABLE `t_dl_media_mapping` ADD COLUMN `es_routing` VARCHAR(100) DEFAULT NULL
COMMENT 'esRouting字段';
ALTER TABLE `t_dl_media_mapping` ADD COLUMN `es_routing_ignore` VARCHAR(5) DEFAULT NULL
COMMENT '如果routing字段值不存在，写入的数据是否可忽略写入,true：可以忽略，false：不可以忽略';


INSERT INTO t_dl_menu (CODE, NAME, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('008002000', '审计日志', '007000000', 'LEAF', '/auditLog/auditLogList', '', now(), now());
INSERT INTO t_dl_menu (code, name, parent_code, type, url, icon, create_time, modify_time) VALUES
  ('004010400', '跳转数据补录列表页面', '004010000', 'LEAF', '/decorate/toList', '', now(), now());
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

INSERT INTO `t_dl_sys_properties` (`properties_key`, `properties_value`, `create_time`, `modify_time`) VALUES
  ('multiplexingReadGlobal', 'false', now(), now());