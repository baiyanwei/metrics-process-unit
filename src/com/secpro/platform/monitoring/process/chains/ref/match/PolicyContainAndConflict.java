package com.secpro.platform.monitoring.process.chains.ref.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.standard.StandardConstant;

/**
 * 策略信息中包含以及冲突比对
 * 
 * @author sxf
 * 
 */
public class PolicyContainAndConflict {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(PolicyContainAndConflict.class);
	private String containAndConflictRules;
	private String policyValues;
	private String containRules;
	private String conflictRules;
	private String splitter = "%%";
	private String inlineSplitter="::";
	private static final String FORMER_NAME = "former";
	private static final String LATTER_NAME = "latter";
	private static final String ALL_NAME = "all";
	private static final int FORMER_SEQUENCE = 1;
	private static final int LATTER_SEQUENCE = 2;
	private static final int ALL_SEQUENCE = 3;

	public PolicyContainAndConflict(String policyValues, String rules) {
		this.policyValues = policyValues;
		this.containAndConflictRules = rules;
		initRules();
	}

	/**
	 * 初始化自定义冲突以及包含的规则
	 */
	private void initRules() {
		if (Assert.isEmptyString(containAndConflictRules)) {
			return;
		}
		String containRegex = "#contain#(.*?)#contain#";
		Pattern pattern = Pattern.compile(containRegex);
		Matcher mat = pattern.matcher(containAndConflictRules);
		if (mat.find()) {
			containRules = mat.group(1);
		}
		String conflictRulesRegex = "#conflict#(.*?)#conflict#";
		pattern = Pattern.compile(conflictRulesRegex);
		mat = pattern.matcher(containAndConflictRules);
		if (mat.find()) {
			conflictRules = mat.group(1);
		}

	}

	/**
	 * 策略信息冲突与包含检查
	 * 
	 * @return
	 */
	public String[] policyContainAndConflict() {
		if (Assert.isEmptyString(policyValues)
				|| Assert.isEmptyString(containAndConflictRules)) {
			return null;
		}
		String[] result = new String[2];
		if (Assert.isEmptyString(containRules)) {
			result[0] = null;
		} else {
			result[0] = containMatch();
		}
		if (Assert.isEmptyString(conflictRules)) {
			result[1] = null;
		} else {
			result[1] = conflictMatch();
		}
		return result;
	}

	/**
	 * 策略冲突检查
	 * 
	 * @return 返回值不为空，即代表策略信息中有冲突策略
	 */
	private String conflictMatch() {
		String[] conflictRulesArr = conflictRules.split(splitter);
		String result = "";
		for (int i = 0; i < conflictRulesArr.length; i++) {
			if (Assert.isEmptyString(conflictRulesArr[i])) {
				continue;
			}
			int sequence = judgeMatchSequence(conflictRulesArr[i]);
			if (sequence == 0) {
				continue;
			}
			conflictRulesArr[i] = removeSequence(conflictRulesArr[i]);
			result = policyMatch(conflictRulesArr[i], sequence);
		}
		return result;
	}

	/**
	 * 策略包含检查
	 * 
	 * @return 返回值不为空，即代表策略信息中有包含策略
	 */
	private String containMatch() {
		String[] containRulesArr = containRules.split(splitter);
		String result = "";
		for (int i = 0; i < containRulesArr.length; i++) {
			if (Assert.isEmptyString(containRulesArr[i])) {
				continue;
			}
			int sequence = judgeMatchSequence(containRulesArr[i]);
			if (sequence == 0) {
				continue;
			}
			containRulesArr[i] = removeSequence(containRulesArr[i]);
			result = policyMatch(containRulesArr[i], sequence);
		}
		return result;
	}

	/**
	 * 移除规则中自定义的比对顺序关键字
	 * 
	 * @param rule
	 * @return
	 */
	private String removeSequence(String rule) {
		String sequenceRegex = "#(.*?)#";
		Pattern pattern = Pattern.compile(sequenceRegex);
		Matcher mat = pattern.matcher(rule);
		if (mat.find()) {
			return rule.substring(mat.end(), rule.length());
		}
		return rule;
	}

	/**
	 * 策略包含或冲突比对
	 * 
	 * @param rule
	 *            比对规则
	 * @param sequence
	 *            比对顺序
	 * 
	 * @return
	 */
	private String policyMatch(String rule, int sequence) {
		if (Assert.isEmptyString(rule) || Assert.isEmptyString(policyValues)) {
			return null;
		}
		String[] values = policyValues.split(splitter);
		// 前比后
		if (sequence == 1) {
			return formerMatch(rule, values);
		} else if (sequence == 2) {
			return latterMatch(rule, values);
		} else {
			return allMatch(rule, values);
		}

	}

	/**
	 * 全比对，即策略中前比较后，以及后比较前
	 * 
	 * @param rule
	 * @param values
	 * @return
	 */
	private String allMatch(String rule, String[] values) {

		StringBuilder result = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (Assert.isEmptyString(values[i])) {
				continue;
			}
			int valueBeforeFlag = 0;
			for (int j = 0; j < values.length; j++) {
				if (Assert.isEmptyString(values[j])) {
					continue;
				}
				if (i == j) {
					continue;
				}
				int matchResultFlag = PolicyMatchUtil.matchTwoValue(values[i],
						values[j], rule);
				if (matchResultFlag == 1) {
					String valueAfter = PolicyMatchUtil.getNameValue(values[j],
							StandardConstant.ORIGIN_NAME);
					if (Assert.isEmptyString(valueAfter)) {
						continue;
					}
					if (valueBeforeFlag == 0) {
						String valueBefore = PolicyMatchUtil.getNameValue(
								values[i], StandardConstant.ORIGIN_NAME);
						if (Assert.isEmptyString(valueBefore)) {
							break;
						}
						result.append(valueBefore + inlineSplitter + valueAfter);
						valueBeforeFlag = 1;
					} else {
						result.append(inlineSplitter + valueAfter);
					}
				}
			}
			if (result.length() > 0 && !(result.toString().endsWith(splitter))) {
				result.append(splitter);
			}
		}
		return result.toString();
	}

	/**
	 * 后比对，即后一条策略与前一条策略比对
	 * 
	 * @param rule
	 * @param values
	 * @return
	 */
	private String latterMatch(String rule, String[] values) {
		StringBuilder result = new StringBuilder();
		for (int i = 1; i < values.length; i++) {
			if (Assert.isEmptyString(values[i])) {
				continue;
			}
			int valueBeforeFlag = 0;
			for (int j = 0; j < i; j++) {
				if (Assert.isEmptyString(values[j])) {
					continue;
				}
				int matchResultFlag = PolicyMatchUtil.matchTwoValue(values[i],
						values[j], rule);
				if (matchResultFlag == 1) {
					String valueAfter = PolicyMatchUtil.getNameValue(values[j],
							StandardConstant.ORIGIN_NAME);
					if (Assert.isEmptyString(valueAfter)) {
						continue;
					}
					if (valueBeforeFlag == 0) {
						String valueBefore = PolicyMatchUtil.getNameValue(
								values[i], StandardConstant.ORIGIN_NAME);
						if (Assert.isEmptyString(valueBefore)) {
							break;
						}
						result.append(valueBefore + inlineSplitter + valueAfter);
						valueBeforeFlag = 1;
					} else {
						result.append(inlineSplitter + valueAfter);
					}
				}
			}
			if (result.length() > 0 && !(result.toString().endsWith(splitter))) {
				result.append(splitter);
			}
		}
		return result.toString();
	}

	/**
	 * 前比对，即前一条策略与后一条策略进行比对
	 * 
	 * @param rule
	 * @param values
	 * @return
	 */
	private String formerMatch(String rule, String[] values) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < values.length - 1; i++) {
			if (Assert.isEmptyString(values[i])) {
				continue;
			}
			int valueBeforeFlag = 0;
			for (int j = i + 1; j < values.length; j++) {
				if (Assert.isEmptyString(values[j])) {
					continue;
				}
				int matchResultFlag = PolicyMatchUtil.matchTwoValue(values[i],
						values[j], rule);
				if (matchResultFlag == 1) {
					String valueAfter = PolicyMatchUtil.getNameValue(values[j],
							StandardConstant.ORIGIN_NAME);
					if (Assert.isEmptyString(valueAfter)) {
						continue;
					}
					if (valueBeforeFlag == 0) {
						String valueBefore = PolicyMatchUtil.getNameValue(
								values[i], StandardConstant.ORIGIN_NAME);
						if (Assert.isEmptyString(valueBefore)) {
							break;
						}
						result.append(valueBefore + inlineSplitter + valueAfter);
						valueBeforeFlag = 1;
					} else {
						result.append(inlineSplitter + valueAfter);
					}
				}
			}
			if (result.length() > 0 && !(result.toString().endsWith(splitter))) {
				result.append(splitter);
			}
		}
		return result.toString();
	}

	/**
	 * 判断规则中自定义比对顺序
	 * 
	 * @param rule
	 * @return
	 */
	private int judgeMatchSequence(String rule) {
		if (Assert.isEmptyString(rule)) {
			return 0;
		}
		String sequenceRegex = "#(.*?)#";
		String sequence = "";
		Pattern pattern = Pattern.compile(sequenceRegex);
		Matcher mat = pattern.matcher(rule);
		if (mat.find()) {
			sequence = mat.group(1);
		}
		if (Assert.isEmptyString(sequence)) {
			return 0;
		}
		if (FORMER_NAME.equals(sequence)) {
			return FORMER_SEQUENCE;
		} else if (LATTER_NAME.equals(sequence)) {
			return LATTER_SEQUENCE;
		} else if (ALL_NAME.equals(sequence)) {
			return ALL_SEQUENCE;
		} else {
			return 0;
		}

	}

}
