package com.secpro.platform.monitoring.process.chains.syslog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IDeviceDao;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.ISyslogDao;
import com.secpro.platform.monitoring.process.dao.impl.DeviceDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;
import com.secpro.platform.monitoring.process.dao.impl.SyslogDao;
import com.secpro.platform.monitoring.process.entity.SyslogBean;
import com.secpro.platform.monitoring.process.services.SyslogStandardRuleService;
import com.secpro.platform.monitoring.process.utils.CollectionRemoveUtil;
import com.secpro.platform.monitoring.process.utils.DateFormatUtil;
/**
 * 数据存储
 * 根据syslog数据库映射规则，将syslog标准化后的数据存入数据库
 * @author sxf
 *
 */
public class SyslogStorage implements IDataProcessChain {
	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(SyslogStorage.class);
	private int chainID = 0;
	// syslog中name字段格式
	private final String nameFormat = "this is {0} syslog";

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
					.error("syslog storage need the SyslogStandardRuleService.");
			return null;
		}
		Map<String, String> dataDBMapping = syslogRuleService
				.get_dataDBMapping();
		if (dataDBMapping == null || dataDBMapping.size() == 0) {
			theLogger
					.error("syslog storage need the data and database Mapping.");
			return null;
		}
		
		List<SyslogBean> syslogBeanList = new ArrayList<SyslogBean>();
		List<Map<String, Object>> deleteList = new ArrayList();
		for (Map<String, Object> syslogData : syslogDatas) {

			long resID=(Long) syslogData
					.get(MetaDataConstant.RESOURCE_ID);
			if (resID == 0) {
				deleteList.add(syslogData);
				theLogger.error("resource id is empty!");
				continue;
			}
			// 查询设备类型描述
			String typeDes = getTypeName(resID);
			Map<String, String> resultMapping = (Map<String, String>) syslogData
					.get(MetaDataConstant.EXECUTE_RESULT);
			String rdate = (String) syslogData
					.get(MetaDataConstant.EXECUTE_DATE);
			if (Assert.isEmptyString(rdate)) {
				rdate = DateFormatUtil.getNowDate();
			}
			SyslogBean syslogBean = new SyslogBean();
			syslogBean.setResID(resID);
			syslogBean.setResultMapping(resultMapping);
			syslogBean.setRdate(rdate);
			syslogBean.setOriSyslog((String) syslogData
					.get(MetaDataConstant.ORIGIN_SYSLOG));
			syslogBean.setEdate(DateFormatUtil.getNowDate());
			syslogBean.setSyslogName(MessageFormat.format(nameFormat, typeDes));
			syslogBeanList.add(syslogBean);
			// System.out.println(syslogBean.getEdate()+syslogBean.getRdate()+syslogBean.getOriSyslog()+syslogBean.getResID()+syslogBean.getSyslogName());
		}
		if (deleteList.size() > 0) {
			CollectionRemoveUtil.listRemove(syslogDatas, deleteList);
		}
		if (syslogBeanList.size() > 0) {
			ISyslogDao syslogDao = new SyslogDao();
			syslogDao.syslogSave(syslogBeanList, dataDBMapping);
		}
		return syslogDatas;
	}


	/**
	 * 查询设备类型名称描述
	 * 
	 * @param cityCode
	 * @param targetIP
	 * @return
	 */
	private String getTypeName(long resID) {
		IDeviceDao devDao = new DeviceDao();
		return devDao.typeNameQuery(resID);
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
