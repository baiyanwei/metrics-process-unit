package com.secpro.platform.monitoring.process.chains.ref.standard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.impl.ConfigAndPolicyDao;

/**
 * 配置以及策略信息标准化 对ssh或者telnet方式采集回来的防火墙配置信息进行标准化
 * 
 * @author sxf
 * 
 */
public class ConfigAndPolicyStandard {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(ConfigAndPolicyStandard.class);
	private String values;
	private String originValues;
	private String standardRules;
	private Map<String, String> initRules = new HashMap<String, String>();
	private String configRules;
	private String policyRules;
	private Map<String, String> predefinedService;
	private List<String> allRule = new ArrayList<String>();
	// 规则分隔符，以及标准化结果行分隔符
	private final String splitter = "%%";

	public ConfigAndPolicyStandard(String values, String rules,long resID) {
		this.originValues = values;
		this.values=values;
		this.standardRules = rules;
		loadPredefinedService(resID);
		initRules();
	}

	/**
	 * 加载规则中需要初始化的内容
	 */
	private void initRules() {
		if (Assert.isEmptyString(standardRules)) {
			return;
		}
		// 得到配置信息标准化脚本
		String configRulesRegex = "#configuration#([\\s\\S]*)#configuration#";
		Pattern pattern = Pattern.compile(configRulesRegex);
		Matcher mat = pattern.matcher(standardRules);
		if (mat.find()) {
			configRules = mat.group(1);
		}
		// 得到策略信息标准化脚本
		String policyRulesRegex = "#policy#([\\s\\S]*)#policy#";
		pattern = Pattern.compile(policyRulesRegex);
		mat = pattern.matcher(standardRules);
		if (mat.find()) {
			policyRules = mat.group(1);
		}
		String formatRuleRegex="#formatRule#([\\s\\S]*)#formatRule#";
		pattern = Pattern.compile(formatRuleRegex);
		mat = pattern.matcher(standardRules);
		if(mat.find()){
			String formatRule=mat.group(1);
			if(!Assert.isEmptyString(formatRule)){
				formatStandard(formatRule);
			}
		}
		String allRuleRegex = "#allRule#([\\s\\S]*)#allRule#";
		pattern = Pattern.compile(allRuleRegex);
		mat = pattern.matcher(standardRules);
		if (mat.find()) {
			String allRules = mat.group(1);
			if (!Assert.isEmptyString(allRules)) {
				String[] oneAllRule = allRules.split(splitter);
				for (int i = 0; i < oneAllRule.length; i++) {
					if (!Assert.isEmptyString(oneAllRule[i])) {
						this.allRule.add(oneAllRule[i]);
					}
				}
			}
		}
		
		// 得到初始化参数
		String initRegex = "#init#([\\s\\S]*)#init#";
		String initInfo = "";
		pattern = Pattern.compile(initRegex);
		mat = pattern.matcher(standardRules);
		if (mat.find()) {
			initInfo = mat.group(1);
		}
		if (Assert.isEmptyString(initInfo)) {
			return;
		}
		// 存储初始化参数
		String[] initValues = initInfo.split(splitter);
		String initValueRegex = "(.*?)=(.*?)$";
		pattern = Pattern.compile(initValueRegex);
		for (int i = 0; i < initValues.length; i++) {
			if (Assert.isEmptyString(initValues[i])) {
				continue;
			}
			mat = pattern.matcher(initValues[i]);
			if (mat.find()) {
				if (mat.groupCount() != 2) {
					continue;
				}
				String key = mat.group(1);
				String value = mat.group(2);
				if (Assert.isEmptyString(key) || Assert.isEmptyString(value)) {
					continue;
				}
				initRules.put(key, value);
			}
		}

	}

	private void formatStandard(String formatRule) {
		if(Assert.isEmptyString(formatRule)){
			return;
		}
		String[] rules=formatRule.split(splitter);
		String formatValues=originValues;
		for(int i=0;i<rules.length;i++){
			if(Assert.isEmptyString(rules[i])){
				continue;
			}
			String actionType = StandardUtil.isCustomActionType(rules[i]);
			if (actionType == null) {
				continue;
			} else {
				rules[i] = StandardUtil.removeCustomAction(rules[i]);
				if("H".equals(actionType)){
					this.originValues=formatValues;
				}else if("N".equals(actionType)){
					formatValues=newLineStandard(rules[i],formatValues);
				}else if("A".equals(actionType)){
					formatValues=throwStandard(rules[i],formatValues,1);
				}else if("B".equals(actionType)){
					formatValues=throwStandard(rules[i],formatValues,2);
				}else if("T".equals(actionType)){
					formatValues=throwStandard(rules[i],formatValues,0);
				}else{
					continue;
				}
			}
		}
		this.values=formatValues;
	}

	private String throwStandard(String rule, String values, int flag) {
		if(Assert.isEmptyString(rule)||Assert.isEmptyString(values)){
			return values;
		}
		
		if(flag==0){
			return values.replaceAll(rule, "");
		}else if(flag==1){
			Pattern pattern = Pattern.compile(rule);
			Matcher mat = pattern.matcher(values);
			if (mat.find()) {
				return values.substring(0,mat.start());
			}
		}else if(flag==2){
			Pattern pattern = Pattern.compile(rule);
			Matcher mat = pattern.matcher(values);
			if (mat.find()) {
				return values.substring(mat.end(), values.length());
			}
		}else{
			return values;
		}
		return values;
	}
	

	private String newLineStandard(String rule,String values) {
		if(Assert.isEmptyString(rule)||Assert.isEmptyString(values)){
			return values;
		}
		
		return values.replaceAll(rule, "");
	}

	/**
	 * 根据防火墙类型编码，加载数据库中对应防火墙的预定义服务
	 * 
	 * @param typeCode
	 */
	private void loadPredefinedService(long resID) {
		IConfigAndPolicyDao configAndPolicyDao = new ConfigAndPolicyDao();
		predefinedService = configAndPolicyDao.predefinedServiceQuery(resID);

	}

	/**
	 * 标准化采集回来的防火墙配置信息 返回值[0]为标准化后的配置信息 返回值[1]为标准化的策略信息 [2]为经过格式标准化后的整个配置信息
	 * 
	 * @return
	 */
	public String[] configAndPolicyStandard() {
		if (Assert.isEmptyString(values) || Assert.isEmptyString(standardRules)) {
			return null;
		}
		String[] configAndPolicyResult = new String[3];
		if (!Assert.isEmptyString(configRules)) {
		
			configAndPolicyResult[0] = configStandard();
		}
		if (!Assert.isEmptyString(policyRules)) {
			configAndPolicyResult[1] = policyStandard();
		}
		configAndPolicyResult[2]=originValues;
		return configAndPolicyResult;
	}

	/**
	 * 策略信息标准化
	 * 
	 * @return
	 */
	private String policyStandard() {
		if (Assert.isEmptyString(policyRules)) {
			return null;
		}
		String[] policyRulesArr = policyRules.split(splitter);
		String result = "";
		for (int i = 0; i < policyRulesArr.length; i++) {
			if (Assert.isEmptyString(policyRulesArr[i])) {
				continue;
			}
			String actionType = StandardUtil
					.isCustomActionType(policyRulesArr[i]);
			if (actionType == null) {
				theLogger.debug("action type of the policy rule is empty");
			} else {
				policyRulesArr[i] = StandardUtil
						.removeCustomAction(policyRulesArr[i]);
				if ("R".equals(actionType)) {
					boolean isStandardAll = judgeStandardObject(policyRulesArr[i]);

					if (isStandardAll) {
						policyRulesArr[i] = removeJudgeStandardObject(policyRulesArr[i]);
					}
					result = result
							+ regexStandard(policyRulesArr[i], isStandardAll,
									result, true, true);
				} else if ("S".equals(actionType)) {
					result = sortStandard(policyRulesArr[i], result);

				} else {
					theLogger
							.debug("without this operation method of custom action type");
				}
			}
		}
		return result;
	}

	/**
	 * 移除规则中指定的值
	 * 
	 * @param rule
	 * @return
	 */
	private String removeJudgeStandardObject(String rule) {
		if (Assert.isEmptyString(rule)) {
			return null;
		}
		return rule.substring("#true#".length(), rule.length());
	}

	/**
	 * 判断规则中是否含有指定的值
	 * 
	 * @param rule
	 * @return
	 */
	private boolean judgeStandardObject(String rule) {
		return rule.startsWith("#true#");
	}

	/**
	 * 排序 对标准化后的策略信息，进行排序 支持倒序，以及按id正序，倒序排列策略信息
	 * 以id排列时，标准化后的信息中必须由id字段，否则不能对id进行排序
	 * 
	 * @param rule
	 * @param beforeResult
	 * @return
	 */
	private String sortStandard(String rule, String beforeResult) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(beforeResult)) {
			return null;
		}
		StringBuilder returnResult = new StringBuilder();
		if (StandardConstant.DESC.equals(rule)) {
			String[] resultArr = beforeResult.split(splitter);
			for (int i = resultArr.length - 1; i >= 0; i--) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}
				returnResult.append(resultArr[i] + splitter);
			}
		} else if (StandardConstant.ID_DESC.equals(rule)
				|| StandardConstant.ID_ASC.equals(rule)) {
			String[] resultArr = beforeResult.split(splitter);
			Map<Integer, String> idAndResult = new HashMap<Integer, String>();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}
				String idValue = getNameValue(resultArr[i],
						StandardConstant.ID_NAME);
				if (Assert.isEmptyString(idValue)) {
					theLogger.debug("the ID is empty,can not be sorted by ID");
					return beforeResult;
				}
				if (!idValue.matches("\\d+")) {
					theLogger
							.debug("ID is not a number,can not be sorted by ID");
					return beforeResult;
				}
				int idInt = Integer.parseInt(idValue);
				idAndResult.put(idInt, resultArr[i]);
			}
			Integer[] idSort = new Integer[idAndResult.size()];
			idSort = idAndResult.keySet().toArray(idSort);
			if (StandardConstant.ID_DESC.equals(rule)) {
				idSort = StandardUtil.sortAlgorithm(idSort, 0);
			} else {
				idSort = StandardUtil.sortAlgorithm(idSort, 1);
			}
			for (int j = 0; j < idSort.length; j++) {
				returnResult.append(idAndResult.get(idSort[j]) + splitter);
			}
		} else {
			return beforeResult;
		}
		return returnResult.toString();
	}

	/**
	 * 根据正则表达式标准化数据
	 * 
	 * @param rule
	 * @param isStandardAll
	 * @param beforeResult
	 * @param isSaveOrigin
	 * @param isSaveName
	 * @return
	 */
	private String regexStandard(String rule, boolean isStandardAll,
			String beforeResult, boolean isSaveOrigin, boolean isSaveName) {
		if (Assert.isEmptyString(rule)) {
			theLogger.debug("the rule is empty");
			return null;
		}
		List<String> ruleNames = formatRuleNames(rule);
		if (ruleNames == null || ruleNames.size() == 0) {
			Pattern patt = Pattern.compile(rule);
			Matcher mat = null;
			// 如果为true，标准化整个values
			if (isStandardAll) {
				mat = patt.matcher(values);
				// 为false时，标准化传入的beforeResult
			} else {
				mat = patt.matcher(beforeResult);
			}
			StringBuilder result = new StringBuilder();
			while (mat.find()) {
				result.append(mat.group()).append(splitter);
			}
			return result.toString();

		}
		// 包含有自定义名称时
		rule = removeRuleNames(rule);
		StringBuilder result = new StringBuilder();

		Pattern patt = Pattern.compile(rule);
		Matcher mat = null;
		if (isStandardAll) {
			mat = patt.matcher(values);
		} else {
			mat = patt.matcher(beforeResult);
		}
		while (mat.find()) {

			if (ruleNames.size() != mat.groupCount()) {
				theLogger.debug("number doesn't match");
				return null;
			}
			int endFlag = 0;
			for (int i = 0; i < ruleNames.size(); i++) {
				String name = ruleNames.get(i);
				if (Assert.isEmptyString(name)) {
					continue;
				}
				String value = mat.group(i + 1);
				if (isSaveName) {
					result.append(name + "=(");
					if (null == value
							|| "any".equals(value.trim().toLowerCase())) {
						if (allRule.contains(name)) {
							result.append("any");
						} else if (value != null) {
							result.append(value);

						}
					} else {
						String returnResult = resolveCustomName(name, value);
						if (!Assert.isEmptyString(returnResult)) {
							result.append(returnResult
									.replaceAll(splitter, ","));
						}

					}
					result.append(")");
					endFlag = 1;
				} else {
					if (null == value) {
						theLogger.debug("the value is empty");
						continue;
					}
					String resultValue = resolveCustomName(name, value);
					if (!Assert.isEmptyString(resultValue)) {
						result.append(resultValue);
						endFlag = 1;
					}
				}
			}
			if (isSaveOrigin) {
				result.append("origin=(" + mat.group() + ")");
				endFlag = 1;
			}
			if (!(result.toString().endsWith(splitter)) && endFlag == 1) {

				result.append(splitter);
			}

		}

		return result.toString();
	}

	/**
	 * 结果连接
	 * 
	 * @param originValue
	 * @param connValue
	 * @return
	 */
	private String stringConn(String originValue, String connValue) {
		if (Assert.isEmptyString(originValue)) {
			if (Assert.isEmptyString(connValue)) {
				return null;
			} else {
				return connValue;
			}
		} else {
			if (Assert.isEmptyString(connValue)) {
				return originValue;
			} else {
				return originValue + connValue;
			}
		}
	}

	/**
	 * 解析自定义名称
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	private String resolveCustomName(String name, String value) {
		if (Assert.isEmptyString(name) || Assert.isEmptyString(value)) {
			return null;
		}
		String result = "";
		if (name.indexOf("&") != -1) {
			String[] subName = name.split("&");
			String resultAnd = "";
			for (int j = 0; j < subName.length; j++) {

				if (Assert.isEmptyString(subName[j])) {
					continue;
				}
				String nameMappingRule = initRules.get(subName[j]);
				if (Assert.isEmptyString(nameMappingRule)) {
					continue;
				}
				resultAnd = stringConn(resultAnd,
						getOneNameValue(subName[j], value));
			}
			if (!Assert.isEmptyString(resultAnd)) {
				result = resultAnd;
			}
		} else if (name.indexOf("|") != -1) {
			String[] subName = name.split("\\|");
			String resultOr = "";
			for (int j = 0; j < subName.length; j++) {

				if (Assert.isEmptyString(subName[j])) {
					continue;
				}
				String nameMappingRule = initRules.get(subName[j]);
				if (Assert.isEmptyString(nameMappingRule)) {
					continue;
				}
				resultOr = getOneNameValue(subName[j], value);
				if (!Assert.isEmptyString(resultOr)) {
					result = resultOr;
					break;
				}
			}

		} else {
			return getOneNameValue(name, value);
		}
		return result;
	}

	/**
	 * 根据自定义名称以及自定义名称的规则，标准化数据
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	private String getOneNameValue(String name, String value) {
		if (Assert.isEmptyString(name)) {
			return null;
		}
		String nameMappingRule = initRules.get(name);
		if (Assert.isEmptyString(nameMappingRule)
				|| Assert.isEmptyString(value)) {
			return value;
		}
		String result = "";
		if (StandardConstant.IP_ADDRESS.equals(name)) {
			result = ipAddressStandard(nameMappingRule, value);
		} else if (StandardConstant.RANGE_ADDRESS.equals(name)) {
			result = rangeAddressStandard(nameMappingRule, value);
		} else if (StandardConstant.NET_ADDRESS.equals(name)) {
			result = netAddressStandard(nameMappingRule, value);
		} else if (StandardConstant.RANGE_ADDRESS_EXCEPT.equals(name)) {
			result = rangeAddressExcept(nameMappingRule, value);
		} else if (StandardConstant.NET_ADDRESS_EXCEPT.equals(name)) {
			result = netAddressExcept(nameMappingRule, value);
		} else if (StandardConstant.PREDEFINED_SERVICE.equals(name)) {
			result = predefinedService(nameMappingRule, value);
		} else if (StandardConstant.ICMP_PROTOCOL.equals(name)) {
			result = icmpProtocol(nameMappingRule, value);
		} else if (StandardConstant.OTHER_PROTOCOL.equals(name)) {
			result = otherProtocol(nameMappingRule, value);
		} else if (StandardConstant.TCP_UDP_PROTOCOL.equals(name)) {
			result = tcpAndUdpProtocol(nameMappingRule, value);
		} else {

			String actionType = StandardUtil
					.isCustomActionType(nameMappingRule);
			if (actionType == null) {
				theLogger.debug("action type of the policy rule is empty");
			} else {
				nameMappingRule = StandardUtil
						.removeCustomAction(nameMappingRule);
				if ("R".equals(actionType)) {
					boolean isStandardAllAnd = judgeStandardObject(nameMappingRule);
					if (isStandardAllAnd) {
						nameMappingRule = removeJudgeStandardObject(nameMappingRule);
					}
					result = regexStandard(nameMappingRule, isStandardAllAnd,
							value, false, false);

				} else if ("D".equals(actionType)) {
					result = displaceStandard(nameMappingRule, value, false);

				} else if ("O".equals(actionType)) {
					result = resolveCustomName(nameMappingRule, value);

				} else if ("M".equals(actionType)) {
					result = mappingStandard(nameMappingRule, value);
				} else {
					theLogger
							.debug("without this operation method of custom action type");
				}
			}
		}
		return result;
	}

	/**
	 * tcp、udp协议解析，包含5个子属性：协议名称、源端口起始，源端口结束，目的端口起始，目的端口结束 支持协议端口为单个端口，或者端口段
	 * 标准化后的协议格式为tcp:1-65535:8080 依次为协议名，源端口，目的端口
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String tcpAndUdpProtocol(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			String result = "";
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				result = regexStandard(rule, isStandardAll, value, false, true);

			} else if ("D".equals(actionType)) {
				result = displaceStandard(rule, value, true);

			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
			if (Assert.isEmptyString(result)) {
				return null;
			}
			String[] resultArr = result.split(splitter);
			StringBuilder returnResult = new StringBuilder();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}
				String protocolName = getNameValue(resultArr[i],
						StandardConstant.TCP_UDP_PROTOCOL_NAME);
				String srcPortS = getNameValue(resultArr[i],
						StandardConstant.TCP_UDP_SRC_PORT_START);
				String srcPortE = getNameValue(resultArr[i],
						StandardConstant.TCP_UDP_SRC_PORT_END);
				String dstPortS = getNameValue(resultArr[i],
						StandardConstant.TCP_UDP_DST_PORT_START);
				String dstPortE = getNameValue(resultArr[i],
						StandardConstant.TCP_UDP_DST_PORT_END);
				if (Assert.isEmptyString(protocolName)
						|| Assert.isEmptyString(dstPortS)) {
					continue;
				}
				returnResult.append(protocolName + ":");
				if (Assert.isEmptyString(srcPortS)) {
					returnResult.append("1-65535:");
				} else {
					if (Assert.isEmptyString(srcPortE)) {
						returnResult.append(srcPortS + ":");
					} else {
						returnResult.append(srcPortS + "-" + srcPortE + ":");
					}
				}
				if (Assert.isEmptyString(dstPortE)) {
					returnResult.append(dstPortS);
				} else {
					returnResult.append(dstPortS + "-" + dstPortE);
				}
				returnResult.append(splitter);

			}
			return returnResult.toString();
		}
	}

	/**
	 * 其他协议标准化，支持一个子属性，协议号 标准化后的其他协议如other:17
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String otherProtocol(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			String result = "";
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				result = regexStandard(rule, isStandardAll, value, false, true);

			} else if ("D".equals(actionType)) {
				result = displaceStandard(rule, value, true);

			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
			if (Assert.isEmptyString(result)) {
				return null;
			}
			String[] resultArr = result.split(splitter);
			StringBuilder returnResult = new StringBuilder();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}
				String otherNum = getNameValue(resultArr[i],
						StandardConstant.OTHER_PROTOCOL_NUM);
				if (Assert.isEmptyString(otherNum)) {
					continue;
				}

				returnResult.append(StandardConstant.OTHER_PROTOCOL + ":"
						+ otherNum + splitter);

			}
			return returnResult.toString();
		}
	}

	/**
	 * icmp协议标准化，支持2个子属性，分别为icmp协议类型，icmp协议编码 标准化后的结果如icmp:0:0依次为协议类型，协议编码
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String icmpProtocol(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			String result = "";
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				result = regexStandard(rule, isStandardAll, value, false, true);

			} else if ("D".equals(actionType)) {
				result = displaceStandard(rule, value, true);

			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
			if (Assert.isEmptyString(result)) {
				return null;
			}
			String[] resultArr = result.split(splitter);
			StringBuilder returnResult = new StringBuilder();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}

				String icmpType = getNameValue(resultArr[i],
						StandardConstant.ICMP_TYPE);
				String icmpCode = getNameValue(resultArr[i],
						StandardConstant.ICMP_CODE);
				// 不允许icmp类型或者icmp编码同时为空
				if (Assert.isEmptyString(icmpType)
						&& Assert.isEmptyString(icmpCode)) {
					continue;
				}

				returnResult.append(StandardConstant.ICMP_PROTOCOL + ":"
						+ icmpType + ":" + icmpCode + splitter);

			}
			return returnResult.toString();
		}
	}

	/**
	 * 映射标准化 提供对标准化后的值的映射转换 如设置udp:17，则在设置映射规则后，为udp的值将转换为17
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String mappingStandard(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return value;
		}
		String[] mapping = rule.split("##");
		Map<String, String> newValueMapping = new HashMap<String, String>();
		for (int i = 0; i < mapping.length; i++) {
			int indexColon = mapping[i].indexOf(":");
			if (indexColon == -1) {
				continue;
			}
			String key = mapping[i].substring(0, indexColon);
			String theValue = mapping[i].substring(indexColon + 1,
					mapping[i].length());
			if (Assert.isEmptyString(key) || Assert.isEmptyString(theValue)) {
				continue;
			}
			newValueMapping.put(key, theValue);

		}
		String newValue = newValueMapping.get(value);
		if (Assert.isEmptyString(newValue)) {
			return value;
		}
		return newValue;
	}

	/**
	 * 防火墙预定义服务映射标准化
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String predefinedService(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		if (predefinedService == null || predefinedService.size() == 0) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			if ("M".equals(actionType)) {
				String predefined = predefinedService.get(value);
				if (Assert.isEmptyString(predefined)) {
					return null;
				}
				return predefined;
			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
		}
	}

	/**
	 * 子网排除标准化 支持对某个子网段，排除其中的部分IP地址，IP地址段，以及IP地址子网
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String netAddressExcept(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			String result = "";
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				result = regexStandard(rule, isStandardAll, value, false, true);

			} else if ("D".equals(actionType)) {
				result = displaceStandard(rule, value, true);

			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
			if (Assert.isEmptyString(result)) {
				return null;
			}
			String[] resultArr = result.split(splitter);
			StringBuilder returnResult = new StringBuilder();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}

				String subNet = getNameValue(resultArr[i],
						StandardConstant.SUBNET);
				String mark = getNameValue(resultArr[i],
						StandardConstant.SUBNET_MARK);
				String exceptIps = getNameValue(resultArr[i],
						StandardConstant.SUBNET_EXCEPT);
				if (Assert.isEmptyString(subNet) || Assert.isEmptyString(mark)
						|| Assert.isEmptyString(exceptIps)) {
					continue;
				}
				String subNetAddressExcept = StandardUtil.netAddressExcept(
						subNet, mark, exceptIps.split(","));
				if (!Assert.isEmptyString(subNetAddressExcept)) {
					returnResult.append(subNetAddressExcept + splitter);
				}

			}
			return returnResult.toString();
		}
	}

	/**
	 * ip地址范围排除标准化 支持ip地址段中排除某些IP地址，或者IP地址范围，IP地址子网
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String rangeAddressExcept(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			String result = "";
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				result = regexStandard(rule, isStandardAll, value, false, true);

			} else if ("D".equals(actionType)) {
				result = displaceStandard(rule, value, true);

			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
			if (Assert.isEmptyString(result)) {
				return null;
			}
			String[] resultArr = result.split(splitter);
			StringBuilder returnResult = new StringBuilder();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}

				String ipS = getNameValue(resultArr[i],
						StandardConstant.RANGE_START);
				String ipE = getNameValue(resultArr[i],
						StandardConstant.RANGE_END);
				String exceptIps = getNameValue(resultArr[i],
						StandardConstant.RANGE_EXCEPT);
				if (Assert.isEmptyString(ipS) || Assert.isEmptyString(ipE)
						|| Assert.isEmptyString(exceptIps)) {
					continue;
				}
				String rangeAddressExcept = StandardUtil.rangeAddressExcept(
						ipS, ipE, exceptIps.split(","));
				if (!Assert.isEmptyString(rangeAddressExcept)) {
					returnResult.append(rangeAddressExcept + splitter);
				}

			}
			return returnResult.toString();
		}
	}

	/**
	 * 得到allValue中指定oneValue名称的值
	 * 
	 * @param allValue
	 * @param oneValue
	 * @return
	 */
	private String getNameValue(String allValue, String oneValue) {
		if (Assert.isEmptyString(allValue) || Assert.isEmptyString(oneValue)) {
			return null;
		}
		String regex = oneValue + "=\\((.*?)\\)";
		Pattern pattern = Pattern.compile(regex);
		Matcher mat = pattern.matcher(allValue);
		if (mat.find()) {
			return mat.group(1);
		}
		return null;
	}

	/**
	 * 子网标准化 将给定的子网以及掩码，换算成相应的子网地址段 支持2个子属性，分别为子网和子网掩码 子网掩码支持xxx.xxx.xxx.xxx/24
	 * xxx.xxx.xxx.xxx:24 xxx.xxx.xxx.xxx 子网掩码为255.255.255.0类型
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String netAddressStandard(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			String result = "";
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				result = regexStandard(rule, isStandardAll, value, false, true);

			} else if ("D".equals(actionType)) {
				result = displaceStandard(rule, value, true);

			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
			if (Assert.isEmptyString(result)) {
				return null;
			}
			String[] resultArr = result.split(splitter);
			StringBuilder returnResult = new StringBuilder();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}

				String subNet = getNameValue(resultArr[i],
						StandardConstant.SUBNET);
				String mark = getNameValue(resultArr[i],
						StandardConstant.SUBNET_MARK);
				if (Assert.isEmptyString(subNet) || Assert.isEmptyString(mark)) {
					continue;
				}
				String subNetAddress = StandardUtil.getSubnetAddress(subNet,
						mark, true);
				if (!Assert.isEmptyString(subNetAddress)) {
					returnResult.append(subNetAddress + splitter);
				}

			}
			return returnResult.toString();
		}
	}

	/**
	 * 地址段标准化 支持两个子属性，分别为地址段开始地址，以及地址段结束地址
	 * 标准化后的结果如xxx.xxx.xxx.xxx-xxx.xxx.xxx.xxx
	 * :88888-99999,冒号后为将对应的地址范围转换为IP无点分整数表现形式
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String rangeAddressStandard(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			String result = "";
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				result = regexStandard(rule, isStandardAll, value, false, true);

			} else if ("D".equals(actionType)) {
				result = displaceStandard(rule, value, true);

			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
			if (Assert.isEmptyString(result)) {
				return null;
			}
			String[] resultArr = result.split(splitter);
			StringBuilder returnResult = new StringBuilder();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}

				String ipS = getNameValue(resultArr[i],
						StandardConstant.RANGE_START);
				String ipE = getNameValue(resultArr[i],
						StandardConstant.RANGE_END);
				if (Assert.isEmptyString(ipS) || Assert.isEmptyString(ipE)) {
					continue;
				}
				if (ipS.matches("\\d+.\\d+.\\d+.\\d+")
						&& ipS.matches("\\d+.\\d+.\\d+.\\d+")) {
					long ipSLong = StandardUtil.ipToLong(ipS);
					long ipELong = StandardUtil.ipToLong(ipE);
					returnResult.append(ipS + "-" + ipE + ":" + ipSLong + "-"
							+ ipELong + splitter);

				}
			}
			return returnResult.toString();
		}
	}

	/**
	 * ip地址标准化 标准化后的结果为xxx.xxx.xxx.xxx:88888 冒号后为相应IP地址对应的整数形式的值
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String ipAddressStandard(String rule, String value) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			return null;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			String result = "";
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				result = regexStandard(rule, isStandardAll, value, false, false);

			} else if ("D".equals(actionType)) {
				result = displaceStandard(rule, value, false);

			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
			if (Assert.isEmptyString(result)) {
				return null;
			}
			String[] resultArr = result.split(splitter);
			StringBuilder returnResult = new StringBuilder();
			for (int i = 0; i < resultArr.length; i++) {
				if (Assert.isEmptyString(resultArr[i])) {
					continue;
				}
				if (resultArr[i].matches("\\d+.\\d+.\\d+.\\d+")) {
					long ipLong = StandardUtil.ipToLong(resultArr[i]);
					returnResult.append(resultArr[i] + ":" + ipLong + splitter);

				}
			}
			return returnResult.toString();
		}
	}

	/**
	 * 替换规则中指定位置的值
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private String displaceStandard(String rule, String value,
			boolean isSaveName) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(value)) {
			return null;
		}
		if (value.indexOf("*") != -1) {
			value = value.replaceAll("\\*", "[*]");
		}
		if (value.indexOf("\\") != -1) {
			value = value.replaceAll("\\\\", "\\\\\\\\");
		}
		rule = rule.replace("{0}", value);

		String actionType = StandardUtil.isCustomActionType(rule);
		if (actionType == null) {
			theLogger.debug("action type of the policy rule is empty");
			return value;
		} else {
			rule = StandardUtil.removeCustomAction(rule);
			if ("R".equals(actionType)) {
				boolean isStandardAll = judgeStandardObject(rule);

				if (isStandardAll) {
					rule = removeJudgeStandardObject(rule);
				}
				String result = regexStandard(rule, isStandardAll, value,
						false, isSaveName);
				return result;
			} else {
				theLogger
						.debug("without this operation method of custom action type");
				return null;
			}
		}
	}

	/**
	 * 去掉正则表达式中所有自定义名称
	 * 
	 * @param rule
	 * @return
	 */
	private String removeRuleNames(String rule) {
		if (Assert.isEmptyString(rule)) {
			return null;
		}
		Pattern pattern = Pattern.compile("(#.*?#)");
		Matcher mat = pattern.matcher(rule);
		return mat.replaceAll("");
	}

	/**
	 * 得打自定义正则表达式每个元素的自定义名称
	 * 
	 * @param rule
	 * @return
	 */
	private List<String> formatRuleNames(String rule) {
		if (Assert.isEmptyString(rule)) {
			return null;
		}
		List<String> names = new ArrayList<String>();
		Pattern pattern = Pattern.compile("#(.*?)#");
		Matcher mat = pattern.matcher(rule);
		while (mat.find()) {
			names.add(mat.group(1));
		}

		return names;
	}

	/**
	 * 标准化配置信息
	 * 
	 * @return
	 */
	private String configStandard() {
		if (Assert.isEmptyString(configRules)) {
			return null;
		}
		String[] configRulesArr = configRules.split(splitter);
		String result = "";
		for (int i = 0; i < configRulesArr.length; i++) {
			if (Assert.isEmptyString(configRulesArr[i])) {
				continue;
			}
			if (StandardConstant.CONFIG_SAVE_ALL_VALUES
					.equals(configRulesArr[i])) {
				return values;
			}
			String actionType = StandardUtil
					.isCustomActionType(configRulesArr[i]);
			if (actionType == null) {
				theLogger.debug("action type of the policy rule is empty");
			} else {
				configRulesArr[i] = StandardUtil
						.removeCustomAction(configRulesArr[i]);
				if ("R".equals(actionType)) {
					result = stringConn(
							result,
							regexStandard(configRulesArr[i], true, result,
									false, false));
					System.out.println(configRulesArr[i]);
				} else if ("E".equals(actionType)) {
					result = stringConn(result, result
							+ equalStandard(configRulesArr[i]));
					if (!Assert.isEmptyString(result)) {
						result += splitter;
					}

				} else {
					theLogger
							.debug("without this operation method of custom action type");
				}
			}
		}
		return result;
	}

	/**
	 * 是否包含某字符串，包含则返回此字符串，不包含返回null
	 * 
	 * @param equalValue
	 * @return
	 */
	private String equalStandard(String equalValue) {
		if (Assert.isEmptyString(equalValue)) {
			return null;
		}
		if (values.indexOf(equalValue) != -1) {
			return equalValue;
		}
		return null;
	}

}
