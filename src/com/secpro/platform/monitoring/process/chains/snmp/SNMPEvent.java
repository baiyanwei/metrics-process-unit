package com.secpro.platform.monitoring.process.chains.snmp;

import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.event.EventAndAlarm;
import com.secpro.platform.monitoring.process.chains.ref.event.EventTypeNameConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IKpiDao;
import com.secpro.platform.monitoring.process.dao.impl.KpiDao;

/**
 * SNMP类型数据事件处理 根据事件规则产生或恢复相应事件
 * 
 * @author sxf
 * 
 */
public class SNMPEvent implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SNMPStorage.class);
	private int chainID = 0;
	// 防火墙SNMP采集超时
	private final String TIMEOUT = "timeout";
	// 防火墙SNMP采集没有此对象
	private final String NOSUCHOBJECT = "nosuchobject";

	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("snmp dataProcess chain ID: " + getChainID() + " name:"
				+ this.getClass().getName());
		if (rawData == null) {
			theLogger.error("invalid rawData in SNMP data processing.");
			return null;
		}
		if (!rawData.getClass().equals(HashMap.class)) {
			theLogger.error("need type of HashMap in SNMP data processing.");
			return null;
		}
		Map<String, Object> snmpData = (Map<String, Object>) rawData;
		Map<String, String> snmpResult = (Map<String, String>) snmpData
				.get(MetaDataConstant.EXECUTE_RESULT);
		if (snmpResult == null || snmpResult.size() == 0) {
			theLogger.debug("snmp result is empty");
			return null;
		}
		long resID = (Long) snmpData.get(MetaDataConstant.RESOURCE_ID);
		int timeoutFlag = 0;
		int nosuchobjceFlag = 0;
		for (String resultKpiID : snmpResult.keySet()) {
			String value = snmpResult.get(resultKpiID);

			if (TIMEOUT.equals(value)) {
				if (timeoutFlag == 0) {
					EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID,
							EventTypeNameConstant.EVENT_TYEP_NAME_FW_TIMEOUT,
							TIMEOUT);
					timeoutFlag = 1;
				}
				break;
			}
			if (NOSUCHOBJECT.equals(value)) {
				if (nosuchobjceFlag == 0) {
					EventAndAlarm
							.JudgeGenerateAndRecoveryEvent(
									resID,
									EventTypeNameConstant.EVENT_TYEP_NAME_FW_NO_SUCH_OBJECT,
									NOSUCHOBJECT);
					nosuchobjceFlag = 1;
				}
				continue;
			}
			long kpiID = Long.parseLong(resultKpiID);
			String kpiName = getKpiName(kpiID);
			if (Assert.isEmptyString(kpiName)) {
				theLogger.debug("kpi name is empty");
				continue;
			}

			EventAndAlarm.JudgeGenerateAndRecoveryEvent(resID, kpiName, value);
		}
		// 若没有产生timeout或者nosuchobject类型事件，则应判断是否有相应的可恢复事件
		if (timeoutFlag == 0) {
			EventAndAlarm.isRecoveryEvent(resID,
					EventTypeNameConstant.EVENT_TYEP_NAME_FW_TIMEOUT);
		}
		if (nosuchobjceFlag == 0) {
			EventAndAlarm.isRecoveryEvent(resID,
					EventTypeNameConstant.EVENT_TYEP_NAME_FW_NO_SUCH_OBJECT);
		}
		return snmpData;
	}

	/**
	 * 查询KPI名称，根据此名称判断是否产生此类型事件
	 * 
	 * @param kpiID
	 * @return
	 */
	private String getKpiName(long kpiID) {
		IKpiDao kpiDao = new KpiDao();
		return kpiDao.kpiNameQuery(kpiID);
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
