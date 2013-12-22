package com.secpro.platform.monitoring.process.chains.ref.db.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IKpiDao;
import com.secpro.platform.monitoring.process.dao.impl.KpiDao;
import com.secpro.platform.monitoring.process.entity.KpiBean;
import com.secpro.platform.monitoring.process.utils.DateFormatUtil;

/**
 * watchdog类型数据存储
 * 
 * @author sxf
 * 
 */
public class WatchDogDBStorgeAdapter extends DBStorage {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(WatchDogDBStorgeAdapter.class);

	public WatchDogDBStorgeAdapter(Object storeData) {
		super(storeData);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dbStorage(Object storeData) throws Exception {
		theLogger.debug("Begin storing data of watchdog");
		if (storeData == null) {
			throw new PlatformException(
					"invalid store data in watchdog database storage.");
		}
		if (!storeData.getClass().equals(HashMap.class)) {
			throw new PlatformException(
					"need type of HashMap in watchdog database storage.");
		}
		Map<String, Object> watchdogData = (Map<String, Object>) storeData;
		Map<String, String> executeResult = (Map<String, String>) watchdogData
				.get(MetaDataConstant.WATCHDOG_EXECUTE_RESULT);
		if (executeResult == null || executeResult.size() == 0) {
			theLogger.debug("the execute results of watchdog data are empty!");
			return;
		}
		long resID = (Long) watchdogData.get(MetaDataConstant.RESOURCE_ID);
		String cdate = DateFormatUtil.getNowDate();
		Set<String> resultKeys = executeResult.keySet();
		List<KpiBean> storeList = new ArrayList<KpiBean>();
		for (String resultKey : resultKeys) {
			String resultValue = executeResult.get(resultKey);
			if (Assert.isEmptyString(resultValue)) {
				continue;
			}
			KpiBean watchdogBean = new KpiBean();
			
			long kpiID=getKpiID(resultKey,resID);
			if(kpiID==0l){
				continue;
			}
			watchdogBean.setCdate(cdate);
			watchdogBean.setResID(resID);
			watchdogBean.setKpiID(kpiID);
			
			watchdogBean.setKpiValue(resultValue);
			
			storeList.add(watchdogBean);
		}
		watchdogStorage(storeList);
		theLogger.debug("end storing data of watchdog!");

	}

	private long getKpiID(String kpiName, long resID) {
		IKpiDao kpiDao = new KpiDao();
		return kpiDao.kpiIDQuery(kpiName, resID);
	}

	private void watchdogStorage(List<KpiBean> storeList) {
		IKpiDao kpiDao = new KpiDao();
		kpiDao.rawKpiSave(storeList);
	}

}
