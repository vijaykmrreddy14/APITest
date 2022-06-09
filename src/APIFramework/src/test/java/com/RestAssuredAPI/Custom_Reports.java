package com.RestAssuredAPI;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.testng.Assert;
import org.testng.ITestResult;

import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class Custom_Reports extends Global_Variables{
	public static  ExtentReports extent;
	public static  ExtentTest logger;
	public static ExtentHtmlReporter htmlReports;
	static String filePath,reportPath,dirpath,filename;
	static final Logger log = Logger.getLogger(Custom_Reports.class);
	
	public static void startReport(){
	
		String strCurDate = new SimpleDateFormat("yyyyMMdd_HH_mm_ss").format(new Date());;
		//filePath = "./env_" + System.getProperty("environment") + "/execution_Reports/" + envRunId.get() + "/" + envRunId.get() + "_Report.html";//+strCurDate+".html";
		
		filePath = envReportLocPath.get() + "/" + envRunId.get() + "_Report.html";// Updted By VJ
		//dirpath = "./recent_execution_Reports/";
		dirpath = "/bridge-api-automation";  //Updated By VJ
		filename = System.getProperty("environment") + "_API_Execution_*.html";
		
		//PropertyConfigurator.configure(sourcePath);
		extent = new ExtentReports (filePath, true);
		
		extent
						.addSystemInfo("Host Name", "CI")
		                .addSystemInfo("Environment", "QA3 Testing")
		                .addSystemInfo("User Name", "Automation");
		                
		                extent.loadConfig(new File(".\\config.xml"));
		                
	}
	
	public static void strtTest(String Tc_Name) {
	
	  logger = extent.startTest(Tc_Name);
	
	}
	
	public void getResult(ITestResult result){
		if(result.getStatus() == ITestResult.FAILURE){
			logger.log(LogStatus.FAIL, "Test Case Failed is "+result.getName());
			logger.log(LogStatus.FAIL, "Test Case Failed is "+result.getThrowable());
		}else if(result.getStatus() == ITestResult.SKIP){
			logger.log(LogStatus.SKIP, "Test Case Skipped is "+result.getName());
		}
	
		extent.endTest(logger);
	}
	
	public static void print_Report(String strStatus, String strStep, String strDesc) {
		System.out.println(strStatus + "--->" + strStep + ": " + strDesc);
		if(strStatus.toUpperCase().equals("PASS")) { 
			logger.log(LogStatus.PASS, strStep, strDesc);
		}
		else if(strStatus.toUpperCase().equals("FAIL")) {
			logger.log(LogStatus.FAIL, strStep, strDesc);
			envExecStatus.set("Fail");
		}
		else if(strStatus.toUpperCase().equals("INFO")) {
			logger.log(LogStatus.INFO, strStep, strDesc);
		}
		else if(strStatus.toUpperCase().equals("WARNING")) {
			logger.log(LogStatus.WARNING, strStep, strDesc);
		}
	}
	
	public static void endReport() {
		extent.flush();
        extent.endTest(logger);
        
	}
	
	public static void copy_LatestReport(){
		try {
			File source = new File(filePath);
			//File dest = new File("./recent_execution_Reports/" +  System.getProperty("environment") + "_Recent_MBAPI_Automation_Execution_report.html");
			
			//File dest = new File("./recent_execution_Reports/" +  System.getProperty("environment") +  "_"+ envRunId.get() + "_Report.html");   //Updated By Vj
			
			File dest = new File("./" +  System.getProperty("environment") +  "_API_Execution_Report.html");   //Updated By Vj on 02/07
			if(dest.exists()) dest.delete();
			FileUtils.copyFile(source, dest);
			if(envExecStatus.get().equals("Fail")) {
			    Assert.fail();
			 }
		 }catch(Exception e) {
			System.out.println(e);
		 }
	}
	
	public static void Delete_LastReport(String reportPath){
		try {
		
			File lastreport = new File(reportPath);
			
			if(lastreport.exists()) lastreport.delete();
		
		}catch(Exception e) {
			System.out.println(e);
		}
	
	}
	
	
}

