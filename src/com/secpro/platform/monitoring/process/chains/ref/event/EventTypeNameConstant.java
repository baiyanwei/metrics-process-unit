package com.secpro.platform.monitoring.process.chains.ref.event;

public class EventTypeNameConstant {
	// 防火墙SNMP超时事件名称
	public static final String EVENT_TYEP_NAME_FW_TIMEOUT = "fw_timeout";
	// 防火墙SNMP没有对应OID事件名称
	public static final String EVENT_TYEP_NAME_FW_NO_SUCH_OBJECT = "fw_nosuchobject";
	// 基线分数事件
	public static final String EVENT_TYEP_NAME_BASELINE_SCORE = "baseline_score";
	// 基线比对结果事件
	public static final String EVENT_TYEP_NAME_BASELINE_MATCH = "baseline_match";
	// 防火墙策略信息包含事件
	public static final String EVENT_TYEP_NAME_POLICY_CONTAIN = "policy_contain";
	// 防火墙策略信息冲突事件
	public static final String EVENT_TYEP_NAME_POLICY_CONFLICT = "policy_conflict";
	// MCA采集机异常事件
	public static final String EVENT_TYEP_NAME_MCA_ERROR = "mca_error";

}
