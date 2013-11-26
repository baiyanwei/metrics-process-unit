package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.standard.StandardUtil;
import com.secpro.platform.monitoring.process.dao.ISyslogDao;
import com.secpro.platform.monitoring.process.entity.SyslogBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;
/**
 * syslog数据相关数据库操作
 * @author sxf
 *
 */
public class SyslogDao implements ISyslogDao{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(SyslogDao.class);
	@Override
	public void syslogDBMappingQuery(Map<String,String> dbMapping) {
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select rule_key,rule_value from syslog_db_mapping";
			result=statement.executeQuery(sql);
			while(result.next()){
				String key=result.getString(1);
				String value=result.getString(2);
				dbMapping.put(key, value);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement,result);
		}
	}

	@Override
	public void syslogRuleMappingQuery(Map<String, Map<String, Map<String,String>>> syslogRuleMapping) {
		// TODO Auto-generated method stub
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select type_code,rule_name,rule_key,rule_value from syslog_rule_mapping order by type_code,rule_name";
			result=statement.executeQuery(sql);
			Map<String,Map<String,String>> ruleNameMapping=new HashMap<String,Map<String,String>>();
			Map<String,String> ruleMapping=new HashMap<String,String>();
	
			String typeCodeMark="";
			String ruleNameMark="";
			while(result.next()){
				String typeCode=result.getString(1);
				String ruleName=result.getString(2);
				String ruleKey=result.getString(3);
				String ruleValue=result.getString(4);
				if(Assert.isEmptyString(typeCodeMark)&&Assert.isEmptyString(ruleNameMark)){
					typeCodeMark=typeCode;
					ruleNameMark=ruleName;
				}
				if(typeCodeMark.equals(typeCode)&&ruleNameMark.equals(ruleName)){
					ruleMapping.put(ruleKey, ruleValue);
				}else if(typeCodeMark.equals(typeCode)&&!ruleNameMark.equals(ruleName)){
					ruleNameMapping.put(ruleNameMark, ruleMapping);
					ruleMapping=new HashMap<String,String>();
					ruleMapping.put(ruleKey, ruleValue);
					ruleNameMark=ruleName;
				}else{
					ruleNameMapping.put(ruleNameMark, ruleMapping);
					syslogRuleMapping.put(typeCodeMark, ruleNameMapping);
					ruleNameMapping=new HashMap<String,Map<String,String>>();
					ruleMapping=new HashMap<String,String>();
					ruleMapping.put(ruleKey, ruleValue);
					typeCodeMark=typeCode;
					ruleNameMark=ruleName;
				}



			}
			ruleNameMapping.put(ruleNameMark, ruleMapping);
			syslogRuleMapping.put(typeCodeMark, ruleNameMapping);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement,result);
		}
		
	}

	@Override
	public Map<String, Map<String, String>> syslogRuleMappingQuery(
			String typeCode) {
		if(Assert.isEmptyString(typeCode)){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql = "select rule_name,rule_key,rule_value from syslog_rule_mapping where type_code='"+typeCode+"' order by rule_name";
			result=statement.executeQuery(sql);
			Map<String,Map<String,String>> ruleNameMapping=new HashMap<String,Map<String,String>>();
			Map<String,String> ruleMapping=new HashMap<String,String>();
			String ruleNameMark="";
			while(result.next()){
				
				String ruleName=result.getString(1);
				String ruleKey=result.getString(2);
				String ruleValue=result.getString(3);
				if(Assert.isEmptyString(ruleNameMark)){
					ruleNameMark=ruleName;
				}
				if(ruleNameMark.equals(ruleName)){
					ruleMapping.put(ruleKey, ruleValue);
				}else{
					ruleNameMapping.put(ruleNameMark, ruleMapping);
					ruleMapping=new HashMap<String,String>();
					ruleMapping.put(ruleKey, ruleValue);
					ruleNameMark=ruleName;
				}



			}
			ruleNameMapping.put(ruleNameMark, ruleMapping);
			return ruleNameMapping;
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
	public void syslogSave(List<SyslogBean> syslogBeans,
			Map<String, String> dataDBMapping) {
		if(syslogBeans==null||dataDBMapping==null||syslogBeans.size()==0||dataDBMapping.size()==0){
			return;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		try {
			statement = conn.createStatement();
			int batch=0;
			for(SyslogBean syslogB:syslogBeans){
				StringBuilder sqlBeforePart=new StringBuilder("insert into raw_syslog(id,syslog_name,rdate,edate,res_id");
				StringBuilder sqlAfterPart=new StringBuilder(" values(raw_syslog_seq.nextval,'"+syslogB.getSyslogName()+"','"+syslogB.getRdate()+"','"+syslogB.getEdate()+"',"+syslogB.getResID());
				String oriSyslog=syslogB.getOriSyslog();
				if(!Assert.isEmptyString(oriSyslog)){
					sqlBeforePart.append(",ori_syslog");
					sqlAfterPart.append(",'"+oriSyslog+"'");
				}
				Map<String,String> syslogResult=syslogB.getResultMapping();
				if(syslogResult!=null&&syslogResult.size()>0){
				for(String key:syslogResult.keySet()){
					if(Assert.isEmptyString(key)){
						continue;
					}
					String dbMapping=dataDBMapping.get(key);
					if(Assert.isEmptyString(dbMapping)){
						continue;
					}
					String value=syslogResult.get(key);
					if(Assert.isEmptyString(value)){
						continue;
					}
					
					sqlBeforePart.append(","+dbMapping);
					sqlAfterPart.append(",'"+value+"'");
					if("srcip".equals(dbMapping)){
						sqlBeforePart.append(","+"src_ip_num");
						sqlAfterPart.append(","+StandardUtil.ipToLong(value));
						
					}
					if("dstip".equals(dbMapping)){
						sqlBeforePart.append(","+"dst_ip_num");
						sqlAfterPart.append(","+StandardUtil.ipToLong(value));
						
					}
				}
				}
				
				sqlBeforePart.append(")");
				sqlAfterPart.append(")");
				statement.addBatch(sqlBeforePart.toString()+sqlAfterPart.toString());
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
	public List<String[]> syslogQueryAndUpdate(List<String> columnNames) {
		if(columnNames==null||columnNames.size()==0){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		PreparedStatement updateState = null;
		ResultSet result=null;
		try {
			
			statement = conn.createStatement();
			StringBuilder sql = new StringBuilder();
			sql.append("select ");
			for(String name:columnNames){
				if(Assert.isEmptyString(name)){
					continue;
				}
				sql.append(name+",");
			}
			sql.append("res_id,id from raw_syslog where hit_status is null order by res_id");
			result=statement.executeQuery(sql.toString());
			List<String[]> syslogValues=new ArrayList<String[]>();
			int batch=0;
			String updateSql="update raw_syslog set hit_status='1' where id=(?)";
			updateState = conn.prepareStatement(updateSql);
			while(result.next()){
				String[] oneSyslog=new String[columnNames.size()+1];
				for(int i=0;i<oneSyslog.length;i++){
					String columnValue=result.getString(i+1);
					if(Assert.isEmptyString(columnValue)){
						columnValue="";
					}
					oneSyslog[i]=columnValue;
					
				}
				syslogValues.add(oneSyslog);
				updateState.setLong(1,result.getLong(columnNames.size()+2));
				updateState.addBatch();
				batch++;
				if(batch==50){
					updateState.executeBatch();
					batch=0;
				}
			}
			if(batch>0){
				updateState.executeBatch();
			}
			return syslogValues;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(updateState);
			DBUtil.closeConnection(conn, statement,result);
			
		}
		return null;
	}

	@Override
	public void syslogHitSave(String startDate,String endDate, Map<Long, Map<String, Long>> hitResult){
		if(Assert.isEmptyString(startDate)||Assert.isEmptyString(endDate)||hitResult==null||hitResult.size()==0){
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement statement = null;
		
		try {
			
			String sql = "insert into raw_syslog_hit(start_date,end_date,res_id,policy_info,hit_count) values(?,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			int batch=1;
			for(long resID:hitResult.keySet()){
				Map<String,Long> oneResult=hitResult.get(resID);
				for(String policyInfo:oneResult.keySet()){
					statement.setString(1, startDate);
					statement.setString(2, endDate);
					statement.setLong(3, resID);
					statement.setString(4, policyInfo);
					statement.setLong(5, oneResult.get(policyInfo));
					statement.addBatch();
					batch++;
					if(batch==50){
						statement.executeBatch();
						batch=0;
					}
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
}
