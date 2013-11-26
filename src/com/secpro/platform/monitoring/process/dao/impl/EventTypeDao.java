package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IEventMsgDao;
import com.secpro.platform.monitoring.process.entity.EventRuleBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;

public class EventTypeDao implements IEventMsgDao{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(EventTypeDao.class);
	@Override
	public String columnQuery(long resID, EventRuleBean eventRuleBean,
			String columnName) {
		if(resID==0||eventRuleBean==null||Assert.isEmptyString(columnName)){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql ="select "+columnName+" from event_type where id="+eventRuleBean.getEventTypeID();
			result=statement.executeQuery(sql);
			if(result.next()){
				
				return result.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement,result);
		}
		return null;
	}

	
	
}
