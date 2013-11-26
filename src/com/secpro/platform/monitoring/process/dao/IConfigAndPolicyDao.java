package com.secpro.platform.monitoring.process.dao;

import java.util.Map;

import com.secpro.platform.monitoring.process.entity.ConfigAndPolicyBean;
import com.secpro.platform.monitoring.process.entity.ContainAndConflictBean;
/**
 * 策略信息包含和冲突检查结果相关数据库操作
 * @author sxf
 *
 */
public interface IConfigAndPolicyDao {
	/**
	 * 查询策略信息中预定义服务
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	public Map<String,String> predefinedServiceQuery(String cityCode,String targetIP);
	/**
	 * 查询策略和配置信息标准化规则路径
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	public String standardRulePathQuery(String cityCode,String targetIP);
	/**
	 * 标准化后的策略配置信息存储
	 * @param configAndPolicyBean
	 */
	public void configAndPolicySave(ConfigAndPolicyBean configAndPolicyBean);
	/**
	 * 查询包含与冲突检查规则
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	public String containAndConflictRuleQuery(String cityCode,String targetIP);
	/**
	 * 冲突与包含检查结果存储
	 * @param containAndConflictBean
	 */
	public void containAndConflictSave(ContainAndConflictBean containAndConflictBean);
	/**
	 * 查询策略信息和配置信息
	 * @param resID
	 * @return
	 */
	public String configAndPolicyQuery(long resID);
}
