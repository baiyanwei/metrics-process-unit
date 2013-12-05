package com.secpro.platform.monitoring.process.dao;

import java.util.List;

import com.secpro.platform.monitoring.process.entity.FWFileBean;

public interface IFWFileDao {
	public void rawFWFileSave(List<FWFileBean> fwFileBeans);
}
