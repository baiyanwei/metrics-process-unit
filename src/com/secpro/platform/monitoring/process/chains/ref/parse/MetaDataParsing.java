package com.secpro.platform.monitoring.process.chains.ref.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.utils.DateFormatUtil;

/**
 * 将各种类型的json格式数据，解析成数据处理所需要的属性
 * 
 * @author sxf
 * 
 */
public class MetaDataParsing {
	private static PlatformLogger theLogger = PlatformLogger.getLogger(MetaDataParsing.class);
	// 缓存任务taskCode中的标记符号
	private static final String CATCHE_TASK_MARK = "_c";

	/**
	 * 判断对象是否为JSONObject类型或者JSONArray类型
	 * 
	 * @param content
	 * @return 返回1表示，此数据类型是jsonObject，2表示为jsonArray类型，0表示以上两种类型都不符合
	 */
	public static int isJsonObjORJsonArray(Object content) {
		if(content==null){
			return 0;
		}
		String contentStr = content.toString();
		if (contentStr.startsWith("{")) {
			return 1;
		} else if (contentStr.startsWith("[")) {
			return 2;
		}
		return 0;

	}

	/**
	 * 获得原始数据中的数据处理类型如：ssh,telnet等
	 * 
	 * @param metaData
	 * @param dataTypes
	 * @return
	 */
	public static String getMetaDataType(Object metaData, String dataTypes) {
		if (metaData == null) {
			return null;
		}
		JSONTokener parser = new JSONTokener(metaData.toString());
		try {
			JSONObject jsonObj = new JSONObject(parser);
			if (jsonObj.has(MetaDataConstant.META_BODY_PROPERTY_NAME) == false) {
				return null;
			}
			Object bodyObj = jsonObj.get(MetaDataConstant.META_BODY_PROPERTY_NAME);
			int objType = isJsonObjORJsonArray(bodyObj);
			String[] dataTypesArr = dataTypes.split(",");
			if (objType == 1) {

				JSONObject jsonObjBody = new JSONObject(bodyObj.toString());
				for (int i = 0; i < dataTypesArr.length; i++) {
					if (jsonObjBody.has(dataTypesArr[i])) {
						return dataTypesArr[i];
					}
				}
			} else if (objType == 2) {
				JSONArray jsonArrBody = new JSONArray(bodyObj.toString());
				String firstData = jsonArrBody.getString(0);
				JSONObject firstJsonObj = new JSONObject(firstData);
				for (int i = 0; i < dataTypesArr.length; i++) {
					if (firstJsonObj.has(dataTypesArr[i])) {
						return dataTypesArr[i];
					}
				}
			} else {
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		}
		return null;

	}

	/**
	 * 解析任务类型数据
	 * 
	 * @param data
	 * @param dataType
	 * @return
	 */
	public static Map<String, Object> getTaskRelatedData(JSONObject data,
			String dataType) {
		if (data == null || Assert.isEmptyString(dataType) == true) {
			return null;
		}

		try {
			Map<String, Object> reletedData = new HashMap<String, Object>();
			reletedData
					.put(MetaDataConstant.EXECUTE_DATE,data.getString(MetaDataConstant.META_EXECUTE_AT_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.CITY_CODE,
					data.getString(MetaDataConstant.META_CITYCODE_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.TASK_CODE,
					data.getString(MetaDataConstant.META_MONITOR_ID_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.TARGET_IP,
					data.getString(MetaDataConstant.META_TARGETIP_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.EXECUTE_COST,
					data.getString(MetaDataConstant.META_EXECUTE_COST_PROPERTY_NAME));
			String metaBody = data.getString(MetaDataConstant.META_BODY_PROPERTY_NAME);
			JSONObject metaBodyJson = new JSONObject(metaBody);
			String content = metaBodyJson.getString(dataType);
			JSONObject contentJson = new JSONObject(content);
			reletedData.put(MetaDataConstant.EXECUTE_RESULT,
					contentJson.getString(MetaDataConstant.META_RESULT_PROPERTY_NAME));
			return reletedData;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		}
		return null;
	}
	public static Map<String,Object> getErrorRelatedData(JSONObject data){
		if (data == null) {
			return null;
		}

		try {
			Map<String, Object> reletedData = new HashMap<String, Object>();
			reletedData
					.put(MetaDataConstant.EXECUTE_DATE,data.getString(MetaDataConstant.META_EXECUTE_AT_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.CITY_CODE,
					data.getString(MetaDataConstant.META_CITYCODE_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.TASK_CODE,
					data.getString(MetaDataConstant.META_MONITOR_ID_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.TARGET_IP,
					data.getString(MetaDataConstant.META_TARGETIP_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.EXECUTE_COST,
					data.getString(MetaDataConstant.META_EXECUTE_COST_PROPERTY_NAME));
			String metaBody = data.getString(MetaDataConstant.META_BODY_PROPERTY_NAME);
			JSONObject metaBodyJson = new JSONObject(metaBody);
			String content = metaBodyJson.getString("error");
			reletedData.put(MetaDataConstant.ERROR_DESCRIPTION,content);
			return reletedData;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		}
		return null;
	}
	public static Map<String, Object> getWatchdogRelatedData(JSONObject data) {
		if (data == null) {
			return null;
		}
		try {
			Map<String,Object> reletedData = new HashMap<String, Object>();
			reletedData.put(MetaDataConstant.WATCHDOG_CITY_CODE,data.getString(MetaDataConstant.WATCHDOG_CITYCODE_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.WATCHDOG_IP,data.getString(MetaDataConstant.WATCHDOG_IP_PROPERTY_NAME));
			JSONObject bodyJson=data.getJSONObject(MetaDataConstant.WATCHDOG_BODY_PROPERTY_NAME);
			JSONObject platformJson=bodyJson.getJSONObject(MetaDataConstant.WATCHDOG_PLATFORM_PROPERTY_NAME);
			Map<String,String> executeResult=new HashMap<String,String>();
			JSONObject diskJson=platformJson.getJSONObject(MetaDataConstant.WATCHDOG_DISK_PROPERTY_NAME);
			executeResult.put(MetaDataConstant.WATCHDOG_DISK_USAGE, diskJson.toString());
			JSONObject memoryJson=platformJson.getJSONObject(MetaDataConstant.WATCHDOG_MEMORY_PROPERTY_NAME);
			executeResult.put(MetaDataConstant.WATCHDOG_MEMORY_USAGE, memoryJson.getString(MetaDataConstant.WATCHDOG_MEMORY_USAGE_PROPERTY_NAME));
			JSONObject cpuJson=platformJson.getJSONObject(MetaDataConstant.WATCHDOG_CPU_PROPERTY_NAME);
			executeResult.put(MetaDataConstant.WATCHDOG_CPU_USAGE, cpuJson.getString(MetaDataConstant.WATCHDOG_CPU_USAGE_PROPERTY_NAME));
			JSONObject swapJson=platformJson.getJSONObject(MetaDataConstant.WATCHDOG_SWAP_PROPERTY_NAME);
			executeResult.put(MetaDataConstant.WATCHDOG_SWAP_USAGE, swapJson.getString(MetaDataConstant.WATCHDOG_SWAP_USAGE_PROPERTY_NAME));
			JSONObject logJson=bodyJson.getJSONObject(MetaDataConstant.WATCHDOG_LOG_PROPERTY_NAME);
			executeResult.put(MetaDataConstant.WATCHDOG_LOG_INCREASE, logJson.getString(MetaDataConstant.WATCHDOG_LOG_INCREASE_PROPERTY_NAME));
			executeResult.put(MetaDataConstant.WATCHDOG_LOG_ERROR, logJson.getString(MetaDataConstant.WATCHDOG_LOG_ERROR_PROPERTY_NAME));
			executeResult.put(MetaDataConstant.WATCHDOG_PROCESS, bodyJson.getString(MetaDataConstant.WATCHDOG_PROCESS_PROPERTY_NAME));
			reletedData.put(MetaDataConstant.WATCHDOG_EXECUTE_RESULT, executeResult);
			return reletedData;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		}
		return null;
	}

	/**
	 * 将JSONObject类型转换为Map类型
	 * 
	 * @param jsonObj
	 * @return
	 */
	public static Map<String, String> jsonObjectToMap(JSONObject jsonObj) {
		if (jsonObj == null) {
			return null;
		}
		try {
			String[] keys = JSONObject.getNames(jsonObj);
			Map<String, String> executeResult = new HashMap<String, String>();
			if (keys != null && keys.length > 0) {
				for (int i = 0; i < keys.length; i++) {

					executeResult.put(keys[i], jsonObj.getString(keys[i]));

				}
			}
			return executeResult;
		} catch (JSONException e) {
			//e.printStackTrace();
			theLogger.exception(e);
		}
		return null;
	}

	/**
	 * 解析类型为syslog的数据
	 * 
	 * @param data
	 * @return
	 */
	public static List<Map<String, Object>> getSyslogRelatedData(JSONObject data) {

		if (data == null) {
			return null;
		}

		try {

			List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

			String cityCode = data.getString(MetaDataConstant.META_CITYCODE_PROPERTY_NAME);
			String metaBody = data.getString(MetaDataConstant.META_BODY_PROPERTY_NAME);
			if (Assert.isEmptyString(metaBody) == false
					&& isJsonObjORJsonArray(metaBody) == 2) {
				JSONArray metaBodyJsonArr = new JSONArray(metaBody);
				for (int i = 0; i < metaBodyJsonArr.length(); i++) {
					Map<String, Object> reletedData = new HashMap<String, Object>();
					reletedData.put(MetaDataConstant.CITY_CODE, cityCode);
					String metricData = metaBodyJsonArr.getString(i);
					if (Assert.isEmptyString(metricData) == false) {
						JSONObject metricDataJson = new JSONObject(metricData);
						JSONObject syslogJsonObj = metricDataJson
								.getJSONObject("syslog");
						if (syslogJsonObj != null) {
							reletedData.put(MetaDataConstant.TARGET_IP, syslogJsonObj
									.getString(MetaDataConstant.META_SYSLOG_TARGETIP_PROPERTY_NAME));
							reletedData
									.put(MetaDataConstant.EXECUTE_DATE,
											syslogJsonObj
													.getString(MetaDataConstant.META_SYSLOG_RECEIVE_DATA_PROPERTY_NAME));
							reletedData
									.put(MetaDataConstant.EXECUTE_RESULT,
											jsonObjectToMap(syslogJsonObj
													.getJSONObject(MetaDataConstant.META_EXECUTECOMMAND_PROPERTY_NAME)));
							if (syslogJsonObj.has(MetaDataConstant.META_SYSLOG_ORIGIN_PROPERTY_NAME)) {
								reletedData.put(MetaDataConstant.ORIGIN_SYSLOG, syslogJsonObj
										.getString(MetaDataConstant.META_SYSLOG_ORIGIN_PROPERTY_NAME));
							}
						}
					}
					resultList.add(0, reletedData);
				}
				return resultList;
			}
		} catch (JSONException e) {
			//e.printStackTrace();
			theLogger.exception(e);
		}
		return null;
	}

	/**
	 * 是否为缓存任务taskCode
	 * 
	 * @param taskCode
	 * @return
	 */
	public static boolean isCacheTask(String taskCode) {
		return taskCode.endsWith(CATCHE_TASK_MARK);
	}
	/**
	 * 判断此数据是否为error类型数据
	 * @param metaData
	 * @return
	 */
	public static boolean isErrorDataType(Object metaData) {
		String dataType = getMetaDataType(metaData, "error");
		if ("error".equals(dataType)) {
			return true;
		}
		return false;
	}
	/**
	 * 去掉缓存标记
	 * @param cacheTaskCode
	 * @return
	 */
	public static String changeCacheTaskCode(String cacheTaskCode){
		
		return cacheTaskCode.substring(0, cacheTaskCode.length()-CATCHE_TASK_MARK.length());
	}
	/**
	 * 判断数据是否为jsonObject类型
	 * @param value
	 * @return
	 */
	public static boolean isJsonObj(String value){
		if(Assert.isEmptyString(value)){
			return false;
		}
		if (value.startsWith("{")&&value.endsWith("}")) {
			return true;
		}
		return false;
	}
}
