package com.secpro.platform.monitoring.process.dao;

import com.secpro.platform.monitoring.process.entity.EventRuleBean;
/**
 * 查询自定义事件消息内的参数值
 * @author sxf
 *
 */
public interface IEventMsgDao {
	public String columnQuery(long resID, EventRuleBean eventRuleBean,String columnName);

}
