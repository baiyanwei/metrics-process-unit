package com.secpro.platform.monitoring.process.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.dao.IFWFileDao;
import com.secpro.platform.monitoring.process.dao.IResourceDao;
import com.secpro.platform.monitoring.process.dao.impl.FWFileDao;
import com.secpro.platform.monitoring.process.dao.impl.ResDao;
import com.secpro.platform.monitoring.process.entity.FWFileBean;
import com.secpro.platform.monitoring.process.utils.DateFormatUtil;
@ServiceInfo(description = "process unit Scan FW Configuration File Service", configurationPath = "dpu/services/ScanFWConfigurationFileService/")
public class ScanFWConfigurationFileService implements IService{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(ScanFWConfigurationFileService.class);
	@XmlElement(name = "intervalTime",type=Long.class, defaultValue ="0")
	public Long intervalTime=60*1000L;
	@XmlElement(name = "scanFilePath", defaultValue ="")
	public String scanFilePath="";
	@XmlElement(name = "storageFilePath", defaultValue ="")
	public String storageFilePath="";
	@XmlElement(name = "fileSlash", defaultValue ="/")
	public String fileSlash="/";
	private Thread scanFileThread;
	private final String fileNameFormat="(\\w+)_(\\d+\\.\\d+\\.\\d+\\.\\d+)(?:.*?(\\.\\w+))?";
	//存储文件时的时间目录，文件存储路径组成：在给定的fileStoragePath目录下，按月创建子目录，在子目录中根据resID存入相应的资源子目录中
	private String storageDateDir;
	
	@Override
	public void start() throws Exception {
		startupScanFile();
		theLogger.info("startUp");
		
	}

	private void startupScanFile() {
		if(intervalTime<=0||Assert.isEmptyString(scanFilePath)||Assert.isEmptyString(storageFilePath)){
			return;
		}
		scanFileThread=new Thread() {
			public void run() {
				try {
					while(true){
						sleep(intervalTime);
						scanFWConfigurationFile();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					theLogger.exception(e);
				}
			}
		};
		//启动线程
		scanFileThread.start();
		
	}
	private void scanFWConfigurationFile() {
		File scanFileDir=new File(scanFilePath);
		if(!scanFileDir.exists()||!scanFileDir.isDirectory()){
			theLogger.error("scanFilePathError",scanFilePath);
			if(scanFileThread!=null){
				scanFileThread.interrupt();
			}
			return;
		}
		File[] files=scanFileDir.listFiles();
		if(files==null||files.length==0){
			theLogger.debug("no new files in the scan file path");
			return;
		}
		List<FWFileBean> resultBeans=new ArrayList<FWFileBean>(); 
		for(int i=0;i<files.length;i++){
			if(files[i]==null||!files[i].isFile()){
				continue;
			}
			String fileName=files[i].getName();
			if(Assert.isEmptyString(fileName)){
				continue;
			}
			Pattern pattern = Pattern.compile(fileNameFormat);
			Matcher mat = pattern.matcher(fileName);
			if (mat.find()) {
				if(mat.groupCount()<3){
					continue;
				}
				String cityCode=mat.group(1);
				String targetIP=mat.group(2);
				String suffix=mat.group(3);
				if(Assert.isEmptyString(cityCode)||Assert.isEmptyString(targetIP)){
					continue;
				}
				long resID=getResID(cityCode,targetIP);
				if(resID==0L){
					theLogger.error("haveNotResID",cityCode,targetIP);
					continue;
				}
				long fileSize=files[i].length();
				String storagePath=getStorageFilePath(resID);
				if(Assert.isEmptyString(storagePath)){
					return;
				}
				String storageFileName=getStorageFileName(resID,suffix);
				if(transferFile(files[i].getPath(),storagePath+storageFileName)){
					FWFileBean fwFileBean=new FWFileBean();
					fwFileBean.setCdate(DateFormatUtil.getNowDate());
					fwFileBean.setFileName(storageFileName);
					fwFileBean.setFilePath(storagePath);
					fwFileBean.setResID(resID);
					fwFileBean.setFileSize(fileSize+"");
					resultBeans.add(fwFileBean);
				}
			}else{
				theLogger.error("fileNameError",fileName);
				continue;
			}
		}
		if(resultBeans.size()>0){
			fwFileDBStorage(resultBeans);
		}
	}
	
	private boolean transferFile(String beforeFilePath, String afterFilePath) {
		if(Assert.isEmptyString(beforeFilePath)||Assert.isEmptyString(afterFilePath)){
			return false;
		}
		FileInputStream fileIn = null;
		FileOutputStream fileOut=null;
		try{
			File beforeFile=new File(beforeFilePath);
			if(!beforeFile.exists()||!beforeFile.isFile()){
				theLogger.error("transferBeforeFileError",beforeFile);
				return false;
			}
			File afterFile=new File(afterFilePath);
			if(!afterFile.getParentFile().exists())
			{
				afterFile.getParentFile().mkdirs();

			}
			if(!afterFile.exists())
			{
				afterFile.createNewFile();
			}
			fileIn = new FileInputStream(beforeFile);
			fileOut = new FileOutputStream(afterFile);
			int byteRead = 0;
			byte[] buffer = new byte[1024];
			while( (byteRead = fileIn.read(buffer)) != -1){
				fileOut.write(buffer,0,byteRead);
			}
			fileOut.flush();
			fileIn.close();
			fileIn=null;
			fileOut.close();
			fileOut=null;
			theLogger.debug("transferSuccess",afterFilePath);
			beforeFile.delete();
			theLogger.debug("beforeFileDelete",beforeFilePath);
			return true;
		}catch(Exception e){
			theLogger.exception(e);
		}finally{
			if(fileIn!=null){
				try {
					fileIn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fileOut!=null){
				try {
					fileOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private String getStorageFileName(long resID, String suffix) {
		String fileName="";
		String nowDate=DateFormatUtil.getNowDate();
		if(Assert.isEmptyString(suffix)){
			fileName=resID+"_"+nowDate;
		}else{
			fileName=resID+"_"+nowDate+suffix;
		}
		return fileName;
	}

	private String getStorageFilePath(long resID) {
		File storageFileDir=new File(storageFilePath);
		if(!storageFileDir.exists()||!storageFileDir.isDirectory()){
			theLogger.error("storageFilePathError",storageFilePath);
			if(scanFileThread!=null){
				scanFileThread.interrupt();
			}
			return null;
		}
		String filePath=storageFilePath;
		Calendar nowDate=Calendar.getInstance();
		int year=nowDate.get(Calendar.YEAR);
		int month=nowDate.get(Calendar.MONTH)+1;
		String nowYearAndMonth=year+""+month;
		if(Assert.isEmptyString(storageDateDir)){
			storageDateDir=nowYearAndMonth;
			
		}else{
			if(!nowYearAndMonth.equals(storageDateDir)){
				storageDateDir=nowYearAndMonth;
			}
		}
		if(filePath.endsWith(fileSlash)){
			filePath+=storageDateDir;
		}else{
			filePath=filePath+fileSlash+storageDateDir;
		}
		filePath=filePath+fileSlash+resID+fileSlash;
		return filePath;
	}

	private long getResID(String cityCode, String targetIP) {
		IResourceDao resDao=new ResDao();
		return resDao.ResIDQuery(cityCode, targetIP);
	}

	private void fwFileDBStorage(List<FWFileBean> fwFileBeans){
		IFWFileDao fwFileDao=new FWFileDao();
		fwFileDao.rawFWFileSave(fwFileBeans);
	}
	@Override
	public void stop() throws Exception {
		if(scanFileThread!=null){
			scanFileThread.interrupt();
		}
		theLogger.info("stopped");
		
	}

}
