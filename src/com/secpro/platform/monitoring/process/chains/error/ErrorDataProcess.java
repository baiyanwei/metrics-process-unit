package com.secpro.platform.monitoring.process.chains.error;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.chains.ref.task.TaskCompleted;
import com.secpro.platform.monitoring.process.dao.ITaskDao;
import com.secpro.platform.monitoring.process.dao.impl.TaskDao;
import com.secpro.platform.monitoring.process.utils.DateFormatUtil;

/**
 * 处理error数据类型
 * 
 * @author sxf
 * 
 */
public class ErrorDataProcess extends Thread {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(ErrorDataProcess.class);
	private Object rawData;

	public ErrorDataProcess(Object rawData) {
		this.rawData = rawData;

	}

	public void run() {
		dataProcess();
	}

	public void dataProcess() {
		theLogger.debug("start processing error data!");
		if (rawData == null) {
			theLogger.error("the data is empty");
			return;
		}

		if (rawData.getClass().equals(JSONObject.class) == false) {
			theLogger
					.error("need type of jsonObject in error data processing.");
			return;
		}
		JSONObject rawDataJson = (JSONObject) rawData;
		Map<String, Object> errorData = MetaDataParsing
				.getErrorRelatedData(rawDataJson);
		if (errorData == null || errorData.size() == 0) {
			theLogger.error("analysis of the data is empty.");

			return;
		}

		// 更新任务相关信息，并更新任务状态为失败
		setTaskStatus(errorData, TaskCompleted.TASK_ERROR);

	}

	// /**
	// * 将执行的缓存任务更新到数据库中
	// * @param taskCode
	// * @param executeDate
	// */
	// private void setCacheTaskStatus(String taskCode, String executeDate) {
	// ITaskDao taskDao = new TaskDao();
	// //taskDao.cacheTaskSave(taskCode, executeDate,TaskDao.TASK_FAILED);
	//
	// }
	/**
	 * 更新数据库中该任务的状态等相关信息
	 * 
	 * @param taskCode
	 * @param executeDate
	 */
	private void setTaskStatus(Map<String, Object> data, int executeResult) {
		Thread setTaskStatus = new TaskCompleted(data, executeResult);
		setTaskStatus.start();
	}
}
