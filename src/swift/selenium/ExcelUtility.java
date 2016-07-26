/*
 * Copyright(c) 2016 Mastek Ltd. All rights reserved.
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
package swift.selenium;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;


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
	
	
	public static HSSFRow GetDataFromValues(String FilePath,String TestCaseID,String TransactionTypeFetched) throws IOException, InterruptedException,Exception
	{
		HSSFRow expectedRow=null;
		HSSFSheet  valuesSheet = GetSheet(FilePath, "Values");
		
		int rowCount = valuesSheet.getLastRowNum()+1;
		int endRow = getRowCount(valuesSheet);
		if(endRow == 0)
		{
			TransactionMapping.pauseExecution = true;
		}

		for(int rowIndex=firstRow;rowIndex<firstRow+endRow;rowIndex++)
		{
			expectedRow = valuesSheet.getRow(rowIndex);
			WebHelper.GetCellInfo(FilePath,expectedRow,rowIndex,rowCount);
		}
		return expectedRow;
	}
	
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
			else if(testCase.equalsIgnoreCase(TransactionMapping.testCaseID.toString()) && transactionType.toString().equals(TransactionMapping.transactionType.toString()))
			{
				if(firstRow == 1 && !isFirstFound)
				{
					firstRow = rowIndex;
					isFirstFound = true;
				}
				loopRowCount++;
			}
			else if((!testCase.equalsIgnoreCase(TransactionMapping.testCaseID.toString()) || !transactionType.toString().equals(TransactionMapping.transactionType.toString()))&& !isFirstFound && rowIndex == rowCount-1)
			{
				TransactionMapping.pauseFun("TestCaseID Or Transaction Didn't Match " + TransactionMapping.testCaseID.toString() +" " + TransactionMapping.transactionType);
				ExcelUtility.writeReport(TransactionMapping.report);
				break;
			}
		}
		return loopRowCount;
	}
	
	// Reads Excel-Sheet values by taking Path and SheetName
	public static HSSFSheet GetSheet(String FilePath, String SheetName)
			throws IOException {
		HSSFSheet workSheet = null;
		try {
			InputStream myXls = new FileInputStream(FilePath);
			HSSFWorkbook workBook = new HSSFWorkbook(myXls);
			workSheet = workBook.getSheet(SheetName);
		} catch (Exception e) {
			TransactionMapping.pauseFun("File Not Found " + SheetName);
			e.printStackTrace();
			return null;
		}
		return workSheet;
	}

	/**
	 * This method is called when Report.csv is generated for the first time in Passing scenarios
	 * @param report
	 * @throws IOException
	 */
	public static void writeReport(Reporter report) throws IOException {
		try {
			report.setReport(report);
			String frmDate = report.getFromDate();
			File file = new File(Automation.configHashMap.get("RESULTOUTPUT")
					.toString());
			report = report.getReport();

			// TM:19/01/2015-Changes made to remove ==null
			if (StringUtils.isBlank(report.strMessage))
				report.strMessage = "";

			// TM:19/01/2015-Changes made to remove ==null
			if (StringUtils.isBlank(report.strTestDescription))
				report.strTestDescription = TransactionMapping.report.strTestDescription;

			// TM:19/01/2015-Added for GroupName Blank
			if (StringUtils.isBlank(report.strGroupName))
				report.strGroupName = TransactionMapping.report.strGroupName;

			if (file.exists() == false) {
				print = new PrintStream(file);
			}
			int usedRows = WebHelper.count(file);
			if (usedRows == 0) {
				print.print("GroupName,Iteration,TestCaseID,TransactionType,TestCaseDescription,StartDate,EndDate,Status,Description,Screenshot");
				print.println();
			}
			usedRows = WebHelper.count(file);
			print = new PrintStream(new FileOutputStream(file, true));
			print.print(myChar + report.strGroupName + myChar + "," + myChar
					+ Automation.configHashMap.get("CYCLENUMBER").toString()
					+ myChar + "," + myChar + report.strTestcaseId + myChar
					+ "," + myChar + report.strTrasactionType + myChar + ","
					+ myChar + report.strTestDescription + myChar + ","
					+ myChar + frmDate + myChar + "," + myChar + report.toDate
					+ myChar + "," + myChar + report.strStatus + myChar + ","
					+ myChar + report.strMessage + myChar + "," + myChar
					+ report.strScreenshot + myChar);
			print.println();
		} catch (IOException ie) {
			TransactionMapping.pauseFun(ie.getMessage());
		} finally {
			TransactionMapping.report.strScreenshot = "";
		}
	}
	
	
	public static Reporter CompareExcel(HSSFSheet actualSheet,HSSFSheet expectedSheet,List<String> columns,List<String> columnsData,String testCaseID,String transactionType, int actualSheetRowCount) throws IOException
	{
		boolean isrowFound = false;
		int expSheetRowCount =getRowCount(expectedSheet); //expectedSheet.getPhysicalNumberOfRows();
		Reporter report =new Reporter();
		report.setReport(report);
		int passCount=0, failCount=0, colCount=0, finalRowCount=0;
		
		//TM-28/09/2015: if expected sheet row count is greater than actual sheet row count than comparison should be on basis of expected sheet row count else vice-versa
		if (expSheetRowCount >= actualSheetRowCount)
			finalRowCount = expSheetRowCount;
		else
			finalRowCount = actualSheetRowCount;
		
		for(int rowIndex=firstRow;rowIndex<firstRow+finalRowCount;rowIndex++)
		{
			passCount=0;
			failCount =0;
			int currentRow = ++WebVerification.currentRowIndex;
			HSSFRow actualRow  = actualSheet.getRow(currentRow);
			HSSFRow expectedRow  = expectedSheet.getRow(rowIndex);
			
			//TM-28/09/2015: if actual and expected sheet row count does not match then break after following the steps in this code block.
			if(actualRow == null || expectedRow == null)
			{
				status.clear();
				report.strStatus = "FAIL";
				report.setStrStatus(report.strStatus);
				rowStatus.add(report.strStatus);
				failCount +=1;						
				
				//TM-28/09/2015: if expected sheet row count is greater than actual sheet row count else vice-versa
				if(actualRow == null)
					report.strActualValue = "Expected No. of rows are greater than Actual No. of rows.";				
				else
					report.strActualValue = "Actual No. of rows are greater than Expected No. of rows.";
						
				actualValue = new ArrayList<String>();
				actualValue.add(report.strActualValue);
				actualRows.add(actualValue);
				PassCount.add(passCount);
				FailCount.add(failCount);	
				report.setReport(report);
				break;
			}
			
			
			if(actualRow.getCell(0).toString().equals(expectedRow.getCell(0).toString())&& actualRow.getCell(1).toString().equals(expectedRow.getCell(1).toString()))
			{
				
				isrowFound =true;
				actualValue = new ArrayList<String>();
				
				//TM:6/08/15-This is unreachable code
				/*if(actualRow == null || expectedRow == null)
				{
					break;
				}*/
				
				colCount = expectedRow.getPhysicalNumberOfCells();
				for(int columnIndex = 3; columnIndex<colCount; columnIndex++)
				{
					HSSFCell actualCell = actualRow.getCell(columnIndex);
					DataFormatter fmt = new DataFormatter();				
					HSSFCell expectedCell = expectedRow.getCell(columnIndex);
					//TM: commented the code to find replacement of continue
					/*if(actualCell == null || expectedCell == null)
					{
						continue;
					}*/
					//TM: Following 'if' is replacement of the above
					if (actualCell != null || expectedCell != null){
						String expectedValue= fmt.formatCellValue(expectedCell);
						
						if(!actualCell.toString().equalsIgnoreCase(expectedValue))
						{
							report.strStatus = "FAIL";
							report.setStrStatus(report.strStatus);						
							failCount +=1;						
							report.strActualValue = "FAIL |" + expectedValue + "|" + actualCell.toString();
						}
						else
						{
							passCount +=1;
							report.strStatus = "PASS";
							report.setStrStatus(report.strStatus);
							report.strActualValue = actualCell.toString();
							//System.out.println(actualCell.toString());
						}
						status.add(report.strStatus);
						actualValue.add(report.strActualValue);
					}
					
				}
				if(status.contains("FAIL"))
				{				
					report.strStatus="FAIL";
				}
				else
				{
					report.strStatus ="PASS";
				}
				status.clear();
				rowStatus.add(report.strStatus);
				PassCount.add(passCount);
				FailCount.add(failCount);	
				actualRows.add(actualValue);
				report.setReport(report);
			}
			else if(isrowFound == false)
			{			
				continue;
				/*MainController.pauseFun("No Rows Found For Comparision");
				break;*/
			}
		}
		if(rowStatus.contains("FAIL"))
		{
			report.strStatus = "FAIL";
		}
		WriteToDetailResults(testCaseID,transactionType,columns, actualRows, passCount, failCount, expSheetRowCount, colCount,report,rowStatus);
		PassCount.clear();
		FailCount.clear();
		return report;
	}
		
	public static Reporter CompareExcelRowWise(HSSFSheet actualSheet,HSSFSheet expectedSheet,List<String> columns,List<String> columnsData,String testCaseID,String transactionType) throws IOException
	{
		boolean isrowFound = false;
		
		int expSheetRowCount = getRowCount(expectedSheet);//expectedSheet.getPhysicalNumberOfRows(); 
		int effectivefirstRow = firstRow;
		int actualSheetRowCount = ExcelUtility.getRowCount(actualSheet);
		Reporter report =new Reporter();
		report.setReport(report);
		int passCount=0, failCount=0, colCount=0, finalRowCount=0;
		WebVerification.currentRowIndex=0;
		
		//TM-28/09/2015: if expected sheet row count not matching actual sheet row count than Come out
		if (expSheetRowCount != actualSheetRowCount){
			TransactionMapping.report.strStatus = "FAIL";
			TransactionMapping.report.strMessage = "Expected No. of rows does not match Actual No. of rows, Please check Expected and Actual sheets";
								
		}
			
		else{			
		
			for(int rowIndex=effectivefirstRow;rowIndex<effectivefirstRow+expSheetRowCount;rowIndex++)
			{
				passCount=0;
				failCount =0;
				int currentRow = ++WebVerification.currentRowIndex;
				HSSFRow actualRow  = actualSheet.getRow(currentRow);
				HSSFRow expectedRow  = expectedSheet.getRow(rowIndex);
				
				//TM-28/09/2015: if actual and expected sheet row count does not match then break after following the steps in this code block.
				if(actualRow == null || expectedRow == null)
				{
					status.clear();
					report.strStatus = "FAIL";
					report.setStrStatus(report.strStatus);
					rowStatus.add(report.strStatus);
					failCount +=1;						
					
					//TM-28/09/2015: if expected sheet row count is greater than actual sheet row count else vice-versa
					if(actualRow == null)
						report.strActualValue = "Expected No. of rows are greater than Actual No. of rows.";				
					else
						report.strActualValue = "Actual No. of rows are greater than Expected No. of rows.";
							
					actualValue = new ArrayList<String>();
					actualValue.add(report.strActualValue);
					actualRows.add(actualValue);
					PassCount.add(passCount);
					FailCount.add(failCount);	
					report.setReport(report);
					break;
				}
				
				
				if(actualRow.getCell(0).toString().equals(expectedRow.getCell(0).toString())&& actualRow.getCell(1).toString().equals(expectedRow.getCell(1).toString()))
				{
					
					
					isrowFound =true;
					actualValue = new ArrayList<String>();								
					
					colCount = expectedRow.getPhysicalNumberOfCells();
					for(int columnIndex = 3; columnIndex<colCount; columnIndex++)
					{
						HSSFCell actualCell = actualRow.getCell(columnIndex);
						DataFormatter fmt = new DataFormatter();				
						HSSFCell expectedCell = expectedRow.getCell(columnIndex);
						
						//TM: Following 'if' is replacement of the above
						if (actualCell != null || expectedCell != null){
							String expectedValue= fmt.formatCellValue(expectedCell);
							
							if(!actualCell.toString().equalsIgnoreCase(expectedValue))
							{
								report.strStatus = "FAIL";
								report.setStrStatus(report.strStatus);						
								failCount +=1;						
								report.strActualValue = "FAIL |" + expectedValue + "|" + actualCell.toString();
							}
							else
							{
								passCount +=1;
								report.strStatus = "PASS";
								report.setStrStatus(report.strStatus);
								report.strActualValue = actualCell.toString();
								System.out.println(actualCell.toString());
							}
							status.add(report.strStatus);
							actualValue.add(report.strActualValue);
						}
						
					}
					if(status.contains("FAIL"))
					{				
						report.strStatus="FAIL";
						TransactionMapping.report.strStatus = "FAIL";
						TransactionMapping.report.strMessage = "Please refer to Detailed Results for more info..";
					}
					else
					{
						report.strStatus ="PASS";
					}
					status.clear();
					rowStatus.add(report.strStatus);
					PassCount.add(passCount);
					FailCount.add(failCount);	
					actualRows.add(actualValue);
					report.setReport(report);
				}
				else if(isrowFound == false)
				{			
					continue;					
				}
			}
			if(rowStatus.contains("FAIL"))
			{
				report.strStatus = "FAIL";
			}
			WriteToDetailResults(testCaseID,transactionType,columns, actualRows, passCount, failCount, expSheetRowCount, colCount,report,rowStatus);
		}
		
		PassCount.clear();
		FailCount.clear();
		return report;
	}

		
		
		/**
		 * This method is called to generate DetailedResults.csv when Web Table Verifications are conducted.
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
				report.strTestcaseId =TransactionMapping.testCaseID.toString();
				report.strTrasactionType = TransactionMapping.transactionType.toString();
				report.strStatus = report.getStrStatus();

				if(WebHelper.file.exists() == false)
				{
					print = new PrintStream(WebHelper.file);
				} 
				/*else
				{*/
					//Tripti: Below 3 lines should be outside of else
					columns.remove("TestCaseID");
					columns.remove("TransactionType");
					columns.remove("CurrentDate");
			//	}

				print = new PrintStream(new FileOutputStream(WebHelper.file, true));
				int	usedRows = WebHelper.count(WebHelper.file);
				if(usedRows == 0)
				{
					//TM: Added println instead of print
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
				TransactionMapping.pauseFun(e.getMessage());

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
