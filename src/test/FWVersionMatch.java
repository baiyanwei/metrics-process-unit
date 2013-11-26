package test;

import com.secpro.platform.core.utils.Assert;
/**
 * 防火墙配置版本比对
 * @author sxf
 *
 */
public class FWVersionMatch {
	public static String[] versionMatch(String verFirst,String verSecond,String newline){
		if(Assert.isEmptyString(verFirst)||Assert.isEmptyString(verSecond)||Assert.isEmptyString(newline)){
			return null;
		}
		verFirst=verFirst.replaceAll(newline+"\\s*"+newline, newline);
		verSecond=verSecond.replaceAll(newline+"\\s*"+newline, newline);
		String[] first=verFirst.split(newline);
		String[] second=verSecond.split(newline);
		String[] addAndDeleteResult=new String[2];
		for(int i=0;i<first.length;i++){
			for(int j=0;j<second.length;j++){
				if(first[i].equals(second[j])){
					verFirst=verFirst.replaceFirst(first[i]+newline,"");
					verSecond=verSecond.replaceFirst(second[j]+newline,"");
					break;
				}
			}
		}
		addAndDeleteResult[0]=verSecond;
		addAndDeleteResult[1]=verFirst;
		return addAndDeleteResult;
	}
}
