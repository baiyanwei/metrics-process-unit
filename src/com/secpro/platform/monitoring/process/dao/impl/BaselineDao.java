package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IBaselineDao;
import com.secpro.platform.monitoring.process.entity.BaselineBean;
import com.secpro.platform.monitoring.process.entity.BaselineMatchBean;
import com.secpro.platform.monitoring.process.entity.BaselineMatchScoreBean;
import com.secpro.platform.monitoring.process.utils.DBUtil;

public class BaselineDao implements IBaselineDao {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(BaselineDao.class);

	public List<BaselineBean> baselineQuery(long resID) {
		if (resID == 0) {
			return null;
		}
		Connection conn = DBUtil.getConnection();
		Statement statement = null;
		ResultSet result = null;
		try {
			statement = conn.createStatement();
			String sql = "select tt1.baseidid, tt1.baseline_type ,tt1.baseline_black_white,tt2.rule,tt1.score,tt1.baseline_desc from (select t3.id baseidid,t3.baseline_type baseline_type,t3.baseline_desc baseline_desc,t3.baseline_black_white baseline_black_white,t1.type_code type_code,t2.score score from sys_res_obj t1 ,baseline_template_mapping t2,sys_baseline t3 where  t1.id="
					+ resID
					+ " and t1.template_id=t2.template_id and t3.id=t2.baseline_id) tt1,baseline_rule tt2 where tt1.type_code=tt2.type_code(+) and tt1.baseidid=tt2.baseline_id(+)";
			result = statement.executeQuery(sql);
			List<BaselineBean> baselineValues = new ArrayList<BaselineBean>();
			while (result.next()) {
				BaselineBean baselineBean = new BaselineBean();
				baselineBean.setId(result.getLong(1));
				baselineBean.setBaselineType(result.getString(2));
				baselineBean.setBaselineBlackWhite(result.getString(3));
				baselineBean.setRule(result.getString(4));
				baselineBean.setScore(result.getInt(5));
				baselineBean.setBaselineDesc(result.getString(6));
				baselineValues.add(baselineBean);
			}
			return baselineValues;
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
	public void baselineMatchSave(List<BaselineMatchBean> matchResult,
			BaselineMatchScoreBean matchScore) {
		if (matchResult == null || matchResult.size() == 0
				|| matchScore == null) {
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement resultState = null;
		PreparedStatement scoreState = null;

		try {
			conn.setAutoCommit(false);
			String sql = "insert into raw_baseline_match(match_result,result,cdate,res_id,baseline_id,task_code) values(?,?,?,?,?,?)";
			resultState = conn.prepareStatement(sql);
			for (BaselineMatchBean matchB : matchResult) {
				resultState.setString(1, matchB.getMatchResult());
				resultState.setString(2, matchB.getResult());
				resultState.setString(3, matchB.getCdate());
				resultState.setLong(4, matchB.getResID());
				resultState.setLong(5, matchB.getBaselineID());
				resultState.setString(6, matchB.getTaskCode());
				resultState.addBatch();
			}
			resultState.executeBatch();
			String sqlScore = "insert into raw_baseline_match_score(total_score,cdate,res_id,task_code) values(?,?,?,?)";

			scoreState = conn.prepareStatement(sqlScore);
			scoreState.setInt(1, matchScore.getTotalScore());
			scoreState.setString(2, matchScore.getCdate());
			scoreState.setLong(3, matchScore.getResID());
			scoreState.setString(4, matchScore.getTaskCode());
			scoreState.execute();
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(scoreState);
			DBUtil.closeConnection(conn, resultState);

		}
	}
}
