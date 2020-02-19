package com.ucar.datalink.flinker.core.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 进程处理工具类(暂时只支持linux)
 * 
 * @author lubiao
 * 
 */
public class ProcessUtils {

	private static Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

	public static int getThisPid() {
		String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
		return Integer.valueOf(pid);
	}

	/**
	 * 检测对应的进程是否存在
	 * 
	 * @param pid
	 *            进程id
	 * @return
	 * @throws IOException
	 */
	public static boolean checkIfJobProcessExists(int pid) throws Exception {
		Process proc = Runtime.getRuntime().exec("ps -ef");
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String line;
		while ((line = bufferedreader.readLine()) != null) {
			if (line.contains(String.valueOf(pid)) && line.contains("com.ucar.datalink.flinker.core.Engine")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取所有的datax进程
	 * 
	 * @return
	 */
	public static Map<String, Integer> getAllDataxProcess() {
		Map<String, Integer> result = new HashMap<String, Integer>();

		try {
			Process proc = Runtime.getRuntime().exec("ps -ef");
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line;
			while ((line = bufferedreader.readLine()) != null) {
				if (line.contains("com.ucar.datalink.flinker.core.Engine")) {
					line = line.replaceAll(" +", " || ");
					System.out.println(line);

					String[] array = StringUtils.split(line, " || ");
					for (String s : array) {
						System.out.println(s);
					}
					result.put(array[array.length - 1], Integer.valueOf(array[1]));
				}
			}
		} catch (Exception e) {
			logger.error("something goes wrong when build datax process list.");
		}
		return result;
	}

	/**
	 * 获取进程id
	 * 
	 * @param process
	 * @return
	 */
	public static int getPid(Process process) {
		if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
			try {
				Field f = process.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				return f.getInt(process);
			} catch (Throwable e) {
				throw new RuntimeException("something goew wrong when get pid", e);
			}
		} else {
			// 其它系统暂不支持
			throw new RuntimeException("unsupported operation system,can't get the pid");
		}
	}

	/**
	 * 杀掉进程
	 * 
	 * @param pid
	 *            进程id
	 * @return
	 */
	public static boolean killProcess(int pid) {
		try {
			Process process = Runtime.getRuntime().exec("kill -9 " + pid);
			if (0 == process.waitFor()) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
}
