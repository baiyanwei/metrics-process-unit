package com.secpro.platform.monitoring.process.dao;

import java.util.List;

import com.secpro.platform.monitoring.process.entity.BaselineBean;
import com.secpro.platform.monitoring.process.entity.BaselineMatchBean;
import com.secpro.platform.monitoring.process.entity.BaselineMatchScoreBean;
/**
 * 基线数据库操作
 * @author sxf
 *
 */
public interface IBaselineDao {
	/**
	 * 查询基线
	 * @param resID
	 * @return
	 */
	public List<BaselineBean> baselineQuery(long resID);
	/**
	 * 基线比对结果存储
	 * @param matchResult
	 * @param matchScore
	 */
	public void baselineMatchSave(List<BaselineMatchBean> matchResult,BaselineMatchScoreBean matchScore);
}
