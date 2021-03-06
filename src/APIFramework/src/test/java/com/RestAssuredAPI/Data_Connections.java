package com.RestAssuredAPI;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;
import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class Data_Connections {
	
	private static Connection connection;
	private static Recordset recordset;

	public static Recordset qry_DataFile(String strQuery)
	{
		try {
			
			Fillo fillo = new Fillo();
			System.out.println("Data File Path :- ./testdata/testData_APIFramework.xls");
			connection = fillo.getConnection("./testdata/testData_APIFramework.xls");
			recordset = connection.executeQuery(strQuery);
			connection.close();
			
		}catch (Exception e) {
			
			
		}
		
		return recordset;
	}
	
	public static void update_DataFile(String strQuery)
	{
		try {
			
			Fillo fillo = new Fillo();
			System.out.println("Data File Path :- ./testdata/testData_APIFramework.xls");
			connection = fillo.getConnection("./testdata/testData_APIFramework.xls");
			recordset = connection.executeQuery(strQuery);
			connection.close();
			
		}catch (Exception e) {
			
			
		}
		
	}

}
