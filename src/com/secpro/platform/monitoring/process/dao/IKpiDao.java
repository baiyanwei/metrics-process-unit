package com.secpro.platform.monitoring.process.dao;

import java.util.List;
import java.util.Map;

import com.secpro.platform.monitoring.process.entity.KpiBean;
/**
 * KPI相关数据库操作
 * @author sxf
 *
 */
public interface IKpiDao {
	/**
	 * 查询对应的kpiID以及标准化脚本
	 * @param cityCode
	 * @param resIP
	 * @return
	 */
	public Map<String, String[]> kpiIDAndRuleQuery(String cityCode,String resIP);
	/**、
	 * kpi类型查询
	 * @param kpiID
	 * @return
	 */
	public String kpiTypeQuery(long kpiID);
	/**
	 * 存储kpi结果
	 * @param snmpList
	 */
	public void rawKpiSave(List<KpiBean> snmpList);
	/**
	 * 查询kpiID以及kpi类型
	 * @param kpiName
	 * @param resID
	 * @return
	 */
	public String[] kpiTypeAndKpiIDQuery(String kpiName,long resID);
	/**
	 * kpi名称查询
	 * @param kpiID
	 * @return
	 */
	public String kpiNameQuery(long kpiID);
}
