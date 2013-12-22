package com.secpro.platform.monitoring.process.chains.snmp;

import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.db.storage.DBStorage;
import com.secpro.platform.monitoring.process.chains.ref.db.storage.SNMPDBStorageAdapter;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;

/**
 * 存储SNMP数据 将标准化后的数据存入数据库
 * 
 * @author sxf
 * 
 */
public class SNMPStorage implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SNMPStorage.class);
	private int chainID = 0;

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
		// 调用snmp存储数据库方法，将数据存入数据库中
		snmpDBStorage(snmpData);

		return snmpData;
	}


	private void snmpDBStorage(Object rawData) {
		DBStorage dbStorage = new SNMPDBStorageAdapter(rawData);
		dbStorage.start();
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
