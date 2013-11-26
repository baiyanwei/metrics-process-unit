package com.secpro.platform.monitoring.process.chains.ref.db.storage;
/**
 * 数据库存储
 * @author sxf
 *
 */
public abstract class DBStorage extends Thread{
	private Object storeData=null;
	public DBStorage(Object storeData){
		this.storeData=storeData;
	}
	public void run(){
		if(storeData!=null){
			try {
				dbStorage(storeData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	abstract public void dbStorage(Object storeData)throws Exception;
}
