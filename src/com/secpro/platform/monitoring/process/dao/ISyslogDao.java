package com.secpro.platform.monitoring.process.dao;

import java.util.List;
import java.util.Map;

import com.secpro.platform.monitoring.process.entity.SyslogBean;
/**
 * syslog数据相关的数据库操作
 * @author sxf
 *
 */
public interface ISyslogDao {
	/**
	 * 加载syslog名称与数据库表字段映射关系
	 * @return
	 */
	public void syslogDBMappingQuery(Map<String,String> dbMapping);
	/**
	 * 加载所有syslog标准化规则
	 * @return
	 */
	public void syslogRuleMappingQuery(Map<String, Map<String, Map<String,String>>> syslogRuleMapping);
	public Map<String,Map<String,String>> syslogRuleMappingQuery(String typeCode);
	/**
	 * 存储标准化后的syslog数据
	 * @param syslogBeans
	 * @param dataDBMapping
	 */
	public void syslogSave(List<SyslogBean> syslogBeans,Map<String,String> dataDBMapping);
	/**
	 * syslog命中时，加载数据库中未计算过命中的syslog信息
	 * @param columnNames
	 * @return
	 */
	public List<String[]> syslogQueryAndUpdate(List<String> columnNames);
	/**
	 * 存储syslog命中结果数据
	 * @param startDate
	 * @param endDate
	 * @param hitResult
	 */
	public void syslogHitSave(String startDate,String endDate, Map<Long, Map<String, Long>> hitResult);
}
