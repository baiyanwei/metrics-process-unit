package com.secpro.platform.monitoring.process.dao;

import java.util.List;

import com.secpro.platform.monitoring.process.entity.EventBean;
import com.secpro.platform.monitoring.process.entity.EventRuleBean;
/**
 * 事件相关数据库操作
 * @author sxf
 *
 */
public interface IEventDao {
	/**
	 * 事件规则查询
	 * @param resID
	 * @param eventTypeName
	 * @return
	 */
	public List<EventRuleBean> eventRuleQuery(long resID,String eventTypeName);
	/**
	 * 已产生的事件查询
	 * @param resID
	 * @param eventTypeID
	 * @return
	 */
	public EventBean eventQuery(long resID,long eventTypeID);
	/**
	 * 
	 * @param eventBean
	 */
	public void eventSave(EventBean eventBean);
	/**
	 * 查询事件消息格式
	 * @param eventTypeID
	 * @return
	 */
	public String eventMessageQuery(long eventTypeID);
	/**
	 * 更新已产生事件的信息
	 * @param eventID
	 * @param message
	 */
	public void eventMessageUpdate(long eventID,String message,int eventLevel);
	/**
	 * 恢复事件并存储历史事件
	 * @param eventBean
	 */
	public void eventHisDeleteAndSave(EventBean eventBean);
}
