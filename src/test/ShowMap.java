package test;

import java.util.Map;
import java.util.Set;

public class ShowMap {
	public static void showMap(Map<String,String> map){
		if(map==null){
			return;
		}
		Set<String> set=map.keySet();
		for(String key:set){
			System.out.println(key+":"+map.get(key));
		}
	}
	public static void showMap3(Map<String, Map<String, Map<String,String>>> map){
		if(map==null){
			return;
		}
		Set<String> s=map.keySet();
		for(String str1:s)
		{
			System.out.println("first:"+str1);
			Map<String,Map<String,String>> map1=map.get(str1);
			Set<String>s1=map1.keySet();
			for(String str2:s1)
			{
				System.out.println("second:"+str2);
				Map<String,String> map2=map1.get(str2);
				Set<String> s2=map2.keySet();
				for(String str3:s2)
				{
					System.out.println("third:"+str3+":"+map2.get(str3));
				}
			}
		}
	}
	public static void showMap2(Map<String, Map<String,String>> map){
		if(map==null){
			return;
		}
		Set<String>s1=map.keySet();
		for(String str1:s1)
		{
			System.out.println("first:"+str1);
			Map<String,String> map2=map.get(str1);
			Set<String> s2=map2.keySet();
			for(String str2:s2)
			{
				System.out.println("second:"+str2+":"+map2.get(str2));
			}
		}
	}
}