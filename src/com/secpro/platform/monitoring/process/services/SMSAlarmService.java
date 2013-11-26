package com.secpro.platform.monitoring.process.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

import test.ReadFile;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.standard.ConfigAndPolicyStandard;
import com.secpro.platform.monitoring.process.dao.INotifyDao;
import com.secpro.platform.monitoring.process.dao.impl.NotifyDao;
import com.secpro.platform.monitoring.process.entity.AlarmBean;
import com.secpro.platform.monitoring.process.sms.ISMSAlarm;
import com.secpro.platform.monitoring.process.utils.CollectionUtil;
import com.secpro.platform.monitoring.process.utils.DateFormat;
@ServiceInfo(description = "process unit SMS alarm service", configurationPath = "dpu/services/SMSAlarmService/")
public class SMSAlarmService implements IService{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(SMSAlarmService.class);
	@XmlElement(name = "intervalTime",type=Long.class, defaultValue ="0")
	public Long intervalTime=5*1000L;
	@XmlElement(name = "resendTime",type=Long.class,defaultValue ="0")
	public Long resendTime=60*1000L;
	@XmlElement(name = "resendNum",type=Integer.class,defaultValue ="3")
	public int resendNum=3;
	@XmlElement(name = "SMSAlarmAdapter", defaultValue ="")
	public String SMSAlarmAdapter="";
	private Thread smsAlarmThread;
	private Thread smsAlarmResendThread;
	private Map<AlarmBean,Integer> resendMessage=new HashMap<AlarmBean,Integer>();
	private List<AlarmBean> sendMessageComplete=new ArrayList<AlarmBean>();
	private ISMSAlarm smsAlarmAdapterIns;
	private final String SEND_MSG_SUCCESS="1";
	private final String SEND_MSG_FAILED="0";
	@Override
	public void start() throws Exception {
		startSMSAlarm();
		startResendSMSAlarm();
		testSSH();
		theLogger.info("startUp");
	}

	private void startResendSMSAlarm() {
		if(smsAlarmThread==null||smsAlarmAdapterIns==null){
			//theLogger.debug("the SMS alarm adapter or SMS alarm thread is empty!");
			return;
		}
		if(resendTime>0L&&resendNum>0){
			smsAlarmResendThread=new Thread() {
				public void run() {
					try {
						while(true){
							sleep(resendTime);
							resendMessage();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						theLogger.exception(e);
					}
				}
			};
			//启动线程
			smsAlarmResendThread.start();
			theLogger.info("resendMsgStart",resendTime,resendNum);
		}
	}

	protected void resendMessage() {
		synchronized(resendMessage){
			if(resendMessage.size()>0){
				List<AlarmBean> removeAlarm=new ArrayList<AlarmBean>();
				for(AlarmBean alarmB:resendMessage.keySet()){
					if(alarmB==null){
						continue;
					}
					int alarmResendNum=resendMessage.get(alarmB);
					if(alarmResendNum<resendNum){
						String mobleTel=alarmB.getMobelTel();
						String message=alarmB.getMessgae();
						if(sendMessage(mobleTel,message)){
							alarmB.setSendMsgStatus(SEND_MSG_SUCCESS);
							alarmB.setSendDate(DateFormat.getNowDate());
							sendMessageComplete(alarmB);
							removeAlarm.add(alarmB);
						}else{
							alarmResendNum++;
							resendMessage.put(alarmB, alarmResendNum);
						}
					}
					if(alarmResendNum>=resendNum){
						alarmB.setSendMsgStatus(SEND_MSG_FAILED);
						alarmB.setSendDate(DateFormat.getNowDate());
						sendMessageComplete(alarmB);
						removeAlarm.add(alarmB);
					}
					
				}
				if(removeAlarm.size()>0){
					CollectionUtil.mapRemove(resendMessage, removeAlarm);
				}
			}
		}
	}

	private void startSMSAlarm() throws Exception {
		if(Assert.isEmptyString(SMSAlarmAdapter)){
			return;
		}
		Class<?> clazz=Class.forName(SMSAlarmAdapter);
		smsAlarmAdapterIns=(ISMSAlarm)clazz.newInstance();
		if(intervalTime>0L){
			smsAlarmThread=new Thread() {
				public void run() {
					try {
						while(true){
							sleep(intervalTime);
							sendMessage();
							sendMessageCompleteStorage();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						theLogger.exception(e);
					}
				}
			};
			//启动线程
			smsAlarmThread.start();
			theLogger.info("sendMsgStart",intervalTime);
		}

	}
	private List<AlarmBean> loadNeedSendMessages() {
		INotifyDao notifyDao=new NotifyDao();
		return notifyDao.sendMessQuery();
		
	}
	private void sendMessageCompleteStorage(){
		INotifyDao notifyDao=new NotifyDao();
		synchronized(sendMessageComplete){
			if(sendMessageComplete.size()>0){
				notifyDao.sendMessSave(sendMessageComplete);
				sendMessageComplete.clear();
			}
			
		}
	}
	private void sendMessage(){
		List<AlarmBean> alarmBeans=loadNeedSendMessages();
		if(alarmBeans!=null&&alarmBeans.size()>0){
			for(AlarmBean alarmB:alarmBeans){
				if(alarmB==null){
					continue;
				}
				String mobleTel=alarmB.getMobelTel();
				String message=alarmB.getMessgae();
				if(sendMessage(mobleTel,message)){
					alarmB.setSendMsgStatus(SEND_MSG_SUCCESS);
					alarmB.setSendDate(DateFormat.getNowDate());
					sendMessageComplete(alarmB);
				}else{
					if(smsAlarmResendThread==null){
						alarmB.setSendMsgStatus(SEND_MSG_FAILED);
						alarmB.setSendDate(DateFormat.getNowDate());
						sendMessageComplete(alarmB);
					}else{
						storageToResendMessage(alarmB);
					}
				}
			}
			alarmBeans.clear();
		}
	}
	private void storageToResendMessage(AlarmBean alarmB) {
		if(alarmB==null){
			return;
		}
		synchronized(resendMessage){
			resendMessage.put(alarmB,0);
		}
		
	}

	private void sendMessageComplete(AlarmBean alarmB) {
		if(alarmB==null){
			return;
		}
		synchronized(sendMessageComplete){
			sendMessageComplete.add(alarmB);
		}
		
	}

	private boolean sendMessage(String mobleTel,String message) {
		if(smsAlarmAdapterIns==null){
			theLogger.error("the SMS alarm adapter is empty!");
			return false;
		}
		if(Assert.isEmptyString(mobleTel)||Assert.isEmptyString(message)){
			return false;
		}
		return smsAlarmAdapterIns.sendSMS(mobleTel, message);
		
		
	}


	@Override
	public void stop() throws Exception {
		if(smsAlarmThread!=null){
			smsAlarmThread.interrupt();
		}
		if(smsAlarmResendThread!=null){
			smsAlarmResendThread.interrupt();
		}
		theLogger.info("stopped");
	}
	public void testSSH(){
		String value=ReadFile.readFile();
		String rule=ReadFile.readRule("F:\\rule.txt");
		System.out.println(rule);
		Long start=System.currentTimeMillis();
		ConfigAndPolicyStandard config=new ConfigAndPolicyStandard(value,rule,"311","127.0.0.1");
		String[] result=config.configAndPolicyStandard();
		String[] resultSplit=result[1].split("%%");
		for(int i=0;i<resultSplit.length;i++){
			System.out.println(resultSplit[i]);
		}
		System.out.println(System.currentTimeMillis()-start);
		//System.out.println(StandardUtil.rangeAddressExcept("10.1.1.1", "10.1.1.254", new String[]{"10.1.1.10-10.1.1.200:"+StandardUtil.ipToLong("10.1.1.10")+"-"+StandardUtil.ipToLong("10.1.1.200")}));
		Map<String,Object> rawData=new HashMap<String,Object>();
		rawData.put("resID", 41l);
		rawData.put(MetaDataConstant.EXECUTE_RESULT,result);
		rawData.put("taskCode", "0001");
//		SSHMatchBaseline ssh=new SSHMatchBaseline();
//		ssh.dataProcess(rawData);
		//DBStorage dbStorage=new SSHTelnetDBStorageAdapter(rawData);
		//dbStorage.start();	
//		String containRule=ReadFile.readRule("F:\\contain.txt");
//		
//		
//		PolicyContainAndConflict p=new PolicyContainAndConflict(result[1],containRule);
//		String[] resultcontain=p.policyContainAndConflict();
//		if(resultcontain!=null){
//		System.out.println(resultcontain[0]);
//		System.out.println(resultcontain[1]);
//		}
	}
}
