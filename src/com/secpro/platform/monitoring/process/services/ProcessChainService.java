package com.secpro.platform.monitoring.process.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.xpath.XPathConstants;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.PropertyLoaderService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.IDataProcessChain;
import com.secpro.platform.monitoring.process.chains.error.ErrorDataProcess;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataConstant;
import com.secpro.platform.monitoring.process.chains.ref.parse.MetaDataParsing;
/**
 * 根据配置文件为各数据处理类型动态加载数据处理链
 * 根据数据的类型调用相应的数据处理链
 * @author sxf
 *
 */
@ServiceInfo(description = "process unit chain service", configurationPath = "/app/mpu/services/ProcessChainService/")
public class ProcessChainService implements IService{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(ProcessChainService.class);
	@XmlElement(name = "dataTypes", defaultValue ="ssh,telnet,snmp,syslog")
	public String _dataTypes=null;
	//数据处理链
	private HashMap<String,List<IDataProcessChain>> _dataProcessChain=new HashMap<String,List<IDataProcessChain>>();
	@Override
	public void start() throws Exception {
		//加载数据处理链
		loadDataProcessChain();
		theLogger.info("startUp");
	}

	@Override
	public void stop() throws Exception {
		theLogger.info("stopped");
	}
	/**
	 * 根据配置文件加载数据处理链
	 * @throws PlatformException
	 */
	@SuppressWarnings("unchecked")
	private void loadDataProcessChain() throws PlatformException{
		PropertyLoaderService propertyLoader=ServiceHelper.findService(PropertyLoaderService.class);
		if(propertyLoader==null)
		{
			throw new PlatformException("Can't find the PropertyLoaderService, Service can't start without PropertyLoaderService.");
		}
		String[] dataTypeArr=_dataTypes.split(",");
		for(int i=0;i<dataTypeArr.length;i++)
		{
			String dataType=dataTypeArr[i];
			if(Assert.isEmptyString(dataType)==true)
			{
				theLogger.error("typeIsNull");
				continue;
			}
			//得到数据处理链名称
			List<String> chainNames=(List<String>) propertyLoader.getValue(getConfigurationPath()+dataType,"",XPathConstants.STRING, List.class, true);
			theLogger.debug(chainNames.toString());
			if(chainNames==null||chainNames.isEmpty()||"".equals(chainNames.get(0)))
			{
				theLogger.error("chainNamesIsNull",dataType);
				continue;
			}
			//得到数据处理链命名空间，即包名路径
			String namespace=(String)propertyLoader.getValue(getConfigurationPath()+dataType+"Namespace","com.secpro.platform.monitoring.process.chains."+dataType,XPathConstants.STRING, String.class, true);
			List<IDataProcessChain> processChainsList=null;
			try {
				processChainsList=new ArrayList<IDataProcessChain>();
				for(int j=0;j<chainNames.size();j++)
				{
					
					String chainName=chainNames.get(j);
					if(Assert.isEmptyString(chainName)==true)
					{
						theLogger.error("chainNameIsNull");
						continue;
					}
					//类路径
					String chainClassPath=getClassPath(namespace,chainName);
					Class<?> clazz=Class.forName(chainClassPath);
					//生成类对象实例
					IDataProcessChain processChain=(IDataProcessChain)clazz.newInstance();
					processChain.setChainID(j+1);
					processChainsList.add(processChain);
					theLogger.debug("loaderChain",dataType,processChain.getChainID(),chainClassPath);
				}
			} catch (Exception e) {
				processChainsList=null;
				theLogger.exception(e);
				//e.printStackTrace();
			}
			if(processChainsList!=null&&processChainsList.size()>0)
			{
				//存储数据处理链
				_dataProcessChain.put(dataType, processChainsList);
				theLogger.info("loaderChains",dataType);
			}
		}
	}
	/**
	 * 获取数据处理链结点的完整类路径
	 * @param namespace
	 * @param chainName
	 * @return
	 */
	private String getClassPath(String namespace, String chainName) {
		if(namespace.endsWith(".")==true)
		{
			namespace=namespace.substring(0,namespace.length()-1);
		}
		if(chainName.startsWith(".")==true)
		{
			chainName=chainName.substring(1,chainName.length());
		}
		return namespace+"."+chainName;
	}
	/**
	 * 获取该服务在配置文件中的路径
	 * @return
	 */
	private String getConfigurationPath(){
		ServiceInfo serviceAnnotation = this.getClass().getAnnotation(ServiceInfo.class);
		if (serviceAnnotation != null) {
			String path=serviceAnnotation.configurationPath();
			if(path.endsWith("/")==false)
			{
				path=path+"/";
			}
			return path;
		}
		return "/app/mpu/services/ProcessChainService/";
	}
	/**
	 * 根据数据类型，调用相应的数据处理链条，完成数据处理要求
	 * @param messageObj
	 * @param cityCode
	 * @throws Exception 
	 */
	public void dataProcess(Object dataObj,String cityCode) throws Exception{
		if(dataObj==null)
		{
			theLogger.error("the metadata is empty,do nothing!");
			return;
		}
		if(Assert.isEmptyString(cityCode))
		{
			theLogger.error("city code of the data is empty");
			return;

		}
		theLogger.debug(dataObj.toString());
		//获得数据中的数据类型名称
		String metaDataType=MetaDataParsing.getMetaDataType(dataObj, _dataTypes);
		if(Assert.isEmptyString(metaDataType))
		{
			//数据类型是否为error
			if(MetaDataParsing.isErrorDataType(dataObj)==false)
			{	
				theLogger.error("the data type of this message is wrong,do nothing!");
				theLogger.debug(dataObj.toString());
				return;
			}
			else
			{
				//启动线程，处理error类型数据
				JSONTokener parser = new JSONTokener(dataObj.toString());
				JSONObject jsonObj=new JSONObject(parser);
				jsonObj.put(MetaDataConstant.META_CITYCODE_PROPERTY_NAME, cityCode);
				ErrorDataProcess errorProcessThread=new ErrorDataProcess(jsonObj);
				errorProcessThread.start();
				return;
			}
		}
		List<IDataProcessChain> chains=_dataProcessChain.get(metaDataType);
		if(chains==null||chains.size()==0)
		{
			throw new PlatformException("the chains of data type: "+metaDataType+" are empty");
			
		}
		JSONTokener parser = new JSONTokener(dataObj.toString());
		JSONObject jsonObj=new JSONObject(parser);
		jsonObj.put(MetaDataConstant.META_CITYCODE_PROPERTY_NAME, cityCode);
		//数据处理链线程
		DataProcessThread thread=new DataProcessThread(jsonObj,chains);
		//启动线程
		thread.start();
		
	}

	/**
	 * 处理watchdog类型数据
	 * @param dataObj
	 * @throws Exception
	 */
	public void watchdogDataProcess(Object dataObj) throws Exception{
		if(dataObj==null)
		{
			theLogger.error("the metadata is empty,do nothing!");
			return;
		}
		theLogger.debug(dataObj.toString());
		List<IDataProcessChain> chains=_dataProcessChain.get("watchdog");
		if(chains==null||chains.size()==0)
		{
			throw new PlatformException("the chains of data type: watchdog are empty");

		}
		JSONTokener parser = new JSONTokener(dataObj.toString());
		JSONObject jsonObj=new JSONObject(parser);
		//数据处理线程
		DataProcessThread thread=new DataProcessThread(jsonObj,chains);
		//启动线程
		thread.start();

	}
}

	
/**
 * 
 * 数据处理线程
 *
 */
class DataProcessThread extends Thread{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(DataProcessThread.class);
	private Long startTime;
	private Long endTime;
	private Object metaData;
	private List<IDataProcessChain> chains;
	//初始化数据处理线程所需要的参数值
	public DataProcessThread(Object metaData,List<IDataProcessChain> chains){
		this.metaData=metaData;
		this.chains=chains;
	}
	public void run(){
		if(metaData==null||chains==null||chains.size()==0){
			theLogger.error("the message or data process chains are empty!");
			return;
		}
		startTime=System.currentTimeMillis();
		Object stepResult=metaData;
		for(IDataProcessChain dataProcessChain:chains)
		{
			if(dataProcessChain==null)
			{
				theLogger.error("the data process chain is empty!");
				return;
			}
			
			try {
				if(stepResult==null)
				{
					theLogger.error("chainInterrupt");
					FileStorageService fileStorage=ServiceHelper.findService(FileStorageService.class);
					if(fileStorage!=null){
						fileStorage.saveMessage(metaData.toString());
					}
					return;
				}
				if("ok".equals(stepResult)==true)
				{
					break;
				}
				//进行数据处理
				stepResult=dataProcessChain.dataProcess(stepResult);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				theLogger.exception(e);
				return;
			}
		}
		if(stepResult==null)
		{
			theLogger.error("chainInterrupt");
			return;
		}
		endTime=System.currentTimeMillis();
		theLogger.info("chainsComplete",endTime-startTime);
	}
}
