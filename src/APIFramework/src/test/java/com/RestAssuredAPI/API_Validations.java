package com.RestAssuredAPI;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.jsoup.Jsoup;


public class API_Validations extends Global_Variables{
General_Utilities objGenUtil = new General_Utilities();


public String status_UI_Taxonomy_Migration_Status_GET(String strExpTmpt, String strActData) {
	String strFlag = "fail";
	try {
		if(strActData.toLowerCase().equals("in progress")){
			if(envReExecCnt.get() < 5) { //To reexecute the service for 5 times
				envReExecFlag.set("Yes");
			    Thread.sleep(4000);
			    Custom_Reports.print_Report("INFO","Status Validation", "As status is "+strActData+" re-Executing the "+envTCName.get()+" API Service");
			    envReExecCnt.set(envReExecCnt.get() + 1);
			    strFlag = "skip";//Skipped to re-execute the service without failing the previous run
			}
			else {
				envReExecFlag.set("No");
				Custom_Reports.print_Report("FAIL","Status Validation", "The folio migration service is still in IN-PROGRESS state even after 5 attempts");
				strFlag = "fail";
			}
		}else if(strActData.toLowerCase().equals("failed")) {
			Custom_Reports.print_Report("FAIL","Status Validation", "The folio migration service is FAILED");
			strFlag = "fail";
		
		}
	
	}catch(Exception e) {
		Custom_Reports.print_Report("FAIL","Exception in status_UI_Taxonomy_Migration_Status_GET method", "Exception Details: " + e);
	}
	return strFlag;
}


public String status_TFA_ValidateOTPService_ExpiredOTP(String strExpTmpt, String strActData) {
	String strExecTime, strExpData, strStartTime, strFlag = "fail";
	strExecTime = objGenUtil.get_ExpData("cal_getbyJsonPath##TFA_OTPService_ValidateOTP_Expire##$.matchedTime", envTCName.get());//Get the date time of the OTP verification service
	strStartTime = objGenUtil.get_ExpData("cal_getbyJsonPath##TFA_OTPService_GetOTP_Length##$.generatedTime", envTCName.get());//Get the date time of the get OTP service for comparison
	LocalTime dteExpected = LocalTime.parse( strStartTime ) ;
	LocalTime dteActual = LocalTime.parse( strExecTime ) ;
	Long lngScnds = Duration.between(dteExpected, dteActual).getSeconds();
	try {
		if(strActData.toLowerCase().equals("valid") && (lngScnds/60) < 15){
			envReExecFlag.set("Yes");
			Custom_Reports.print_Report("INFO","OTP Verification", "The generated OTP is successfully validated. " + " Executed Time: " + strExecTime);
			Thread.sleep(10000);
			Custom_Reports.print_Report("INFO","Wait", "Wait for 10 seconds");
			strFlag = "pass";
		}else {
			envReExecFlag.set("No");
			if((lngScnds/60)>=15) {
				Custom_Reports.print_Report("PASS","OTP Verification", "The OTP is valid only for 15 mins. Start Time:" + strStartTime + ", End Time: " + strExecTime + ", Duration in sec: "+lngScnds );
				strFlag = "pass";
			}else {
				Custom_Reports.print_Report("FAIL","OTP Verification", "OTP expiration validations failed. Start Time:" + strStartTime + ", End Time: " + strExecTime+ ", Duration in sec: "+lngScnds);
				strFlag = "fail";
		}
		}
	}catch(Exception e) {
	
		Custom_Reports.print_Report("FAIL", "Exception in TFA_OTPService_ValidateOTP_Expire method", "Exception Details: " + e);
	}
	return strFlag;
}


}
