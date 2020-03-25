package com.ucar.datalink.flinker.core.admin.record;

import com.ucar.datalink.flinker.api.util.Mysql8Utils;
import com.ucar.datalink.flinker.core.admin.util.DataSourceController;
import com.ucar.datalink.flinker.core.job.meta.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.MessageFormat;
/**
 * db工具类
 * 
 * @author lubiao
 */
public class JobExecutionDbUtils {
	private static final Logger logger = LoggerFactory.getLogger(JobExecutionDbUtils.class);

	private static final String INSERT_SQL_TEMPLATE = "insert into t_dl_flinker_job_execution(job_id,worker_address,start_time,state,pid,exception,job_queue_execution_id,original_configuration )values(?,?,?,?,?,?,?,?)";

	private static final String UPDATE_SQL_TEMPLATE = "update t_dl_flinker_job_execution " + "set job_id=?," + "worker_address=?," + "start_time=?,"
			+ "end_time=?," + "state=?," + "byte_speed_per_second=?," + "record_speed_per_second=?," + "total_record=?," + "total_error_records=?,"
			+ "wait_reader_time=?," + "wait_writer_time=?," + "percentage=?," + "exception=?," + "pid=?," +"task_communication_info=?"+ " where id=?";

	private static final String UPDATE_SQL_TIMING_TASK = "update t_timing_task_history set status=?, modify_time=now()   where id=?";

	private static final String QUERY_STATE_SQL_TEMPLATE = "select state from t_dl_flinker_job_execution where id=?";


	/**
	 * job execution配置中有一个 orginal的字段，用来存储最原始，最全的job配置信息，这个配置信息比 job_config表里的那个job配置还要全，所以不能用job_config里面的那个内容
	 * 但这个 orginal内容是在 第一次插入job execution id之后，运行job之前完成的，所以只能等到job运行的时候再去更新这个字段
	 * 由于这个字段内容比较多，每次更新job的时候最好就不要更新这个字段了，所以更新的时候加了判断，如果 jobExecution变量关联的 这个
	 */
	private static final String UPDATE_ORIGINAL_SQL_TEMPLATE = "update t_dl_flinker_job_execution " + "set job_id=?," + "worker_address=?," + "start_time=?,"
			+ "end_time=?," + "state=?," + "byte_speed_per_second=?," + "record_speed_per_second=?," + "total_record=?," + "total_error_records=?,"
			+ "wait_reader_time=?," + "wait_writer_time=?," + "percentage=?," + "exception=?," + "pid=?," +"task_communication_info=?, original_configuration=? where id=?";

	private static final String UPDATE_JOB_EXECUTION_STATE_SQL = "UPDATE t_dl_flinker_job_execution SET state=?,start_time=?,end_time=?,exception=? WHERE id=?";

	private static final String UPDATE_STATE_SQL =
			"UPDATE t_dl_flinker_job_execution SET state=?,end_time=?,exception=? WHERE id=?";

	/**
	 * 根据id 获取job execution表的所有字段，暂时用不到，先加上
	 */
	private static final String SELECT_ALL_FIELD_SQL_TEMPLATE = "SELECT id,job_id,worker_address,pid,start_time,end_time,state,byte_speed_per_second,record_speed_per_second," +
			"total_error_records,wait_reader_time,wait_writer_time,percentage,exception,job_queue_execution_id,task_communication_info,original_configuration FROM t_dl_flinker_job_execution WHERE id=?";

	private static String address;
	private static Integer port;
	private static String schema;
	private static String userName;
	private static String password;
	private static Connection connection;

	/**
	 * 重试三次
 	 */
	private static final int RETRY_TIMES = 3;

	static {
		try {
			parseDbConfig();
			initConnection();
		} catch (Exception e) {
			logger.error("something goes wrong when initializing jdbc connection for " + JobExecutionDbUtils.class.getName(), e);
		}
	}

	public static State getJobState(long id) throws SQLException {
		for(int i=0;i<RETRY_TIMES;i++) {
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				pstmt = connection.prepareStatement(QUERY_STATE_SQL_TEMPLATE);
				pstmt.setLong(1, id);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					return State.valueOf(rs.getString("state"));
				}
				break;
			} catch(Exception e) {
				logger.error(e.getMessage(),e);
				try {
					initConnection();
				} catch (Exception e1) {
					logger.error(e.getMessage(),e);
				}
			} finally {
				closeResultSet(rs);
				closeStatement(pstmt);
			}
		}
		throw new RuntimeException(MessageFormat.format("JobExecution for id {0} is not founded.", id));
	}



	public static void initJobExecutionByHAInvoke(JobExecution jobExecution) throws SQLException {
		updateJobExecution(jobExecution);
	}


	public static void updateJobExecutionState(JobExecution jobExecution) throws SQLException {
		for(int i=0;i<RETRY_TIMES;i++) {
			PreparedStatement pstmt = null;
			try {
				pstmt = connection.prepareStatement(UPDATE_JOB_EXECUTION_STATE_SQL);
				pstmt.setString(1, jobExecution.getState().toString());
				pstmt.setTimestamp(2, new Timestamp(jobExecution.getStartTime()));
				pstmt.setTimestamp(3, new Timestamp(jobExecution.getEndTime()));
				pstmt.setString(4,jobExecution.getException());
				pstmt.setLong(5,jobExecution.getId());
				pstmt.executeUpdate();
				break;
			} catch(Exception e) {
				logger.error(e.getMessage(),e);
				try {
					initConnection();
				} catch (Exception e1) {
					logger.error(e.getMessage(),e);
				}
			} finally{
				closeStatement(pstmt);
			}
		}
		checkFailureStateAndRecord(jobExecution);
	}


	public static void updateState(JobExecution jobExecution) throws SQLException {
		for(int i=0;i<RETRY_TIMES;i++) {
			PreparedStatement pstmt = null;
			try {
				pstmt = connection.prepareStatement(UPDATE_STATE_SQL);
				pstmt.setString(1, jobExecution.getState().toString());
				pstmt.setTimestamp(2, new Timestamp(jobExecution.getEndTime()));
				if(jobExecution.getException()==null) {
					pstmt.setString(3,"");
				} else {
					pstmt.setString(3,jobExecution.getException());
				}
				pstmt.setLong(4,jobExecution.getId());
				pstmt.executeUpdate();
				break;
			} catch(Exception e) {
				logger.error(e.getMessage(),e);
				try {
					initConnection();
				} catch (Exception e1) {
					logger.error(e.getMessage(),e);
				}
			} finally{
				closeStatement(pstmt);
			}
		}
		checkFailureStateAndRecord(jobExecution);
	}


	public static void insertJobExecution(JobExecution jobExecution) throws SQLException {
		for(int i=0;i<RETRY_TIMES;i++) {
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				pstmt = connection.prepareStatement(INSERT_SQL_TEMPLATE, Statement.RETURN_GENERATED_KEYS);
				pstmt.setLong(1, jobExecution.getJobId());
				pstmt.setString(2, jobExecution.getWorkerAddress());
				pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				pstmt.setString(4, jobExecution.getState().toString());
				pstmt.setInt(5, jobExecution.getPid());
				pstmt.setString(6, jobExecution.getException());
				pstmt.setLong(7, jobExecution.getJobQueueExecutionId());
				pstmt.setString(8, jobExecution.getOriginalConfiguration());
				pstmt.executeUpdate();
				rs = pstmt.getGeneratedKeys();
				if (rs.next()) {
					Long id = rs.getLong(1);
					jobExecution.setId(id);
				}
				break;
			} catch(Exception e) {
				logger.error(e.getMessage(),e);
				try {
					initConnection();
				} catch (Exception e1) {
					logger.error(e.getMessage(),e);
				}
			} finally {
				closeResultSet(rs);
				closeStatement(pstmt);
			}
		}
		checkFailureStateAndRecord(jobExecution);
	}

	public static void updateJobExecution(JobExecution jobExecution) throws SQLException {
		for(int i=0;i<RETRY_TIMES;i++) {
			PreparedStatement pstmt = null;
			try {
				pstmt = connection.prepareStatement(UPDATE_SQL_TEMPLATE);
				pstmt.setLong(1, jobExecution.getJobId());
				pstmt.setString(2, jobExecution.getWorkerAddress());
				pstmt.setTimestamp(3, new Timestamp(jobExecution.getStartTime()));
				pstmt.setTimestamp(4, jobExecution.getEndTime() == null ? null : new Timestamp(jobExecution.getEndTime()));
				pstmt.setString(5, jobExecution.getState().toString());
				pstmt.setLong(6, jobExecution.getByteSpeedPerSecond());
				pstmt.setLong(7, jobExecution.getRecordSpeedPerSecond());
				pstmt.setLong(8, jobExecution.getTotalRecord());
				pstmt.setLong(9, jobExecution.getTotalErrorRecords());
				pstmt.setFloat(10, jobExecution.getWaitReaderTime());
				pstmt.setFloat(11, jobExecution.getWaitWriterTime());
				pstmt.setDouble(12, jobExecution.getPercentage());
				pstmt.setString(13, jobExecution.getException());
				pstmt.setInt(14, jobExecution.getPid());
				pstmt.setString(15, jobExecution.getTaskCommunicationInfo());
				pstmt.setLong(16, jobExecution.getId());
				pstmt.executeUpdate();
				break;
			} catch(Exception e) {
				logger.error(e.getMessage(),e);
				try {
					initConnection();
				} catch (Exception e1) {
					logger.error(e.getMessage(),e);
				}
			} finally {
				closeStatement(pstmt);
			}
		}
		checkFailureStateAndRecord(jobExecution);
	}





	public static void updateJobExecutionAndOriginalConfiguration(JobExecution jobExecution) throws SQLException {
		for(int i=0;i<RETRY_TIMES;i++) {
			PreparedStatement pstmt = null;
			try {
				pstmt = connection.prepareStatement(UPDATE_ORIGINAL_SQL_TEMPLATE);
				pstmt.setLong(1, jobExecution.getJobId());
				pstmt.setString(2, jobExecution.getWorkerAddress());
				pstmt.setTimestamp(3, new Timestamp(jobExecution.getStartTime()));
				pstmt.setTimestamp(4, jobExecution.getEndTime() == null ? null : new Timestamp(jobExecution.getEndTime()));
				pstmt.setString(5, jobExecution.getState().toString());
				pstmt.setLong(6, jobExecution.getByteSpeedPerSecond());
				pstmt.setLong(7, jobExecution.getRecordSpeedPerSecond());
				pstmt.setLong(8, jobExecution.getTotalRecord());
				pstmt.setLong(9, jobExecution.getTotalErrorRecords());
				pstmt.setFloat(10, jobExecution.getWaitReaderTime());
				pstmt.setFloat(11, jobExecution.getWaitWriterTime());
				pstmt.setDouble(12, jobExecution.getPercentage());
				pstmt.setString(13, jobExecution.getException());
				pstmt.setInt(14, jobExecution.getPid());
				pstmt.setString(15, jobExecution.getTaskCommunicationInfo());
				pstmt.setString(16, jobExecution.getOriginalConfiguration());
				pstmt.setLong(17, jobExecution.getId());
				pstmt.executeUpdate();
				break;
			} catch(Exception e) {
				logger.error(e.getMessage(),e);
				try {
					initConnection();
				} catch (Exception e1) {
					logger.error(e.getMessage(),e);
				}
			} finally {
				closeStatement(pstmt);
			}
		}
		checkFailureStateAndRecord(jobExecution);
	}



	public static void updateTimingTask(Long id,int status) throws SQLException {
		for(int i=0;i<RETRY_TIMES;i++) {
			PreparedStatement pstmt = null;
			try {
				pstmt = connection.prepareStatement(UPDATE_SQL_TIMING_TASK);
				pstmt.setLong(1, status);
				pstmt.setLong(2, id);
				pstmt.executeUpdate();
			} catch(Exception e) {
				logger.error(e.getMessage(),e);
				try {
					initConnection();
				} catch (Exception e1) {
					logger.error(e.getMessage(),e);
				}
			} finally {
				closeStatement(pstmt);
			}
		}
	}

	private static void parseDbConfig() throws Exception {
/*
		Properties properties = new Properties();
		properties.load(new FileInputStream(CoreConstant.DATAX_ADMIN_CONF));
		address = properties.getProperty(AdminConstants.DATAX_DB_ADDRESS);
		port = Integer.valueOf(properties.getProperty(AdminConstants.DATAX_DB_PORT));
		schema = properties.getProperty(AdminConstants.DATAX_DB_SCHEMA);
		userName = properties.getProperty(AdminConstants.DATAX_DB_USERNAME);
		password = Encryption.decrypt(properties.getProperty(AdminConstants.DATAX_DB_PASSWORD));
*/
		address = DataSourceController.getInstance().getAddress();
        port = DataSourceController.getInstance().getPort();
        schema = DataSourceController.getInstance().getSchema();
        userName = DataSourceController.getInstance().getUserName();
        password = DataSourceController.getInstance().getPassword();
	}

	private static void initConnection() throws Exception {
		parseDbConfig();
		String url = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}", address, String.valueOf(port), schema);
		Class.forName("com.mysql.jdbc.Driver");
		connection = Mysql8Utils.getMysqlConnection(url, userName, password);
	}

	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				logger.error("error occurred when close ResultSet", e);
			}
		}
	}

	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				logger.error("error occurred when close Statement", e);
			}
		}
	}

	/**
	 * 如果Job运行状态是FAILED，则打印当前线程的堆栈信息
	 * @param je
	 */
	private static void checkFailureStateAndRecord(JobExecution je) {
		try {
			if(je!=null && je.getState()!=null) {
				if(State.FAILED == je.getState()) {
					logger.error( callStatck() );
				}
			}
		} catch(Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	/**
	 * dump堆栈信息
	 * @return
	 */
	private static String callStatck() {
		Throwable ex = new Throwable();
		StackTraceElement[] stackElements = ex.getStackTrace();
		StringBuilder sb = new StringBuilder();
		sb.append("current state is FAILED,stack :");
		if (stackElements != null) {
			for (int i = 0; i < stackElements.length; i++) {
				sb.append(stackElements[i].getClassName()).append("#");
				sb.append(stackElements[i].getMethodName());
				sb.append("(");
				sb.append(stackElements[i].getFileName());
				sb.append(" ").append(stackElements[i].getLineNumber());
				sb.append(")").append("\n");
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	public static void reConnect() {
		if(connection != null) {
			try {
				connection.close();
				initConnection();
			} catch (SQLException e) {
				logger.error("error occurred when close Connection", e);
			} catch (Exception e) {
				logger.error("re init connect failure", e);
			}
		}
	}

}
