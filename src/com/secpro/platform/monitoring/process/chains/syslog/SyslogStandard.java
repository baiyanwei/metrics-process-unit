package com.secpro.platform.monitoring.process.chains.syslog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;
import com.secpro.platform.monitoring.process.services.SyslogStandardRuleService;
import com.secpro.platform.monitoring.process.utils.CollectionRemoveUtil;
/**
 * 数据标准化
 * 对syslog数据结果进行第二次标准化处理
 * @author sxf
 *
 */
public class SyslogStandard implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SyslogStandard.class);
	private int chainID = 0;
	// 时间标准化对应名称
	private final String DATE_PROPERTY_NAME = "date";

	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("syslog dataProcess chain ID: " + getChainID()
				+ " name:" + this.getClass().getName());
		if (rawData == null) {
			theLogger.error("invalid rawData in syslog data processing.");
			return null;
		}
		if (rawData.getClass().equals(ArrayList.class) == false) {
			theLogger
					.error("need type of ArrayList in syslog data processing.");
			return null;
		}
		List<Map<String, Object>> syslogDatas = (List) rawData;
		if(syslogDatas.size()==0){
			return null;
		}
		SyslogStandardRuleService syslogRuleService = ServiceHelper
				.findService(SyslogStandardRuleService.class);
		if (syslogRuleService == null) {
			theLogger
					.error("syslog standard need the SyslogStandardRuleService.");
			return null;
		}
		Map<String, Map<String, Map<String, String>>> ruleMapping = syslogRuleService
				.get_ruleMapping();
		if (ruleMapping == null || ruleMapping.size() == 0) {
			theLogger
					.debug("rule mapping is null,don't need standard the syslog data");
			return rawData;
		}
		
		List<Map<String, Object>> deleteList = new ArrayList();
		for (Map<String, Object> syslogData : syslogDatas) {

			long resID = (Long) syslogData.get(MetaDataConstant.RESOURCE_ID);
			if (resID==0L) {
				deleteList.add(syslogData);
				theLogger.error("res id is empty!");
				continue;
			}
			String typeCode = getTypeCode(resID);
			if (Assert.isEmptyString(typeCode)) {
				deleteList.add(syslogData);
				theLogger.error("type code is empty!");
				continue;
			}
			Object result = syslogData.get(MetaDataConstant.EXECUTE_RESULT);
			if (result == null) {
				continue;
			}
			Map<String, Map<String, String>> ruleNameMapping = ruleMapping
					.get(typeCode);
			if (ruleNameMapping == null || ruleNameMapping.size() == 0) {
				continue;
			}
			syslogData.put(MetaDataConstant.EXECUTE_RESULT,
					syslogStandard(ruleNameMapping, result));

		}
		if (deleteList.size() > 0) {
			CollectionRemoveUtil.listRemove(syslogDatas, deleteList);
		}
		return syslogDatas;
	}

	/**
	 * syslog标准化
	 * 
	 * @param ruleNameMapping
	 * @param result
	 * @return
	 */
	private Object syslogStandard(
			Map<String, Map<String, String>> ruleNameMapping, Object result) {
		Map<String, String> resultMapping = (Map<String, String>) result;
		for (String key : resultMapping.keySet()) {
			Map<String, String> ruleMapping = ruleNameMapping.get(key);
			if (ruleMapping == null || ruleMapping.size() == 0) {
				continue;
			}
			if (DATE_PROPERTY_NAME.equals(key)) {
				String dateRule = ruleMapping.get("1");
				if (!Assert.isEmptyString(dateRule)) {
					resultMapping.put(key,
							dateStandard(resultMapping.get(key), dateRule));

				}
				continue;
			}
			String value = resultMapping.get(key);
			String ruleValue = ruleMapping.get(value);
			if (!Assert.isEmptyString(ruleValue)) {
				resultMapping.put(key, ruleValue);
			}
		}
		return resultMapping;
	}

	/**
	 * 对syslog日期数据进行标准化，转换为yyyyMMddHHmmss格式
	 * 
	 * @param dateBefore
	 * @param dateRule
	 * @return
	 */
	private String dateStandard(String dateBefore, String dateRule) {
		try {

			if (Assert.isEmptyString(dateBefore)) {
				return dateBefore;
			}

			String dateAfter = null;

			if (dateRule.indexOf("y") == -1) {

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(new Date());
				int year = calendar.get(Calendar.YEAR);
				SimpleDateFormat format = new SimpleDateFormat(dateRule,
						Locale.US);

				Date dateD = format.parse(dateBefore);
				SimpleDateFormat simpeleDataFormat = new SimpleDateFormat(
						"MMddHHmmss");
				String dateS1 = simpeleDataFormat.format(dateD);
				dateAfter = year + dateS1;
				return dateAfter;

			} else {
				SimpleDateFormat format = new SimpleDateFormat(dateRule,
						Locale.US);

				Date dateD = format.parse(dateBefore);
				SimpleDateFormat simpeleDataFormat = new SimpleDateFormat(
						"yyyyMMddHHmmss");
				dateAfter = simpeleDataFormat.format(dateD);
				return dateAfter;

			}
		} catch (Exception e) {
			// e.printStackTrace();
			theLogger.exception(e);
		}
		return dateBefore;
	}

	/**
	 * 根据参数查询对应的typeCode设备型号编码
	 * 
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	private String getTypeCode(long resID) {
		IResourceDao resDao = new ResDao();
		return resDao.typeCodeQuery(resID);
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

}
