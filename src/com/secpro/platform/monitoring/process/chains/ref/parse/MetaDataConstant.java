package com.secpro.platform.monitoring.process.chains.ref.parse;

/**
 * 元数据解析相关常量
 * 
 * @author sxf
 * 
 */
public class MetaDataConstant {

	// public static final String META_TIMESTAMP_PROPERTY_NAME = "timestamp";
	// 元数据中任务调度ID
	public static final String META_SCHEDULE_ID_PROPERTY_NAME = "sid";
	public static final String META_TASK_ID_PROPERTY_NAME = "tid";
	// 元数据结果体
	public static final String META_BODY_PROPERTY_NAME = "body";
	// public static final String META_MID_PROPERTY_NAME = "mid";
	public static final String META_EXECUTE_AT_PROPERTY_NAME = "ea";
	public static final String META_EXECUTE_COST_PROPERTY_NAME = "ec";
	// 元数据中目的IP地址
	public static final String META_TARGETIP_PROPERTY_NAME = "tip";
	public static final String META_EXECUTECOMMAND_PROPERTY_NAME = "s";
	// 元数据中任务执行结果
	public static final String META_RESULT_PROPERTY_NAME = "c";
	public static final String META_SYSLOG_ORIGIN_PROPERTY_NAME = "o";
	public static final String META_SYSLOG_TARGETIP_PROPERTY_NAME = "ip";
	public static final String META_SYSLOG_RECEIVE_DATA_PROPERTY_NAME = "ct";
	public static final String META_CITYCODE_PROPERTY_NAME = "city_code";
	public static final String CITY_CODE = "cityCode";
	// 解析后任务调度ID
	public static final String SCHEDULE_ID = "scheduleID";
	public static final String TASK_ID = "taskID";
	// 解析后目的IP地址
	public static final String TARGET_IP = "targetIP";
	public static final String ERROR_DESCRIPTION = "errorDesc";
	public static final String EXECUTE_RESULT = "executeResult";
	public static final String ORIGIN_SYSLOG = "originSyslog";
	public static final String EXECUTE_DATE = "executeAt";
	public static final String EXECUTE_COST = "executeCost";
	// watchdog元数据解析
	public static final String WATCHDOG_DATE_PROPERTY_NAME = "time";
	public static final String WATCHDOG_CITYCODE_PROPERTY_NAME = "citycode";
	public static final String WATCHDOG_BODY_PROPERTY_NAME = "body";
	public static final String WATCHDOG_IP_PROPERTY_NAME = "lhost";
	public static final String WATCHDOG_PLATFORM_PROPERTY_NAME = "pf";
	public static final String WATCHDOG_DISK_PROPERTY_NAME = "disk";
	public static final String WATCHDOG_MEMORY_PROPERTY_NAME = "mem";
	public static final String WATCHDOG_MEMORY_USAGE_PROPERTY_NAME = "utiliMem";
	public static final String WATCHDOG_CPU_PROPERTY_NAME = "cpu";
	public static final String WATCHDOG_CPU_USAGE_PROPERTY_NAME = "total";
	public static final String WATCHDOG_SWAP_PROPERTY_NAME = "swap";
	public static final String WATCHDOG_SWAP_USAGE_PROPERTY_NAME = "utiliSwap";
	public static final String WATCHDOG_LOG_PROPERTY_NAME = "log";
	public static final String WATCHDOG_LOG_INCREASE_PROPERTY_NAME = "loginc";
	public static final String WATCHDOG_LOG_ERROR_PROPERTY_NAME = "logerr";
	public static final String WATCHDOG_OPERATION_PROPERTY_NAME = "op";
	public static final String WATCHDOG_PROCESS_PROPERTY_NAME = "ps";
	public static final String WATCHDOG_CITY_CODE = "cityCode";
	public static final String WATCHDOG_IP = "ip";
	public static final String WATCHDOG_DISK_USAGE = "mca_disk_usage";
	public static final String WATCHDOG_MEMORY_USAGE = "mca_memory_usage";
	public static final String WATCHDOG_CPU_USAGE = "mca_cpu_usage";
	public static final String WATCHDOG_SWAP_USAGE = "mca_swap_usage";
	public static final String WATCHDOG_LOG_INCREASE = "mca_log_increase";
	public static final String WATCHDOG_LOG_ERROR = "mca_log_error";
	public static final String WATCHDOG_PROCESS = "mca_process_exist";
	public static final String WATCHDOG_EXECUTE_RESULT = "executeResult";
	
	public static final String RESOURCE_ID="resID";
}
