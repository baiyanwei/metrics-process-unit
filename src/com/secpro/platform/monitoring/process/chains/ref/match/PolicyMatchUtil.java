package com.secpro.platform.monitoring.process.chains.ref.match;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.process.chains.ref.standard.StandardConstant;
/**
 * 策略比对工具方法类
 * @author sxf
 *
 */
public class PolicyMatchUtil {
	private static PlatformLogger theLogger = PlatformLogger.getLogger(PolicyMatchUtil.class);
	/**
	 * 根据规则比对两个值
	 * 规则中支持&、|、()操作符
	 * 
	 * @param value1
	 * @param value2
	 * @param rule
	 * @return 比对成功返回1，比对不成功返回0
	 */
	public static int matchTwoValue(String value1,String value2,String rule){
		if(Assert.isEmptyString(value1)||Assert.isEmptyString(value2)||Assert.isEmptyString(rule)){
			return 0;
		}
		int matchResultFlag=0;
		int bracketsFlag=0;
		int inBracketsValue=0;
		String bracketsOper="";
		while(!Assert.isEmptyString(rule)){
			String[] ruleOneOperResult=getRuleOneOper(rule);
			if(Assert.isEmptyString(ruleOneOperResult[1])){
				String[] result=matchTwoValueByOper(value1,value2,rule);
				if(result==null){
					matchResultFlag=0;
					break;
				}
				rule=result[0];
				matchResultFlag=Integer.parseInt(result[1]);
			}else{
				rule=ruleOneOperResult[0];
				String ruleOper=ruleOneOperResult[1];
				if("(".equals(ruleOper)){
					if(Assert.isEmptyString(rule)){
						matchResultFlag=0;
						break;
					}
					bracketsFlag=1;
					String[] result=matchTwoValueByOper(value1,value2,rule);
					if(result==null){
						matchResultFlag=0;
						break;
					}
					rule=result[0];
					inBracketsValue=Integer.parseInt(result[1]);

				}else if(")".equals(ruleOper)){
					if("&".equals(bracketsOper)){
						if(inBracketsValue==0||matchResultFlag==0){
							matchResultFlag=0;
							break;
						}
					}else if("|".equals(bracketsOper)){
						if(inBracketsValue==1||matchResultFlag==1){
							matchResultFlag=1;
							break;
						}
					}else{
						matchResultFlag=0;
						break;
					}
					bracketsFlag=0;

				}else if("&".equals(ruleOper)){
					if(Assert.isEmptyString(rule)){
						matchResultFlag=0;
						break;
					}
					if(bracketsFlag==0){
						if(matchResultFlag==0){
							break;
						}
						if(rule.startsWith("(")){

							bracketsOper="&";
							continue;
						}else{
							String[] result=matchTwoValueByOper(value1,value2,rule);
							if(result==null){
								matchResultFlag=0;
								break;
							}
							rule=result[0];
							if(Integer.parseInt(result[1])==0){
								matchResultFlag=0;
								break;
							}
						}

					}else{
						if(inBracketsValue==0){
							inBracketsValue=0;
							rule=rule.substring(rule.indexOf(")"), rule.length());
						}else{
							String[] result=matchTwoValueByOper(value1,value2,rule);
							if(result==null){
								matchResultFlag=0;
								break;
							}
							rule=result[0];
							if(Integer.parseInt(result[1])==0){
								inBracketsValue=0;
								rule=rule.substring(rule.indexOf(")"), rule.length());
							}
						}

					}

				}else if("|".equals(ruleOper)){
					if(Assert.isEmptyString(rule)){
						matchResultFlag=0;
						break;
					}
					if(bracketsFlag==0){
						if(matchResultFlag==1){
							break;
						}

						if(rule.startsWith("(")){
							bracketsOper="|";
							continue;
						}else{
							String[] result=matchTwoValueByOper(value1,value2,rule);
							if(result==null){
								matchResultFlag=0;
								break;
							}
							rule=result[0];
							if(Integer.parseInt(result[1])==1){
								matchResultFlag=1;
								break;
							}
						}

					}else{
						if(inBracketsValue==1){
							inBracketsValue=1;
							rule=rule.substring(rule.indexOf(")"), rule.length());
						}else{
							String[] result=matchTwoValueByOper(value1,value2,rule);
							if(result==null){
								matchResultFlag=0;
								break;
							}
							rule=result[0];
							if(Integer.parseInt(result[1])==1){
								inBracketsValue=1;
								rule=rule.substring(rule.indexOf(")"), rule.length());
							}
						}
					}
				}
			}
		}
		return matchResultFlag;
	}
	/**
	 * 根据自定义比较符号，比较两个值
	 * 支持>、>=、<、<=、=、!=
	 * @param value1
	 * @param value2
	 * @param rule
	 * @return
	 */
	private static String[] matchTwoValueByOper(String value1,String value2,String rule){
		String[] ruleMatchOneValueResult=getRuleMatchOneValue(rule);
		if(ruleMatchOneValueResult==null){
			return null;
		}
		rule=ruleMatchOneValueResult[0];
		String name=ruleMatchOneValueResult[1];
		String matchOper=ruleMatchOneValueResult[2];
		if(Assert.isEmptyString(name)||Assert.isEmptyString(matchOper)){
			return null;
		}
		String[] result=new String[2];
		String firstValue=getNameValue(value1,name);
		String secondValue=getNameValue(value2,name);
		int matchResult=0;
		if(StandardConstant.SRC_IP_NAME.equals(name)||StandardConstant.DST_IP_NAME.equals(name)){
			matchResult=ipAddressOrRangeMatch(firstValue,secondValue,matchOper);
		}else if(StandardConstant.SERVICE_NAME.equals(name)){
			matchResult=serviceMatch(firstValue,secondValue,matchOper);
		}else{
			matchResult=otherMatch(firstValue,secondValue,matchOper);
		}
		result[0]=rule;
		if(matchResult==0){
			result[1]="0";
		}else{
			result[1]="1";
		}
		return result;
	}
	/**
	 * 除ip地址以及服务的其他类型数据的比对
	 * 支持比对符=、!=
	 * @param firstValue
	 * @param secondValue
	 * @param matchOper
	 * @return
	 */
	private static int otherMatch(String firstValue, String secondValue,String matchOper) {
		if("=".equals(matchOper)){
			if(Assert.isEmptyString(firstValue)&&Assert.isEmptyString(secondValue)){
				return 1;
			}else if(Assert.isEmptyString(firstValue)||Assert.isEmptyString(secondValue)){
				return 0;
			}else{
				return firstValue.equals(secondValue)?1:0;
			}
		}else if("!=".equals(matchOper)){
			if(Assert.isEmptyString(firstValue)&&Assert.isEmptyString(secondValue)){
				return 0;
			}else if(Assert.isEmptyString(firstValue)||Assert.isEmptyString(secondValue)){
				return 1;
			}else{
				return firstValue.equals(secondValue)?0:1;
			}
		}else{
			theLogger.debug("Does not support this match operator");
			return 0;
		}
	}
	/**
	 * service比对
	 * @param firstValue
	 * @param secondValue
	 * @param matchOper
	 * @return
	 */
	private static int serviceMatch(String firstValue, String secondValue,String matchOper) {
		if(Assert.isEmptyString(firstValue)||Assert.isEmptyString(secondValue)||Assert.isEmptyString(matchOper)){
			return 0;
		}
		
		if(">=".equals(matchOper)){
			int result=ipOrServiceEqualMatch(firstValue,secondValue);

			if(result==0){
				result=serviceLessOrMoreThanMatch(firstValue,secondValue);
			}
			return result;
		}else if(">".equals(matchOper)){
			return serviceLessOrMoreThanMatch(firstValue,secondValue);
		}else if("=".equals(matchOper)){
			return ipOrServiceEqualMatch(firstValue,secondValue);
		}else if("<=".equals(matchOper)){
			int result=ipOrServiceEqualMatch(firstValue,secondValue);
			if(result==0){
				result=serviceLessOrMoreThanMatch(secondValue,firstValue);
			}
			return result;
		}else if("<".equals(matchOper)){
			return serviceLessOrMoreThanMatch(secondValue,firstValue);

		}else {
			return 0;
		}
	}
	/**
	 * service比对>或者<
	 * 支持类型为tcp:1-65535:8080，icmp协议以及其他协议的比对
	 * @param firstValue
	 * @param secondValue
	 * @return
	 */
	private static int serviceLessOrMoreThanMatch(String firstValue,String secondValue){
		if(Assert.isEmptyString(firstValue)||Assert.isEmptyString(secondValue)){
			return 0;
		}
		String[] serviceFirstValues=firstValue.split(",");
		String[] serviceSecondValues=secondValue.split(",");
		int result=0;
		if(!Assert.isEmptyString(serviceSecondValues[0])&&"any".equals(serviceSecondValues[0].trim().toLowerCase())){
			result=0;
		}
		else if(!Assert.isEmptyString(serviceFirstValues[0])&&"any".equals(serviceFirstValues[0].trim().toLowerCase()))
		{
			result=1;

		}else{
			int number=0;
			int moreThanFlag=0;
			for(int i=0;i<serviceSecondValues.length;i++){
				if(Assert.isEmptyString(serviceSecondValues[i])){
					continue;
				}
				for(int j=0;j<serviceFirstValues.length;j++){
					if(Assert.isEmptyString(serviceFirstValues[j])){
						continue;
					}
					if(serviceFirstValues[j].equals(serviceSecondValues[i])){
						number++;
						break;
					}
					int secondColonIndex=serviceSecondValues[i].indexOf(":");
					int firstColonIndex=serviceFirstValues[j].indexOf(":");
					if(secondColonIndex==-1||firstColonIndex==-1){
						continue;
					}
					String secondProtocol=serviceSecondValues[i].substring(0,secondColonIndex);
					String firstProtocol=serviceFirstValues[j].substring(0,firstColonIndex);
					if(Assert.isEmptyString(secondProtocol)||Assert.isEmptyString(firstProtocol)){
						continue;
					}
					if(secondProtocol.equals(firstProtocol)){
						if(StandardConstant.ICMP_PROTOCOL.equals(secondProtocol)){
							int secondColonIndexS=serviceSecondValues[i].indexOf(":",secondColonIndex+1);
							int firstColonIndexS=serviceFirstValues[j].indexOf(":",firstColonIndex+1);
							String secondType=serviceSecondValues[i].substring(secondColonIndex+1, secondColonIndexS);
							String firstType=serviceFirstValues[j].substring(firstColonIndex+1, firstColonIndexS);
							String secondCode=serviceSecondValues[i].substring(secondColonIndexS+1,serviceSecondValues[i].length());
							String firstCode=serviceFirstValues[j].substring(firstColonIndexS+1,serviceFirstValues[j].length());
							if(Assert.isEmptyString(firstType)||Assert.isEmptyString(firstCode)||Assert.isEmptyString(secondType)||Assert.isEmptyString(secondCode)){
								continue;
							}
							if(firstType.equals(secondType)&&secondCode.equals(firstCode)){
								number++;
								break;
							}
						}else if(StandardConstant.OTHER_PROTOCOL.equals(secondProtocol)){
							String secondOther=serviceSecondValues[i].substring(secondColonIndex+1,serviceSecondValues[i].length());
							String firstOther=serviceFirstValues[j].substring(firstColonIndex+1,serviceFirstValues[j].length());
							if(Assert.isEmptyString(secondOther)||Assert.isEmptyString(firstOther)){
								continue;
							}
							if(secondOther.equals(firstOther)){
								number++;
								break;
							}
							//							int secondOtherLine=secondOther.indexOf("-");
							//							int firstOtherLine=firstOther.indexOf("-");
							//							if(secondOtherLine==-1){
							//								if(firstOtherLine==-1){
							//									continue;
							//								}else{
							//									long secondOtherL=Long.parseLong(secondOther);
							//									long firstOtherStart=Long.parseLong(firstOther.substring(0,firstOtherLine));
							//									long firstOtherEnd=Long.parseLong(firstOther.substring(firstOtherLine+1,firstOther.length()));
							//									if(secondOtherL>=firstOtherStart&&secondOtherL<=firstOtherEnd){
							//										number++;
							//										moreThanFlag=1;
							//										break;
							//									}
							//								}
							//							}else{
							//								if(firstOtherLine==-1){
							//									continue;
							//								}else{
							//									long secondOtherStart=Long.parseLong(secondOther.substring(0, secondOtherLine));
							//									long secondOtherEnd=Long.parseLong(secondOther.substring( secondOtherLine+1,secondOther.length()));
							//									long firstOtherStart=Long.parseLong(firstOther.substring(0,firstOtherLine));
							//									long firstOtherEnd=Long.parseLong(firstOther.substring(firstOtherLine+1,firstOther.length()));
							//									if(secondOtherStart>=firstOtherStart&&secondOtherEnd<=firstOtherEnd){
							//										number++;
							//										moreThanFlag=1;
							//										break;
							//									}
							//								}
							//							}


						}else{
							
							int secondColonIndexS=serviceSecondValues[i].indexOf(":",secondColonIndex+1);
							int firstColonIndexS=serviceFirstValues[j].indexOf(":",firstColonIndex+1);
							String secondSrcPort=serviceSecondValues[i].substring(secondColonIndex+1, secondColonIndexS);
							String firstSrcPort=serviceFirstValues[j].substring(firstColonIndex+1, firstColonIndexS);
							String secondDstPort=serviceSecondValues[i].substring(secondColonIndexS+1,serviceSecondValues[i].length());
							String firstDstPort=serviceFirstValues[j].substring(firstColonIndexS+1,serviceFirstValues[j].length());
							if(Assert.isEmptyString(secondSrcPort)||Assert.isEmptyString(secondDstPort)||Assert.isEmptyString(firstSrcPort)||Assert.isEmptyString(firstDstPort)){
								continue;
							}
							if(secondSrcPort.equals(firstSrcPort)){
								if(secondDstPort.equals(firstDstPort)){
									number++;
									break;
								}else{
									int secondDstLine=secondDstPort.indexOf("-");
									int firstDstLine=firstDstPort.indexOf("-");
									if(secondDstLine==-1){
										if(firstDstLine==-1){
											continue;
										}else{
											long secondDstPortL=Long.parseLong(secondDstPort);
											long firstDstPortStart=Long.parseLong(firstDstPort.substring(0,firstDstLine));
											long firstDstPortEnd=Long.parseLong(firstDstPort.substring(firstDstLine+1,firstDstPort.length()));
											if(secondDstPortL>=firstDstPortStart&&secondDstPortL<=firstDstPortEnd){
												number++;
												moreThanFlag=1;
												break;
											}
										}
									}else{
										if(firstDstLine==-1){
											continue;
										}else{
											long secondDstPortStart=Long.parseLong(secondDstPort.substring(0, secondDstLine));
											long secondDstPortEnd=Long.parseLong(secondDstPort.substring( secondDstLine+1,secondDstPort.length()));
											long firstDstPortStart=Long.parseLong(firstDstPort.substring(0,firstDstLine));
											long firstDstPortEnd=Long.parseLong(firstDstPort.substring(firstDstLine+1,firstDstPort.length()));
											if(secondDstPortStart>=firstDstPortStart&&secondDstPortEnd<=firstDstPortEnd){
												number++;
												moreThanFlag=1;
												break;
											}
										}
									}
								}
							}else{
								int secondSrcLine=secondSrcPort.indexOf("-");
								int firstSrcLine=firstSrcPort.indexOf("-");
								if(secondSrcLine==-1){
									if(firstSrcLine==-1){
										continue;
									}else{
										long secondSrcPortL=Long.parseLong(secondSrcPort);
										long firstSrcPortStart=Long.parseLong(firstSrcPort.substring(0,firstSrcLine));
										long firstSrcPortEnd=Long.parseLong(firstSrcPort.substring(firstSrcLine+1,firstSrcPort.length()));
										if(secondSrcPortL>=firstSrcPortStart&&secondSrcPortL<=firstSrcPortEnd){
											if(secondDstPort.equals(firstDstPort)){
												number++;
												moreThanFlag=1;
												break;
											}
											int secondDstLine=secondDstPort.indexOf("-");
											int firstDstLine=firstDstPort.indexOf("-");
											if(secondDstLine==-1){
												if(firstDstLine==-1){
													continue;
												}else{
													long secondDstPortL=Long.parseLong(secondDstPort);
													long firstDstPortStart=Long.parseLong(firstDstPort.substring(0,firstDstLine));
													long firstDstPortEnd=Long.parseLong(firstDstPort.substring(firstDstLine+1,firstDstPort.length()));
													if(secondDstPortL>=firstDstPortStart&&secondDstPortL<=firstDstPortEnd){
														number++;
														moreThanFlag=1;
														break;
													}
												}
											}else{
												if(firstDstLine==-1){
													continue;
												}else{
													long secondDstPortStart=Long.parseLong(secondDstPort.substring(0, secondDstLine));
													long secondDstPortEnd=Long.parseLong(secondDstPort.substring( secondDstLine+1,secondDstPort.length()));
													long firstDstPortStart=Long.parseLong(firstDstPort.substring(0,firstDstLine));
													long firstDstPortEnd=Long.parseLong(firstDstPort.substring(firstDstLine+1,firstDstPort.length()));
													if(secondDstPortStart>=firstDstPortStart&&secondDstPortEnd<=firstDstPortEnd){
														number++;
														moreThanFlag=1;
														break;
													}
												}
											}
										}
									}
								}else{
									if(firstSrcLine==-1){
										continue;
									}else{
										long secondSrcPortStart=Long.parseLong(secondSrcPort.substring(0,secondSrcLine));
										long secondSrcPortEnd=Long.parseLong(secondSrcPort.substring(secondSrcLine+1,secondSrcPort.length()));
										long firstSrcPortStart=Long.parseLong(firstSrcPort.substring(0,firstSrcLine));
										long firstSrcPortEnd=Long.parseLong(firstSrcPort.substring(firstSrcLine+1,firstSrcPort.length()));
										if(secondSrcPortStart>=firstSrcPortStart&&secondSrcPortEnd<=firstSrcPortEnd){
											if(secondDstPort.equals(firstDstPort)){
												number++;
												moreThanFlag=1;
												break;
											}
	
											int secondDstLine=secondDstPort.indexOf("-");
											int firstDstLine=firstDstPort.indexOf("-");
											if(secondDstLine==-1){
												if(firstDstLine==-1){
													continue;
												}else{
													long secondDstPortL=Long.parseLong(secondDstPort);
													long firstDstPortStart=Long.parseLong(firstDstPort.substring(0,firstDstLine));
													long firstDstPortEnd=Long.parseLong(firstDstPort.substring(firstDstLine+1,firstDstPort.length()));
													if(secondDstPortL>=firstDstPortStart&&secondDstPortL<=firstDstPortEnd){
														number++;
														moreThanFlag=1;
														break;
													}
												}
											}else{
												if(firstDstLine==-1){
													continue;
												}else{
													long secondDstPortStart=Long.parseLong(secondDstPort.substring(0, secondDstLine));
													long secondDstPortEnd=Long.parseLong(secondDstPort.substring( secondDstLine+1,secondDstPort.length()));
													long firstDstPortStart=Long.parseLong(firstDstPort.substring(0,firstDstLine));
													long firstDstPortEnd=Long.parseLong(firstDstPort.substring(firstDstLine+1,firstDstPort.length()));
													if(secondDstPortStart>=firstDstPortStart&&secondDstPortEnd<=firstDstPortEnd){
														number++;
														moreThanFlag=1;
														break;
													}
												}
											}
										}
									}
								}
							}
						}
					}else{
						continue;
					}
					
				}

				if(moreThanFlag!=0&&number==serviceSecondValues.length){
					result=1;
				}else{
					result=0;
				}
			}
		}
		return result;

	}
	/**
	 * ip地址，以及ip地址段比对
	 * @param firstValue
	 * @param secondValue
	 * @param matchOper
	 * @return
	 */
	private static int ipAddressOrRangeMatch(String firstValue, String secondValue,String matchOper) {
		if(Assert.isEmptyString(firstValue)||Assert.isEmptyString(secondValue)||Assert.isEmptyString(matchOper)){
			return 0;
		}
		if(">=".equals(matchOper)){
			int result=ipOrServiceEqualMatch(firstValue,secondValue);

			if(result==0){
				result=ipLessOrMoreThanMatch(firstValue,secondValue);
			}
			return result;
		}else if(">".equals(matchOper)){
			return ipLessOrMoreThanMatch(firstValue,secondValue);
		}else if("=".equals(matchOper)){
			return ipOrServiceEqualMatch(firstValue,secondValue);
		}else if("<=".equals(matchOper)){
			int result=ipOrServiceEqualMatch(firstValue,secondValue);
			if(result==0){
				result=ipLessOrMoreThanMatch(secondValue,firstValue);
			}
			return result;
		}else if("<".equals(matchOper)){
			return ipLessOrMoreThanMatch(secondValue,firstValue);

		}else {
			return 0;
		}

	}
	/**
	 * 比对服务或者ip地址类型的两个值，是否符合=操作
	 * @param firstValue
	 * @param secondValue
	 * @return
	 */
	private static int ipOrServiceEqualMatch(String firstValue,String secondValue){
		if(Assert.isEmptyString(firstValue)||Assert.isEmptyString(secondValue)){
			return 0;
		}
		String[] ipFirstValues=firstValue.split(",");
		String[] ipSecondValues=secondValue.split(",");
		int result=0;
		if(firstValue.equals(secondValue)){
			return 1;
		}
		if(!Assert.isEmptyString(ipFirstValues[0])&&!Assert.isEmptyString(ipSecondValues[0])&&"any".equals(ipFirstValues[0].trim().toLowerCase())&&"any".equals(ipSecondValues[0].trim().toLowerCase()))
		{
			return 1;
		}
		if(ipFirstValues.length==ipSecondValues.length){

			int number=0;
			for(int i=0;i<ipFirstValues.length;i++){
				if(Assert.isEmptyString(ipFirstValues[i])){
					continue;
				}
				for(int j=0;j<ipSecondValues.length;j++){
					if(Assert.isEmptyString(ipSecondValues[j])){
						continue;
					}
					if(ipFirstValues[i].equals(ipSecondValues[j])){
						number++;
						break;
					}
				}
			}
			if(number==ipFirstValues.length){
				result=1;
			}
		}else{
			result=0;
		}
		return result;
	}
	/**
	 * ip地址以及地址段比对>、<
	 * @param firstValue
	 * @param secondValue
	 * @return
	 */
	private static int ipLessOrMoreThanMatch(String firstValue,String secondValue){
		if(Assert.isEmptyString(firstValue)||Assert.isEmptyString(secondValue)){
			return 0;
		}
		String[] ipFirstValues=firstValue.split(",");
		String[] ipSecondValues=secondValue.split(",");
		int result=0;
		if(!Assert.isEmptyString(ipSecondValues[0])&&"any".equals(ipSecondValues[0].trim().toLowerCase())){
			result=0;
		}
		else if(!Assert.isEmptyString(ipFirstValues[0])&&"any".equals(ipFirstValues[0].trim().toLowerCase()))
		{
			result=1;

		}else{
			int number=0;
			int moreThanFlag=0;
			for(int i=0;i<ipSecondValues.length;i++){
				if(Assert.isEmptyString(ipSecondValues[i])){
					continue;
				}
				for(int j=0;j<ipFirstValues.length;j++){
					if(Assert.isEmptyString(ipFirstValues[j])){
						continue;
					}
					int secondLineIndex=ipSecondValues[i].indexOf("-");
					int firstLineIndex=ipFirstValues[j].indexOf("-");
					if(secondLineIndex==-1){
						if(firstLineIndex==-1){
							if(ipFirstValues[j].equals(ipSecondValues[i])){
								number++;
								break;
							}
						}else{
							int secondColonIndex=ipSecondValues[i].indexOf(":");
							int firstColonIndex=ipFirstValues[j].indexOf(":");
							if(secondColonIndex==-1||firstColonIndex==-1){
								continue;
							}
							long secondIPAddress=Long.parseLong(ipSecondValues[i].substring(secondColonIndex+1, ipSecondValues[i].length()));
							String firstRangeAddress=ipFirstValues[j].substring(firstColonIndex+1, ipFirstValues[j].length());
							int firstLineIndexA=firstRangeAddress.indexOf("-");
							if(firstLineIndexA==-1){
								continue;
							}
							long firstStartIp=Long.parseLong(firstRangeAddress.substring(0, firstLineIndexA));
							long firstEndIp=Long.parseLong(firstRangeAddress.substring(firstLineIndexA+1,firstRangeAddress.length()));
							if(secondIPAddress>=firstStartIp&&secondIPAddress<=firstEndIp){
								number++;
								moreThanFlag=1;
								break;
							}
						}
					}else{
						if(firstLineIndex==-1){
							continue;
						}else{
							if(ipFirstValues[j].equals(ipSecondValues[i])){
								number++;
								break;
							}
							int secondColonIndex=ipSecondValues[i].indexOf(":");
							int firstColonIndex=ipFirstValues[j].indexOf(":");
							if(secondColonIndex==-1||firstColonIndex==-1){
								continue;
							}
							String secondRangeAddress=ipSecondValues[i].substring(secondColonIndex+1, ipSecondValues[i].length());
							int secondLineIndexA=secondRangeAddress.indexOf("-");
							if(secondLineIndexA==-1){
								continue;
							}
							long secondStartIp=Long.parseLong(secondRangeAddress.substring(0, secondLineIndexA));
							long secondEndIp=Long.parseLong(secondRangeAddress.substring(secondLineIndexA+1,secondRangeAddress.length()));
							String firstRangeAddress=ipFirstValues[j].substring(firstColonIndex+1, ipFirstValues[j].length());
							int firstLineIndexA=firstRangeAddress.indexOf("-");
							if(firstLineIndexA==-1){
								continue;
							}
							long firstStartIp=Long.parseLong(firstRangeAddress.substring(0, firstLineIndexA));
							long firstEndIp=Long.parseLong(firstRangeAddress.substring(firstLineIndexA+1,firstRangeAddress.length()));
							if(secondStartIp>=firstStartIp&&secondEndIp<=firstEndIp){
								number++;
								moreThanFlag=1;
								break;
							}
						}

					}
				}
			}
			if(moreThanFlag!=0&&number==ipSecondValues.length){
				result=1;
			}else{
				result=0;
			}
		}
		return result;
	}
	public static String getNameValue(String allValue, String oneValue) {
		String regex=oneValue+"=\\((.*?)\\)";
		Pattern pattern = Pattern.compile(regex);
		Matcher mat = pattern.matcher(allValue);
		if (mat.find()) {
			return mat.group(1);
		}
		return null;
	}
	/**
	 * 返回自定义规则中名称，以及比对操作符
	 * @param rule
	 * @return
	 */
	private static String[] getRuleMatchOneValue(String rule) {
		String[] result=new String[3];
		int colonIndex=rule.indexOf("::");
		if(colonIndex==-1){
			return null;
		}
		int andIndex=rule.indexOf("&");
		int orIndex=rule.indexOf("|");
		int bracketsIndex=rule.indexOf(")");
		if(andIndex==-1&&orIndex==-1&&bracketsIndex==-1){
			result[0]="";
			result[1]=rule.substring(0,colonIndex);
			result[2]=rule.substring(colonIndex+2,rule.length());
		}else{
			if(andIndex==-1){
				andIndex=rule.length()+1;
			}
			if(orIndex==-1){
				orIndex=rule.length()+1;
			}
			if(bracketsIndex==-1){
				bracketsIndex=rule.length()+1;
			}
			int smallIndex=andIndex;
			if(orIndex<smallIndex){
				smallIndex=orIndex;
			}
			if(bracketsIndex<smallIndex){
				smallIndex=bracketsIndex;
			}
			result[0]=rule.substring(smallIndex,rule.length());
			result[1]=rule.substring(0,colonIndex);
			result[2]=rule.substring(colonIndex+2,smallIndex);
		}
		return result;
	}
	/**
	 * 返回自定义规则中的操作符，如& | ()
	 * @param rule
	 * @return
	 */
	private static String[] getRuleOneOper(String rule) {
		String[] ruleAndOper=new String[2];
		if(rule.startsWith("(")){
			ruleAndOper[0]=rule.substring(1, rule.length());
			ruleAndOper[1]="(";
		}else if(rule.startsWith(")")){
			ruleAndOper[0]=rule.substring(1, rule.length());
			ruleAndOper[1]=")";
		}else if(rule.startsWith("&")){
			ruleAndOper[0]=rule.substring(1, rule.length());
			ruleAndOper[1]="&";
		}else if(rule.startsWith("|")){
			ruleAndOper[0]=rule.substring(1, rule.length());
			ruleAndOper[1]="|";
		}

		return ruleAndOper;
	}
}
