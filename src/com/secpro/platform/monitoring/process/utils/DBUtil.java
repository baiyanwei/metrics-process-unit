package com.secpro.platform.monitoring.process.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.storage.services.DataBaseStorageService;
/**
 * 数据库相关操作工具方法类
 * @author sxf
 *
 */
public class DBUtil {
	private static PlatformLogger theLogger = PlatformLogger.getLogger(DBUtil.class);
	private static DataBaseStorageService dataBaseStorage;
	static{
		//得到数据库存储服务
		dataBaseStorage=ServiceHelper.findService(DataBaseStorageService.class);
	}
	/**
	 * 获取数据库连接
	 * @return
	 */
	public static Connection getConnection(){
		if(dataBaseStorage!=null)
		{
			try {
				return dataBaseStorage.getConnection();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
			}
		}
		return null;
		
	}
	/**
	 * 关闭连接
	 * @param conn
	 */
	public static void closeConnection(Connection conn){
		if(conn!=null)
		{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
			}
		}
	}
	public static void closeConnection(Connection conn,Statement statement)
	{
		if(statement!=null)
		{
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
			}
		}
		if(conn!=null)
		{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
			}
		}
		
	}
	public static void closeConnection(Connection conn,Statement statement,ResultSet...results)
	{
		for(ResultSet result:results){
			if(result!=null)
			{
				try {
					result.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					theLogger.exception(e);
				}
			}
		}
		if(statement!=null)
		{
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
			}
		}
		if(conn!=null)
		{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
			}
		}
		
	}
	public static void closeConnection(Statement statement){
		if(statement!=null)
		{
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
			}
		}
	}

}
