package com.RestAssuredAPI;

import java.io.File;

import com.codoid.products.fillo.Recordset;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class Execute_APIActions extends Global_Variables {
	
	General_Utilities objGenUtil = new General_Utilities();
	
	public Response perform_GetAction(String strBaseUrl,String strData,String strHdeAuth)
	{
		RestAssured.baseURI = strBaseUrl;
		RequestSpecification request = RestAssured.given();
		request.relaxedHTTPSValidation();
		//strHdeAuth = get_AuthToken(strHdeAuth);      //Commented for now as we dont have authentication 
		//request.header("Authorization",strHdeAuth); //Commented for now as we dont have authentication
		strData = objGenUtil.get_ExpData(strData,Global_Variables.envTCName.get()); //need to be developed
		Response response = request.get(strBaseUrl+strData);
		return response;
		
	}
	
	public Response perform_POSTAction(String strUrl,Recordset recPostdata,int intIteration,String strAction)
	{
		Response response = null;
		String strTemplatePth,strJson,strPayloadPth,strHdeAuth;
		
		envPayloadPth.set(envReportPath.get().toLowerCase()+"/inputdata_"+envTCName.get()+"_Itr_"+intIteration);
		try {
			strHdeAuth = recPostdata.getField("auth_Header");
			RequestSpecification request = RestAssured.given();
			request.relaxedHTTPSValidation();
			Custom_Reports.print_Report("INFO","API Service URL", "API Service URL: "+ strUrl);
			strJson = objGenUtil.build_JSONRequest(envTemplatePth.get(), recPostdata,envTCName.get()); //To build JSON based on the input provided in the excel
			objGenUtil.save_StringasJSON(strJson, envPayloadPth.get()); //Save JSON	request
			File flePayload = new File(envPayloadPth.get() + ".JSON");
			if(!strHdeAuth.equals("")) strHdeAuth = get_AuthToken(strHdeAuth);
			request.body(flePayload);
			if (strAction.equals("POST")) { //To handle both put and post operation in one function .Updated By VJ
				//response =	request.contentType("application/json").header("Authorization", strHdeAuth).post(strUrl);// For authentication header
				response =	request.contentType("application/json").post(strUrl);
			}else {
				//response =	request.contentType("application/json").header("Authorization", strHdeAuth).put(strUrl);// For authentication header
				response =	request.contentType("application/json").put(strUrl);
			}
			Custom_Reports.print_Report("INFO","API Service URL", "API Service	URL: "+ strUrl);
			}catch (Exception e)
			{
			Custom_Reports.print_Report("FAIL","Exception while performing API action", "Exception Details: " + e);
			}
			return response;
	}
	
	 public String get_AuthToken(String strJSONPath) {
        String strExpData = objGenUtil.get_ExpData(strJSONPath, envTCName.get());
        if(!strExpData.equals(""))return "Bearer " + strExpData;
        else return "";
	 }


}
