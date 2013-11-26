package com.secpro.platform.monitoring.process.dao;
/**
 * 资源数据相关的数据库操作
 * @author sxf
 *
 */
public interface IResourceDao {
	/**
	 * 查询typeCode设备型号编码
	 * @param cityCode
	 * @param resIP
	 * @return
	 */
	public String typeCodeQuery(String cityCode,String resIP);
	/**
	 * 查询resID资源ID
	 * @param cityCode
	 * @param resIP
	 * @return
	 */
	public long ResIDQuery(String cityCode,String resIP);
	/**
	 * 资源启停状态查询
	 * @param resID
	 * @return
	 */
	public String resPausedQuery(long resID);
}
