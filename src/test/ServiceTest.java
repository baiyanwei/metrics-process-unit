package test;

import com.secpro.platform.core.services.IService;

public class ServiceTest implements IService{

	@Override
	public void start() throws Exception {
		testSSH();
		
	}
	public void testSSH(){
		String value=ReadFile.readFile();
//		String rule=ReadFile.readRule("F:\\rule.txt");
//		System.out.println(rule);
//		Long start=System.currentTimeMillis();
//		ConfigAndPolicyStandard config=new ConfigAndPolicyStandard(value,rule,"311","127.0.0.1");
//		String[] result=config.configAndPolicyStandard();
//		String[] resultSplit=result[0].split("%%");
//		for(int i=0;i<resultSplit.length;i++){
//			System.out.println(resultSplit[i]);
//		}
//		System.out.println(System.currentTimeMillis()-start);
//		//System.out.println(StandardUtil.rangeAddressExcept("10.1.1.1", "10.1.1.254", new String[]{"10.1.1.10-10.1.1.200:"+StandardUtil.ipToLong("10.1.1.10")+"-"+StandardUtil.ipToLong("10.1.1.200")}));
//		Map<String,Object> rawData=new HashMap<String,Object>();
//		rawData.put("resID", 41l);
//		rawData.put(MetaDataConstant.EXECUTE_RESULT,result);
//		rawData.put("taskCode", "0001");
//		SSHMatchBaseline ssh=new SSHMatchBaseline();
//		ssh.dataProcess(rawData);
		//DBStorage dbStorage=new SSHTelnetDBStorageAdapter(rawData);
		//dbStorage.start();	
//		String containRule=ReadFile.readRule("F:\\contain.txt");
//		
//		
//		PolicyContainAndConflict p=new PolicyContainAndConflict(result[1],containRule);
//		String[] resultcontain=p.policyContainAndConflict();
//		if(resultcontain!=null){
//		System.out.println(resultcontain[0]);
//		System.out.println(resultcontain[1]);
//		}
	}
	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
