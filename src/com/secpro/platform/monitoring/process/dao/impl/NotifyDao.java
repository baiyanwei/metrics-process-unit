package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.INotifyDao;
import com.secpro.platform.monitoring.process.entity.AlarmBean;
import com.secpro.platform.monitoring.process.entity.UserInfoBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;

public class NotifyDao implements INotifyDao{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(NotifyDao.class);
	@Override
	public List<UserInfoBean> notifyUserQuery(long resID, long eventRuleID) {
		if(resID==0||eventRuleID==0){
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql ="select t1.id,t1.user_name,t1.mobel_tel,t1.enable_account from sys_user_info t1,notify_user_rule t2 where t2.res_id="+resID+" and t2.event_rule_id="+eventRuleID+" and t2.user_id=t1.id";
			result=statement.executeQuery(sql);
			List<UserInfoBean> users=new ArrayList<UserInfoBean>();
			while(result.next()){
				UserInfoBean user=new UserInfoBean();
				user.setId(result.getLong(1));
				user.setUserName(result.getString(2));
				user.setMobelTel(result.getString(3));
				user.setEnableAccount(result.getString(4));
				users.add(user);
			}
			return users;
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
	public void notifySave(List<AlarmBean> alarms) {
		if(alarms==null||alarms.size()==0){
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement statement = null;
		
		try {
			String sql = "insert into send_msg(id,cdate,user_name,mobel_tel,message) values(send_msg_seq.nextval,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			int batch=0;
			for(AlarmBean alarmBean:alarms){
				statement.setString(1, alarmBean.getCdate());
				statement.setString(2, alarmBean.getUserName());
				statement.setString(3, alarmBean.getMobelTel());
				statement.setString(4, alarmBean.getMessgae());
				//statement.setString(5, alarmBean.getSendMsgStatus());
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
	public List<AlarmBean> sendMessQuery() {
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result=null;
		try {
			statement = conn.createStatement();
			String sql ="select id,cdate,user_name,mobel_tel,message from send_msg where send_msg_status is null";
			result=statement.executeQuery(sql);
			List<AlarmBean> alarmBeans=new ArrayList<AlarmBean>();
			while(result.next()){
				AlarmBean alarmB=new AlarmBean();
				alarmB.setId(result.getLong(1));
				alarmB.setCdate(result.getString(2));
				alarmB.setUserName(result.getString(3));
				alarmB.setMobelTel(result.getString(4));
				alarmB.setMessgae(result.getString(5));
				alarmBeans.add(alarmB);
			}
			return alarmBeans;
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
	public void sendMessSave(List<AlarmBean> alarms) {
		if(alarms==null||alarms.size()==0){
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement insertSendMsgHis = null;
		PreparedStatement deleteSendMsg=null;
		try {
			conn.setAutoCommit(false);
			String deleteSql = "delete from send_msg where id=?";
			deleteSendMsg=conn.prepareStatement(deleteSql);
			String insertSql="insert into send_mes_his(id,cdate,user_name,mobel_tel,message,send_msg_status,send_date) values(?,?,?,?,?,?,?)";
			insertSendMsgHis=conn.prepareStatement(insertSql);
			int batch=0;
			for(AlarmBean alarmB:alarms){
				deleteSendMsg.setLong(1, alarmB.getId());
				deleteSendMsg.addBatch();
				insertSendMsgHis.setLong(1, alarmB.getId());
				insertSendMsgHis.setString(2, alarmB.getCdate());
				insertSendMsgHis.setString(3, alarmB.getUserName());
				insertSendMsgHis.setString(4, alarmB.getMobelTel());
				insertSendMsgHis.setString(5, alarmB.getMessgae());
				insertSendMsgHis.setString(6, alarmB.getSendMsgStatus());
				insertSendMsgHis.setString(7, alarmB.getSendDate());
				insertSendMsgHis.addBatch();
				batch++;
				if(batch==50){
					deleteSendMsg.executeBatch();
					insertSendMsgHis.executeBatch();
					batch=0;
				}
				
				if(batch>0){
					deleteSendMsg.executeBatch();
					insertSendMsgHis.executeBatch();
				}
			}
			conn.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(deleteSendMsg);
			DBUtil.closeConnection(conn, insertSendMsgHis);
		}
	}

}
