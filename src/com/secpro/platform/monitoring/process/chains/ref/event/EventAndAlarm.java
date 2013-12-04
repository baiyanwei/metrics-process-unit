package com.secpro.platform.monitoring.process.chains.ref.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
import com.secpro.platform.monitoring.process.dao.IEventDao;
import com.secpro.platform.monitoring.process.dao.IEventMsgDao;
import com.secpro.platform.monitoring.process.dao.INotifyDao;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.impl.EventDao;
import com.secpro.platform.monitoring.process.dao.impl.NotifyDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;
import com.secpro.platform.monitoring.process.entity.AlarmBean;
import com.secpro.platform.monitoring.process.entity.EventBean;
import com.secpro.platform.monitoring.process.entity.EventRuleBean;
import com.secpro.platform.monitoring.process.entity.UserInfoBean;
import com.secpro.platform.monitoring.process.utils.DateFormatUtil;
/**
 * 事件以及告警产生以及恢复等相关处理
 * @author sxf
 *
 */
public class EventAndAlarm {
	private static PlatformLogger theLogger = PlatformLogger.getLogger(EventAndAlarm.class);
	//产生告警
	private final static String GENERATE_ALARM="1";
	//资源没有停用
	private final static String NOT_PAUSED="0";
	//信息未发送
	//private final static String MESSAGE_NOT_SENT="0";
	//重复告警
	private final static String REPEAT_ALARM="1";
	//事件可自动恢复
	private final static String EVENT_RECOVER="1";
	//自动恢复事件是否发送告警短信
	private final static String RECOVER_SET_MSG="1";
	//自动恢复事件，填写确认人或者清除人信息
	private final static String SYSTEM_AUTO="auto";
	//事件自动清除后，如果发送告警短信，短信告警头
	private final static String RECOVER_MSG_HEAD="[恢复]";
	//账户是否可用，1表示账户不可用
	private final static String ENABLE_ACCOUNT="0";
	private final static String VALUE_NAME="value";
	//各事件级别，事件消息头
	private static Map<Integer,String> levelHead=new HashMap<Integer,String>();
	//表名与实现类对应关系
	private static Map<String,String > tableMapping=new HashMap<String,String>();
	//初始化事件消息头，表名与实现类的对应关系
	static{
		if (levelHead!=null){
			levelHead.put(1, "通知");
			levelHead.put(2, "轻微");
			levelHead.put(3, "严重");
			levelHead.put(4, "紧急");
		}
		//在生成事件信息时，支持加入以下表中的任一字段
		if(tableMapping!=null){
			tableMapping.put("event_type", "com.secpro.platform.monitoring.process.dao.impl.EventTypeDao");
			tableMapping.put("sys_event_rule", "com.secpro.platform.monitoring.process.dao.impl.EventDao");
			tableMapping.put("sys_res_obj", "com.secpro.platform.monitoring.process.dao.impl.ResDao");
			tableMapping.put("sys_dev_type", "com.secpro.platform.monitoring.process.dao.impl.DeviceDao");
			tableMapping.put("sys_dev_company", "com.secpro.platform.monitoring.process.dao.impl.CompanyDao");
			tableMapping.put("sys_city", "com.secpro.platform.monitoring.process.dao.impl.CityDao");
		}
	}
	/**
	 * 事件及告警入口方法，根据eventTypeName,判断是否产生此类型事件，以及是否有可恢复事件，进行事件的产生与恢复，并根据告警规则产生短信告警以及恢复事件短信告警
	 * @param resID 资源ID
	 * @param eventTypeName 事件类型
	 * @param value 此与阀值比较，判断是否会产生告警
	 */
	public static void JudgeGenerateAndRecoveryEvent(long resID, String eventTypeName,
			String value) {
		if(resID==0||Assert.isEmptyString(eventTypeName)||Assert.isEmptyString(value)){
			return;
		}
		List<EventRuleBean> eventRules=getEventRule(resID,eventTypeName);
		if(eventRules==null||eventRules.size()==0){
			theLogger.debug("without this type of event, does not generate any events");
			return;
		}
		//得到此资源可用状态。
		String resPaused=getResPaused(resID);
		//resPaused为0表示资源为启用状态1表示停用状态
		if(NOT_PAUSED.equals(resPaused)){
			theLogger.debug("the resource is not paused");
			//资源启用状态，应根据事件规则，产生事件，并根据事件规则，是否产生短信告警
			boolean isGenerateEvent=isGenerateEvent(resID,value,eventRules,true);
			//没有产生事件
			if(isGenerateEvent==false){
				theLogger.debug("the resource is not generate event");
				//在没有产生事件后，判断是否有可恢复的事件
				isRecoveryEvent(resID,eventRules,true);
				
			}
			//资源停用状态，不应产生事件，但是如若有已产生的事件，应判断是否此事件可被恢复
		}else{
			theLogger.debug("the resource is paused");
			//判断根据规则是否会产生事件，但是不生成新事件
			boolean isGenerate=isGenerateEvent(resID,value,eventRules,false);
			//不会产生事件，则根据规则，判断是否有可被恢复事件
			if(isGenerate==false){
				isRecoveryEvent(resID,eventRules,false);
			}
		}
		
	}
	/**
	 * 根据RES ID和KPI ID获得相应规则组
	 * @param resID
	 * @param kpiID
	 * @return
	 */
	private static List<EventRuleBean> getEventRule(long resID,String eventTypeName) {
		
		IEventDao eventDao=new EventDao();
		return eventDao.eventRuleQuery(resID, eventTypeName);
	}
	/**
	 * 根据事件规则组eventRules，判断value值是否能产生此类事件
	 * 当generateEvent==true时，表示当判断可以产生事件时，生成此事件。当generateEvent==false时，判断是否会产生事件，返回判断结果，不会生成事件
	 * @param resID
	 * @param value
	 * @param eventRules 事件规则集
	 * @return
	 */
	private static boolean isGenerateEvent(long resID,String value,
			List<EventRuleBean> eventRules,boolean generateEvent) {
		if(resID==0||Assert.isEmptyString(value)||eventRules==null||eventRules.size()==0){
			return false;
		}
		for(EventRuleBean eventRuleBean:eventRules){
			String thresholdOpr=eventRuleBean.getThresholdOpr();
			if(Assert.isEmptyString(thresholdOpr)){
				theLogger.debug("threshold Opration is empty");
				continue;
			}
			String threshold=eventRuleBean.getThresholdValue();
			if(Assert.isEmptyString(threshold)){
				theLogger.debug("threshold is empty");
				continue;
			}
			boolean isGenerate=false;
			//解析类似watchdog数据中disk是一组数据的情况，将组成员依次与阀值进行比对，并将产生事件的成员组合到一起
			if(MetaDataParsing.isJsonObj(value)){
				String valuesGroup="";
				int generateFlag=0;
				try {
					JSONObject valuesJson=new JSONObject(value);
					String[] valueKey = JSONObject.getNames(valuesJson);
					for (int i = 0; i < valueKey.length; i++) {
						String oneValue=valuesJson.getString(valueKey[i]);
						isGenerate=isGenerateEvent(oneValue,thresholdOpr,threshold);
						if(isGenerate){
							valuesGroup=valuesGroup+valueKey[i]+":value is"+oneValue;
							generateFlag=1;
						}
					}
					if(generateFlag==1){
						isGenerate=true;
						value=valuesGroup;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					theLogger.exception(e);
				}
			}else{
				//根据阀值以及阀值操作类型，判断此value会不会产生此类型事件
				isGenerate=isGenerateEvent(value,thresholdOpr,threshold);
			}
			//产生此类型事件
			if(isGenerate){
				//如若不需要生成此类型事件，只进行判断，则直接返回true（会产生此类型事件）
				if(generateEvent==false){
					return true;
				}
				theLogger.debug("generate event");
				//查看事件记录，查询是否有同事件类型的事件已产生
				EventBean eventBean=findSameEventType(resID,eventRuleBean.getEventTypeID());
				//没有产生过此类型事件，则按照规则生成此类型的事件
				if(eventBean==null){
					theLogger.debug("generate new event");
					generateEvent(resID,eventRuleBean,value);
				}
				else{
					int eventLevel=eventRuleBean.getEnentLevel();
					//已产生事件的级别
					int occurredEventLevel=eventBean.getEventLevel();
					//如果已产生的事件，跟这个事件类型级别相同，则只更新事件信息
					if(eventLevel==occurredEventLevel){
						String message=getEventMessage(resID,eventRuleBean,value);
						theLogger.debug("this event level equal occurred event level");
						String cdate=DateFormatUtil.getNowDate();
						changeEvent(eventBean.getId(),message,cdate,eventLevel);
						eventBean.setMessage(message);
						eventBean.setCdate(cdate);
						if(REPEAT_ALARM.equals(eventRuleBean.getRepeat())&&GENERATE_ALARM.equals(eventRuleBean.getSetMsg())){
							theLogger.debug("Repeat the alarm");
							generateAlarm(eventBean,eventRuleBean.getId());
						}
					}
					else {
						//如果事件级别与已产生事件级别不同，则更新已产生事件的相应信息
						theLogger.debug("this event level higher or lower than occurred event level");
						String message=getEventMessage(resID,eventRuleBean,value);
						String cdate=DateFormatUtil.getNowDate();
						changeEvent(eventBean.getId(),message,cdate,eventLevel);
						eventBean.setMessage(message);
						eventBean.setCdate(cdate);
						eventBean.setEventLevel(eventLevel);
						if(GENERATE_ALARM.equals(eventRuleBean.getSetMsg())){
							generateAlarm(eventBean,eventRuleBean.getId());
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * 更新事件的message信息
	 * @param eventID
	 * @param message
	 */
	private static void changeEvent(long eventID, String message,String cdate,int eventLevel) {
		IEventDao eventDao=new EventDao();
		eventDao.eventMessageUpdate(eventID, message);
		
	}
	/**
	 * 生成此类规则的事件，并根据事件告警规则，生成相应短信告警
	 * @param resID
	 * @param eventRuleBean
	 */
	private static void generateEvent(long resID, EventRuleBean eventRuleBean,String value) {
		if(resID==0||eventRuleBean==null){
			return;
		}
		EventBean eventBean=new EventBean();
		String message=getEventMessage(resID,eventRuleBean,value);
		eventBean.setEventLevel(eventRuleBean.getEnentLevel());
		eventBean.setMessage(message);
		eventBean.setCdate(DateFormatUtil.getNowDate());
		eventBean.setResID(resID);
		eventBean.setEventTypeID(eventRuleBean.getEventTypeID());
		IEventDao eventDao=new EventDao();
		eventDao.eventSave(eventBean);
		//是否产生短信告警
		if(GENERATE_ALARM.equals(eventRuleBean.getSetMsg())){
			generateAlarm(eventBean,eventRuleBean.getId());
		}
		
	}
	/**
	 * 根据事件产生短信告警
	 * @param eventBean
	 * @param eventRuleID
	 */
	private static void generateAlarm(EventBean eventBean, long eventRuleID) {
		theLogger.debug("generate alarm start");
		if(eventRuleID==0||eventBean==null){
			return;
		}
		long resID=eventBean.getResID();
		if(resID==0){
			return;
		}
		List<UserInfoBean> users=getAlarmUsers(resID,eventRuleID);
		if(users==null||users.size()==0){
			theLogger.debug("No message recipient, does not generate SMS alarm");
			return;
		}
		List<AlarmBean> alarms=new ArrayList<AlarmBean>();
		String cdate=DateFormatUtil.getNowDate();
		for(UserInfoBean user:users){
			//如果此用户账户为停用状态，则不会为该用户产生短信告警
			if(!ENABLE_ACCOUNT.equals(user.getEnableAccount())){
				continue;
			}
			AlarmBean alarmBean=new AlarmBean();
			alarmBean.setCdate(cdate);
			alarmBean.setMessgae(eventBean.getMessage());
			alarmBean.setMobelTel(user.getMobelTel());
			alarmBean.setUserName(user.getUserName());
			//alarmBean.setSendMsgStatus(MESSAGE_NOT_SENT);
			alarms.add(alarmBean);
		}
		generateAlarm(alarms);
		theLogger.debug("generate alarm end");
	}
	/**
	 * 产生短信告警信息
	 * @param alarms
	 */
	private static void generateAlarm(List<AlarmBean> alarms) {
		INotifyDao notifyDao=new NotifyDao();
		notifyDao.notifySave(alarms);
		
	}
	/**
	 * 获得此类告警短信接收人信息
	 * @param resID
	 * @param eventRuleID
	 * @return
	 */
	private static List<UserInfoBean> getAlarmUsers(long resID, long eventRuleID) {
		INotifyDao notifyDao=new NotifyDao();
		return notifyDao.notifyUserQuery(resID, eventRuleID);
	}
	/**
	 * 根据事件信息标准化规则，获得该类事件的事件信息
	 * @param resID
	 * @param eventRuleBean
	 * @return
	 */
	private static String getEventMessage(long resID, EventRuleBean eventRuleBean,String value) {
		if(resID==0||eventRuleBean==null){
			return null;
		}
		String msgFormatRegex="\\{(.*?):(.*?)\\}";;
		StringBuilder message=new StringBuilder();
		String levelHeadMeg=levelHead.get(eventRuleBean.getEnentLevel());
		message.append("[").append(levelHeadMeg).append("]");
		String msgFormat=getEventMessage(eventRuleBean.getEventTypeID());
		if(Assert.isEmptyString(msgFormat)){
			return message.toString();
		}
		
		Pattern patt=Pattern.compile(msgFormatRegex);
		Matcher mat = patt.matcher(msgFormat);
		int index=0;
		while(mat.find()){ 
			message.append(msgFormat.substring(index, mat.start()));
			index=mat.end();
			String tableName=mat.group(1);
			String columnName=mat.group(2);
			if(Assert.isEmptyString(tableName)||Assert.isEmptyString(columnName)){
				message.append("{"+tableName+":"+columnName+"}");
				continue;
			}
			if(VALUE_NAME.equals(tableName)){
				message.append(value);
				continue;
			}
			String impl=tableMapping.get(tableName);
			if(Assert.isEmptyString(impl)){
				message.append("{"+tableName+":"+columnName+"}");
				continue;
			}
			try {
				Class<?> clazz= Class.forName(impl);
				IEventMsgDao eventMsgDao=(IEventMsgDao)clazz.newInstance();
				String replace=eventMsgDao.columnQuery(resID, eventRuleBean,columnName);
				if(Assert.isEmptyString(replace)){
					message.append("{"+tableName+":"+columnName+"}");
					continue;
				}
				message.append(replace);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
			}
			
			}
		message.append(msgFormat.substring(index,msgFormat.length()));
			
		
		return message.toString();
	}
	/**
	 * 根据事件类型ID，获得该事件的格式化信息
	 * @param eventTypeID
	 * @return
	 */
	private static String getEventMessage(long eventTypeID) {
		IEventDao eventDao=new EventDao();
		return eventDao.eventMessageQuery(eventTypeID);
	}
	/**
	 * 事件记录中是否有同类型的事件
	 * @param resID
	 * @param eventTypeID
	 * @return
	 */
	private static EventBean findSameEventType(long resID, long eventTypeID) {
		IEventDao eventDao=new EventDao();
		return eventDao.eventQuery(resID, eventTypeID);
	}
	/**
	 * 根据阀值类型以及阀值，判断value值是否能产生事件。仅判断，不生成事件
	 * @param value
	 * @param thresholdOpr
	 * @param threshold
	 * @return 是否会产生此类型的事件
	 */
	private static boolean isGenerateEvent(String value, String thresholdOpr,
			String threshold) {
		if(Assert.isEmptyString(value)||Assert.isEmptyString(thresholdOpr)||Assert.isEmptyString(threshold)){
			return false;
		}
		String floatRegex="[0-9]+.?[0-9]+";
		boolean valueIsFloat=value.matches(floatRegex);
		boolean thresholdIsFloat=threshold.matches(floatRegex);
		//阀值操作类型为>,>=,<,<=,=时，将进行数学运算，并把阀值以及value转换成float类型进行比对。当阀值操作类型为==，！=时，将进行字符串比对运算
		if(">".equals(thresholdOpr)){
			if(valueIsFloat==false||thresholdIsFloat==false){
				theLogger.debug("Threshold type conflict!");
				return false;
			}
			Float valueF=Float.parseFloat(value);
			Float thresholdF=Float.parseFloat(threshold);
			return valueF>thresholdF?true:false;
		}else if("<".equals(thresholdOpr)){
			if(valueIsFloat==false||thresholdIsFloat==false){
				theLogger.debug("Threshold type conflict!");
				return false;
			}
			Float valueF=Float.parseFloat(value);
			Float thresholdF=Float.parseFloat(threshold);
			return valueF<thresholdF?true:false;
		}else if("=".equals(thresholdOpr)){
			if(valueIsFloat==false||thresholdIsFloat==false){
				theLogger.debug("Threshold type conflict!");
				return false;
			}
			Float valueF=Float.parseFloat(value);
			Float thresholdF=Float.parseFloat(threshold);
			return valueF==thresholdF?true:false;
		}else if(">=".equals(thresholdOpr)){
			if(valueIsFloat==false||thresholdIsFloat==false){
				theLogger.debug("Threshold type conflict!");
				return false;
			}
			Float valueF=Float.parseFloat(value);
			Float thresholdF=Float.parseFloat(threshold);
			return valueF>=thresholdF?true:false;
		}else if("<=".equals(thresholdOpr)){
			if(valueIsFloat==false||thresholdIsFloat==false){
				theLogger.debug("Threshold type conflict!");
				return false;
			}
			Float valueF=Float.parseFloat(value);
			Float thresholdF=Float.parseFloat(threshold);
			return valueF<=thresholdF?true:false;
		}else if("==".equals(thresholdOpr)){
			return value.equals(threshold)?true:false;
		}else if("!=".equals(thresholdOpr)){
			if("null".equals(threshold.toLowerCase())){
				if(!Assert.isEmptyString(value)){
					return true;
				}
			}
			return (!value.equals(threshold))?true:false;
		}else{
			theLogger.debug("Doesn't recognize the threshold match type");
		}
		return false;
	}
	/**
	 * 根据事件规则，判断是否有可自动恢复事件，并根据条件产生短信恢复告警
	 * 
	 * @param resID
	 * @param eventRules
	 * @param sendMsg 为false时，不会产生短信恢复告警，当资源为停用状态时，应只恢复事件，不产生恢复告警短信
	 * @return
	 */
	private static boolean isRecoveryEvent(long resID,List<EventRuleBean> eventRules,boolean sendMsg) {
		if(resID==0||eventRules==null||eventRules.size()==0){
			return false;
		}
		EventRuleBean eventRuleBean=eventRules.get(0);
		if(!EVENT_RECOVER.equals(eventRuleBean.getEventRecover())) {
			theLogger.debug("the event isn't auto recoverey");
			return false;
		}
		EventBean eventBean=findSameEventType(resID,eventRuleBean.getEventTypeID());
		if(eventBean==null){
			theLogger.debug("haven't need recovery event");
			return false;
		}
		if(sendMsg==false)
		{
			theLogger.debug("the event is recovery");
			recoveryEvent(eventBean,false,0);
			return true;
		}
		boolean recoverSendMsg=false;
		long eventRuleID=0;
		int eventLevel=eventBean.getEventLevel();
		for(EventRuleBean eventRuleB:eventRules){
			if(eventRuleB.getEnentLevel()==eventLevel){
				if(RECOVER_SET_MSG.equals(eventRuleB.getRecoverSetMsg()));
				recoverSendMsg=true;
				eventRuleID=eventRuleB.getId();
				break;
			}
		}
		theLogger.debug("the event is recovery");
		recoveryEvent(eventBean,recoverSendMsg,eventRuleID);
		return true;
	}
	/**
	 * 自动恢复已经生成的事件
	 * @param eventBean
	 * @param recoverSendMsg 事件自动恢复是否发送恢复短信，false不发送，true为发送
	 * @param eventRuleID
	 */
	private static void recoveryEvent(EventBean eventBean,
			boolean recoverSendMsg,long eventRuleID) {
		if(eventBean==null){
			return;
		}
		if(Assert.isEmptyString(eventBean.getConfirmUser())||Assert.isEmptyString(eventBean.getConfirmDate())){
			eventBean.setConfirmUser(SYSTEM_AUTO);
			eventBean.setConfirmDate(DateFormatUtil.getNowDate());
		}
		eventBean.setClearUser(SYSTEM_AUTO);
		eventBean.setClearDate(DateFormatUtil.getNowDate());
		IEventDao eventDao=new EventDao();
		eventDao.eventHisDeleteAndSave(eventBean);
		if(recoverSendMsg==false||eventRuleID==0){
			theLogger.debug("Do not send recovery SMS");
			return;
		}
		//重新构造短信头
		eventBean.setMessage(RECOVER_MSG_HEAD+eventBean.getMessage());
		theLogger.debug("send recovery SMS");
		//发送恢复短信
		generateAlarm(eventBean, eventRuleID);
		
	}
	/**
	 * 得到此资源的启用状态
	 * @param resID
	 * @return
	 */
	private static String getResPaused(long resID) {
		IResourceDao resDao=new ResDao();
		return resDao.resPausedQuery(resID);
	}
	public static void isRecoveryEvent(long resID,String eventTypeName){
		if(resID==0||Assert.isEmptyString(eventTypeName)){
			return;
		}
		List<EventRuleBean> eventRules=getEventRule(resID,eventTypeName);
		if(eventRules==null||eventRules.size()==0){
			theLogger.debug("without this type of event, does not recovery any events");
			return;
		}
		//得到此资源可用状态。
		String resPaused=getResPaused(resID);
		//resPaused为0表示资源为启用状态1表示停用状态
		if(NOT_PAUSED.equals(resPaused)){
			theLogger.debug("the resource is not paused");
				//在没有产生事件后，判断是否有可恢复的事件
			isRecoveryEvent(resID,eventRules,true);
				
			
			//资源停用状态，不应产生事件，但是如若有已产生的事件，应判断是否此事件可被恢复
		}else{
			theLogger.debug("the resource is paused");
			isRecoveryEvent(resID,eventRules,false);
			
		}
	}
	
}
