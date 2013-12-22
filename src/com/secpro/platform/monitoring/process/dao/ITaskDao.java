package com.secpro.platform.monitoring.process.dao;

import com.secpro.platform.monitoring.process.entity.TaskBean;

public interface ITaskDao {
	public void taskStatusUpdate(TaskBean taskBean);
	//public void cacheTaskSave(TaskBean taskBean);
	public long resIDQuery(String taskID);
}
