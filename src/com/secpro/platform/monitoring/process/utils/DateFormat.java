package com.secpro.platform.monitoring.process.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 时间相关操作工具类
 * @author sxf
 *
 */
public class DateFormat {
	public static String timestampFormat(Long timestamp){
		Date date=new Date(timestamp);
		SimpleDateFormat simpleFormat=new SimpleDateFormat("yyyyMMddHHmmss");
		return simpleFormat.format(date);
	}
	public static String getNowDate(){
		Date date=new Date();
		SimpleDateFormat simpleFormat=new SimpleDateFormat("yyyyMMddHHmmss");
		return simpleFormat.format(date);
	}
}
