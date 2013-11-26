package com.secpro.platform.monitoring.process.chains.ref.other;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
/**
 * 读取相应规则文件
 * @author sxf
 *
 */
public class ReadRuleFile {
	private static PlatformLogger theLogger = PlatformLogger.getLogger(ReadRuleFile.class);
	/**
	 * 读取规则文件，规则文件行以%%分割
	 * @param rulePath
	 * @return
	 */
	public static String readRule(String rulePath) {
		if(Assert.isEmptyString(rulePath)){
			return null;
		}
		FileReader fileRead = null;
		BufferedReader buffRead=null;
		try {
			fileRead = new FileReader(new File(rulePath));
			buffRead=new BufferedReader(fileRead);
			StringBuilder result=new StringBuilder();
			String ss;
			while((ss=buffRead.readLine())!=null){
				result.append(ss+"%%");
			}
			return result.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			theLogger.exception(e);
		}finally{
			if(fileRead!=null){
				try {
					fileRead.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(buffRead!=null){
				try {
					buffRead.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
		
	}
}
