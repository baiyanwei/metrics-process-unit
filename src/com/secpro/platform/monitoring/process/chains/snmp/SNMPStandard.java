package com.secpro.platform.monitoring.process.chains.snmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.standard.StandardUtil;
import com.secpro.platform.monitoring.process.chains.ref.task.TaskCompleted;
import com.secpro.platform.monitoring.process.dao.IKpiDao;
import com.secpro.platform.monitoring.process.dao.impl.KpiDao;
import com.secpro.platform.monitoring.process.utils.CollectionUtil;

/**
 * 对SNMP数据执行结果进行标准化处理 并更新任务状态等相关信息
 * 
 * @author sxf
 * 
 */
public class SNMPStandard implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SNMPStandard.class);
	private int chainID = 0;

	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("snmp dataProcess chain ID: " + getChainID());
		if (rawData == null) {
			theLogger.error("invalid rawData in SNMP data processing.");
			return null;
		}
		if (!rawData.getClass().equals(HashMap.class)) {
			theLogger.error("need type of HashMap in SNMP data processing.");
			return null;
		}
		Map<String, Object> snmpData = (Map<String, Object>) rawData;
		String taskCode = (String) snmpData.get(MetaDataConstant.TASK_CODE);
		if (Assert.isEmptyString(taskCode)) {
			theLogger.error("the task code of snmp data is empty");
			return null;
		}
		String executeDate = (String) snmpData
				.get(MetaDataConstant.EXECUTE_DATE);
		// 更新任务状态
		setTaskStatus(taskCode, executeDate);
		String cityCode = (String) snmpData.get(MetaDataConstant.CITY_CODE);
		String targetIP = (String) snmpData.get(MetaDataConstant.TARGET_IP);
		if (Assert.isEmptyString(cityCode) || Assert.isEmptyString(targetIP)) {
			theLogger.error("city code or target IP is empty.");
			return null;
		}
		Map<String, String> resultMapping = (Map<String, String>) snmpData
				.get(MetaDataConstant.EXECUTE_RESULT);
		if (resultMapping == null || resultMapping.size() == 0) {
			theLogger.error("the snmp execute result are empty!");
			return null;
		}
		// 查询kpiID以及对应的标准化规则
		Map<String, String[]> kpiIDAndRules = loadKpiIDAndRule(cityCode,
				targetIP);
		if (kpiIDAndRules == null || kpiIDAndRules.size() == 0) {
			theLogger.error("kpi id or snmp rule is empty.");
			return null;
		}
		List deleteList = new ArrayList();
		for (String snmpKey : resultMapping.keySet()) {
			if (Assert.isEmptyString(snmpKey)) {
				deleteList.add(snmpKey);
				continue;
			}
			String snmpValue = resultMapping.get(snmpKey);
			if (Assert.isEmptyString(snmpValue)) {
				resultMapping.put(snmpKey, "nosuchobject");
			}
			String[] idAndRule = kpiIDAndRules.get(snmpKey);
			if (idAndRule == null || idAndRule.length != 2
					|| Assert.isEmptyString(idAndRule[0])) {
				deleteList.add(snmpKey);
				theLogger.debug("haven't kpi id of the snmp data");
				continue;
			}
			if (!"nosuchobject".equals(snmpValue)
					&& !"timeout".equals(snmpValue)
					&& !Assert.isEmptyString(idAndRule[1])) {
				// 对snmp执行结果进行标准化
				snmpValue = snmpStandard(snmpValue, idAndRule[1]);
				if (Assert.isEmptyString(snmpValue)) {
					snmpValue = "nosuchobject";
				}
			}
			// 将kpiID与执行结果对应，kpiID为String类型
			resultMapping.put(idAndRule[0], snmpValue);
			deleteList.add(snmpKey);
		}
		if (deleteList.size() > 0) {
			CollectionUtil.mapRemove(resultMapping, deleteList);
		}
		return snmpData;
	}

	/**
	 * SNMP数据标准化 支持R,P自定义类型 R:正则标准化P:计算百分比标准化
	 * 
	 * @param snmpValue
	 * @param rules
	 * @return
	 */
	private String snmpStandard(String snmpValue, String rules) {
		String[] rule = rules.split("\\^");
		for (int i = 0; i < rule.length; i++) {
			if (Assert.isEmptyString(rule[i])) {
				continue;
			}
			String actionType = StandardUtil.isCustomActionType(rule[i]);
			if (actionType == null) {
				snmpValue = regexFormat(snmpValue, rule[i]);
			} else {
				rule[i] = rule[i].substring(3, rule[i].length());
				if ("R".equals(actionType)) {
					snmpValue = regexFormat(snmpValue, rule[i]);
				} else if ("P".equals(actionType)) {
					snmpValue = mathFormat(snmpValue, rule[i]);
				} else {
					theLogger
							.debug("without this operation method of custom action type");
				}
			}
		}
		return snmpValue;
	}

	/**
	 * 百分比标准化
	 * 
	 * @param values
	 * @param mathOperation
	 * @return
	 */
	private String mathFormat(String values, String mathOper) {
		if (Assert.isEmptyString(values) || Assert.isEmptyString(mathOper)) {
			return null;
		}
		String[] value = values.split("\\^");
		if (value.length < 2) {
			return values;
		}
		String floatRegex = "[0-9]+.?[0-9]+";
		if ("/".equals(mathOper)) {
			if (value[0].matches(floatRegex) && value[1].matches(floatRegex)) {
				float value1 = Float.parseFloat(value[0]);
				float value2 = Float.parseFloat(value[1]);
				if (value2 != 0) {
					return String.valueOf((float) Math.round(value1 / value2
							* 10000) / 100);
				}
			}
		}
		return values;
	}

	/**
	 * 正则标准化
	 * 
	 * @param value
	 * @param regex
	 * @return
	 */
	private String regexFormat(String value, String regex) {
		if (Assert.isEmptyString(value) || Assert.isEmptyString(regex)) {
			return null;
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher match = pattern.matcher(value);
		if (match.find()) {
			StringBuilder afterRegex = new StringBuilder();
			for (int i = 1; i <= match.groupCount(); i++) {
				if (i == match.groupCount()) {
					afterRegex.append(match.group(i));
					break;
				}
				afterRegex.append(match.group(i)).append("^");
			}
			return afterRegex.toString();
		}
		return value;
	}

	/**
	 * 查询此资源对应的KPI相关数据
	 * 
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	private Map<String, String[]> loadKpiIDAndRule(String cityCode,
			String targetIP) {
		IKpiDao kpiDao = new KpiDao();
		return kpiDao.kpiIDAndRuleQuery(cityCode, targetIP);
	}

	@Override
	public void setChainID(int chainID) {
		this.chainID = chainID;

	}

	@Override
	public int getChainID() {
		// TODO Auto-generated method stub
		return this.chainID;
	}

	/**
	 * 更新任务状态相关信息
	 * 
	 * @param taskCode
	 * @param executeDate
	 */
	private void setTaskStatus(String taskCode, String executeDate) {
		Thread setTaskStatus = new TaskCompleted(taskCode, executeDate);
		setTaskStatus.start();
	}

}