package com.RestAssuredAPI;

public class Global_Variables {
	
	public static ThreadLocal<String> envTCName = new ThreadLocal<String>();
	public static ThreadLocal<String> envFnName = new ThreadLocal<String>();
	public static ThreadLocal<String> envTemplatePth = new ThreadLocal<String>();
	public static ThreadLocal<String> envBslPath = new ThreadLocal<String>();
	public static ThreadLocal<String> envPayloadPth = new ThreadLocal<String>();
	public static ThreadLocal<String> envRunId = new ThreadLocal<String>();
	public static ThreadLocal<String> envExecType = new ThreadLocal<String>();
	public static ThreadLocal<String> envActResPath = new ThreadLocal<String>();
	public static ThreadLocal<String> envExecStatus = new ThreadLocal<String>();
	public static ThreadLocal<String> envReportPath = new ThreadLocal<String>();
	public static ThreadLocal<String> envReportLocPath = new ThreadLocal<String>();
	public static ThreadLocal<String> envReExecFlag = new ThreadLocal<String>();
	public static ThreadLocal<Integer> envReExecCnt = new ThreadLocal<Integer>();
	public static ThreadLocal<String> envJenkinsRun = new ThreadLocal<String>();

}
