package com.secpro.platform.monitoring.process.chains.ref.standard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.secpro.platform.core.utils.Assert;
/**
 * 标准化工具方法
 * @author sxf
 *
 */
public class StandardUtil {
	private static final String HEAD_REGEX="<[A-Z]>";
	/**
	 * 规则是否以自定义操作符开始
	 * @param rule
	 * @return 返回自定义操作符
	 */
	public static String isCustomActionType(String rule){
		if(Assert.isEmptyString(rule)){
			return null;
		}
		if(rule.length()<3){
			return null;
		}
		if(rule.substring(0,3).matches(HEAD_REGEX)){
			return rule.substring(1, 2);
		}
		return null;

	}
	/**
	 * 移除规则中自定义的操作符
	 * @param rule
	 * @return
	 */
	public static String removeCustomAction(String rule){
		if(Assert.isEmptyString(rule)){
			return null;
		}
		return rule.substring(3,rule.length());

	}
	/**
	 * 将点分式IP地址转换为十进制数字格式
	 * @param strip
	 * @return
	 */
	public static long ipToLong(String strip) 
	{ 
		if(Assert.isEmptyString(strip)||isDotIpAddress(strip)==false){
			return 0;
		}
		int j=0; 
		int i=0; 
		long[]ip=new long[4]; 
		int position1=strip.indexOf("."); 
		int position2=strip.indexOf(".",position1+1); 
		int position3=strip.indexOf(".",position2+1); 
		ip[0]=Long.parseLong(strip.substring(0,position1)); 
		ip[1]=Long.parseLong(strip.substring(position1+1,position2)); 
		ip[2]=Long.parseLong(strip.substring(position2+1,position3)); 
		ip[3]=Long.parseLong(strip.substring(position3+1)); 
		return(ip[0]<<24)+(ip[1]<<16)+(ip[2]<<8)+ip[3];//ip1*256*256*256+ip2*256*256+ip3*256+ip4 
	} 
	/**
	 * 判断是否点分IP地址
	 * @param ipAddress
	 * @return
	 */
	public static boolean isDotIpAddress(String ipAddress) {
		String ip = "(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})";
		Pattern pattern = Pattern.compile(ip);
		Matcher matcher = pattern.matcher(ipAddress);
		return matcher.matches();
	}
	/**
	 * 将整形IP地址转换为点分格式IP地址
	 * @param longip
	 * @return
	 */
	public static String longToIp(long longip) 
	{ 
		StringBuffer sb=new StringBuffer(""); 
		sb.append(String.valueOf(longip>>>24));//直接右移24位 
		sb.append("."); 
		sb.append(String.valueOf((longip&0x00ffffff)>>>16));//将高8位置0，然后右移16位 
		sb.append("."); 
		sb.append(String.valueOf((longip&0x0000ffff)>>>8)); 
		sb.append("."); 
		sb.append(String.valueOf(longip&0x000000ff)); 
		//sb.append("."); 
		return sb.toString(); 
	} 
	/**
	 * 根据IP地质子网掩码，计算地址范围
	 * @param subNet
	 * @param mark
	 * @return
	 */
	public static String getSubnetAddress(String subNet, String mark,boolean isReturnLongIP) {

		if(Assert.isEmptyString(subNet)||Assert.isEmptyString(mark)){
			return null;
		}
		String networkAddress="";
		String broadcastAddress="";
		if(mark.matches("\\d+.\\d+.\\d+.\\d+")){
			String[] subNetSplit=subNet.split("\\.");
			String[] markSplit=mark.split("\\.");
			for(int i=0;i<4;i++){
				int hostBit=0;
				int ipOneInt=Integer.parseInt(subNetSplit[i]);
				int markOneInt=Integer.parseInt(markSplit[i]);
				String markOneBinary=Integer.toBinaryString(markOneInt);
				if("0".equals(markOneBinary))
				{
					hostBit=8;
				}else{
					int location=markOneBinary.indexOf("0");
					if(location!=-1){
						hostBit=markOneBinary.length()-location;
					}
				}
				int andResult=ipOneInt&markOneInt;
				if(i==3){
					andResult++;
				}
				networkAddress+=andResult;
				if(hostBit==0){
					broadcastAddress+=andResult;
				}else{
					int decimal=0;
					for(int j=hostBit-1;j>=0;j--){
						decimal+=Math.pow(2, j);
					}
					int orResult=andResult|decimal;
					if(i==3){
						orResult--;
					}
					broadcastAddress+=orResult;
				}
				if(i!=3){
					networkAddress+=".";
					broadcastAddress+=".";
				}
			}
		}else if(mark.matches("\\d+")){
			int markInt=Integer.parseInt(mark);
			if(markInt>32||markInt<0){
				return null;
			}
			String[] subNetSplit=subNet.split("\\.");

			for(int i=0;i<4;i++){
				int ipOneInt=Integer.parseInt(subNetSplit[i]);
				if(markInt-8>=0){
					networkAddress+=ipOneInt;
					broadcastAddress+=ipOneInt;
					markInt-=8;
				}else{
					int decimalNet=0;
					for(int j=7;j>=8-markInt;j--){
						decimalNet+=Math.pow(2, j);

					}

					int andResult=ipOneInt&decimalNet;
					if(i==3){
						andResult++;
					}
					networkAddress+=andResult;
					int decimalBro=0;
					for(int j=7-markInt;j>=0;j--){
						decimalBro+=Math.pow(2, j);
					}
					int orResult=andResult|decimalBro;
					if(i==3){
						orResult--;
					}
					broadcastAddress+=orResult;
					markInt=0;
				}
				if(i!=3){
					networkAddress+=".";
					broadcastAddress+=".";
				}

			}

		}
		if(Assert.isEmptyString(networkAddress)||Assert.isEmptyString(broadcastAddress)){
			return null;
		}
		if(isReturnLongIP){
			return networkAddress+"-"+broadcastAddress+":"+ipToLong(networkAddress)+"-"+ipToLong(broadcastAddress);
		}else{
			return networkAddress+"-"+broadcastAddress;
		}
	}
	/**
	 * 排除子网中部分IP地址，或者地址段
	 * @param subnet
	 * @param mark
	 * @param except
	 * @return
	 */
	public static String netAddressExcept(String subnet,String mark,String[] except){
		if(Assert.isEmptyString(subnet)||Assert.isEmptyString(mark)||except==null||except.length==0){
			return null;
		}
		String rangeAddress=getSubnetAddress(subnet,mark,false);
		if(Assert.isEmptyString(rangeAddress)){
			return null;
		}
		int index=rangeAddress.indexOf("-");
		if(index==-1){
			return null;
		}
		String ipS=rangeAddress.substring(0,index);
		String ipE=rangeAddress.substring(index+1, rangeAddress.length());
		return rangeAddressExcept(ipS,ipE,except);
	}
	/**
	 * 排除IP地址段中某些IP地址，或者IP地址段
	 * @param ipS
	 * @param ipE
	 * @param except
	 * @return
	 */
	public static String rangeAddressExcept(String ipS,String ipE,String[] except){
		if(Assert.isEmptyString(ipS)||Assert.isEmptyString(ipE)||except==null||except.length==0){
			return null;
		}
		if(!ipS.matches("\\d+.\\d+.\\d+.\\d+")||!ipS.matches("\\d+.\\d+.\\d+.\\d+")){
			return null;
		}
		long ipSNum=ipToLong(ipS);
		long ipENum=ipToLong(ipE);
		String result=ipS+"-"+ipE+":"+ipSNum+"-"+ipENum+"%%";
		for(int i=0;i<except.length;i++){
			if(Assert.isEmptyString(except[i])){
				continue;
			}
			//			if(!except[i].matches("\\d+.\\d+.\\d+.\\d+")){
			//				continue;
			//			}
			if(Assert.isEmptyString(result)){
				return null;
			}
			String[] resultArr=result.split("%%");
			//			long exceptNum=ipToLong(except[i]);

			int exceptIndexLine=except[i].indexOf("-");
			if(exceptIndexLine==-1){
				int exceptIndexC=except[i].indexOf(":");
				long exceptNum;
				if(exceptIndexC==-1){
					if(!except[i].matches("\\d+.\\d+.\\d+.\\d+")){
						continue;
					}
					exceptNum=ipToLong(except[i]);
				}else{
					String numString=except[i].substring(exceptIndexC+1,except[i].length());
					if(!numString.matches("\\d+")){
						continue;
					}
					exceptNum=Long.parseLong(numString);
				}
				for(int j=0;j<resultArr.length;j++){
					if(Assert.isEmptyString(resultArr[j])){
						result=result.replaceAll("%%%%", "%%");
						continue;
					}

					int indexC=resultArr[j].indexOf(":");
					if(indexC==-1){
						result=result.replaceFirst(resultArr[j]+"%%", "");
						continue;
					}

					String ipNums=resultArr[j].substring(indexC+1, resultArr[j].length());

					if(Assert.isEmptyString(ipNums)){
						result=result.replaceFirst(resultArr[j]+"%%", "");
						continue;
					}


					int indexLine=ipNums.indexOf("-");
					if(indexLine==-1){
						if(!ipNums.matches("\\d+")){
							result=result.replaceFirst(resultArr[j]+"%%", "");
							continue;
						}
						if(Long.parseLong(ipNums)==exceptNum){
							result=result.replaceFirst(resultArr[j]+"%%", "");
						}else{
							continue;
						}
					}else{
						String ipStart=ipNums.substring(0, indexLine);
						String ipEnd=ipNums.substring(indexLine+1,ipNums.length());
						if(Assert.isEmptyString(ipStart)||!ipStart.matches("\\d+")||Assert.isEmptyString(ipEnd)||!ipEnd.matches("\\d+")){

							result=result.replaceAll(resultArr[j]+"%%", "");
							continue;
						}
						long ipStartLong=Long.parseLong(ipStart);
						long ipEndLong=Long.parseLong(ipEnd);
						if(exceptNum>=ipStartLong&&exceptNum<=ipEndLong){
							if(exceptNum==ipStartLong){
								if(ipStartLong+1==ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipEndLong)+":"+ipEndLong);
								}else{
									ipStartLong+=1;
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+"-"+longToIp(ipEndLong)+":"+ipStartLong+"-"+ipEndLong);

								}
							}else if(exceptNum==ipEndLong){
								if(ipEndLong-1==ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+":"+ipStartLong);
								}else{
									ipEndLong-=1;
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+"-"+longToIp(ipEndLong)+":"+ipStartLong+"-"+ipEndLong);
								}
							}else{
								if(exceptNum-1==ipStartLong&&exceptNum+1==ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+":"+ipStartLong+"%%"+longToIp(ipEndLong)+":"+ipEndLong);
								}else if(exceptNum-1==ipStartLong&&exceptNum+1!=ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+":"+ipStartLong+"%%"+longToIp(exceptNum+1)+"-"+longToIp(ipEndLong)+":"+(exceptNum+1)+"-"+ipEndLong);
								}else if(exceptNum-1!=ipStartLong&&exceptNum+1==ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipEndLong)+":"+ipEndLong+"%%"+longToIp(ipStartLong)+"-"+longToIp(exceptNum-1)+":"+ipStartLong+"-"+(exceptNum-1));
								}
								else{
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+"-"+longToIp(exceptNum-1)+":"+ipStartLong+"-"+(exceptNum-1)+"%%"+longToIp(exceptNum+1)+"-"+longToIp(ipEndLong)+":"+(exceptNum+1)+"-"+ipEndLong);
								}
							}
						}else{
							continue;
						}
					}
				}
			}else{
				int exceptIndexC=except[i].indexOf(":");
				if(exceptIndexC==-1){
					continue;
				}

				String numRangeString=except[i].substring(exceptIndexC+1,except[i].length());
				int exceptIndexLineA=numRangeString.indexOf("-");
				if(exceptIndexLineA==-1){
					continue;
				}
				String numStartString=numRangeString.substring(0,exceptIndexLineA);
				String numEndString=numRangeString.substring(exceptIndexLineA+1,numRangeString.length());
				if(!numStartString.matches("\\d+")||!numEndString.matches("\\d+")){
					continue;
				}
				long exceptNumStart=Long.parseLong(numStartString);
				long exceptNumEnd=Long.parseLong(numEndString);
				if(exceptNumEnd<exceptNumStart){
					continue;
				}

				for(int j=0;j<resultArr.length;j++){
					if(Assert.isEmptyString(resultArr[j])){
						result=result.replaceAll("%%%%", "%%");
						continue;
					}

					int indexC=resultArr[j].indexOf(":");
					if(indexC==-1){
						result=result.replaceFirst(resultArr[j]+"%%", "");
						continue;
					}

					String ipNums=resultArr[j].substring(indexC+1, resultArr[j].length());

					if(Assert.isEmptyString(ipNums)){
						result=result.replaceFirst(resultArr[j]+"%%", "");
						continue;
					}


					int indexLine=ipNums.indexOf("-");
					if(indexLine==-1){
						if(!ipNums.matches("\\d+")){
							result=result.replaceFirst(resultArr[j]+"%%", "");
							continue;
						}
						long ipLong=Long.parseLong(ipNums);
						if(ipLong>=exceptNumStart&&ipLong<=exceptNumEnd){
							result=result.replaceFirst(resultArr[j]+"%%", "");
						}else{
							continue;
						}
					}else{
						String ipStart=ipNums.substring(0, indexLine);
						String ipEnd=ipNums.substring(indexLine+1,ipNums.length());
						if(Assert.isEmptyString(ipStart)||!ipStart.matches("\\d+")||Assert.isEmptyString(ipEnd)||!ipEnd.matches("\\d+")){

							result=result.replaceAll(resultArr[j]+"%%", "");
							continue;
						}
						long ipStartLong=Long.parseLong(ipStart);
						long ipEndLong=Long.parseLong(ipEnd);
						if(exceptNumStart>=ipStartLong&&exceptNumEnd<=ipEndLong){
							if(exceptNumStart==ipStartLong&&exceptNumEnd==ipEndLong){
								result=result.replaceFirst(resultArr[j]+"%%", "");
							}else if(exceptNumStart==ipStartLong&&exceptNumEnd<ipEndLong){
								if(exceptNumEnd+1==ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipEndLong)+":"+ipEndLong);

								}else{
									result=result.replaceFirst(resultArr[j],longToIp(exceptNumEnd+1)+"-"+longToIp(ipEndLong)+":"+(exceptNumEnd+1)+"-"+ipEndLong);
								}
							}else if(exceptNumStart>ipStartLong&&exceptNumEnd==ipEndLong){
								if(exceptNumStart-1==ipStartLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+":"+ipStartLong);

								}else{
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+"-"+longToIp(exceptNumStart-1)+":"+ipStartLong+"-"+(exceptNumStart-1));
								}
							}else{

								if(exceptNumStart-1==ipStartLong&&exceptNumEnd+1==ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+":"+ipStartLong+"%%"+longToIp(ipEndLong)+":"+ipEndLong);
								}else if(exceptNumStart-1==ipStartLong&&exceptNumEnd+1!=ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+":"+ipStartLong+"%%"+longToIp(exceptNumEnd+1)+"-"+longToIp(ipEndLong)+":"+(exceptNumEnd+1)+"-"+ipEndLong);
								}else if(exceptNumStart-1!=ipStartLong&&exceptNumEnd+1==ipEndLong){
									result=result.replaceFirst(resultArr[j],longToIp(ipEndLong)+":"+ipEndLong+"%%"+longToIp(ipStartLong)+"-"+longToIp(exceptNumStart-1)+":"+ipStartLong+"-"+(exceptNumStart-1));
								}
								else{
									result=result.replaceFirst(resultArr[j],longToIp(ipStartLong)+"-"+longToIp(exceptNumStart-1)+":"+ipStartLong+"-"+(exceptNumStart-1)+"%%"+longToIp(exceptNumEnd+1)+"-"+longToIp(ipEndLong)+":"+(exceptNumEnd+1)+"-"+ipEndLong);
								}
							}

						}else{
							continue;
						}
					}
				}


			}
		}
		return result;
	}
	/**
	 * 排序算法 
	 * descOrAsc为0时，降序，为1时，升序
	 * @param idSort
	 * @param descOrAsc
	 * @return
	 */
	public static Integer[] sortAlgorithm(Integer[] idSort,int descOrAsc){
		if(idSort==null||idSort.length==0){
			return null;
		}
		int temp=0; 
		for (int i = 0; i < idSort.length-1 ; i++) { 
			for (int j = 0; j < idSort.length - i - 1; j++){
				if(descOrAsc==0){
					if (idSort[j]<idSort[j + 1]){
						temp=idSort[j];
						idSort[j]=idSort[j + 1];
						idSort[j + 1]=temp;
					} 
				}else if(descOrAsc==1){
					if (idSort[j]>idSort[j + 1]){
						temp=idSort[j];
						idSort[j]=idSort[j + 1];
						idSort[j + 1]=temp;
					} 
				}
			}
		}
		return idSort;
	}
}
