package com.secpro.platform.monitoring.process.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.utils.DateFormat;
@ServiceInfo(description = "process unit chain service", configurationPath = "dpu/services/FileStorageService/")
public class FileStorageService implements IService{
	private static PlatformLogger theLogger = PlatformLogger.getLogger(FileStorageService.class);
	@XmlElement(name = "fileStoragePath", defaultValue ="")
	public String fileStoragePath="";
	@XmlElement(name = "fileStorageName", defaultValue ="")
	public String fileStorageName="";
	@XmlElement(name = "maxStorageSize",type=Long.class, defaultValue ="104857600")
	public long maxStorageSize=0L;
	@XmlElement(name = "executeFileSaveTimer",type=Long.class, defaultValue ="5000")
	public long executeFileSaveTimer=5000L;
	private Thread fileStorageThread;
	private List<String> saveMessage=new ArrayList<String>();
	private long storedSize=0L;
	private String fileName="";
	private String slash="/";
	@Override
	public void start() throws Exception {
		startFileStroage();
		theLogger.info("startUp");
	}

	private void startFileStroage() {
		if(Assert.isEmptyString(fileStoragePath)||Assert.isEmptyString(fileStorageName)){
			return;
		}
		if(executeFileSaveTimer>0L&&maxStorageSize>0L){
			fileStorageThread=new Thread() {
				public void run() {
					try {
						while(true){
							sleep(executeFileSaveTimer);
							//文件存储
							fileStorage();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						theLogger.exception(e);
					}

				}
			};
			//启动线程
			fileStorageThread.start();
			
		}
		
	}
	private void fileStorage() {
		if(saveMessage==null||saveMessage.size()==0||Assert.isEmptyString(fileStoragePath)||Assert.isEmptyString(fileStorageName)){
			return;
		}

		
		if(storedSize<maxStorageSize){
			if(Assert.isEmptyString(fileName)){

				int index;
				if((index=fileStorageName.lastIndexOf("."))==-1){
					fileName=fileStorageName+DateFormat.getNowDate()+".log";
				}else{
					fileName=fileStorageName.substring(0,index)+DateFormat.getNowDate()+fileStorageName.substring(index,fileStorageName.length());
				}
			}
		}else{
			if(Assert.isEmptyString(fileName)){
				return;
			}
			int index;
			if((index=fileStorageName.lastIndexOf("."))==-1){
				fileName=fileStorageName+DateFormat.getNowDate()+".log";
			}else{
				fileName=fileStorageName.substring(0,index)+DateFormat.getNowDate()+fileStorageName.substring(index,fileStorageName.length());
			}

			storedSize=0;
		}
		String filePath=combineFileStoragePath(fileStoragePath,fileName);
		if(Assert.isEmptyString(filePath)){
			return;
		}
		FileWriter fileWriter=null;
		synchronized(saveMessage){
			try{
				for(String message:saveMessage){
					if(Assert.isEmptyString(message)){
						continue;
					}
					File file=new File(filePath);
					if(!file.getParentFile().exists())
					{
						file.getParentFile().mkdirs();

					}
					if(!file.exists()){
						file.createNewFile();
					}
					fileWriter=new FileWriter(file,true);
					message+="\n";
					fileWriter.write(message,0,message.length());
					fileWriter.flush();
					storedSize+=message.length();
				}
				saveMessage.clear();
			}catch(Exception e){
				theLogger.exception(e);
			}finally{
				if(fileWriter!=null){
					try {
						fileWriter.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	private String combineFileStoragePath(String path,String fileName){
		if(Assert.isEmptyString(path)||Assert.isEmptyString(fileName)){
			return null;
		}
		String filePath="";
		boolean pathIsHaveSlash=path.endsWith(slash);
		boolean nameIsHaveSlash=fileName.startsWith(slash);
		if(pathIsHaveSlash&&nameIsHaveSlash){
			filePath=path+fileName.substring(1,fileName.length());
		}else if(pathIsHaveSlash||nameIsHaveSlash){
			filePath=path+fileName;
		}else{
			filePath=path+slash+fileName;
		}
		return filePath;
	}
	public void saveMessage(String message){
		if(fileStorageThread==null||saveMessage==null){
			return;
		}
		synchronized(saveMessage){
			saveMessage.add(message);
		}

	}

	@Override
	public void stop() throws Exception {
		if(fileStorageThread!=null){
			fileStorageThread.interrupt();
		}
		theLogger.info("stopped");
	}

}
