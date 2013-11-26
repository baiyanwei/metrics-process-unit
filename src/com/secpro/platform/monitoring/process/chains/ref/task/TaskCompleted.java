package com.secpro.platform.monitoring.process.chains.ref.task;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.dao.ITaskDao;
import com.secpro.platform.monitoring.process.dao.impl.TaskDao;
import com.secpro.platform.monitoring.process.utils.DateFormat;
/**
 * 任务相关处理
 * @author sxf
 *
 */
public class TaskCompleted extends Thread{
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(TaskCompleted.class);
	private String taskCode;
	private String executeDate;
	private int executeStatus;
	private String executeDes;
	public TaskCompleted(String taskCode,String executeDate){
		this.taskCode=taskCode;
		this.executeDate=executeDate;
	}
	public void run(){
		if(Assert.isEmptyString(taskCode)){
			return;
		}
		
		if(Assert.isEmptyString(executeDate)){
		    this.executeDate=DateFormat.getNowDate();
		}
		if (MetaDataParsing.isCacheTask(taskCode)) {
			setCacheTaskStatus(MetaDataParsing.changeCacheTaskCode(taskCode),executeDate);
		} else {
			setTaskStatus(taskCode, executeDate);
		}
		theLogger.debug("set task status successful!");
	}
	private void setCacheTaskStatus(String taskCode, String executeDate) {
		ITaskDao taskDao = new TaskDao();
		//taskDao.cacheTaskStatusUpdate(taskCode, executeDate,TaskDao.TASK_SUCCESS);
		
	}
	private void setTaskStatus(String taskCode, String executeDate) {
		ITaskDao taskDao = new TaskDao();
		//taskDao.taskStatusUpdate(taskCode, executeDate,TaskDao.TASK_SUCCESS);
	}
}
