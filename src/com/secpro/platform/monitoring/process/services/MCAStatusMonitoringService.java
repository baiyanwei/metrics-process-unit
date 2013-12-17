package com.secpro.platform.monitoring.process.services;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.event.EventAndAlarm;
import com.secpro.platform.monitoring.process.chains.ref.event.EventTypeNameConstant;
import com.secpro.platform.monitoring.process.dao.IKpiDao;
import com.secpro.platform.monitoring.process.dao.impl.KpiDao;
import com.secpro.platform.monitoring.process.utils.DateFormatUtil;
/**
 * 根据watchdog上发情况监控采集机状态是否正常
 * 若超过一定时间，未收到watchdog上发数据，则该采集机处于异常状态，应根据事件规则产生相应事件以及短信告警
 * @author sxf
 *
 */
@ServiceInfo(description = "process unit MCA status monitoring service", configurationPath = "/app/mpu/services/MCAStatusMonitoringService/")
public class MCAStatusMonitoringService implements IService{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(MCAStatusMonitoringService.class);
	@XmlElement(name = "intervalTime",type=Long.class, defaultValue ="0")
	public Long intervalTime=60*1000L;
	@XmlElement(name = "overTime",type=Long.class, defaultValue ="0")
	public Long overTime=20*60*1000L;
	private Thread monitorThread;
	private Map<Long,String> mcaLastReceiveDate;
	//mca异常产生事件名称
	//private final String MCA_STATUS_ERROR_EVENT_NAME="mca error";
	@Override
	public void start() throws Exception {
		startUpMCAStatusMonitoring();
		theLogger.info("startUp");
	}
	/**
	 * 启动对MCA状态监控
	 */
	private void startUpMCAStatusMonitoring() {
		if(intervalTime==0L||overTime==0L){
			return;
		}
		monitorThread=new Thread() {
			public void run() {
				try {
					while(true){
						sleep(intervalTime);
						mcaStatusMonitoring();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					theLogger.exception(e);
				}
			}

			
		};
		//启动线程
		monitorThread.start();
		theLogger.info("mcaStatusMonitorStart",intervalTime,overTime);
		
	}
	private void mcaStatusMonitoring() {
		theLogger.debug("execute mca status monitoring");
		Map<Long,String> mcaResReceiveDate=getMCALastReceiveDate();
		if(mcaResReceiveDate==null||mcaResReceiveDate.size()==0){
			theLogger.error("all of MCA last time to receive data are empty!");
			return;
		}
		long nowTimeMillis=System.currentTimeMillis();
		for(long resID:mcaResReceiveDate.keySet()){
			String receiveDate=mcaResReceiveDate.get(resID);
			if(Assert.isEmptyString(receiveDate)){
				if(mcaLastReceiveDate==null||mcaLastReceiveDate.size()==0||Assert.isEmptyString(receiveDate=mcaLastReceiveDate.get(resID))){
					mcaResReceiveDate.put(resID, DateFormatUtil.getNowDate());
				}else{
					judgeMCAStatus(nowTimeMillis, DateFormatUtil.getDateTimeMillis(receiveDate),resID);
					mcaResReceiveDate.put(resID, receiveDate);
				}
			}else{
				judgeMCAStatus(nowTimeMillis, DateFormatUtil.getDateTimeMillis(receiveDate),resID);
			}
		}
		this.mcaLastReceiveDate=mcaResReceiveDate;
		
	}
	/**
	 * 根据数据库查询到的上一次watchdog上传数据时间，判断此mca端是否处于异常状态，并依据事件规则判断是否产生事件以及告警以及恢复事件，恢复告警
	 * @param nowTime 当前时间毫秒格式
	 * @param lastTime 上次watchdog上传数据日期毫秒格式
	 * @param resID 资源ID
	 */
	private void judgeMCAStatus(long nowTime,long lastTime,long resID){
		if(nowTime-lastTime>overTime){
			EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID, EventTypeNameConstant.EVENT_TYEP_NAME_MCA_ERROR, EventTypeNameConstant.EVENT_TYEP_NAME_MCA_ERROR);
		}else{
			EventAndAlarm.isRecoveryEvent(resID, EventTypeNameConstant.EVENT_TYEP_NAME_MCA_ERROR);
		}
	}
	private Map<Long, String> getMCALastReceiveDate() {
		IKpiDao kpiDao=new KpiDao();
		return kpiDao.kpiLastDateQuery();
	}
	@Override
	public void stop() throws Exception {
		if(monitorThread!=null){
			monitorThread.interrupt();
		}
		theLogger.info("stopped");
		
	}

}
