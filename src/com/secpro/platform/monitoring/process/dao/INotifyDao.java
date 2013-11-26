package com.secpro.platform.monitoring.process.dao;

import java.util.List;

import com.secpro.platform.monitoring.process.entity.AlarmBean;
import com.secpro.platform.monitoring.process.entity.UserInfoBean;
/**
 * 告警相关数据库操作
 * @author sxf
 *
 */
public interface INotifyDao {
	/**
	 * 查询告警发送人等信息
	 * @param resID
	 * @param eventRuleID
	 * @return
	 */
	public List<UserInfoBean> notifyUserQuery(long resID,long eventRuleID);
	/**
	 * 告警信息存储
	 * @param alarms
	 */
	public void notifySave(List<AlarmBean> alarms);
	public List<AlarmBean> sendMessQuery();
	public void sendMessSave(List<AlarmBean> alarms);
}
