package com.ucar.datalink.flinker.core.admin;

import com.ucar.datalink.flinker.core.util.container.CoreConstant;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
/**
 * 本地job文件管理器
 * 
 * @author lubiao
 * 
 */
public class JobFileManager {

	public Set<String> getAllJobNames() {
		File[] files = getAllJobFiles();

		if (files != null && files.length > 0) {
			HashSet<String> result = new HashSet<String>();
			for (File f : files) {
				result.add(f.getName());
			}
			return result;
		}
		return new HashSet<String>();
	}

	public void addJob(String jobName, String jobContent) {
		// 判断待添加的instance是否已经存在
		File[] files = getAllJobFiles();
		for (File file : files) {
			if (file.getName().equals(jobName)) {
				throw new RuntimeException(MessageFormat.format("the file with name {0} already exists", jobName));
			}
		}
		// 创建目录和文件
		File file = new File(CoreConstant.DATAX_JOB_HOME + File.separatorChar + jobName);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		save(file, jobContent);
	}

	public void deleteJob(String jobName) {
		File jobFile = new File(CoreConstant.DATAX_JOB_HOME + File.separatorChar + jobName);
		if (!jobFile.exists()) {
			throw new RuntimeException(MessageFormat.format("the file with name {0} does not exist.", jobName));
		}

		// 执行备份操作
		String bakFilePath = CoreConstant.DATAX_JOB_HOME + "_bak" + File.separatorChar + jobName + "_bak_" + System.currentTimeMillis();
		File bakFile = new File(bakFilePath);
		try {
			FileUtils.copyFile(jobFile, bakFile);
		} catch (IOException e) {
			throw new RuntimeException(MessageFormat.format("job [{0}] backup failed.", jobName), e);
		}

		// 执行删除操作
		jobFile.delete();
	}

	public void updataInstance(String jobName, String jobContent) {
		File file = new File(CoreConstant.DATAX_JOB_HOME + File.separatorChar + jobName);
		if (!file.exists()) {
			throw new RuntimeException(MessageFormat.format("the file with name {0} does not exist.", jobName));
		}
		save(file, jobContent);
	}

	private File[] getAllJobFiles() {
		File rootdir = new File(CoreConstant.DATAX_JOB_HOME);
		File[] instanceDirs = rootdir.listFiles(new FileFilter() {

			public boolean accept(File pathname) {
				return pathname.isFile();
			}
		});
		return instanceDirs;
	}

	private void save(File file, String fileContent) {
		BufferedWriter bw = null;
		try {
			file.createNewFile();
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(fileContent);
			bw.flush();
		} catch (IOException e) {
			throw new RuntimeException("instance add failed.", e);
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
