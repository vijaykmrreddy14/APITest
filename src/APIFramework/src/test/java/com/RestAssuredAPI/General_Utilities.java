package com.RestAssuredAPI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.annotations.Test;
import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Recordset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.restassured.response.Response;

public class General_Utilities extends Global_Variables {
		
	public void save_ResponceJSON(Response response, String strPath)
	{
		String responseBody = response.getBody().asString();
		save_StringasJSON(responseBody, strPath);
	}
	
	
	public void save_StringasJSON(String strJson, String strPath)
	{
		try {
			File filepath = new File(strPath);
			filepath.getParentFile().mkdirs();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(strJson);
			String prettyJsonString = gson.toJson(je);
			FileWriter file = new FileWriter(strPath + ".JSON");
			file.write(prettyJsonString);
			file.flush();
			file.close();
		} catch (Exception e) {
			Custom_Reports.print_Report("FAIL","Exception while saving saving JSON file","Exception Details: " + e);
		}
	}
	
	
	public void save_ResponceByteArray(Response response, String strPath)
	{
		try {
			InputStream is = response.asInputStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1!=(n=is.read(buf)))
			{
				out.write(buf, 0, n);
			}
			out.close();
			is.close();
			byte[] bytResponse = out.toByteArray();
			String strByteArray = new String(bytResponse);
			FileWriter file = new FileWriter(strPath + ".txt");
			file.write(strByteArray);
			file.flush();
			file.close();
		} catch (IOException e) {
			Custom_Reports.print_Report("FAIL","Exception in saving Byte Array", "ExceptionDetails: " + e);
		}
	}
	
	
	public void compare_JSONResponse(String strExpPath,String strActPath)
	{
		try
		{
			String strExpectedJson, strActualJson;
			strExpectedJson= get_Expected_Data(strExpPath+".JSON");
			strActualJson = FileUtils.readFileToString(new File(strActPath+".JSON"),
			Charset.defaultCharset());
			JSONCompareResult result = JSONCompare.compareJSON(strExpectedJson, strActualJson,
			JSONCompareMode.LENIENT);
			if (result.failed() == true ) {
				//Fails when the no of expected JSON node not matching with the actual JSON message
				Custom_Reports.print_Report("INFO","Response Comparison", "###Difference observed	between the expected and the actual result. Find below the detailed analysis");
				Custom_Reports.print_Report("INFO","Comparison Report ", result.toString());
				Custom_Reports.print_Report("INFO","Subset Validation", "Proceeding with subset	validation...");
				if(strExpectedJson.contains("[") != true) strExpectedJson = "[" + strExpectedJson + "]";
				if(strActualJson.contains("[") != true) strActualJson = "[" + strActualJson + "]";
				JSONArray arrExpRes = new JSONArray(strExpectedJson);
				JSONArray arrActRes = new JSONArray(strActualJson);
				compare_JSONArray(arrExpRes,arrActRes);
		}
			else {
				Custom_Reports.print_Report("PASS","Response Comparison", "No Issues observed in the response comparison");
			}
		}
		catch (Exception e)
		{
			Custom_Reports.print_Report("FAIL","Exception in JSON response validation", "ExceptionDetails: " + e);
		}
	}
	
	
	
	public void compare_JSONArray(JSONArray arrExpRes, JSONArray arrActRes) {
		try {
			String strflag = "fail",strtemp = "";
			JSONObject objTmpt;
			API_Validations objMBDval = new API_Validations();
			int intFailFlg;
			String strFldNode = ""; //response node that was compared against the
			String strKey, strExpData, strActData,strExpTmpt;
			Custom_Reports.print_Report("INFO","JSON Subset Validation", "Validating whether the expected result is a subset of the actual result");
			//objTmpt = get_JSONTemplate("response", envTemplatePth.get());//To get response Template
			strtemp = get_JSONTemplate("response", envTemplatePth.get());//To get response Template
			objTmpt = new JSONObject(strtemp); //Added By VJ
			for(int incExp=0; incExp < arrExpRes.length(); incExp++) {
				JSONObject objExpJsn = arrExpRes.getJSONObject(incExp);
				intFailFlg = objExpJsn.length();
				for(int incAct=0; incAct < arrActRes.length(); incAct++) {
					int intFailCnt = 0;
					strflag = "pass";
					JSONObject objActJsn = arrActRes.getJSONObject(incAct);
					Iterator<?> keys = objActJsn.keys();
					while( keys.hasNext() ) {
						strKey = (String)keys.next();
						strExpData = objExpJsn.get(strKey).toString();
						strActData = objActJsn.get(strKey).toString();
						strExpTmpt = objTmpt.get(strKey).toString();//Taking the value from the responsetemplate
						if( !(strExpTmpt.toLowerCase().equals("random")) && !(strActData.equals(strExpData)))
						{ //First check is for the response template
							strflag = "fail";
							intFailCnt = intFailCnt + 1;
							if(strExpTmpt.length()>4 && strExpTmpt.toLowerCase().trim().substring(0,4).contains("cal_")) {
								Custom_Reports.print_Report("INFO","Response Key Validation", "Validation for the response key ###" + strKey + "###");
								strExpTmpt = strExpTmpt.replace(strExpTmpt.substring(0, 4), "");
								strflag = val_ResponseKey(strExpTmpt, strActData);
							}
						}
						if(strflag.equals("pass") ) {break;}
						else if(intFailCnt < intFailFlg) {intFailFlg = intFailCnt; strFldNode = objActJsn.toString(); }
					}
					if(strflag.equals("fail")) {
						Custom_Reports.print_Report("FAIL","JSON Subset Validation", "Expected JSON node is not available in the response. Expected Response: " + objExpJsn.toString() + "\nNearest	possible respone node in the response:" + strFldNode + "\n");
					}
					else if(strflag.equals("pass")) {
						Custom_Reports.print_Report("PASS","JSON Subset Validation", "Expected JSON node is available in the response file. Compared Json : " + objExpJsn.toString());
					}
				}
			}	
		} catch(Exception e) {
			Custom_Reports.print_Report("FAIL","Exception in JSON subset comparison", "Exception Details: " + e);
		}
	}
		
		
		public String append_TimeStamp(String strText)
		{
		//String strYear = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		String strTimeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
		return strText + "_" + strTimeStamp;
		}
		
		
		
		public void compare_StringResponse(String strExpPath,String strActPath)
		{
			try {
				String expectedJson = get_Expected_Data(strExpPath+".txt");
				if (expectedJson.contains("texttocompare")) expectedJson = expectedJson.split(":")[1];
				//String expectedJson = FileUtils.readFileToString(new File(strExpPath+".txt"),
				Charset.defaultCharset();
				String actualJson = FileUtils.readFileToString(new	File(strActPath+".txt"),Charset.defaultCharset());
				if (expectedJson.equals(actualJson)) {
				Custom_Reports.print_Report("PASS","Byte array Validation", "The byte array is equal");
				}
				else {
				Custom_Reports.print_Report("FAIL","Byte array Validation", "The byte array is <b>NOT</b>equal. Exp data: " + expectedJson +" Actual data:" + actualJson);
			}
			}catch (Exception e) {
				Custom_Reports.print_Report("FAIL","Exception in byte array comparison", "Exception Details: "	+ e);
			}
		}
		
		
		public String build_JSONRequest(String strJsonpth, Recordset recPostdata, String strTC_Name) {
			int intctr = 1;
			String strQry, strFldData, strJson = "";
			boolean blnComplete = false;
			try {
			strJson = get_JSONTemplate("request", envTemplatePth.get()); //Added By VJ
			strJson = build_TestRequest(strJson, recPostdata);
			}catch(Exception e) {
				Custom_Reports.print_Report("FAIL","Exception in building JSON request", "Exception Details: "+ e);
			}
			return strJson;
		}
		
		
		public String build_TestRequest(String strData, Recordset recdata) {
			boolean blnComplete = false;
			String strFldData;
			int intctr = 1;
			try {
				while(blnComplete != true)
				if(strData.contains("\"Field_"+intctr+"\"")) {
				strFldData = recdata.getField("Field_"+intctr);
				strFldData = get_ExpData(strFldData,envTCName.get());
				strData = strData.replace("\"Field_"+intctr+"\"", "\"" + strFldData + "\"");//including double quotes as part of the replacement to avoid replacing more than one field (field 1 and field 10 would get				replaced)
				intctr = intctr + 1;
				}
				else {
				blnComplete = true;
			}
			}catch(Exception e) {
				Custom_Reports.print_Report("FAIL","Exception in building request", "Exception Details: " + e);
			}
			return strData;
		}
		
		
		public String build_GETRequest(String strData, Recordset recdata) {
			boolean blnComplete = false;
			String strFldData;
			int intctr = 1;
			try {
				while(blnComplete != true)
					if(strData.contains("Field_"+intctr)) {
						strFldData = recdata.getField("Field_"+intctr);
						strFldData = get_ExpData(strFldData,envTCName.get());
						strData = strData.replace("Field_"+intctr, strFldData );
						intctr = intctr + 1;
					}
					else {
						blnComplete = true;
				}
			}catch(Exception e) {
				Custom_Reports.print_Report("FAIL","Exception in building request", "Exception Details: " + e);
			}
			return strData;
		}
		
		
		public String getAbsolutePath(String strPath) {
			try {
				if (System.getProperty("envJenkinsRun").equals("Yes")){
					strPath =strPath.replace(".", "/APIReport");
					return strPath;
				}
				else {
					File file = new File(strPath);
					return file.getCanonicalPath();
			}
			}catch(Exception e) {
				Custom_Reports.print_Report("FAIL","Exception in getting absolute path for the file", "ExceptionDetails: " + e);
				return "Failed";
			}
		}
		
		
		public String get_ExpData(String strExpData, String strTC_Name) {
			String[] arrOpVal=null;
			try
			{
				if(strExpData.length()>4 && strExpData.toLowerCase().trim().substring(0, 4).contains("cal_")) {
					String strMthName;
					Class classToCall = General_Utilities.class;//Class.forName("general_Utilities");
					arrOpVal = strExpData.split("##");
					strMthName = arrOpVal[0].replace(arrOpVal[0].substring(0, 4), ""); //To remove Cal_from the method name
					Method methodToExecute = classToCall.getDeclaredMethod(strMthName, new Class[]{String[].class} );
					strExpData = (String) methodToExecute .invoke(classToCall.newInstance(), new Object[]{arrOpVal});
			}
			}catch(Exception e)
			{
				Custom_Reports.print_Report("FAIL","Exception while getting the expected data from excel or pre-requisite JSON", "Exception Details: " + e);
			}
			return strExpData;
		}
		
		
		public String getbyJsonPath(String[] arrOpVal) {
			String strResPth, strResFle, strVal="a";
			strResPth = envReportLocPath.get() + "/" + arrOpVal[(arrOpVal.length - 2)].toLowerCase() + "/";
			strResFle = "response_"+ arrOpVal[(arrOpVal.length - 2)].toLowerCase() +"_itr_*.JSON";
			//"Folio_Filter_List_POST"+"*.JSON";
			strResPth = getFilePathByWildCard(strResPth, strResFle);
			strVal = getValueByJsonPath(strResPth,arrOpVal[(arrOpVal.length - 1)]);
			return strVal;
		}
		
		
		public String val_ResponseKey(String strExpTmpt, String strActData) {
			String strFlag = "pass";
			String [] arrval = strExpTmpt.split("##");
			switch(arrval[0].toLowerCase()) {
				case "stringlength":
					Custom_Reports.print_Report("INFO","Response Length Verificaion", "Verifying the response length for " + Integer.parseInt(arrval[1]) + " digits. Response: " +strActData );
					strFlag = val_ResponseLength(strActData,Integer.parseInt(arrval[1]));  //Need to be developed
					break;
				case "compareresponse":
					Custom_Reports.print_Report("INFO","Response Comparison", "Comparing the response with the output of pre-requisite service. Actual Response: " +strActData );
					strFlag = val_From_Response(arrval, strActData);  //Need to be developed
					break;
				case "directcompare":
					Custom_Reports.print_Report("INFO","Response Comparison", "Expected response:-  "+ arrval[1].toLowerCase() +"  ### Actual Response:- " +strActData.toLowerCase() );
					if((arrval[1].toLowerCase()).equals(strActData.toLowerCase())) strFlag = "pass";
					else strFlag = "fail";
					break;
				default:
					try {
						Class objMBDCls = API_Validations.class; //Need to be developed
						Method methodToExecute  = objMBDCls.getDeclaredMethod(arrval[0], new Class[]{String.class,String.class} );
						strFlag = (String) methodToExecute .invoke(objMBDCls.newInstance(), new Object[]{strExpTmpt,strActData});
						break; 
					}catch (Exception e) {
						Custom_Reports.print_Report("FAIL", "Exception calling a custom method from val_ResponseKey, Method Name: " + arrval[0], "Exception Details: " + e);
					}

			}
			//if(strExpTmpt.toLowerCase().contains("negative")) strFlag = neg_Validation(strFlag);
			return strFlag;
			}

		
	

	public String val_ResponseLength(String strActData, int int_Length) {
		String strFlag = "pass";
		if(strActData.length() != int_Length) {
			Custom_Reports.print_Report("INFO","Response Length Verification", "The generated Response is not in " + int_Length + " digits. " + "Response: "+ strActData );
			strFlag = "fail";
		}
		else {
			Custom_Reports.print_Report("INFO","Response Length Verification", "The generated Response is in " + int_Length + " digits. " + "Response: "+ strActData );
			strFlag = "pass";
		}
		return strFlag;
	
	}
	
	public String val_From_Response(String[] arrval, String strActData) {
		String strFlag = "pass", strExpData;
		strExpData = getbyJsonPath(arrval);
		Custom_Reports.print_Report("INFO","Response Comparison", "Comparing the response with the output of pre-requisite service. Expected Response: " +strExpData );
		if(strExpData.equals(strActData)) strFlag = "pass";
		else strFlag = "fail";
		return strFlag;
	}
	public String neg_Validation(String strFlag) {
		if(strFlag.equals("fail")) {
			strFlag = "pass";
			Custom_Reports.print_Report("PASS","Negative Validation", "This test case carries negative validation and it is successfully validated" );
		}
		else {
			strFlag = "fail";
			Custom_Reports.print_Report("FAIL","Negative Validation", "This test case carries negative validation and the validation failed. Please compare the response for more details");
		}
		return strFlag;
	}
		
	/**	public String getTaxIDfrRevert(String[] strOpVal) {
			String strVal;
			strVal = getbyJsonPath(strOpVal);
			strVal = data_Connections.get_MongoDbVal("mbd_" +
			System.getProperty("environment"),"FolioRequest","TaxonomyNewId","RunId",strVal);
			return strVal;
		}
		
		
		public String getFloReqIDfrRevert(String[] strOpVal) {
			String strVal;
			strVal = getbyJsonPath(strOpVal);
			strVal = data_Connections.get_MongoDbVal("mbd_" +
			System.getProperty("environment"),"FolioRequest","_id","RunId",strVal);
			return strVal;
		}
		
		
		public String getTaxNmfrRevert(String[] strOpVal) {
			String strVal;
			strVal = getbyJsonPath(strOpVal);
			strVal = data_Connections.get_MongoDbVal("mbd_" +
			System.getProperty("environment"),"FolioRequest","TaxonomyNewName","RunId",strVal);
			return strVal;
		}**/
		
		
		public String getDatafrmExcel(String[] strOpVal) {
			String strQry, strVal;
			strQry = "Select Ref_Value from Reference_Values where Test_Case_Name='" + strOpVal[1] +
			"' and Field = '" + strOpVal[2] + "'";
			String strRetVal=null;
			try {
				Recordset recSet =Data_Connections.qry_DataFile(strQry);
				if(recSet != null) {
				recSet.next();
				strRetVal = recSet.getField("Ref_Value");
			}
			}catch (FilloException e){
				// TODO Auto-generated catch block
				Custom_Reports.print_Report("FAIL","Exception in querying datafile", "Exception Details: " + e);
			}
			return strRetVal;
		}
		
		
		public void putDatainExcel(String[] strOpVal) {
			String strQry, strVal;
			strQry = "Select Ref_Value from Reference_Values where Test_Case_Name='" + strOpVal[1] +"' and Field = '" + strOpVal[2] + "'";
			strVal = chk_RecordSetIsNull(strQry,"Ref_Value");
		}
		
		
		
		public static String chk_RecordSetIsNull(String strQry, String strClmnName)
		{
			String strRetVal=null;
			try {
				Recordset recSet =Data_Connections.qry_DataFile(strQry);
				if(recSet != null)
				{
				recSet.next();
				strRetVal = recSet.getField(strClmnName);
				}
			}
			catch (FilloException e)
			{
			// TODO Auto-generated catch block
				Custom_Reports.print_Report("FAIL","Exception in querying datafile", "Exception Details: " + e);
			}
			return strRetVal;
		}
		
		
		public static String get_GUID()
		{
			String GenGUID = randomAlphaNumeric(8) + "-" + randomAlphaNumeric(4) + "-" +
			randomAlphaNumeric(4) + "-" + randomAlphaNumeric(4) + "-" + randomAlphaNumeric(12);
			return GenGUID;
		}
		
		
	public static String randomAlphaNumeric(int intRndStrLen)
	{
		String strAlphaNum = "abcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder builder = new StringBuilder();
		while (intRndStrLen-- != 0)
		{
			int intRndChar = (int)(Math.random()*strAlphaNum.length());
			builder.append(strAlphaNum.charAt(intRndChar));
		}
		return builder.toString();
	}
		
		
	public static String getValueByJsonPath(String strJsonPath,String strSearchExp)
	{
		String strJson = null,strVal= null;
		try
		{
			strJson = FileUtils.readFileToString(new File(strJsonPath), Charset.defaultCharset());
			//System.out.println(strJson);
			strVal = com.jayway.jsonpath.JsonPath.read(strJson,strSearchExp).toString();
			System.out.println(strSearchExp + " : " + strVal);
			}
		catch (Exception e)
		{
			Custom_Reports.print_Report("FAIL","Exception while getting value from JSON path","Exception Details: " + e);
		}
		return strVal;
	}
		
		

	public static String  getFilePathByWildCard(String strFilePath,String strWildCard){
		File strOpVal = null; 
		try 
		{
			File dir = new File(strFilePath); 
			FileFilter fileFilter = new WildcardFileFilter(strWildCard);
			File[] files = dir.listFiles(fileFilter);
			for (int i = 0; i < files.length; i++) 
			{
				strOpVal = files[i];
			}
			
		} 
		catch (Exception e) 
		{
			Custom_Reports.print_Report("FAIL","Exception in getFilePathByWildCard", "Exception Details: " + e);
		}
		
		return strOpVal.toString();
	
	}
	
	
	public String get_JSONTemplate(String strKey, String strTmptPath) {
		JSONObject objExpTmp =null;
		String strExpectedJson = "";
		try {
			strExpectedJson = FileUtils.readFileToString(new File(strTmptPath),Charset.defaultCharset());
			objExpTmp = new JSONObject(strExpectedJson);
			strExpectedJson = objExpTmp.get(strKey).toString();
		
		}catch (Exception e) {
			Custom_Reports.print_Report("FAIL","Exception in get_JSONTemplate", "Exception Details: " + e);
		}
		return strExpectedJson;
	
	}
	
	
	
	public static void copy_File(String strSource,String strTarget){
		try {
			File source = new File(strSource);
			File dest = new File(strTarget);
			if(dest.exists()) dest.delete();
			FileUtils.copyFile(source, dest);
		}catch(Exception e) {
			Custom_Reports.print_Report("FAIL","Exception while copying the recent report", "Exception Details: " + e);
		}
	}

	
	public String get_Expected_Data(String strExpPath) {
		String strExpectedJson= null,strtemp = null;
		try {
		
			if(new File(strExpPath).exists()) strExpectedJson = FileUtils.readFileToString(new File(strExpPath),Charset.defaultCharset());
			else {
			
			// JSONObject objJSON = get_JSONTemplate("response", envTemplatePth.get());
			
			strtemp = get_JSONTemplate("response", envTemplatePth.get());//To get response Template
			
			JSONObject objJSON = new JSONObject(strtemp);  //Added By VJ
			
			if(objJSON.has("texttocompare")) strExpectedJson = objJSON.get("texttocompare").toString();
			else strExpectedJson = objJSON.toString();
			}
		
		} catch (Exception e) {
		// TODO Auto-generated catch block
			Custom_Reports.print_Report("FAIL", "Exception while getting data from Basline or Template, Method Name: get_Expected_Data", "Exception Details: " + e);
		}
		
		return strExpectedJson;
	}
		
}
