package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IDeviceDao;
import com.secpro.platform.monitoring.process.dao.IEventMsgDao;
import com.secpro.platform.monitoring.process.entity.EventRuleBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;

public class DeviceDao implements IDeviceDao,IEventMsgDao{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(DeviceDao.class);
	@Override
	public String typeNameQuery(String cityCode, String resIP) {
		if(Assert.isEmptyString(cityCode)||Assert.isEmptyString(resIP)){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select t1.type_name from sys_dev_type t1,sys_res_obj t2 where t1.type_code=t2.type_code and t2.city_code='"+cityCode+"' and t2.res_ip='"+resIP+"'";
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

	@Override
	public String columnQuery(long resID, EventRuleBean eventRuleBean,
			String columnName) {
		// TODO Auto-generated method stub
		if(resID==0||eventRuleBean==null||Assert.isEmptyString(columnName)){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql ="select t1."+columnName+" from sys_dev_type t1,sys_res_obj t2 where t2.id="+resID+" and t1.type_code=t2.type_code";
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
