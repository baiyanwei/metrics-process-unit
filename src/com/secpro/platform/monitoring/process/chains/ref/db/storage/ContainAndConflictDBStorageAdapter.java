package com.secpro.platform.monitoring.process.chains.ref.db.storage;

import java.util.HashMap;
import java.util.Map;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.dao.IConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.dao.impl.ConfigAndPolicyDao;
import com.secpro.platform.monitoring.process.entity.ContainAndConflictBean;
import com.secpro.platform.monitoring.process.utils.DateFormat;
/**
 * 包含和冲突信息存储
 * @author sxf
 *
 */
public class ContainAndConflictDBStorageAdapter extends DBStorage {

	private static PlatformLogger theLogger = PlatformLogger
			.getLogger(ContainAndConflictDBStorageAdapter.class);

	public ContainAndConflictDBStorageAdapter(Object storeData) {
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
		if (!storeData.getClass().equals(HashMap.class)) {
			throw new PlatformException(
					"need type of HashMap in ssh or telnet database storage.");
		}
		Map<String, Object> containAndConflictData = (Map<String, Object>) storeData;
		String[] executeResult = (String[]) containAndConflictData
				.get(MetaDataConstant.EXECUTE_RESULT);
		if (executeResult == null) {
			theLogger
					.debug("the execute results of contain and conflict data are empty!");
			return;
		}
		long resID = (Long) containAndConflictData.get("resID");
		String taskCode = (String) containAndConflictData
				.get(MetaDataConstant.TASK_CODE);
		String cdate = DateFormat.getNowDate();
		StringBuilder containAndConflictInfo = new StringBuilder();
		if (Assert.isEmptyString(executeResult[0])) {
			containAndConflictInfo.append("#contain##contain#");
		} else {
			containAndConflictInfo.append("#contain#" + executeResult[0]
					+ "#contain#");
		}
		if (Assert.isEmptyString(executeResult[1])) {
			containAndConflictInfo.append("#conflict##conflict#");
		} else {
			containAndConflictInfo.append("#conflict#" + executeResult[1]
					+ "#conflict#");
		}
		ContainAndConflictBean containAndConflictBean = new ContainAndConflictBean();

		containAndConflictBean.setCdate(cdate);
		containAndConflictBean.setTaskCode(taskCode);
		containAndConflictBean.setContainAndConflictInfo(containAndConflictInfo
				.toString());
		containAndConflictBean.setResID(resID);
		IConfigAndPolicyDao configAndPolicyDao = new ConfigAndPolicyDao();
		configAndPolicyDao.containAndConflictSave(containAndConflictBean);
		theLogger.debug("end storing data of ssh or telnet!");

	}
}
