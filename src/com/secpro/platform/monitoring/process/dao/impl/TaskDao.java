package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.ITaskDao;
import com.secpro.platform.monitoring.process.entity.TaskBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;

public class TaskDao implements ITaskDao {
	private static PlatformLogger theLogger = PlatformLogger.getLogger(CompanyDao.class);
	public static final String TASK_SUCCESS = "1";
	public static final String TASK_FAILED = "0";

	@Override
	public void taskStatusUpdate(TaskBean taskBean) {
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
//		try {
//			statement = conn.createStatement();
//			String sql = "UPDATE SYS_TASK_STATUS SET EXEU_DATE='" + executeDate
//					+ "',STATUS='" + status + "' WHERE TASK_CODE='"
//					+ taskCode + "'";
//			statement.execute(sql);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			//e.printStackTrace();
//			theLogger.exception(e);
//		} finally {
//			DBUtil.closeConnection(conn, statement);
//		}
	}

	@Override
	public void cacheTaskSave(TaskBean taskBean) {
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
//		try {
//			statement = conn.createStatement();
//			String sql = "SELECT * FROM SYS_TASK_STATUS WHERE TASK_CODE='"
//					+ taskCode + "'";
//			result = statement.executeQuery(sql);
//			if (result.next()) {
//				String operation=result.getString(3);
//				String content=result.getString(4);
//				//String cDate=result.getString(5);
//				//String plan_date=result.getString(6);
//				String res_ip=result.getString(9);
//				String city_code=result.getString(10);
//				String cacheTaskCode=taskCode+"_c"+executeDate;
//				String insertSql="";
//				if(Assert.isEmptyString(city_code)){
//					insertSql="insert into sys_task_status(id,task_code,operation,content,cdate,plan_date,exeu_date,status,res_ip) values(sys_task_status_seq.nextval,'"+cacheTaskCode+"','"+operation+"','"+content+"','"+executeDate+"','"+executeDate+"','"+executeDate+"','"+status+"','"+res_ip+"')";
//				}else{
//					insertSql="insert into sys_task_status(id,task_code,operation,content,cdate,plan_date,exeu_date,status,res_ip,city_code) values(sys_task_status_seq.nextval,'"+cacheTaskCode+"','"+operation+"','"+content+"','"+executeDate+"','"+executeDate+"','"+executeDate+"','"+status+"','"+res_ip+"','"+city_code+"')";
//				}
//				statement.execute(insertSql);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			//e.printStackTrace();
//			theLogger.exception(e);
//		} finally {
//			DBUtil.closeConnection(conn, statement, result);
//		}

	}

}
