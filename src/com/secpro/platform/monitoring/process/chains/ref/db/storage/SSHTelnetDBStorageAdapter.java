package com.secpro.platform.monitoring.process.chains.ref.db.storage;

import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.impl.ConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.entity.ConfigAndPolicyBean;
import com.secpro.platform.monitoring.process.utils.DateFormat;
/**
 * ssh、telnet类型数据存储
 * @author sxf
 *
 */
public class SSHTelnetDBStorageAdapter extends DBStorage{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(SSHTelnetDBStorageAdapter.class);
	public SSHTelnetDBStorageAdapter(Object storeData) {
		super(storeData);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void dbStorage(Object storeData) throws Exception {
		theLogger.debug("Begin storing data of ssh or telnet");
		if (storeData == null) {
			throw new PlatformException(
					"invalid store data in watchdog database storage.");
		}
		if(!storeData.getClass().equals(HashMap.class)){
			throw new PlatformException("need type of HashMap in ssh or telnet database storage.");
		}
		Map<String,Object> sshOrTelnetData=(Map<String,Object>)storeData;
		String[] executeResult=(String[]) sshOrTelnetData.get(MetaDataConstant.EXECUTE_RESULT);
		if(executeResult==null){
			theLogger.debug("the execute results of ssh or telnet data are empty!");
			return;
		}
		long resID=(Long) sshOrTelnetData.get("resID");
		String taskCode=(String) sshOrTelnetData.get(MetaDataConstant.TASK_CODE);
		String cdate=DateFormat.getNowDate();
		StringBuilder configAndPolicyInfo=new StringBuilder();
		if(Assert.isEmptyString(executeResult[0])){
			configAndPolicyInfo.append("#configuration##configuration#");
		}else{
			configAndPolicyInfo.append("#configuration#"+executeResult[0]+"#configuration#");
		}
		if(Assert.isEmptyString(executeResult[1])){
			configAndPolicyInfo.append("#policy##policy#");
		}else{
			configAndPolicyInfo.append("#policy#"+executeResult[1]+"#policy#");
		}
		ConfigAndPolicyBean configAndPolicyBean=new ConfigAndPolicyBean();
		
		configAndPolicyBean.setCdate(cdate);
		configAndPolicyBean.setTaskCode(taskCode);
		configAndPolicyBean.setConfigAndPolicyInfo(configAndPolicyInfo.toString());
		configAndPolicyBean.setResID(resID);
		IConfigAndPolicyDao configAndPolicyDao=new ConfigAndPolicyDao();
		configAndPolicyDao.configAndPolicySave(configAndPolicyBean);
		theLogger.debug("end storing data of ssh or telnet!");
		
	}

}
