package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IEventDao;
import com.secpro.platform.monitoring.process.dao.IEventMsgDao;
import com.secpro.platform.monitoring.process.entity.EventBean;
import com.secpro.platform.monitoring.process.entity.EventRuleBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;

public class EventDao implements IEventDao, IEventMsgDao {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(EventDao.class);

	@Override
	public List<EventRuleBean> eventRuleQuery(long resID, String eventTypeName) {
		if (resID == 0 || Assert.isEmptyString(eventTypeName)) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet resultPri = null;
		ResultSet resultPub = null;
		try {
			statement = conn.createStatement();
			String sqlPri = "select t1.id,t1.event_level,t1.threshold_value,t1.set_msg,t1.res_id,t1.event_type_id,t1.recover_set_msg,t1.repeat,t1.threshold_opr,t2.event_recover from sys_event_rule t1,event_type t2 where t1.res_id="
					+ resID
					+ " and t2.event_type_name='"
					+ eventTypeName
					+ "' and t1.event_type_id=t2.id order by t1.event_level desc";
			resultPri = statement.executeQuery(sqlPri);
			List<EventRuleBean> eventRules = new ArrayList<EventRuleBean>();
			while (resultPri.next()) {
				EventRuleBean eventBean = new EventRuleBean();
				eventBean.setId(resultPri.getLong(1));
				eventBean.setEnentLevel(resultPri.getInt(2));
				eventBean.setThresholdValue(resultPri.getString(3));
				eventBean.setSetMsg(resultPri.getString(4));
				eventBean.setResID(resultPri.getLong(5));
				eventBean.setEventTypeID(resultPri.getLong(6));
				eventBean.setRecoverSetMsg(resultPri.getString(7));
				eventBean.setRepeat(resultPri.getString(8));
				eventBean.setThresholdOpr(resultPri.getString(9));
				eventBean.setEventRecover(resultPri.getString(10));
				eventRules.add(eventBean);
			}
			if (eventRules.size() > 0) {
				return eventRules;
			}
			String sqlPub = "select t1.id,t1.event_level,t1.threshold_value,t1.set_msg,t1.event_type_id,t1.recover_set_msg,t1.repeat,t1.threshold_opr,t2.event_recover from sys_event_rule t1,event_type t2 where  t2.event_type_name='"
					+ eventTypeName
					+ "' and t1.event_type_id=t2.id order by t1.event_level desc";
			resultPub = statement.executeQuery(sqlPub);
			while (resultPub.next()) {
				EventRuleBean eventBean = new EventRuleBean();
				eventBean.setId(resultPub.getLong(1));
				eventBean.setEnentLevel(resultPub.getInt(2));
				eventBean.setThresholdValue(resultPub.getString(3));
				eventBean.setSetMsg(resultPub.getString(4));
				// eventBean.setResID(resID);
				eventBean.setEventTypeID(resultPub.getLong(5));
				eventBean.setRecoverSetMsg(resultPub.getString(6));
				eventBean.setRepeat(resultPub.getString(7));
				eventBean.setThresholdOpr(resultPub.getString(8));
				eventBean.setEventRecover(resultPub.getString(9));
				eventRules.add(eventBean);
			}
			if (eventRules.size() > 0) {
				return eventRules;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement, resultPri, resultPub);
		}
		return null;
	}

	@Override
	public EventBean eventQuery(long resID, long eventTypeID) {
		if (resID == 0 || eventTypeID == 0) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			String sql = "select id,event_level,message,cdate,confirm_user,confirm_date from sys_event where res_id="
					+ resID + " and event_type_id=" + eventTypeID;
			result = statement.executeQuery(sql);
			if (result.next()) {
				EventBean eventBean = new EventBean();
				eventBean.setId(result.getLong(1));
				eventBean.setEventLevel(result.getInt(2));
				eventBean.setMessage(result.getString(3));
				eventBean.setCdate(result.getString(4));
				eventBean.setConfirmUser(result.getString(5));
				eventBean.setConfirmDate(result.getString(6));
				eventBean.setResID(resID);
				eventBean.setEventTypeID(eventTypeID);
				return eventBean;
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
	public void eventSave(EventBean eventBean) {
		if (eventBean == null) {
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement statement = null;

		try {

			String sql = "insert into sys_event(id,event_level,message,cdate,res_id,event_type_id) values(sys_event_seq.nextval,?,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			statement.setInt(1, eventBean.getEventLevel());
			statement.setString(2, eventBean.getMessage());
			statement.setString(3, eventBean.getCdate());
			statement.setLong(4, eventBean.getResID());
			statement.setLong(5, eventBean.getEventTypeID());
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
	public String eventMessageQuery(long eventTypeID) {
		if (eventTypeID == 0) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			String sql = "select msg_format from event_msg where event_type_id="
					+ eventTypeID;
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
	public String columnQuery(long resID, EventRuleBean eventRuleBean,
			String columnName) {
		if (resID == 0 || eventRuleBean == null
				|| Assert.isEmptyString(columnName)) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			String sql = "select " + columnName
					+ " from sys_event_rule where id=" + eventRuleBean.getId();
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
	public void eventMessageUpdate(long eventID, String message) {
		if (eventID == 0 || Assert.isEmptyString(message)) {
			return;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		try {
			statement = conn.createStatement();
			String sql = "update sys_event set message='" + message
					+ "' where id=" + eventID;
			statement.execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement);
		}
	}

	// @Override
	// public void eventDeleteAndSave(EventBean eventBean, long eventID) {
	// Connection conn = DBUtil.getConnection();
	// Statement statement = null;
	//
	// try {
	// conn.setAutoCommit(false);
	// statement=conn.createStatement();
	// String deleteSql = "delete sys_event where id="+eventID;
	// statement.execute(deleteSql);
	// String insertSql="";
	// String confirmUser=eventBean.getConfirmUser();
	// String confirmDate=eventBean.getConfirmDate();
	// if(!Assert.isEmptyString(confirmUser)&&!Assert.isEmptyString(confirmDate)){
	// insertSql="insert into sys_event(id,event_level,message,cdate,res_id,event_type_id,confirm_user,confirm_date) values(sys_event_seq.nextval,"+eventBean.getEventLevel()+",'"+eventBean.getMessage()+"','"+eventBean.getCdate()+"',"+eventBean.getResID()+","+eventBean.getEventTypeID()+",'"+confirmUser+"','"+confirmDate+"')";
	// }else{
	// insertSql="insert into sys_event(id,event_level,message,cdate,res_id,event_type_id) values(sys_event_seq.nextval,"+eventBean.getEventLevel()+",'"+eventBean.getMessage()+"','"+eventBean.getCdate()+"',"+eventBean.getResID()+","+eventBean.getEventTypeID()+")";
	// }
	// statement.execute(insertSql);
	// conn.commit();
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// try {
	// conn.rollback();
	// } catch (SQLException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// e.printStackTrace();
	// } finally {
	// DBUtil.closeConnection(conn, statement);
	// }
	//
	// }

	@Override
	public void eventHisDeleteAndSave(EventBean eventBean) {
		if (eventBean == null) {
			return;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		PreparedStatement prepSta = null;

		try {
			conn.setAutoCommit(false);
			statement = conn.createStatement();
			String deleteSql = "delete sys_event where id=" + eventBean.getId();
			statement.execute(deleteSql);
			String insertSql = "insert into sys_event_his(id,event_level,message,cdate,confirm_user,confirm_date,clear_user,clear_date,res_id,event_type_id) values(?,?,?,?,?,?,?,?,?,?)";
			prepSta = conn.prepareStatement(insertSql);
			prepSta.setLong(1, eventBean.getId());
			prepSta.setInt(2, eventBean.getEventLevel());
			prepSta.setString(3, eventBean.getMessage());
			prepSta.setString(4, eventBean.getCdate());
			prepSta.setString(5, eventBean.getConfirmUser());
			prepSta.setString(6, eventBean.getConfirmDate());
			prepSta.setString(7, eventBean.getClearUser());
			prepSta.setString(8, eventBean.getClearDate());
			prepSta.setLong(9, eventBean.getResID());
			prepSta.setLong(10, eventBean.getEventTypeID());
			prepSta.execute();
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				theLogger.exception(e1);
			}
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(prepSta);
			DBUtil.closeConnection(conn, statement);

		}

	}

}
