package com.secpro.platform.monitoring.process.chains.telnet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.event.EventAndAlarm;
import com.secpro.platform.monitoring.process.chains.ref.match.BaselineMatch;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IBaselineDao;
import com.secpro.platform.monitoring.process.dao.impl.BaselineDao;
import com.secpro.platform.monitoring.process.entity.BaselineBean;
import com.secpro.platform.monitoring.process.entity.BaselineMatchBean;
import com.secpro.platform.monitoring.process.entity.BaselineMatchScoreBean;
import com.secpro.platform.monitoring.process.utils.DateFormat;
/**
 * 基线比对
 * telnet采集回来的策略以及配置信息根据基线比对规则，与基线进行比对，并将比对结果存入数据库
 * 可产生基线比对等相应的事件
 * @author sxf
 *
 */
public class TelnetMatchBaseline implements IDataProcessChain{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(TelnetMatchBaseline.class);
	private int chainID=0;
	private static final String MATCH_SUCCESS="1";
	private static final String MATCH_FAILED="0";
	private static final String CONFIG_BASELINE="0";
	private static final String POLICY_BASELINE="1";
	private static final String BASELINE_SCORE_NAME="baseline score";
	private static final String BASELINE_MATCH_NAME="baseline match";
	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("telnet dataProcess chain ID: "+getChainID());
		if (rawData == null) {
			theLogger.error(
					"invalid rawData in telnet data processing.");
			return null;
		}
		if(!rawData.getClass().equals(HashMap.class)){
			theLogger.error("need type of HashMap in telnet data processing.");
			return null;
		}
		Map telnetData=(Map)rawData;
		long resID=(Long)telnetData.get("resID");
		if(resID==0){
			theLogger.error("resource id is empty!");
			return null;
		}
		List<BaselineBean> baselineValues=getBaselineAndRule(resID);
		if(baselineValues==null){
			theLogger.debug("This model does not set the baseline");
		}else {
			List<BaselineMatchBean> matchValues=new ArrayList<BaselineMatchBean>();
			String cdate=DateFormat.getNowDate();
			String taskCode=(String) telnetData.get(MetaDataConstant.TASK_CODE);
			String[] executeResult=(String[]) telnetData.get(MetaDataConstant.EXECUTE_RESULT);
			int totalScore=0;
			StringBuilder matchResult=new StringBuilder();
			for(BaselineBean baselineB:baselineValues){
				BaselineMatchBean matchB=new BaselineMatchBean();
				long baselineID=baselineB.getId();
				String baselineType=baselineB.getBaselineType();
				String baselineBlackWhite=baselineB.getBaselineBlackWhite();
				String rule=baselineB.getRule();
				String baselineDesc=baselineB.getBaselineDesc();
				int score=baselineB.getScore();
				if(Assert.isEmptyString(rule)){
					matchB.setBaselineID(baselineID);
					matchB.setCdate(cdate);
					matchB.setResID(resID);
					matchB.setTaskCode(taskCode);
					matchB.setMatchResult(MATCH_FAILED);
					matchResult.append(baselineDesc+"%%");
				}else{
					String result="";
					if(CONFIG_BASELINE.equals(baselineType)){
						result=baselineMatch(executeResult[0],rule,0,baselineBlackWhite);
					}else{
						result=baselineMatch(executeResult[1],rule,1,baselineBlackWhite);
					}
					if(Assert.isEmptyString(result)){
						matchB.setBaselineID(baselineID);
						matchB.setCdate(cdate);
						matchB.setResID(resID);
						matchB.setTaskCode(taskCode);
						matchB.setMatchResult(MATCH_FAILED);
					}else{
						matchB.setBaselineID(baselineID);
						matchB.setCdate(cdate);
						matchB.setResID(resID);
						matchB.setTaskCode(taskCode);
						matchB.setMatchResult(MATCH_SUCCESS);
						matchB.setResult(result);
						totalScore+=score;
					}
				}
				matchValues.add(matchB);
			}
			BaselineMatchScoreBean matchScore=new BaselineMatchScoreBean();
			matchScore.setCdate(cdate);
			matchScore.setResID(resID);
			matchScore.setTaskCode(taskCode);
			matchScore.setTotalScore(totalScore);
			matchValuesStorage(matchValues,matchScore);
			EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID, BASELINE_SCORE_NAME, totalScore+"");
			if(Assert.isEmptyString(matchResult.toString())){
				EventAndAlarm.isRecoveryEvent(resID, BASELINE_MATCH_NAME);
			}else{
				EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID, BASELINE_MATCH_NAME, matchResult.toString());
			}
		}
		return telnetData;
	}

	/**
	 * 存储配置和策略信息与基线比对的结果
	 * @param matchValues
	 * @param matchScore
	 */
	private void matchValuesStorage(List<BaselineMatchBean> matchValues,BaselineMatchScoreBean matchScore) {
		IBaselineDao baselineDao=new BaselineDao();
		baselineDao.baselineMatchSave(matchValues,matchScore);
		
	}
	/**
	 * 进行基线比对
	 * @param values
	 * @param rule
	 * @param isConfigOrPolicy
	 * @param baselineBlackWhite
	 * @return
	 */
	private String baselineMatch(String values,String rule, int isConfigOrPolicy, String baselineBlackWhite) {
		return BaselineMatch.baselineMatch(values, rule, isConfigOrPolicy, baselineBlackWhite);

	}
	/**
	 * 查询基线以及比对规则
	 * @param resID
	 * @return
	 */
	private List<BaselineBean> getBaselineAndRule(long resID) {
		IBaselineDao baselineDao=new BaselineDao();
		return baselineDao.baselineQuery(resID);
	}

	@Override
	public void setChainID(int chainID) {
		this.chainID=chainID;
	}

	@Override
	public int getChainID() {
		// TODO Auto-generated method stub
		return this.chainID;
	}

}
