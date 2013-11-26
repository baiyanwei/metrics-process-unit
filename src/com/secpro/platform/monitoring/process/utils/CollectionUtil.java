package com.secpro.platform.monitoring.process.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
/**
 * 集合类型数据删除方法
 * @author sxf
 *
 */
public class CollectionUtil {
	public static void listRemove(Collection collect,List deleteList){
		if(collect==null||collect.size()==0||deleteList==null||deleteList.size()==0){
			return;
		}
		for(Object obj:deleteList){
			if(obj!=null){
				collect.remove(obj);
			}
		}
	}
	public static void mapRemove(Map map,List deleteList){
		if(map==null||map.size()==0||deleteList==null||deleteList.size()==0){
			return;
		}
		for(Object obj:deleteList){
			if(obj!=null){
				map.remove(obj);
			}
		}
	}
}
