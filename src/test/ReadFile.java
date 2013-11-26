package test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
	private static String fileName="F:\\ca.cfg";
	private static String ruleName="F:\\qmrule.txt";
	private static String containName="F:\\contain.txt";
	public static String readFile() {
		FileReader fileRead = null;
		BufferedReader buffRead=null;
		try {
			fileRead = new FileReader(new File(fileName));
			buffRead=new BufferedReader(fileRead);
			StringBuilder result=new StringBuilder();
			String ss;
			while((ss=buffRead.readLine())!=null){
				result.append(ss+"^");
			}
			return result.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public static String readRule(String filePath) {
		FileReader fileRead = null;
		BufferedReader buffRead=null;
		try {
			fileRead = new FileReader(new File(filePath));
			buffRead=new BufferedReader(fileRead);
			StringBuilder result=new StringBuilder();
			String ss;
			while((ss=buffRead.readLine())!=null){
				result.append(ss+"%%");
			}
			return result.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
