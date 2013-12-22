package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IEventMsgDao;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.entity.EventRuleBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;
/**
 * 资源数据相关数据库操作
 * @author sxf
 *
 */
public class ResDao implements IResourceDao,IEventMsgDao{
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(ResDao.class);
	@Override
	public String typeCodeQuery(long resID) {
		if(resID==0L){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select type_code from sys_res_obj where id="+resID;
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
	public long resIDQuery(String cityCode, String resIP) {
		if(Assert.isEmptyString(cityCode)||Assert.isEmptyString(resIP)){
			return 0;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select id from sys_res_obj where city_code='"+cityCode+"' and res_ip='"+resIP+"'";
			result=statement.executeQuery(sql);
			if(result.next()){
				return result.getLong(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement,result);
		}
		return 0;
	}

	@Override
	public String resPausedQuery(long resID) {
		if(resID==0){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select res_paused from sys_res_obj where id="+resID;
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
		if(resID==0||eventRuleBean==null||Assert.isEmptyString(columnName)){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql ="select "+columnName+" from sys_res_obj where id="+resID;
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
	public long resIDQueryByTaskRegion(String taskRegion, String resIP) {
		if(Assert.isEmptyString(taskRegion)||Assert.isEmptyString(resIP)){
			return 0;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select t2.id from sys_city t1,sys_res_obj t2 where t2.res_ip='"+resIP+"' and t1.city_code=t2.city_code and t1.task_region='"+taskRegion+"'";
			result=statement.executeQuery(sql);
			if(result.next()){
				return result.getLong(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement,result);
		}
		return 0;
	}

}
