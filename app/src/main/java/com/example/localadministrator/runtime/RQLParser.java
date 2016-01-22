package com.example.localadministrator.runtime;

import java.util.Arrays;
import java.lang.Long;
import java.util.Date;

public class RQLParser {
	String RQL;
	String DeviceName = "";
	String DeviceType = "";
	String Conditions = "";
	String Services   = "";
	String pureRQL = "";
	String[] parameters=null;
	Long initTime ;
	Long distriTime;
	static final int UNPROCESSED=0;
	static final int UNDERGOING=1;
	static final int FINISHED=2;
	int status = UNPROCESSED;
	//Here we must make sure that the -o of the RQL before | is the -i of the RQl after |

	//do we need to setup the reflection between indicators and real devices?
	
	public RQLParser(String inputRQL){
		this.RQL = inputRQL;
		initTime = System.currentTimeMillis();
		System.out.println("the initTime is :"+initTime);
		parseRQL();
	}

	public int getStatus(){
		return this.status;
	}
	//change the tasks' status
	// ???why their only one status change what happened to the other status
	public void changeStatus(int newStatus){
		if(status == UNPROCESSED && newStatus==UNDERGOING){
			distriTime =  System.currentTimeMillis();
			System.out.println("the distriTime is:"+distriTime);
		}
		// 0->1, 1->0, 1->2; only these should be allowed. to-be-done
		this.status = newStatus;
	}

	public void print(){
		String printInfo = "RQL:"+pureRQL+"\n"+"device:"+DeviceName+DeviceType+"\n"
				+"Conditions:"+Conditions+"\nServices"+Services+"\nstatus"+status+"\n";
		System.out.println("the printInfo is :"+printInfo);
	}

	//the correctness of RQL should be a plugin of eclipse made from ANTLE
	//then, parse RQL for two reasons:
	//1. find the Target Device: input RQL, output modified RQL and requirements for devices. 
	//2. execute the detailed task

	public void parseRQL(){
		RQLCommand rqlc;
		//do we want to add exception throw strategy here????
		//to-be-modified
		if(RQL.indexOf("|")>0){
			//one RQL script contains multiple RQL command. 
			String[] rqlCommands = RQL.split("\\|");
			int ii;
			for(ii=0;ii<rqlCommands.length;ii++){
				rqlc = new RQLCommand(rqlCommands[ii]);
				DeviceName += " "+rqlc.reqDeviceName();
				DeviceType += " "+rqlc.reqDeviceType();		
				Conditions += " "+rqlc.reqCondition();
				Services   += " "+rqlc.reqServiceType();
				pureRQL += "|"+rqlc.getPureRQL();
				parameters = rqlc.reqNoun_service_parameters();
			}
			DeviceName = DeviceName.trim();
			DeviceType = DeviceType.trim();
			Conditions = Conditions.trim();
			Services =Services.trim();
			pureRQL =pureRQL.replaceFirst("\\|", "");

		}else{
			rqlc = new RQLCommand(RQL);
			DeviceName = rqlc.reqDeviceName();
			DeviceType = rqlc.reqDeviceType();		
			Conditions = rqlc.reqCondition();
			Services   = rqlc.reqServiceType();
			pureRQL    = rqlc.getPureRQL();
			parameters = rqlc.reqNoun_service_parameters();
		}
		
	}

	public boolean checkGrammar(){
		return true;
	}
	
	public String getDeviceName(){
		return DeviceName;
	}
	
	public String getDeviceType(){
		return DeviceType;
	}
	
	public String getConditions(){
		return Conditions;
	}
	
	public String getServices(){
		return Services;
	}
	
	public String getPureRQL(){
		return pureRQL;
	}
	
	private class RQLCommand{
		public String rqlCommand;
		private String verb;
		private String noun;
		private String adverb;
		
		private String noun_device_indicator;
		private String noun_device_name;
		private String noun_device_type;
		
		private String noun_service_indicator;
		private String noun_service_name;	
		private String[] noun_service_parameters;

		String[] verbs = {"pull","push","execute"};
		String[] device_owner = {"trusted","any","other"};
		String[] device_type  = {"smartphone","tablet","glasses","watch"};
		String[] adverbs       = {"--opti=","--cons="};
		
		public RQLCommand(String inputRQLCommand){
			inputRQLCommand.replace("]"," ");
			inputRQLCommand.replace("["," ");
			rqlCommand = inputRQLCommand.trim();
			this.parse();
			System.out.println("the rql is "+rqlCommand);
			//this.print();
		}
		
		public void print(){
			String printInfo = "RQL:"+rqlCommand+"\n"+"verb, noun, adverb:"+verb+noun+adverb+"\n"+"device:"+noun_device_name+noun_device_type+"\n"
					+"service:"+noun_service_name+"\n"; 
			System.out.println("the rql info is:"+printInfo);
		}
		
		public boolean parse(){
			//step 1: parse noun and verb;
			String[] tmp = rqlCommand.split(" ");
			if(tmp.length==2){
				this.verb = tmp[0];
				this.noun = tmp[1];
				this.adverb = "";
			}else if(tmp.length==3){
				this.verb = tmp[0];
				this.noun = tmp[1];
				this.adverb = tmp[2];
			}
			else{
				System.out.println("RQL Parser Error: missing verb / noun"+rqlCommand);
				return false;
			}
			//step 2: separate device indicator and service indicator
			if(noun.indexOf(":")>0){
				tmp = null;
				tmp = noun.split(":");
				noun_device_indicator = tmp[0]; //get the device info
				noun_service_indicator = tmp[1]; //get the service info
			}else{
				noun_device_indicator = "";
				noun_service_indicator = noun;
			}
			//step 3.1 : parse device indicator
			if(noun_device_indicator.indexOf("/")>0){
				tmp = null;
				tmp = noun_device_indicator.split("/");
				noun_device_name = tmp[0];
				noun_device_type = tmp[1];
			}else{
				noun_device_name = noun_device_indicator;
				noun_device_type = "";
			}
			
			//step 3.2 : parse service indicator
			tmp = null;
			tmp  = noun_service_indicator.split("/");
			
			if(tmp.length>2){
				// used to get the parameters of the service, the filePath etc.
				noun_service_parameters = new String[tmp.length-2];

				for(int ii=2;ii<tmp.length;ii++){
					noun_service_parameters[ii-2] = tmp[ii];

					System.out.println("the parameters is:"+noun_service_parameters);
				}
			}
			noun_service_name = tmp[0]+"/"+tmp[1];
			
			return this.grammarCheck();
		}
		
		public boolean grammarCheck(){
			if(!Arrays.asList(verbs).contains(this.verb)){
				return false;
			}
			if(this.adverb!="" && !Arrays.asList(adverbs).contains(this.adverb.substring(0, 7))){
				System.out.println("RQL Parser Error: wrong verb / adverb");
				return false;
			}
			return true;
			
		}
		
		public String reqDeviceType(){
			return this.noun_device_type;
		}
		public String reqDeviceName(){
			return this.noun_device_name;
		}
		public String reqServiceType(){
			return this.noun_service_name;
		}
		public String[] reqNoun_service_parameters(){
			return this.noun_service_parameters;
		}
		public String reqCondition(){
			return this.adverb;
		}
		
		public String getPureRQL(){
			return this.verb+" "+this.noun_service_indicator;
		}
		
	}


}
