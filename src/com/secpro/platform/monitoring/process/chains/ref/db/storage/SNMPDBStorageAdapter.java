package com.secpro.platform.monitoring.process.chains.ref.db.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IKpiDao;
import com.secpro.platform.monitoring.process.dao.impl.KpiDao;
import com.secpro.platform.monitoring.process.entity.KpiBean;
import com.secpro.platform.monitoring.process.utils.DateFormatUtil;

/**
 * SNMP类型数据存储
 * 
 * @author sxf
 * 
 */
public class SNMPDBStorageAdapter extends DBStorage {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SNMPDBStorageAdapter.class);
	// 超时事件名称
	//private static final String TIMEOUT = "timeout";
	// 没有对应对象事件名称
	//private static final String NOSUCHOBJECT = "nosuchobject";
	// 是否为数值类型
	//private static final String NUMBER_VALUE = "1";

	public SNMPDBStorageAdapter(Object storeData) {
		super(storeData);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dbStorage(Object storeData) throws PlatformException {
		theLogger.debug("Begin storing data of snmp");
		if (storeData == null) {
			theLogger.error("invalid store data in SNMP database storage.");
			return;
		}
		if (!storeData.getClass().equals(HashMap.class)) {
			theLogger.error("need type of HashMap in SNMP database storage.");
			return;
		}
		Map<String, Object> snmpData = (Map<String, Object>) storeData;

		Map<String, String> executeResult = (Map<String, String>) snmpData
				.get(MetaDataConstant.EXECUTE_RESULT);
		if (executeResult == null || executeResult.size() == 0) {
			theLogger.debug("the execute results of snmp data are empty!");
			return;
		}
		long resID = (Long) snmpData.get(MetaDataConstant.RESOURCE_ID);
		if(resID==0l){
			return;
		}
		String cdate = DateFormatUtil.getNowDate();
		List<KpiBean> storeList = new ArrayList<KpiBean>();
		for (String resultKey : executeResult.keySet()) {
			String resultValue = executeResult.get(resultKey);
			if (Assert.isEmptyString(resultValue)) {
				continue;
			}
			// 将存入的String类型的kpiID转换为long
			long kpiID = Long.parseLong(resultKey);
			if(kpiID==0){
				continue;
			}
			KpiBean snmpBean = new KpiBean();
			snmpBean.setCdate(cdate);
			snmpBean.setResID(resID);
			snmpBean.setKpiID(kpiID);
			snmpBean.setKpiValue(resultValue);
//			if (TIMEOUT.equals(resultValue) || NOSUCHOBJECT.equals(resultValue)) {
//				
//			} else {
//				String strORNum = getKpiStrORNum(kpiID);
//				// 为1时，此数据位数字类型，为0时，为字符串类型
//				if (NUMBER_VALUE.equals(strORNum)) {
//					snmpBean.setValueInt(Float.parseFloat(resultValue));
//				} else {
//					snmpBean.setValueStr(resultValue);
//				}
//			}
			storeList.add(snmpBean);
		}
		if (storeList.size() > 0) {
			snmpStorage(storeList);
		}
		theLogger.debug("end storing data of snmp!");
	}

	/**
	 * 将SNMP数据存入数据库
	 * 
	 * @param storeList
	 */
	private void snmpStorage(List<KpiBean> storeList) {
		IKpiDao kpiDao = new KpiDao();
		kpiDao.rawKpiSave(storeList);
	}

//	/**
//	 * 查询kpi值的类型，数字或者字符串
//	 * 
//	 * @param kpiID
//	 * @return
//	 */
//	private String getKpiStrORNum(long kpiID) {
//		IKpiDao kpiDao = new KpiDao();
//		return kpiDao.kpiTypeQuery(kpiID);
//	}

}
