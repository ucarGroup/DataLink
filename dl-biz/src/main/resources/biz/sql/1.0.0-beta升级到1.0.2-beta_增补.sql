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

update t_dl_menu set name = '数据补录' where code = '004010400';