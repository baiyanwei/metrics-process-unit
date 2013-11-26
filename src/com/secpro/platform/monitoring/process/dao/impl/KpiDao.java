package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IKpiDao;
import com.secpro.platform.monitoring.process.entity.KpiBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;
/**
 * kpi相关数据库操作
 * @author sxf
 *
 */
public class KpiDao implements IKpiDao{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(KpiDao.class);
	@Override
	public Map<String, String[]> kpiIDAndRuleQuery(String cityCode, String resIP) {
		if(Assert.isEmptyString(cityCode)||Assert.isEmptyString(resIP)){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select t1.miboid,t1.kpi_id,t1.rule from sys_kpi_oid t1,sys_res_obj t2 where city_code='"+cityCode+"' and res_ip='"+resIP+"' and t1.type_code=t2.type_code";
			result=statement.executeQuery(sql);
			Map<String,String[]> kpiIDAndRuleMapping=new HashMap<String,String[]>();
			while(result.next()){
				String[] idAndRule=new String[2];
				idAndRule[0]=result.getString(2);
				idAndRule[1]=result.getString(3);
				kpiIDAndRuleMapping.put(result.getString(1),idAndRule);
				
			}
			return kpiIDAndRuleMapping;
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
	public String kpiTypeQuery(long kpiID) {
		if(kpiID==0){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select kpi_type from sys_kpi_info where id="+kpiID;
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
	public void rawKpiSave(List<KpiBean> snmpList) {
		if(snmpList==null||snmpList.size()==0){
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement statement = null;
		
		try {
			
			String sql = "insert into raw_kpi(res_id,kpi_id,cdate,value_str,value_num) values(?,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			int batch=0;
			for(KpiBean snmpBean:snmpList){
				statement.setLong(1, snmpBean.getResID());
				statement.setLong(2, snmpBean.getKpiID());
				statement.setString(3, snmpBean.getCdate());
				statement.setString(4, snmpBean.getValueStr());
				statement.setFloat(5, snmpBean.getValueInt());
				statement.addBatch();
				batch++;
				if(batch==50){
					statement.executeBatch();
					batch=0;
				}
			}
			if(batch>0){
				statement.executeBatch();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement);
		}
		
	}

	@Override
	public String[] kpiTypeAndKpiIDQuery(String kpiName,long resID) {
		if(Assert.isEmptyString(kpiName)||resID==0){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select t1.kpi_type,t1.id from sys_kpi_info t1,sys_res_obj t2 where t2.id="+resID+" and t1.class_id=t2.class_id and kpi_name='"+kpiName+"'";
			result=statement.executeQuery(sql);
			if(result.next()){
				String[] resultS=new String[2];
				resultS[0]=result.getString(1);
				resultS[1]=result.getString(2);
				return resultS;
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
	public String kpiNameQuery(long kpiID) {
		if(kpiID==0){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql ="select kpi_name from sys_kpi_info where id="+kpiID;
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
