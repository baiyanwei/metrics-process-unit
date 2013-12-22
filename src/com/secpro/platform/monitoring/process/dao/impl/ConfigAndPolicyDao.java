package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.entity.ConfigAndPolicyBean;
import com.secpro.platform.monitoring.process.entity.ContainAndConflictBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;

public class ConfigAndPolicyDao implements IConfigAndPolicyDao {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(ConfigAndPolicyDao.class);

	@Override
	public Map<String, String> predefinedServiceQuery(long resID) {
		if (resID==0L) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			String sql = "select t1.service_name,t1.SERVICE_PROT_MAPPING from predefined_service t1,sys_res_obj t2 where t2.id="+resID+" and t1.type_code=t2.type_code";
			result = statement.executeQuery(sql);
			Map<String, String> predefinedService = new HashMap<String, String>();
			while (result.next()) {

				String key = result.getString(1);
				String value = result.getString(2);
				predefinedService.put(key, value);
			}
			return predefinedService;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement, result);
		}
		return null;
	}

	@Override
	public String standardRulePathQuery(long resID) {
		if (resID==0L) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			String sql = "select t1.STANDARD_RULE from config_policy_rule t1,sys_res_obj t2 where t2.id="+resID+" and t1.type_code=t2.type_code";
			result = statement.executeQuery(sql);
			if (result.next()) {

				return result.getString(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement, result);
		}
		return null;
	}

	@Override
	public void configAndPolicySave(ConfigAndPolicyBean configAndPolicyBean) {
		if (configAndPolicyBean == null) {
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement statement = null;

		try {

			String sql = "insert into raw_config_policy(id,cdate,config_policy_info,res_id,task_code) values(config_policy_seq.nextval,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1, configAndPolicyBean.getCdate());
			statement
					.setString(2, configAndPolicyBean.getConfigAndPolicyInfo());
			statement.setLong(3, configAndPolicyBean.getResID());
			statement.setString(4, configAndPolicyBean.getTaskCode());
			statement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement);
		}
	}

	@Override
	public String containAndConflictRuleQuery(long resID) {
		if (resID==0L) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			String sql = "select t1.contain_conflict_rule from config_policy_rule t1,sys_res_obj t2 where t2.id="+resID+" and t1.type_code=t2.type_code";
			result = statement.executeQuery(sql);
			if (result.next()) {

				return result.getString(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement, result);
		}
		return null;
	}

	@Override
	public void containAndConflictSave(
			ContainAndConflictBean containAndConflictBean) {
		if (containAndConflictBean == null) {
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement statement = null;

		try {

			String sql = "insert into raw_contain_conflict(cdate,CONTAIN_CONFLICT_INFO,res_id,task_code) values(?,?,?,?)";
			statement = conn.prepareStatement(sql);
			statement.setString(1, containAndConflictBean.getCdate());
			statement.setString(2,
					containAndConflictBean.getContainAndConflictInfo());
			statement.setLong(3, containAndConflictBean.getResID());
			statement.setString(4, containAndConflictBean.getTaskCode());
			statement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement);
		}

	}

	@Override
	public String configAndPolicyQuery(long resID) {
		if (resID == 0) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			String sql = "select config_policy_info from raw_config_policy where res_id="
					+ resID + " order by cdate desc ";
			result = statement.executeQuery(sql);
			if (result.next()) {

				return result.getString(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement, result);
		}
		return null;
	}

}
