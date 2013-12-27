package com.secpro.platform.monitoring.process.chains.ref.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.standard.StandardConstant;
import com.secpro.platform.monitoring.process.chains.ref.standard.StandardUtil;

/**
 * 基线比对相关操作
 * 
 * @author sxf
 * 
 */
public class BaselineMatch {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(BaselineMatch.class);
	private static final String splitter = "%%";
	private static final String ruleSplitter = "##";

	public static String baselineMatch(String values, String rule,
			int isConfigOrPolicy, String baselineBlackWhite) {
		if (Assert.isEmptyString(values) || Assert.isEmptyString(rule)) {
			return null;
		}
		String[] configOrPolicyValue = values.split(splitter);
		String result = "";
		if (isConfigOrPolicy == 0) {
			// 基线为配置类型
			result = baselineConfigMatch(configOrPolicyValue, rule);
		} else {
			// 基线为策略类型
			result = baselinePolicyMatch(configOrPolicyValue, rule);
		}
		return result;
	}

	/**
	 * 配置信息与配置基线进行比对 比对成功则返回配置信息中匹配基线的值
	 * 
	 * @param configValue
	 * @param rule
	 * @return
	 */
	private static String baselineConfigMatch(String[] configValue, String rule) {
		if (configValue == null || configValue.length == 0
				|| Assert.isEmptyString(rule)) {
			return null;
		}
		String[] oneRule = rule.split(ruleSplitter);
		String actionType = StandardUtil.isCustomActionType(oneRule[0]);
		if (actionType == null) {
			theLogger.debug("action type of the policy rule is empty");
		} else {
			if ("E".equals(actionType)) {

				String baselineValue = StandardUtil
						.removeCustomAction(oneRule[0]);
				for (int i = 0; i < configValue.length; i++) {
					if (Assert.isEmptyString(baselineValue)
							|| Assert.isEmptyString(configValue[i])) {
						return null;
					}
					if (baselineValue.equals(configValue[i])
							|| configValue[i].indexOf(baselineValue) >= 0) {
						return baselineValue;
					}
				}
			} else if ("R".equals(actionType)) {

				String baselineValue = StandardUtil
						.removeCustomAction(oneRule[0]);
				for (int i = 0; i < configValue.length; i++) {
					if (Assert.isEmptyString(baselineValue)
							|| Assert.isEmptyString(configValue[i])) {
						return null;
					}
					Pattern patt = Pattern.compile(baselineValue);
					Matcher mat = patt.matcher(configValue[i]);
					if (mat.find()) {
						if (oneRule.length >= 2 && mat.groupCount() > 0) {
							String actionTypeS = StandardUtil
									.isCustomActionType(oneRule[1]);
							if (actionType == null) {
								theLogger
										.debug("action type of the policy rule is empty");
								return null;
							} else {
								if ("C".equals(actionTypeS)) {
									String baselineValueS = StandardUtil
											.removeCustomAction(oneRule[1]);
									if (Assert.isEmptyString(baselineValueS)) {
										return null;
									}
									if (baselineValueS.startsWith(">")
											|| baselineValueS.startsWith("<")
											|| baselineValueS.startsWith("=")
											|| baselineValueS.startsWith("!")) {
										boolean match;
										if (baselineValueS.startsWith("=", 1)) {
											match = matchResult(mat.group(1),
													baselineValueS.substring(0,
															2),
													baselineValueS.substring(2,
															baselineValueS
																	.length()));
										} else {
											match = matchResult(mat.group(1),
													baselineValueS.substring(0,
															1),
													baselineValueS.substring(1,
															baselineValueS
																	.length()));

										}
										if (match == true) {
											return mat.group();
										}
									}
								}
							}
						} else {
							return mat.group();
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * 根据自定义的比对操作符，将两个值的比对结果返回
	 * 
	 * @param value
	 * @param oper
	 * @param matchValue
	 * @return
	 */
	private static boolean matchResult(String value, String oper,
			String matchValue) {
		if (Assert.isEmptyString(value) || Assert.isEmptyString(oper)
				|| Assert.isEmptyString(matchValue)) {
			return false;
		}
		String floatRegex = "[0-9]+.?[0-9]*";
		boolean valueIsFloat = value.matches(floatRegex);
		boolean thresholdIsFloat = matchValue.matches(floatRegex);

		if (">".equals(oper)) {
			if (valueIsFloat == false || thresholdIsFloat == false) {
				theLogger.debug("match type conflict!");
				return false;
			}
			Float valueF = Float.parseFloat(value);
			Float thresholdF = Float.parseFloat(matchValue);
			return valueF > thresholdF ? true : false;
		} else if ("<".equals(oper)) {
			if (valueIsFloat == false || thresholdIsFloat == false) {
				theLogger.debug("match type conflict!");
				return false;
			}
			Float valueF = Float.parseFloat(value);
			Float thresholdF = Float.parseFloat(matchValue);
			return valueF < thresholdF ? true : false;
		} else if ("=".equals(oper)) {
			if (valueIsFloat == false || thresholdIsFloat == false) {
				theLogger.debug("match type conflict!");
				return false;
			}
			Float valueF = Float.parseFloat(value);
			Float thresholdF = Float.parseFloat(matchValue);
			return valueF == thresholdF ? true : false;
		} else if (">=".equals(oper)) {
			if (valueIsFloat == false || thresholdIsFloat == false) {
				theLogger.debug("match type conflict!");
				return false;
			}
			Float valueF = Float.parseFloat(value);
			Float thresholdF = Float.parseFloat(matchValue);
			return valueF >= thresholdF ? true : false;
		} else if ("<=".equals(oper)) {
			if (valueIsFloat == false || thresholdIsFloat == false) {
				theLogger.debug("match type conflict!");
				return false;
			}
			Float valueF = Float.parseFloat(value);
			Float thresholdF = Float.parseFloat(matchValue);
			return valueF <= thresholdF ? true : false;
		} else if ("==".equals(oper)) {
			return value.equals(matchValue) ? true : false;
		} else if ("!=".equals(oper)) {
			if ("null".equals(matchValue.toLowerCase())) {
				if (!Assert.isEmptyString(value)) {
					return true;
				}
			}
			return (!value.equals(matchValue)) ? true : false;
		} else {
			theLogger.debug("Doesn't recognize the match type");
		}
		return false;
	}

	/**
	 * 策略信息与策略基线进行比对 比对成功则返回策略信息中匹配这条基线的值
	 * 
	 * @param policyValue
	 * @param rule
	 * @return
	 */
	private static String baselinePolicyMatch(String[] policyValue, String rule) {
		if (policyValue == null || policyValue.length == 0
				|| Assert.isEmptyString(rule)) {
			return null;
		}
		String[] oneRule = rule.split(ruleSplitter);
		if (oneRule.length < 2) {
			return null;
		}
		String actionType = StandardUtil.isCustomActionType(oneRule[0]);
		if (actionType == null) {
			theLogger.debug("action type of the policy rule is empty");
		} else {
			if ("V".equals(actionType)) {
				String baselineValue = StandardUtil.removeCustomAction(oneRule[0]);
				if(!Assert.isEmptyString(oneRule[1])){
					int location;
					if((location=getMatchLocation(oneRule[1]))!=0){
						String matchRule=removeMatchLocation(oneRule[1]);
						if(location>0){
							if(location<=policyValue.length){
								int matchResultFlag = PolicyMatchUtil.matchTwoValue(policyValue[location-1], baselineValue, matchRule);
								if (matchResultFlag == 1) {
									String valueAfter = PolicyMatchUtil.getNameValue(policyValue[location-1], StandardConstant.ORIGIN_NAME);
									if (!Assert.isEmptyString(valueAfter)) {
										return valueAfter;
									}
								}
							}
						}else {
							if(Math.abs(location)<=policyValue.length){
								int matchResultFlag = PolicyMatchUtil.matchTwoValue(policyValue[policyValue.length+location], baselineValue, matchRule);
								if (matchResultFlag == 1) {
									String valueAfter = PolicyMatchUtil.getNameValue(policyValue[policyValue.length+location], StandardConstant.ORIGIN_NAME);
									if (!Assert.isEmptyString(valueAfter)) {
										return valueAfter;
									}
								}
							}
						}
					}else{
						for (int i = 0; i < policyValue.length; i++) {
							int matchResultFlag = PolicyMatchUtil.matchTwoValue(
									policyValue[i], baselineValue, oneRule[1]);
							if (matchResultFlag == 1) {
								String valueAfter = PolicyMatchUtil.getNameValue(policyValue[i], StandardConstant.ORIGIN_NAME);
								if (!Assert.isEmptyString(valueAfter)) {
									return valueAfter;
								}

							}
						}
					}
				}
			}
		}

		return null;
	}

	private static String removeMatchLocation(String matchRule) {
		if(Assert.isEmptyString(matchRule)){
			return null;
		}
		
		return matchRule.replaceFirst("#([-]?\\d+)#", "");
	}

	private static int getMatchLocation(String matchRule) {
		if(Assert.isEmptyString(matchRule)){
			return 0;
		}
		String locationRegex="#([-]?\\d+)#";
		Pattern patt=Pattern.compile(locationRegex);
		Matcher mat = patt.matcher(matchRule);
		if(mat.find()){
			String location= mat.group(1);
			return Integer.valueOf(location);
		}
		return 0;
	}
	
}
