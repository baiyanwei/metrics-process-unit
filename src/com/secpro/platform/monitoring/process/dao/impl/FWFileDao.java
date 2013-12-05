package com.secpro.platform.monitoring.process.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IFWFileDao;
import com.secpro.platform.monitoring.process.entity.FWFileBean;
import com.secpro.platform.monitoring.process.entity.KpiBean;
import com.secpro.platform.monitoring.process.services.FileStorageService;
import com.secpro.platform.monitoring.process.utils.DBUtil;

public class FWFileDao implements IFWFileDao{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(FWFileDao.class);
	@Override
	public void rawFWFileSave(List<FWFileBean> fwFileBeans) {
		if(fwFileBeans==null||fwFileBeans.size()==0){
			return;
		}
		Connection conn = DBUtil.getConnection();
		PreparedStatement statement = null;
		
		try {
			
			String sql = "insert into raw_fw_file(id,cdate,res_id,file_path,file_name,file_size) values(raw_fw_file_seq.nextval,?,?,?,?,?)";
			statement = conn.prepareStatement(sql);
			int batch=0;
			for(FWFileBean fwFile:fwFileBeans){
				statement.setString(1, fwFile.getCdate());
				statement.setLong(2, fwFile.getResID());
				statement.setString(3, fwFile.getFilePath());
				statement.setString(4, fwFile.getFileName());
				statement.setString(5, fwFile.getFileSize());
				statement.addBatch();
				batch++;
				if(batch==50){
					statement.executeBatch();
					batch=0;
				}
			}
			if(batch>0){
				statement.executeBatch();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		} finally {
			DBUtil.closeConnection(conn, statement);
		}
		
	}

}
