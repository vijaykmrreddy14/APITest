package com.RestAssuredAPI;

import java.io.File;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import com.codoid.products.fillo.Recordset;

import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Execution_Initiator_API extends Global_Variables {
	
	
	
	@Test
	public void API_Automation()
	{
	 try
		{
			String strDriverQry,strAction,strUrl,strTcDriverQry;
			envExecStatus.set("Pass");
			LocalDateTime dteStartTime; //need to be developed
			Execute_APIActions objAPIAction = new  Execute_APIActions();
			Response_Validation objResVal = new Response_Validation();
			General_Utilities objGenUtil = new General_Utilities();
			envRunId.set(objGenUtil.append_TimeStamp("API_Execution")); //RunId for every execution //need to be developed
			if (System.getProperty("environment") == null){
				System.setProperty("environment","qa");
				System.setProperty("envJenkinsRun","No");
			}
			else
			{
				envReportLocPath.set("./env_"+System.getProperty("environment")+"/execution_Reports/"+envRunId.get());
				System.setProperty("envJenkinsRun","Yes");
			}
			
			strDriverQry="Select * from API_Business_Flow where Exec_Flag='YES'";
			
			Recordset recTCDriver= Data_Connections.qry_DataFile(strDriverQry);//Updated by vijay
			
			while(recTCDriver.next()){
				String[] arrDependTcs=null;
				envReExecCnt.set(0);//setting re execution count to 0
				ArrayList<String> arrList = new ArrayList<String>();
				String strpreReq = recTCDriver.getField("PreRequest");
				if(strpreReq != ""){
				  arrDependTcs = strpreReq.split(";");
				  for(int incPreTcs=0;incPreTcs<=arrDependTcs.length-1;incPreTcs++){
					File preReqPath = new File("./env_" + System.getProperty("environment") + "/execution_Reports/" + envRunId.get() + "/" + arrDependTcs[incPreTcs]);//checking if the prerequis
					if(!preReqPath.exists()) arrList.add(arrDependTcs[incPreTcs]);
				  }
					arrList.add(recTCDriver.getField("Test_Case_Name"));
				}
				else
				 arrList.add(recTCDriver.getField("Test_Case_Name"));
				for(int incTcs = 0;incTcs<arrList.size();incTcs++){
					strDriverQry="select * from API_Business_Flow where Test_Case_Name='"+arrList.get(incTcs)+"'";
					Recordset recDriver= Data_Connections.qry_DataFile(strDriverQry);
					recDriver.next();
					dteStartTime = LocalDateTime.now();
					envTCName.set(recDriver.getField("Test_Case_Name"));
					envFnName.set(recDriver.getField("Funtionality"));
					envTemplatePth.set("JSON_Templates/" + envFnName.get() + "/" + envTCName.get()+ ".JSON"); //changes made by VJ
					strAction = recDriver.getField("Action");
					envExecType.set(recDriver.getField("Val_Type"));
					strUrl = recDriver.getField("URL").replace("<env>", System.getProperty("environment"));
					//if(envReExecFlag.get() != "Yes") Custom_Reports.strtTest(envTCName.get().toString()); //Reports
					envReExecFlag.set("No");
					int intIteration = 1;
					Custom_Reports.print_Report("INFO", "********Execution Initiation********", "Execution is in progress for the test case" + envTCName.get() + ". Action : "+strAction);
					 //envReportPath.set("./env" + System.getProperty("environment") + "/execution_Reports/" + envRunId.get() + "/" + envTCName.get()); 
	
					envReportPath.set(envReportLocPath.get() + "/" + envTCName.get());//Updated by vj
					Response response = null;
				  
				   switch(strAction){
					  case "GET":
						  String strData, strGetQry, strHdeAuth;
						  strGetQry="Select * from API_GET_Input_Data where Test_Case_Name='"+ envTCName.get() + "' and Iteration > 0";
						  Recordset recGetdata = Data_Connections.qry_DataFile(strGetQry);
						  while(recGetdata.next()){
							  Custom_Reports.print_Report("INFO","Iteration","<b>*******Executing Iteration"+ intIteration+"********</b>");
							  strData = recGetdata.getField("Input_Data");
							  strData = objGenUtil.build_GETRequest(strData,recGetdata);
							  strHdeAuth = recGetdata.getField("auth_Header");
							  response = objAPIAction.perform_GetAction(strUrl, strData, strHdeAuth);
							  objResVal.perform_ResponseAction(response,recGetdata,intIteration);
							  intIteration = intIteration + 1;	
						  }
					  break;
					  case "PUT":
					  case "POST":
						 String strPostQry;
						 strPostQry = "Select * from API_Request_Input_Data_"+System.getProperty("environment")+" where Test_Case_Name='" + envTCName.get() +"' and Iteration >0";
						 Recordset recPostdata = Data_Connections.qry_DataFile(strPostQry);
						 while(recPostdata.next()){
							 Custom_Reports.print_Report("INFO","Iteration","<b> *******Executing Iteration"+ intIteration+"*******</b>");
							 response = objAPIAction.perform_POSTAction(strUrl,recPostdata,intIteration,strAction); 
							 objResVal.perform_ResponseAction(response,recPostdata,intIteration);
							intIteration = intIteration + 1;
						}
					break;
					}
					if(envReExecFlag.get() == "Yes"){
					   incTcs = incTcs-1;// To re-execute the UI helper Get service untill the status is completed or failed
					   Custom_Reports.print_Report("INFO","Re-Executing Service", "Re-Executing the service" + envTCName.get());
					}
					else{
					   execution_WrapUp(dteStartTime, envTCName.get());
					}
					   recDriver = null;
				}
			}
			//Custom_Reports.print_Report("FAIL","Exception in Driver method", "Exeception Details: " + e);
			 Custom_Reports.copy_LatestReport();
			 recTCDriver = null;

		}catch (Exception e){
			   Custom_Reports.print_Report("FAIL","Exception in Driver method", "Exeception Details: " + e);
			   Custom_Reports.endReport();
			   Custom_Reports.copy_LatestReport();
			   Custom_Reports.log.error("Exception");
		}
	}
				
		
	
	
	public void execution_WrapUp(LocalDateTime dteStartTime, String strTC_Name) {
		long lngExecutedTime;
		LocalDateTime dteEndTime = LocalDateTime.now();
		lngExecutedTime = (Duration.between(dteStartTime,dteEndTime).toMillis())/1000;
		String strWrpQry = "Update API_Business_Flow Set Time_Taken="+lngExecutedTime + ", Last_Executed='"+ dteEndTime + "' where Test_Case_Name='" +strTC_Name + "'";
		Data_Connections.update_DataFile(strWrpQry); //records the response time of a service
		Custom_Reports.endReport(); //completing the report for each test case
		Custom_Reports.print_Report("INFO","Execution Completed", "<b>Execution for	the test case " + strTC_Name + " is completed</b>");
		System.out.println("\n");
	}

}
