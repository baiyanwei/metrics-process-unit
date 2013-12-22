package com.secpro.platform.monitoring.process.chains.watchdog;

import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.db.storage.DBStorage;
import com.secpro.platform.monitoring.process.chains.ref.db.storage.WatchDogDBStorgeAdapter;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;
/**
 * 数据存储
 * 将watchdog相应数据存入数据库
 * @author sxf
 *
 */
public class WatchDogStorage implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(WatchDogStorage.class);
	private int chainID = 0;

	@Override
	public Object dataProcess(Object rawData) throws Exception {
		theLogger.debug("watchdog dataProcess chain ID: " + getChainID());
		if (rawData == null) {
			theLogger.error("invalid rawData in watchdog data processing.");
			return null;
		}
		if (!rawData.getClass().equals(HashMap.class)) {
			theLogger
					.error("need type of HashMap in watchdog data processing.");
			return null;
		}
		Map<String, Object> watchdogData = (Map<String, Object>) rawData;
		
		// 调用watchdog存储数据库方法，将数据存入数据库中
		watchdogDBStorage(watchdogData);
		return watchdogData;
	}

	/**
	 * 存储watchdog类型数据
	 * 
	 * @param watchdogData
	 */
	private void watchdogDBStorage(Map<String, Object> watchdogData) {
		DBStorage dbStorage = new WatchDogDBStorgeAdapter(watchdogData);
		// 启动线程
		dbStorage.start();

	}

	/**
	 * 查询资源ID
	 * 
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	

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
