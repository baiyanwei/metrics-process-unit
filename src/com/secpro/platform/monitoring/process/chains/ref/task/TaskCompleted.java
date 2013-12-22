package com.secpro.platform.monitoring.process.chains.ref.task;

import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.dao.ITaskDao;
import com.secpro.platform.monitoring.process.dao.impl.TaskDao;
import com.secpro.platform.monitoring.process.entity.TaskBean;

/**
 * 任务相关处理
 * 
 * @author sxf
 * 
 */
public class TaskCompleted extends Thread {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(TaskCompleted.class);
	public static final int TASK_SUCCESS = 1;
	public static final int TASK_ERROR = 0;
	private static final String TASK_SUCCESS_DES = "success";
	private Map<String, Object> _data;
	private int _executeResult;

	public TaskCompleted(Map<String, Object> data, int executeResult) {
		this._data = data;
		this._executeResult = executeResult;
	}

	public void run() {
		theLogger.debug("start update task status");
		if (_data == null || _data.size() == 0) {
			return;
		}
		String taskCode = (String) _data.get(MetaDataConstant.SCHEDULE_ID);
		if (Assert.isEmptyString(taskCode)) {
			return;
		}
		if (MetaDataParsing.isCacheTask(taskCode)) {
			// setCacheTaskStatus(MetaDataParsing.changeCacheTaskCode(taskCode),executeDate);
			theLogger.debug("cache task don't need update task status");
			return;
		} else {
			TaskBean taskBean = new TaskBean();
			taskBean.setScheduleID(taskCode);
			taskBean.setExecuteAt(Long.parseLong((String) _data
					.get(MetaDataConstant.EXECUTE_DATE)));
			taskBean.setExecuteCost(Long.parseLong((String) _data
					.get(MetaDataConstant.EXECUTE_COST)));
			taskBean.setExecuteStatus(_executeResult);
			if (TASK_SUCCESS == _executeResult) {
				taskBean.setExecuteDes(TASK_SUCCESS_DES);
			} else {
				taskBean.setExecuteDes((String) _data
						.get(MetaDataConstant.ERROR_DESCRIPTION));
			}
			setTaskStatus(taskBean);
		}
		theLogger.debug("set task status end");
	}

	// private void setCacheTaskStatus(String taskCode, String executeDate) {
	// ITaskDao taskDao = new TaskDao();
	// //taskDao.cacheTaskStatusUpdate(taskCode,
	// executeDate,TaskDao.TASK_SUCCESS);
	//
	// }
	private void setTaskStatus(TaskBean taskBean) {
		ITaskDao taskDao = new TaskDao();
		taskDao.taskStatusUpdate(taskBean);
	}
}
