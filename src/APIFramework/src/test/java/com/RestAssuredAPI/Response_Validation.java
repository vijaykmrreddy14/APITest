package com.RestAssuredAPI;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.codoid.products.fillo.Recordset;
import io.restassured.response.Response;


public class Response_Validation extends Global_Variables{
	
	
	public void perform_ResponseAction(Response response,Recordset recTestData,int intIteration)
	{
		try{
			General_Utilities objGenUtil = new General_Utilities();
			int intStsCode = Integer.parseInt(recTestData.getField("Status_Code"));
			envBslPath.set("./env_" + System.getProperty("environment")+"/response_Baseline/" + envTCName.get().toLowerCase() + "_itr_"+ intIteration);
			envActResPath.set(envReportPath.get().toLowerCase() + "/responce_" + envTCName.get().toLowerCase() + "_itr_" + intIteration);
			String StrCurDate = new SimpleDateFormat("yyyymmdd").format(new Date());;
			validate_ResponseCode(response,intStsCode);// To validate the response code of for the service
			
			switch(envExecType.get()){
		  
			  case "Baseline_JSON" :
				Custom_Reports.print_Report("INFO","Resonse Baseline", "Baselining the response JSON for the test case" + envTCName.get() +"_Iteration"+ intIteration);
				objGenUtil.save_ResponceJSON(response, envActResPath.get());
				objGenUtil.copy_File(envActResPath.get() + ".JSON", envBslPath.get() +".JSON");
				Custom_Reports.print_Report("INFO","BaseLine response","<a href='"+objGenUtil.getAbsolutePath(envBslPath.get())+".JSON'>Click to open Base Line Path</a>");
				break;
			  case "Compare_JSON" :
				Custom_Reports.print_Report("INFO","Respose JSON Comparison", "Comparison of the response and the baseline JNSON for the test case" + envTCName.get() +"_Iteration"+ intIteration);
				objGenUtil.save_ResponceJSON(response,envActResPath.get());
				Custom_Reports.print_Report("INFO","BaseLine response","<a href='"+objGenUtil.getAbsolutePath(envBslPath.get())+".JSON'>Click to open Base Line Result</a>");
				Custom_Reports.print_Report("INFO","Actual response","<a href='"+objGenUtil.getAbsolutePath(envActResPath.get())+".JSON'>Click to open Actual Result</a>");
				objGenUtil.compare_JSONResponse(envBslPath.get(),envActResPath.get());
				break;
			  case "Baseline_ByteArrayAsString" ://built to handle the byte array response
				Custom_Reports.print_Report("INFO","Baseline ByteArray", "Baselining the response byte array for the test case" + envTCName.get() +"_Iteration"+ intIteration);
				objGenUtil.save_ResponceByteArray(response, envBslPath.get());
				objGenUtil.save_ResponceByteArray(response, envActResPath.get());
				objGenUtil.copy_File(envActResPath.get() + ".txt", envBslPath.get() +".txt");
				Custom_Reports.print_Report("INFO","BaseLine response","<a href='"+objGenUtil.getAbsolutePath(envBslPath.get())+".txt'>Click to open Base Line Path</a>");
				break;
			  case "Compare_ByteArrayAsString" ://built to handle the byte array response
				Custom_Reports.print_Report("INFO","Byte Array Comparison", "Comparision of the response and the baseline byte array for the test case" + envTCName.get() +"_Iteration"+ intIteration);
				objGenUtil.save_ResponceByteArray(response, envActResPath.get());
				objGenUtil.compare_StringResponse(envBslPath.get(), envActResPath.get());
				Custom_Reports.print_Report("INFO","BaseLine response","<a href='"+objGenUtil.getAbsolutePath(envBslPath.get())+".txt'>Click to open Base Line Path</a>");
				Custom_Reports.print_Report("INFO","Actual Response","<a href='"+objGenUtil.getAbsolutePath(envActResPath.get())+".txt'>Click to open Actual Result</a>");
				break;
			}
		} catch(Exception e) {
			Custom_Reports.print_Report("FAIL","Exception in response validation", "Exception Details: " +e);
		}
	}
	
	
	public String validate_ResponseCode(Response response, int intResCode) {
		String strFlag = "Pass";
		if ((response.statusCode()== 408) ||(response.statusCode()==500)||(response.statusCode()== 502)||(response.statusCode()==503)||(response.statusCode()== 504) ) {
			try {
				if(envReExecCnt.get() < 3) { //To re-execute the service for 3 times for bad request
					envReExecFlag.set("Yes");
					Thread.sleep(100000);
					Custom_Reports.print_Report("INFO","Response Code Validation", "As Response code is "+response.statusCode()+" re-Executing the	"+envTCName.get()+" API Service");
					envReExecCnt.set(envReExecCnt.get() + 1);
					strFlag = "Fail";
				}
				else {
					strFlag = "Fail";
					envReExecFlag.set("No");
					Custom_Reports.print_Report("FAIL","Response Code Validation", envTCName.get()+" API Service got failed even after re-Executing for 3 attempts");
				}
				}
			catch(Exception e) {
				strFlag = "Fail";
				Custom_Reports.print_Report("FAIL","Exception in Validating	Response code", "Exception Details: " + e);
			}
		}
		else {
			if (intResCode != 0 && response.statusCode() == intResCode) {
				Custom_Reports.print_Report("PASS","Response code Validation", "The response code is as expected. <b>Expected: " + intResCode + "||Actual: "+ response.statusCode() + "</b>");
				strFlag = "Pass";
			}
			else {
				strFlag = "Fail";
				Custom_Reports.print_Report("FAIL","Response code Validation","The response code is not as expected. <b>Expected: " + intResCode + "||Actual: "+ response.statusCode() + "</b>");
			}
		}
		
		return strFlag;
	}
}
	
