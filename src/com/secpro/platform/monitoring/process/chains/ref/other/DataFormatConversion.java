package com.secpro.platform.monitoring.process.chains.ref.other;

import com.secpro.platform.core.utils.Assert;

public class DataFormatConversion {
	public static final String CONTAIN_PROPERTY_NAME="contain";
	public static final String CONFLICT_PROPERTY_NAME="conflict";
	public static String conversion(String beforeData,String propertyName){
		if(Assert.isEmptyString(beforeData)||Assert.isEmptyString(propertyName)){
			return beforeData;
		}
		if(CONTAIN_PROPERTY_NAME.equals(propertyName)){
			beforeData=beforeData.replaceAll("::", "包含策略:");
		}else if(CONFLICT_PROPERTY_NAME.equals(propertyName)){
			beforeData=beforeData.replaceAll("::", "冲突策略:");
		}
		beforeData=beforeData.replaceAll("%%", ";");
		return beforeData;
	}
}
