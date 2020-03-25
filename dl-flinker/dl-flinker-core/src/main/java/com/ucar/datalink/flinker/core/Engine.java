package com.ucar.datalink.flinker.core;

import com.ucar.datalink.common.zookeeper.DLinkZkUtils;
import com.ucar.datalink.common.zookeeper.ZkConfig;
import com.ucar.datalink.flinker.api.element.ColumnCast;
import com.ucar.datalink.flinker.api.exception.DataXException;
import com.ucar.datalink.flinker.api.spi.ErrorCode;
import com.ucar.datalink.flinker.api.statistics.PerfTrace;
import com.ucar.datalink.flinker.api.statistics.VMInfo;
import com.ucar.datalink.flinker.api.util.Configuration;
import com.ucar.datalink.flinker.api.util.GsonUtil;
import com.ucar.datalink.flinker.core.admin.AdminConstants;
import com.ucar.datalink.flinker.core.admin.bean.JobConfigBean;
import com.ucar.datalink.flinker.core.admin.except.CanIgnoreException;
import com.ucar.datalink.flinker.core.admin.record.JobConfigDbUtils;
import com.ucar.datalink.flinker.core.admin.record.JobExecution;
import com.ucar.datalink.flinker.core.admin.record.JobExecutionDbUtils;
import com.ucar.datalink.flinker.core.admin.record.JobExecutionRecorder;
import com.ucar.datalink.flinker.core.admin.util.DataSourceInitController;
import com.ucar.datalink.flinker.api.util.ErrorRecord;
import com.ucar.datalink.flinker.core.job.JobContainer;
import com.ucar.datalink.flinker.core.job.meta.State;
import com.ucar.datalink.flinker.core.statistics.communication.Communication;
import com.ucar.datalink.flinker.core.taskgroup.TaskGroupContainer;
import com.ucar.datalink.flinker.core.util.ConfigParser;
import com.ucar.datalink.flinker.core.util.ConfigurationValidate;
import com.ucar.datalink.flinker.core.util.ExceptionTracker;
import com.ucar.datalink.flinker.core.util.FrameworkErrorCode;
import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import com.ucar.datalink.flinker.core.util.container.LoadUtil;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Engine是DataX入口类，该类负责初始化Job或者Task的运行容器，并运行插件的Job或者Task逻辑
 */
public class Engine {
	private static final Logger LOG = LoggerFactory.getLogger(Engine.class);

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	private static String RUNTIME_MODE;

	public static long jobId;

	/* check job model (job/task) first */
	public void start(Configuration allConf) {

		// 绑定column转换信息
		ColumnCast.bind(allConf);

		/**
		 * 初始化PluginLoader，可以获取各种插件配置
		 */
		LoadUtil.bind(allConf);

		boolean isJob = !("taskGroup".equalsIgnoreCase(allConf.getString(CoreConstant.DATAX_CORE_CONTAINER_MODEL)));

		AbstractContainer container;
		long instanceId;
		int taskGroupId = -1;
		if (isJob) {
			allConf.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_MODE, RUNTIME_MODE);
			container = new JobContainer(allConf);
			instanceId = allConf.getLong(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID, 0);

		} else {
			container = new TaskGroupContainer(allConf);
			instanceId = allConf.getLong(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID);
			taskGroupId = allConf.getInt(CoreConstant.DATAX_CORE_CONTAINER_TASKGROUP_ID);
		}

		// 缺省打开perfTrace
		boolean traceEnable = allConf.getBool(CoreConstant.DATAX_CORE_CONTAINER_TRACE_ENABLE, true);
		boolean perfReportEnable = allConf.getBool(CoreConstant.DATAX_CORE_REPORT_DATAX_PERFLOG, true);

		int priority = 0;
		try {
			priority = Integer.parseInt(System.getenv("SKYNET_PRIORITY"));
		} catch (NumberFormatException e) {
			LOG.warn("prioriy set to 0, because NumberFormatException, the value is: " + System.getProperty("PROIORY"));
		}

		Configuration jobInfoConfig = allConf.getConfiguration(CoreConstant.DATAX_JOB_JOBINFO);
		// 初始化PerfTrace
		PerfTrace perfTrace = PerfTrace.getInstance(isJob, instanceId, taskGroupId, priority, traceEnable);
		perfTrace.setJobInfo(jobInfoConfig);
		perfTrace.setPerfReportEnalbe(perfReportEnable);
		perfTrace.setOriginalConfiguration(GsonUtil.toJson(allConf));
		container.start();

	}

	// 注意屏蔽敏感信息

	public static String filterJobConfiguration(final Configuration configuration) {
		Configuration jobConfWithSetting = configuration.getConfiguration("job").clone();

		Configuration jobContent = jobConfWithSetting.getConfiguration("content");

		filterSensitiveConfiguration(jobContent);

		jobConfWithSetting.set("content", jobContent);

		return jobConfWithSetting.beautify();
	}

	public static Configuration filterSensitiveConfiguration(Configuration configuration) {
		Set<String> keys = configuration.getKeys();
		for (final String key : keys) {
			boolean isSensitive = StringUtils.endsWithIgnoreCase(key, "password") || StringUtils.endsWithIgnoreCase(key, "accessKey");
			if (isSensitive && configuration.get(key) instanceof String) {
				configuration.set(key, configuration.getString(key).replaceAll(".", "*"));
			}
		}
		return configuration;
	}

	public static void entry(final String[] args) throws Throwable {
		Options options = new Options();
		options.addOption("job", true, "Job config.");
		options.addOption("jobid", true, "Job unique id.");
		options.addOption("mode", true, "Job runtime mode.");
		options.addOption("jqeid",true,"Job Queue Execution id");
		options.addOption("timingJobId",true,"Job timer id");
		options.addOption("executeId", true, "Job execution id");
		BasicParser parser = new BasicParser();
		CommandLine cl = parser.parse(options, args);

		String jobPath = cl.getOptionValue("job");


		Long jqeid = Long.valueOf(cl.getOptionValue("jqeid"));

		// 如果用户没有明确指定jobid, 则 datax.py 会指定 jobid 默认值为-1
		String jobIdString = cl.getOptionValue("jobid");
		RUNTIME_MODE = cl.getOptionValue("mode");


		long job_id = Long.parseLong(jobIdString);
		if(job_id == -1) {
			throw new RuntimeException("job id 不能为-1");
		}
		Engine.jobId = job_id;
		JobConfigBean bean = JobConfigDbUtils.readConfig(job_id);
		Configuration configuration = Configuration.from(bean.getJob_content());
		configuration = ConfigParser.assembleConfiguration(configuration);

		//设置job execution id
		long executeId = -1;
		try {
			if(cl.getOptionValue("executeId") != null) {
				executeId = Long.valueOf(cl.getOptionValue("executeId"));
			}
		} catch(Exception e) {}
		configuration.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_EXECUTION_ID, executeId);
		Communication comm = new Communication();
		comm.setState(State.RUNNING);
		comm.setTimestamp(System.currentTimeMillis());
		//启动直接插入一条记录，将job execution改为运行状态
		JobExecutionRecorder.getInstance().record(job_id, executeId, comm);

		//设置定时任务id
		String timingJobIdString = cl.getOptionValue("timingJobId");
		if(StringUtils.isNotBlank(timingJobIdString) && !"-1".equalsIgnoreCase(timingJobIdString)){
			PerfTrace perfTrace = PerfTrace.getInstance();
			perfTrace.setTimingJobId(Long.valueOf(timingJobIdString));
		}

		long jobId;
		if (!"-1".equalsIgnoreCase(jobIdString)) {
			jobId = Long.parseLong(jobIdString);
		} else {
			// only for dsc & ds & datax 3 update
			String dscJobUrlPatternString = "/instance/(\\d{1,})/config.xml";
			String dsJobUrlPatternString = "/inner/job/(\\d{1,})/config";
			String dsTaskGroupUrlPatternString = "/inner/job/(\\d{1,})/taskGroup/";
			List<String> patternStringList = Arrays.asList(dscJobUrlPatternString, dsJobUrlPatternString, dsTaskGroupUrlPatternString);
			jobId = parseJobIdFromUrl(patternStringList, jobPath);
		}

		boolean isStandAloneMode = "standalone".equalsIgnoreCase(RUNTIME_MODE);
		if (!isStandAloneMode && jobId == -1) {
			// 如果不是 standalone 模式，那么 jobId 一定不能为-1
			throw DataXException.asDataXException(FrameworkErrorCode.CONFIG_ERROR, "非 standalone 模式必须在 URL 中提供有效的 jobId.");
		}
		configuration.set(CoreConstant.DATAX_CORE_CONTAINER_JOB_ID, jobId);



		// 打印vmInfo
		VMInfo vmInfo = VMInfo.getVmInfo();
		if (vmInfo != null) {
			LOG.info(vmInfo.toString());
		}
		LOG.info("\n" + Engine.filterJobConfiguration(configuration) + "\n");
		LOG.debug(configuration.toJSON());
		ConfigurationValidate.doValidate(configuration);
		String[] array = StringUtils.split(jobPath, "/");
		String jobName = array[array.length - 1];
		try {
			RunningDataManager.register(jqeid,jobId, jobName);
			Engine engine = new Engine();
			engine.start(configuration);
		} finally {
			RunningDataManager.release();
		}
	}

	/**
	 * -1 表示未能解析到 jobId
	 *
	 * only for dsc & ds & datax 3 update
	 */
	private static long parseJobIdFromUrl(List<String> patternStringList, String url) {
		long result = -1;
		for (String patternString : patternStringList) {
			result = doParseJobIdFromUrl(patternString, url);
			if (result != -1) {
				return result;
			}
		}
		return result;
	}

	private static long doParseJobIdFromUrl(String patternString, String url) {
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return Long.parseLong(matcher.group(1));
		}

		return -1;
	}

	public static void main(String[] args) throws Exception {
		int exitCode = 0;
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(CoreConstant.DATAX_ADMIN_CONF));
			final String zkServers = getProperty(properties, AdminConstants.DATAX_ZKSERVERS);
			DLinkZkUtils.init(new ZkConfig(zkServers, 10000, 10000),"/datalink");

			DataSourceInitController.getInstance().initialize();
			Engine.entry(args);
			recordErr(args);
		} catch (Throwable e) {
			exitCode = 1;
			LOG.error("\n\n经DataX智能分析,该任务最可能的错误原因是:\n" + ExceptionTracker.trace(e));

			if (e instanceof DataXException) {
				DataXException tempException = (DataXException) e;
				ErrorCode errorCode = tempException.getErrorCode();
				if (errorCode instanceof FrameworkErrorCode) {
					FrameworkErrorCode tempErrorCode = (FrameworkErrorCode) errorCode;
					exitCode = tempErrorCode.toExitValue();
				}
			}

			if(e instanceof  OutOfMemoryError) {
				System.gc();
			}
			saveJobFailureState(args,e);
			System.exit(exitCode);
		}finally {

		}
		System.exit(exitCode);
	}

	/**
	 * 先尝试用JobExecutionRecorder 更新job状态，如果JobExecutionRecorder也没有初始化过，那就从args参数中解析
	 * 解析出job_execution_id，然后更新数据库，将job状态改为失败
	 * 有一种情况是从控制台启动的，但是JobExecutionRecorder没有初始化过，这样就无法更改job状态了
	 * @param args
	 */
	private static void saveJobFailureState(String[] args, Throwable e) {
		try {
			String job_execution_id = null;
			//如果执行过 JobExecutionRecorder的操作，那么JobExecutionRecorder中的jobExecution就不为null了，可以用
			//JobExecutionRecorder类直接更新
			if( JobExecutionRecorder.getInstance().getJobExecution() != null) {
				if(RunningDataManager.isJobKilling()) {
					JobExecutionRecorder.getInstance().updateJobExecutionState(State.KILLED, e);
				} else {
					JobExecutionRecorder.getInstance().updateJobExecutionState(State.FAILED, e);
				}
				return;
			}

			if(e instanceof CanIgnoreException) {
				return;
			}

			//如果JobExecutionRecorder中的变量jobExecution为null，就需要从启动参数中解析 job_execution_id了
			//如果是通过data-link控制体界面启动的任务是不传递job_execution_id的，也就无法保存了
			//如果是通过高可用方式调用过来的，就带了job_execution_id，解析出这个id并保存
			if(args!=null && args.length>0) {
				for(int i=0;i<args.length;i++) {
					if("--executeId".equals(args[i])) {
						if( (i+1)<args.length ) {
							job_execution_id = args[i+1];
							break;
						}
					}
				}
			}
			if(job_execution_id!=null) {
				JobExecution je = new JobExecution();
				je.setId(Long.parseLong(job_execution_id));
				je.setEndTime(System.currentTimeMillis());
				je.setState(State.FAILED);
				je.setException( ErrorRecord.assembleError("Engine runbnning job failure", e) );
				JobExecutionDbUtils.updateJobExecutionState(je);
			}
		} catch(Throwable ex) {
			LOG.error(ex.getMessage(),ex);
		}
	}

	/**
	 * 如果异常记录类 ErrRecord中有异常信息，将这些异常记录到数据库中
	 * @param args
	 */
	private static void recordErr(String[] args) {
		if(!ErrorRecord.hasErr()) {
			return;
		}
		try {
			String job_execution_id = null;
			//如果JobExecutionRecorder中的变量jobExecution为null，就需要从启动参数中解析 job_execution_id了
			//如果是通过data-link控制体界面启动的任务是不传递job_execution_id的，也就无法保存了
			//如果是通过高可用方式调用过来的，就带了job_execution_id，解析出这个id并保存
			if(args!=null && args.length>0) {
				for(int i=0;i<args.length;i++) {
					if("--executeId".equals(args[i])) {
						if( (i+1)<args.length ) {
							job_execution_id = args[i+1];
							break;
						}
					}
				}
			}
			if(job_execution_id!=null) {
				JobExecution je = new JobExecution();
				je.setId(Long.parseLong(job_execution_id));
				je.setEndTime(System.currentTimeMillis());
				je.setState(State.FAILED);
				je.setException( ErrorRecord.assembleError() );
				JobExecutionDbUtils.updateJobExecutionState(je);
			}
		}catch(Exception e) {
			LOG.error(e.getMessage(),e);
		}
	}

	private static String getProperty(Properties properties, String key) {
		return org.apache.commons.lang.StringUtils.trim(properties.getProperty(org.apache.commons.lang.StringUtils.trim(key)));
	}
}
