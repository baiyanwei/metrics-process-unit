package com.secpro.platform.monitoring.process.chains;
/**
 * 数据处理链接口
 * @author sxf
 *
 */
public interface IDataProcessChain {
	public Object dataProcess(Object rawData) throws Exception;
	public void setChainID(int chainID);
	public int getChainID();
}
