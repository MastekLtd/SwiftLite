/*
 * Copyright(c) 2015 Mastek Ltd. All rights reserved.
 * 
 *	SwiftLite is distributed in the hope that it will be useful.
 *
 *	This file is part of SwiftLite Framework: Licensed under the Apache License, 
 *	Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 * 
 *	http://www.apache.org/licenses/LICENSE-2.0
 * 
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and 
 *	limitations under the License.
 */

package SwiftSeleniumWeb;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
//import javax.sound.midi.ControllerEventListener;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExcelUtility {
	public static char myChar = 34;
	public static HSSFCell testCaseID=null;
	public static HSSFCell transactionType = null;
	protected static List<String> status = new ArrayList<String>();
	protected static List<String> rowStatus = new ArrayList<String>();
	protected static List<String> actualValue = new ArrayList<String>();
	protected static List<List<String>> actualRows = new ArrayList<List<String>>();
	public static PrintStream print=null;
	public static List<Integer> PassCount = new ArrayList<Integer>();
	public static List<Integer> FailCount = new ArrayList<Integer>(); 
	public static int firstRow = 1;//newly Added code for Loop Action
	public static int dynamicNum = 0;

	/**
	 * Reads the Values sheet from Input Excel and returns the row 
	 *  
	 * @param FilePath
	 * @param TestCaseID
	 * @param TransactionType
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public static HSSFRow GetDataFromValues(String FilePath,String TestCaseID,String TransactionType) throws IOException, InterruptedException,Exception
	{
		HSSFRow expectedRow=null;
		HSSFSheet  valuesSheet = GetSheet(FilePath, "Values");
		
		int rowCount = valuesSheet.getLastRowNum()+1;
		int endRow = getRowCount(valuesSheet);
		if(endRow == 0)
		{
			MainController.pauseExecution = true;
		}

		for(int rowIndex=firstRow;rowIndex<firstRow+endRow&&!MainController.pauseExecution;rowIndex++)
		{
			expectedRow = valuesSheet.getRow(rowIndex);
			WebHelper.GetCellInfo(FilePath,expectedRow,rowIndex,rowCount);
		}
		return expectedRow;
	}
	
	/**
	 * Get row count of a sheet 
	 * @param valSheet
	 * @return
	 * @throws IOException
	 */
	public static int getRowCount(HSSFSheet valSheet) throws IOException
	{
		int loopRowCount=0;
		firstRow=1;
		Boolean isFirstFound = false;
		
		int rowCount = valSheet.getLastRowNum()+1;
		for(int rowIndex=1;rowIndex<rowCount;rowIndex++)
		{
			HSSFRow row =valSheet.getRow(rowIndex);
			testCaseID = row.getCell(0);
			String testCase = null;
			if(testCaseID == null)
			{
				testCase ="";
			}
			else
			{
				testCase = testCaseID.toString();
			}
			transactionType = row.getCell(1);
			if(testCaseID == null && transactionType == null)
			{
				break;
			}
			else if(testCase.equalsIgnoreCase(MainController.controllerTestCaseID.toString()) && transactionType.toString().equals(MainController.controllerTransactionType.toString()))
			{
				if(firstRow == 1 && !isFirstFound)
				{
					firstRow = rowIndex;
					isFirstFound = true;
				}
				loopRowCount++;
			}
			else if((!testCase.equalsIgnoreCase(MainController.controllerTestCaseID.toString()) || !transactionType.toString().equals(MainController.controllerTransactionType.toString()))&& !isFirstFound && rowIndex == rowCount-1)
			{
				MainController.pauseFun("TestCaseID Or Transaction Didn't Match " + MainController.controllerTestCaseID +" " + MainController.controllerTransactionType );
				ExcelUtility.writeReport(SwiftSeleniumWeb.WebDriver.report);
				break;
			}
		}
		return loopRowCount;
	}

	/**
	 * Reads Excel-Sheet values by taking Path and SheetName
	 * @param FilePath
	 * @param SheetName
	 * @return
	 * @throws IOException
	 */
	public static HSSFSheet GetSheet(String FilePath,String SheetName) throws IOException
	{
		HSSFSheet workSheet = null;
		try
		{
		InputStream myXls = new FileInputStream(FilePath);		
		HSSFWorkbook workBook = new HSSFWorkbook(myXls);
		workSheet = workBook.getSheet(SheetName);		
		}	
		catch(Exception e)
		{
			MainController.pauseFun("File Not Found "+SheetName);
			return null;
		}
		return workSheet;		
	}

	/**
	 * Writes SummaryResults
	 * @param report
	 * @throws IOException
	 */
	public static void writeReport(Reporter report) throws IOException
	{
		try
		{
		report.setReport(report);
		String frmDate = report.getFromDate();
		File file=  new File(Automation.configHashMap.get("RESULTOUTPUT").toString());
		report=report.getReport();
		
		if (report.strMessage == null)
			report.strMessage = "";		

		if (report.strTestDescription == null)
			report.strTestDescription = "";		
		
		if(file.exists() == false)
		{
			 print = new PrintStream(file);
		}
		int usedRows = WebHelper.count(file);
		if(usedRows == 0)
		{
			print.print("GroupName,Iteration,TestCaseID,TransactionType,TestCaseDesription,StartDate,EndDate,Status,Description,Screenshot");
			print.println();
		}
		usedRows = WebHelper.count(file);
		print = new PrintStream(new FileOutputStream(file, true));
		print.print(myChar + report.strGroupName + myChar + ","+ myChar + Automation.configHashMap.get("CYCLENUMBER").toString()+ myChar + ","+ myChar + report.strTestcaseId+ myChar + "," + myChar + report.strTrasactionType + 
				myChar + "," + myChar + report.strTestDescription + myChar + "," + myChar + frmDate + myChar + "," + myChar + report.toDate + myChar + "," + myChar +report.strStatus + myChar + "," + myChar +report.strMessage + myChar +","+myChar+report.strScreenshot + myChar);
		print.println();
		}
		catch(IOException ie)
		{
			MainController.pauseFun(ie.getMessage());
		}
		finally
		{
			SwiftSeleniumWeb.WebDriver.report.strScreenshot="";
		}
	}


	/**
	 * Writes WebVerification Results to the Excel Sheet
	 * @param testCaseID
	 * @param transactionType
	 * @param columns
	 * @param columnsData
	 * @param passCount
	 * @param failCount
	 * @param rowCount
	 * @param colCount
	 * @param report
	 * @param status
	 * @throws IOException
	 */
	public static void WriteToDetailResults(String testCaseID,String transactionType ,List<String> columns,List<List<String>> columnsData,int passCount,int failCount,int rowCount,int colCount,Reporter report,List<String> status) throws IOException
	{
		try
		{
			report=report.getReport();
			report.frmDate = Automation.dtFormat.format(WebHelper.frmDate);
			report.strTestcaseId =MainController.controllerTestCaseID.toString();
			report.strTrasactionType = MainController.controllerTransactionType.toString();
			report.strStatus = report.getStrStatus();

			if(WebHelper.file.exists() == false)
			{
				print = new PrintStream(WebHelper.file);
			} 
			
			columns.remove("TestCaseID");
			columns.remove("TransactionType");
			columns.remove("CurrentDate");

			print = new PrintStream(new FileOutputStream(WebHelper.file, true));
			int	usedRows = WebHelper.count(WebHelper.file);
			if(usedRows == 0)
			{
				print.println("Iteration,TestCaseID,TransactionType,CurrentDate,RowType,Status,PassCount,FailCount");
			}
			usedRows = WebHelper.count(WebHelper.file);

			print.print(ExcelUtility.myChar+Automation.configHashMap.get("CYCLENUMBER").toString()+ExcelUtility.myChar+","+ExcelUtility.myChar+report.strTestcaseId+
					ExcelUtility.myChar+","+ExcelUtility.myChar+
					report.strTrasactionType+ExcelUtility.myChar+","
					+ExcelUtility.myChar+report.frmDate+ExcelUtility.myChar+","+
					ExcelUtility.myChar+"Header"+ExcelUtility.myChar+","+
					ExcelUtility.myChar+report.strStatus+ExcelUtility.myChar+","+
					ExcelUtility.myChar+""+ExcelUtility.myChar+","+
					ExcelUtility.myChar+""+ExcelUtility.myChar);
			int counter=0;
			while(columns.isEmpty()== false)
			{			if(counter!=columns.size()){
				print.print(","+ExcelUtility.myChar+columns.get(counter)+ExcelUtility.myChar);
				counter++;
			}
			else
			{
				break;
			}
			}
			print.println();
			rowCount = actualRows.size();
			for(int rowIndex=0;rowIndex<rowCount;rowIndex++)
			{
				print.print(ExcelUtility.myChar+Automation.configHashMap.get("CYCLENUMBER").toString()+ExcelUtility.myChar+","+ExcelUtility.myChar+report.strTestcaseId+
						ExcelUtility.myChar+","+ExcelUtility.myChar+
						report.strTrasactionType+ExcelUtility.myChar+","
						+ExcelUtility.myChar+report.frmDate+ExcelUtility.myChar+","+
						ExcelUtility.myChar+"Data"+ExcelUtility.myChar+","+
						ExcelUtility.myChar+rowStatus.get(rowIndex).toString()+ExcelUtility.myChar+","+
						ExcelUtility.myChar+PassCount.get(rowIndex)+ExcelUtility.myChar+","+
						ExcelUtility.myChar+FailCount.get(rowIndex)+ExcelUtility.myChar);
				counter =0;
				while(actualRows.isEmpty() ==false)
				{
					if(counter != actualRows.get(rowIndex).size())
					{
						System.out.print(actualRows.get(rowIndex).get(counter));
						print.print(","+ExcelUtility.myChar+actualRows.get(rowIndex).get(counter)+ExcelUtility.myChar);
						counter++;
					}
					else
					{
						break;
					}
				}
				print.println();

			}
		}
		catch(Exception e)
		{
			MainController.pauseFun(e.getMessage());

		}
		finally
		{
			actualRows.clear();
			rowStatus.clear();
			columns.clear();
			columnsData.clear();

		}

	}

}
