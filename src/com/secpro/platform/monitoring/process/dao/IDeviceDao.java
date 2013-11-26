package com.secpro.platform.monitoring.process.dao;
/**
 * 设备相关数据库操作
 * @author sxf
 *
 */
public interface IDeviceDao {
	/**
	 * 查询设备类型名称
	 * @param cityCode
	 * @param resIP
	 * @return
	 */
	public String typeNameQuery(String cityCode,String resIP);
}
