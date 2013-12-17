package test;

import com.secpro.platform.core.utils.Assert;
/**
 * 防火墙配置版本比对
 * 
 * @author sxf
 *
 */
public class FWVersionMatch {
	/**
	 * 两个文本比对，文本二比对文本一，得出增加与删除文本内容
	 * 
	 * @param verFirst 前一个文本，即旧文本
	 * @param verSecond 后一个文本，即新文本
	 * @param newline
	 * @return String[0]为文本二较文本一，增加的内容。String[1]为文本二较文本一，删除的内容。
	 */
	public static String[] versionMatch(String verFirst,String verSecond,String newline){
		if(null == verFirst ||verFirst.trim().length() == 0||null == verSecond ||verSecond.trim().length() == 0||null == newline ||newline.trim().length() == 0){
			return null;
		}
		String[] addAndDeleteResult={"",""};
		if(verFirst.equals(verSecond)){
			return addAndDeleteResult;
		}
		verFirst=verFirst.replaceAll(newline+"\\s*"+newline, newline);
		verSecond=verSecond.replaceAll(newline+"\\s*"+newline, newline);
		String[] first=verFirst.split(newline);
		String[] second=verSecond.split(newline);
		
		for(int i=0;i<first.length;i++){
			if(first[i].trim().length()==0){
				continue;
			}
			for(int j=0;j<second.length;j++){
				if(second[j].trim().length()==0){
					continue;
				}
				if(first[i].equals(second[j])){
					verFirst=verFirst.replaceFirst(first[i]+newline,"");
					verSecond=verSecond.replaceFirst(second[j]+newline,"");
					break;
				}
			}
		}
		if(verSecond.trim().length()==0){
			addAndDeleteResult[0]="";
		}else{
			String[] secondResult=verSecond.split(newline);
			StringBuilder result=new StringBuilder();
			for(int j=0;j<secondResult.length;j++){
				if(secondResult[j].trim().length()>0){
					result.append(secondResult[j]);
					result.append("\n");
				}
			}
			addAndDeleteResult[0]=result.toString();
		}
		if(verFirst.trim().length()==0){
			addAndDeleteResult[1]="";
		}else{
			String[] firstResult=verFirst.split(newline);
			StringBuilder result=new StringBuilder();
			for(int j=0;j<firstResult.length;j++){
				if(firstResult[j].trim().length()>0){
					result.append(firstResult[j]);
					result.append("\n");
				}
			}
			addAndDeleteResult[1]=result.toString();
		}
		
		
	
		return addAndDeleteResult;
	}
}
