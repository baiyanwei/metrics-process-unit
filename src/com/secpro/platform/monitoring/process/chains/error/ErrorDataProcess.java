package com.secpro.platform.monitoring.process.chains.error;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.dao.ITaskDao;
import com.secpro.platform.monitoring.process.dao.impl.TaskDao;
import com.secpro.platform.monitoring.process.utils.DateFormat;
/**
 * 处理error数据类型
 * @author sxf
 *
 */
public class ErrorDataProcess extends Thread{
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(ErrorDataProcess.class);
	private Object rawData;
	public ErrorDataProcess(Object rawData){
		this.rawData=rawData;
		
	}
	public void run(){
		dataProcess();
	}
	public void dataProcess() {
		if (rawData == null) {
			theLogger.error("the data is empty");
			return;
		}
		theLogger.debug("start processing error data!");
		try {
			JSONTokener parser = new JSONTokener(rawData.toString());
			JSONObject dataJsonObj = new JSONObject(parser);
			if (!dataJsonObj.has(MetaDataConstant.META_MONITOR_ID_PROPERTY_NAME)) {
				theLogger.error("the task code of snmp data is empty");
				return;
			}
			String taskCode = dataJsonObj
					.getString(MetaDataConstant.META_MONITOR_ID_PROPERTY_NAME);
			if (Assert.isEmptyString(taskCode)) {
				theLogger.error("the task code of snmp data is empty");
				return;
			}
			String executeDate="";
			if(dataJsonObj.has(MetaDataConstant.META_TIMESTAMP_PROPERTY_NAME)){
				Long timestamp = dataJsonObj
						.getLong(MetaDataConstant.META_TIMESTAMP_PROPERTY_NAME);
				executeDate = DateFormat.timestampFormat(timestamp);
			}
			else{
				executeDate=DateFormat.getNowDate();
			}
			//是否为缓存任务
			if (MetaDataParsing.isCacheTask(taskCode)) {
				setCacheTaskStatus(MetaDataParsing.changeCacheTaskCode(taskCode),executeDate);
			} else {
				setTaskStatus(taskCode, executeDate);
			}
			theLogger.debug("process error data successful!");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			theLogger.exception(e);
			//e.printStackTrace();
		}
	}
	/**
	 * 将执行的缓存任务更新到数据库中
	 * @param taskCode
	 * @param executeDate
	 */
	private void setCacheTaskStatus(String taskCode, String executeDate) {
		ITaskDao taskDao = new TaskDao();
		//taskDao.cacheTaskSave(taskCode, executeDate,TaskDao.TASK_FAILED);
		
	}
	/**
	 * 更新数据库中该任务的状态等相关信息
	 * @param taskCode
	 * @param executeDate
	 */
	private void setTaskStatus(String taskCode, String executeDate) {
		ITaskDao taskDao = new TaskDao();
		//taskDao.taskStatusUpdate(taskCode, executeDate,TaskDao.TASK_FAILED);

	}
}
