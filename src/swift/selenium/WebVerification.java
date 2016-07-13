package swift.selenium;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@SuppressWarnings("unused")
public class WebVerification {
	private static HashMap<String, Object> vTableListMap = new HashMap<String, Object>();
	private static HashMap<String, Object> templateMap = new HashMap<String, Object>();
	private static List<String> columns = new ArrayList<String>();
	private static List<String> columnsData = new ArrayList<String>();
	private static Date vdate =null;
	private static HashMap<String,Object> inputHashTable =new HashMap<String, Object>();
	private static List<List<String>> rows = new ArrayList<List<String>>();
	public static int currentRowIndex = 0;
	private static boolean isTableFound = false;
	public static boolean isFromVerification = false;
	public static boolean isDatabaseExpected = false;//TM:29-07-2015: New variable for case DB
	//private static Date dt =null; TM:18-03-2015 - Commenting this variable as it is causing NullPointerException in catch patch of the code
	public static String GroupBy = "";

	/**
	 * This method is called to perform web verifications as per following conditions :-
	 * 1. if Actual Sheet does not exist then create a new one.
	 * 2. if Expected Sheet does not exist then fail the entire TC and record in Results.csv "Expected Sheet not found| Actual Sheet created" after creating Actual Sheet
	 * 3. if Expected Sheet exists but does not has any rows to compare then fail the entire TC and record in Results.csv "No rows found in Expected Sheet| Actual Sheet created" after creating Actual Sheet 
	 * 4. if No. of rows in Expected Sheet > No. of rows in Actual Sheet 
	 * 		a. compare the compatible no. of rows of in both the sheets
	 * 		b. record failure in Results.csv with message "See detailed results"
	 * 		c. In DetailedResults.csv record comparison results of all the compatible rows and at the end record a message "Expected No. of rows are greater than Actual No. of rows.
	 * 5. if No. of rows in Actual Sheet > No. of rows in Expected Sheet
	 * 		a. compare the compatible no. of rows of in both the sheets
	 * 		b. record failure in Results.csv with message "See detailed results"
	 * 		c. In DetailedResults.csv record comparison results of all the compatible rows and at the end record a message "Actual No. of rows are greater than Expected No. of rows."
	 * 6. If  No. of rows in Actual Sheet = No. of rows in Expected Sheet then compare all the rows expected and record pass/fail
	 * @param transactionType
	 * @param testcaseID
	 * @throws IOException
	 * @throws Exception
	 */
	public static void performVerification(String transactionType,String testcaseID) throws IOException,Exception
	{
		HSSFSheet vTableSheet = ExcelUtility.GetSheet(Automation.configHashMap.get("VERIFICATIONTABLELISTPATH").toString(),"VerificationTables");
		int rowCount = vTableSheet.getLastRowNum()+1;
		vTableListMap = WebHelper.getValueFromHashMap(vTableSheet);
		Reporter report = new Reporter();
		HSSFSheet actualSheet=null;
		WebElement tableElement = null;
		List<WebElement> rowElements = null;
		String ActualPath = null;	
		String expectedSheetPath = null;
		String SheetName = "ActualValues";
		vdate = new Date();
		WebHelper.frmDate =new Date();
		String previousControlName, nextControlName="";
		HashMap<String,Object> inputHashTableHeader =new HashMap<String, Object>();
		
		for(int rowIndex=1;rowIndex<rowCount&&!TransactionMapping.pauseExecution;rowIndex++)
		{
			try
			{
				HSSFRow vRow = vTableSheet.getRow(rowIndex);
				String executeFlag = WebHelper.getCellData("Verify", vTableSheet, rowIndex,inputHashTable);
				String vTransaction = WebHelper.getCellData("TransactionType", vTableSheet, rowIndex,inputHashTable);
				if(isTableFound == true && !vTransaction.equalsIgnoreCase(transactionType))
				{
					break;
				}
				
				if(executeFlag.toString().equalsIgnoreCase("Y") && vTransaction.toString().equalsIgnoreCase(transactionType.toString()))
				{ 
					isTableFound = true;
					
					String functionalFlag = WebHelper.getCellData("Functional", vTableSheet, rowIndex,inputHashTable);				
					String templateDir = WebHelper.getCellData("TemplateAdditionalPath", vTableSheet, rowIndex,inputHashTable);
					String templateSheet = WebHelper.getCellData("TemplateSheet", vTableSheet, rowIndex,inputHashTable);
					String expectedDirPath = WebHelper.getCellData("ExpectedDataAdditionalPath", vTableSheet, rowIndex,inputHashTable);
					String expectedSheet = WebHelper.getCellData("ExpectedDataSheet", vTableSheet, rowIndex,inputHashTable);
					String templatePath = Automation.configHashMap.get("VERIFCATIONTEMPLATEPATH").toString() + templateDir.toString() + "\\" + templateSheet.toString();
					System.out.println(templatePath);
					ActualPath = Automation.configHashMap.get("EXPECTEDVALUESPATH").toString()+ expectedDirPath + "\\" +transactionType + "_Actual.xls";
					//TM:16-01-2015
					expectedSheetPath=Automation.configHashMap.get("EXPECTEDVALUESPATH").toString()+expectedDirPath.toString()+"\\"+expectedSheet.toString();
					HSSFSheet layoutSheet = ExcelUtility.GetSheet(templatePath, "Layout");
					inputHashTableHeader=WebHelper.getValueFromHashMap(layoutSheet);
					int templateRowCount = layoutSheet.getLastRowNum()+1;
					templateMap = WebHelper.getValueFromHashMap(layoutSheet);

					for(int templateIndex=1;templateIndex<templateRowCount&&!TransactionMapping.pauseExecution;templateIndex++)
					{
						HSSFRow layoutRow = layoutSheet.getRow(templateIndex);
						String tableID = WebHelper.getCellData("TableID", layoutSheet, templateIndex,inputHashTable);
						String tableType = WebHelper.getCellData("TableType", layoutSheet, templateIndex,inputHashTable);
						String tableIDType = WebHelper.getCellData("TableIDType", layoutSheet, templateIndex,inputHashTable);
						String startRow = WebHelper.getCellData("StartRow", layoutSheet, templateIndex,inputHashTable);
						String endRow = WebHelper.getCellData("EndRow", layoutSheet, templateIndex,inputHashTable);
						String columnName = WebHelper.getCellData("ColumnName", layoutSheet, templateIndex,inputHashTable);
						String rowNo = WebHelper.getCellData("Row", layoutSheet, templateIndex,inputHashTable);
						String colNo = WebHelper.getCellData("Column", layoutSheet, templateIndex,inputHashTable);
						String controlName = WebHelper.getCellData("ControlName", layoutSheet, templateIndex,inputHashTable);
						String controlType = WebHelper.getCellData("ControlType", layoutSheet, templateIndex,inputHashTable);
						String controlID = WebHelper.getCellData("ControlID", layoutSheet, templateIndex,inputHashTable);
												
						if(inputHashTableHeader.get("Previous_ControlName")!= null){
							previousControlName = WebHelper.getCellData("Previous_ControlName", layoutSheet, templateIndex,inputHashTable);
						}
						if(inputHashTableHeader.get("Next_ControlName")!= null){
							nextControlName = WebHelper.getCellData("Next_ControlName", layoutSheet, templateIndex,inputHashTable);
						}
						
						
						report.strTestcaseId = testcaseID.toString();
						report.strTrasactionType = vTransaction.toString();		
						report.strTestDescription = MainController.testDesciption;
						report.frmDate = Automation.dtFormat.format(vdate);
						report.strIteration = Automation.configHashMap.get("CYCLENUMBER").toString();
						report.strStatus = "PASS";
						report.strMessage = " ";
						if(tableType.equalsIgnoreCase("NonUniform"))
						{
							columns.add("TestCaseID");
							columns.add("TransactionType");
							columns.add("CurrentDate");
							// tableElement =  Automation.driver.findElement(By.id(tableID.toString()));
							// tableElement = getElementByType(tableIDType.toString(), tableID.toString());
							columnsData = new ArrayList<String>();
							columnsData.add(testcaseID.toString());
							columnsData.add(vTransaction.toString());
							columnsData.add(Automation.dtFormat.format(vdate));
							int colNoInt;
							String strXPath;
							
							for(templateIndex=1;templateIndex<templateRowCount;templateIndex++)
							{
								layoutRow = layoutSheet.getRow(templateIndex);
								columnName = WebHelper.getCellData("ColumnName", layoutSheet, templateIndex,inputHashTable);
								columns.add(columnName.toString());
								
								rowNo = WebHelper.getCellData("Row", layoutSheet, templateIndex,inputHashTable);
								colNo = WebHelper.getCellData("Column", layoutSheet, templateIndex,inputHashTable);
								colNoInt = Integer.parseInt(colNo);
										
								//String sVal = Automation.selenium.getTable(tableID.toString() + "." + rowNo + "." + colNo);//RC Code
								//strXPath = tableID + "/tbody/tr[" + (rowNo) + "]/td[" + (colNoInt) + "]";//Alternative if below line doesn't work 	
								strXPath = tableID + "/tbody/tr[" + (rowNo+1) + "]/td[" + (colNoInt+1) + "]"; 	
								String sVal = Automation.driver.findElement(By.xpath(strXPath)).getText();
								columnsData.add(sVal);
							}
							rows.add(columnsData);
							
						}
						else if(tableType.equalsIgnoreCase("DB"))
						{
							if(tableIDType.equalsIgnoreCase("Expected"))
							{
								isDatabaseExpected = true;
								SheetName = "Expected";
								ActualPath = Automation.configHashMap.get("EXPECTEDVALUESPATH").toString() + expectedDirPath+"\\"+transactionType + "_Expected.xls";								
							}
							ResultSet rs  = JDBCConnection.establishDBConn("", tableID);
							isTableFound = true;
							columns.add("TestCaseID");
							columns.add("TransactionType");
							columns.add("CurrentDate");

							for(int rIndex=Integer.parseInt(startRow.toString());rs.next(); rIndex++)//rowElements.size()
							{	
								columnsData =new ArrayList<String>();
								columnsData.add(testcaseID.toString());
								columnsData.add(vTransaction.toString());
								columnsData.add(Automation.dtFormat.format(vdate));

								for(templateIndex=1;templateIndex<templateRowCount;templateIndex++)
								{
									layoutRow = layoutSheet.getRow(templateIndex);
									columnName = WebHelper.getCellData("ColumnName", layoutSheet, templateIndex,inputHashTable);
									colNo = WebHelper.getCellData("Column", layoutSheet, templateIndex,inputHashTable); 

									if(rIndex == Integer.parseInt(startRow.toString()))
									{
										columns.add(columnName.toString());
									}
									columnsData.add(rs.getString(Integer.parseInt(colNo)));									
								}
								rows.add(columnsData);								
							}
						}
						
						else if(tableType.equalsIgnoreCase("Uniform"))
							
						{						
							int rCount=0;							
							columns.add("TestCaseID");
							columns.add("TransactionType");
							columns.add("CurrentDate");
							int colNoInt;
							String strXPath;
							
							int pageCount=0;
							
							while (true){								
								
								tableElement = getElementByType(tableIDType, tableID);
								rowElements = tableElement.findElements(By.xpath(tableID + "/tbody/tr"));
								
								if(endRow.equalsIgnoreCase("0")||endRow.equalsIgnoreCase("")){								
									rCount = rowElements.size();
								}
								else{								
									rCount = Integer.parseInt(endRow);
									if(rCount>rowElements.size())
									{
										rCount = rowElements.size();
									}
								}
								
									for(int rIndex=Integer.parseInt(startRow.toString());rIndex<rCount; rIndex++){//row-wise loop
										
										columnsData.add(testcaseID.toString());
										columnsData.add(vTransaction.toString());
										columnsData.add(Automation.dtFormat.format(vdate));
										fetchTableData(layoutSheet, tableID, templateRowCount, pageCount, startRow, rIndex);									
										
									}
								
									if (isNavigationComplete( nextControlName)){
										pageCount++;
										break;
									}
									else{
										continue;
									}								
							}							
						
						}
						
						else if(tableType.equalsIgnoreCase("UniformGroupBy"))
							
						{						
							System.out.println("Value of Group By is " + GroupBy);
							int rCount=0;
							int startRowMatched=0;
							columns.add("TestCaseID");
							columns.add("TransactionType");
							columns.add("CurrentDate");
							int colNoInt;
							String strXPath;
							
							int pageCount=0;
							Boolean bGroupByComplete= false;
							
							while (true){
								
									startRowMatched=0;
									rowElements =new ArrayList<WebElement>();
									tableElement = getElementByType(tableIDType, tableID);
									rowElements = tableElement.findElements(By.xpath(tableID + "/tbody/tr"));							
									
									// Find Matching Group By Element
									Boolean bFoundGroupBy = false;
									for (WebElement elementGroupBy: rowElements){
										String strClassName =elementGroupBy.getAttribute("class");
										if (strClassName.equals("group")){
											
											if ( elementGroupBy.findElement(By.cssSelector(" strong")).getText().trim().equals(GroupBy)){
												bFoundGroupBy=true;
												break;											
											
											}
											
										}
										startRowMatched++;
									}
									
									if (!bFoundGroupBy){// Group by not found on First Page																
											
										if (isNavigationComplete(nextControlName)){
											break;
										}
										else{
											continue;
										}											
										
									}									
									
									startRow = Integer.toString(startRowMatched+1);																
									rCount = rowElements.size();								
									
									for(int rIndex=Integer.parseInt(startRow.toString());rIndex<rCount; rIndex++)//row-wise loop
									{
											String strClassName =rowElements.get(rIndex).getAttribute("class");
											if (strClassName.equals("group") && (!rowElements.get(rIndex).getText().trim().equals(GroupBy))){
												bGroupByComplete= true;
												break;
											}
											columnsData.add(testcaseID.toString());
											columnsData.add(vTransaction.toString());
											columnsData.add(Automation.dtFormat.format(vdate));	
											fetchTableData(layoutSheet, tableID, templateRowCount, pageCount, startRow, rIndex);							
											
										}
										
										if (bGroupByComplete) {
											break;
										}
										else{
											
											if (isNavigationComplete( nextControlName)){
												pageCount++;
												break;
											}
											else{
												continue;
											}
										}																		
																
								}		
						
						}
						else if(tableType.equalsIgnoreCase("UniformDynamic"))
						{
							columns.add("TestCaseID");
							columns.add("TransactionType");
							columns.add("CurrentDate");
							int colNoInt;
							String strXPath;

							if(!Automation.configHashMap.get("SELENIUMEXECUTION").toString().equalsIgnoreCase("RC"))
							{
								tableElement = getElementByType(tableIDType, tableID);
								rowElements = tableElement.findElements(By.tagName("tr"));
							}

							for(int rIndex=Integer.parseInt(startRow.toString());rIndex<rowElements.size(); rIndex++)
							{
								columnsData =new ArrayList<String>();
								columnsData.add(testcaseID.toString());
								columnsData.add(vTransaction.toString());
								columnsData.add(Automation.dtFormat.format(vdate));
								for(templateIndex=1;templateIndex<templateRowCount;templateIndex++)
								{
									layoutRow = layoutSheet.getRow(templateIndex);
									columnName = WebHelper.getCellData("ColumnName", layoutSheet, templateIndex,inputHashTable);
									colNo = WebHelper.getCellData("Column", layoutSheet, templateIndex,inputHashTable); 
									colNoInt = Integer.parseInt(colNo);

									if(rIndex == Integer.parseInt(startRow.toString()))
									{
										columns.add(columnName.toString());
									}
									
									//String sVal = Automation.selenium.getTable(tableID.toString() + "." + rIndex + "." + colNo);
									strXPath = tableID + "/tbody/tr[" + (rIndex+1) + "]/td[" + (colNoInt+1) + "]"; 	
									String sVal = Automation.driver.findElement(By.xpath(strXPath)).getText();
									columnsData.add(sVal);
									
								}

								rows.add(columnsData);
							}		
							break;
						}
						else if(tableType.toString().equalsIgnoreCase("ControlNames"))
						{
							//isTableFound = true;
							isFromVerification = true;
							columns.add("TestCaseID");
							columns.add("TransactionType");
							columns.add("CurrentDate");
							columnsData =new ArrayList<String>();
							columnsData.add(testcaseID.toString());
							columnsData.add(vTransaction.toString());
							columnsData.add(Automation.dtFormat.format(vdate));

							for(templateIndex=1;templateIndex<templateRowCount;templateIndex++)
							{
								layoutRow = layoutSheet.getRow(templateIndex);

								columnName = WebHelper.getCellData("ColumnName", layoutSheet, templateIndex,inputHashTable);
								controlType = WebHelper.getCellData("ControlType", layoutSheet, templateIndex,inputHashTable);
								controlName = WebHelper.getCellData("ControlName", layoutSheet, templateIndex,inputHashTable);
								controlID   = WebHelper.getCellData("ControlID", layoutSheet, templateIndex,inputHashTable);
								tableElement = getElementByType(controlID.toString(), controlName.toString());
								columns.add(columnName.toString());
								String sVal= WebHelper.doAction("",controlType.toString(), controlID.toString(), controlName.toString(), "", "", "V", tableElement,false,null,null,0,0,"","");
								columnsData.add(sVal);

							}
							rows.add(columnsData);

						}
						
						/*Added by SS on 02-Apr-16
						 * Pre-requisites: tableID locator should result in parent row element 
						 * Column elements should be specified along with Control Name i.e Locator(It should signify that it is direct child)
						 * e.g. ==>    .//div[@class='SCDWAddress'] in case of XPath(note the dot sign in beginning)
						 * for each row element, column elements are found in iterative manner
						 * ControlType field is not used
						 */
						else if(tableType.toString().equalsIgnoreCase("DivTable"))
						{
							
							int rCount=0;
							isFromVerification = true;
							isDatabaseExpected= false;
							rowElements = WebHelper.getElementsByType( tableIDType,  tableID,  "","","");
							
							if(endRow.equalsIgnoreCase("0")||endRow.equalsIgnoreCase("")){								
								rCount = rowElements.size();
							}
							else{								
								rCount = Integer.parseInt(endRow);
								if(rCount>rowElements.size())
								{
									rCount = rowElements.size();
								}
							}
							
							String sVal="";
							WebElement cellElement=null;
							int rowNumber=0;
							
							for(int rIndex=Integer.parseInt(startRow.toString());rIndex<rCount; rIndex++){//row-wise loop
																		
								if (rowNumber==0){
									columns.add("TestCaseID");
									columns.add("TransactionType");
									columns.add("CurrentDate");
								}
								
								columnsData =new ArrayList<String>();
								columnsData.add(testcaseID.toString());
								columnsData.add(vTransaction.toString());
								columnsData.add(Automation.dtFormat.format(vdate));

								for(templateIndex=1;templateIndex<templateRowCount;templateIndex++)
								{
									layoutRow = layoutSheet.getRow(templateIndex);
	
									columnName = WebHelper.getCellData("ColumnName", layoutSheet, templateIndex,inputHashTable);
									controlType = WebHelper.getCellData("ControlType", layoutSheet, templateIndex,inputHashTable);
									controlName = WebHelper.getCellData("ControlName", layoutSheet, templateIndex,inputHashTable);
									controlID   = WebHelper.getCellData("ControlID", layoutSheet, templateIndex,inputHashTable);
									
									cellElement =rowElements.get(rIndex).findElement(By.xpath(controlName.toString()));
									
									sVal=cellElement.getText();							
									
									if (rowNumber==0){
										columns.add(columnName.toString());
									}									
									columnsData.add(sVal);
	
								}
								rows.add(columnsData);
								rowNumber++;
								cellElement=null;
							
							}

						}
						
					}
					/* TM-28/09/2015: Updated the code for better reporting :-
					 * 1. If actual sheet does not already exist then create a new actual sheet with the captured data
					 * 2. If actual sheet already exists then append the newly captured at the end of the last record in the sheet
					 */
					actualSheet =createActualSheet(vTransaction.toString(),columns,rows,ActualPath,SheetName);	
					
					if(isDatabaseExpected)
						continue;
					//TM: 16-01-2015
					/*if(!expectedSheet.toString().equalsIgnoreCase("$IGNORE$"))
					{*/
					File expectedFile= new File(expectedSheetPath);
					//TM-28/09/2015: Check if expected sheet exists only that proceed with comparison else FAIL the complete TC and record appropriate message in Results.csv
					if(expectedFile.exists()){
						HSSFSheet expectedSht = ExcelUtility.GetSheet(expectedSheetPath, "Expected");
						int expectedRowcount = ExcelUtility.getRowCount(expectedSht);
						//TM-28/09/2015: Compare actual and expected sheet only if the row count of expected sheet is greater than 0 else FAIL the complete TC and record appropriate message in Results.csv
						if(expectedRowcount > 0)
							report = ExcelUtility.CompareExcel(actualSheet,expectedSht,columns,columnsData,testcaseID.toString(),vTransaction.toString(), rows.size());
						else{
							report.strStatus = "FAIL";
							report.strMessage = "No rows found in Expected Sheet| Actual Sheet created";
						}
						report.setReport(report);
					}
					else
					{
						report.strStatus = "FAIL";
						report.strMessage = "Expected Sheet not found| Actual Sheet created";
					}
					//}

				}				
				else if(!vTransaction.equalsIgnoreCase(transactionType)&& rowIndex == rowCount-1)
				{
					TransactionMapping.pauseFun("Transaction " +transactionType + " not Found" );
				}
			
			}
			catch(Exception e)
			{
				report.toDate = Automation.dtFormat.format(new Date());//TM:18-03-2015-changed from dt to new Date() as dt was null at this point and NullPointerException was caused
				report.strStatus = "FAIL";
				report.strMessage = e.getMessage();
				ExcelUtility.writeReport(report);
				TransactionMapping.pauseFun(e.getMessage());
			}
			finally
			{
				columns.clear();
				columnsData.clear();
				rows.clear();				
			}
		}
		//dt =new Date();//TM:18-03-2015-dt variable declaration has been removed as it is not required in the code

		report.getReport();		
		report.strTestDescription = MainController.testDesciption;
		report.toDate = Automation.dtFormat.format(new Date());//TM:18-03-2015-changed from dt to new Date() as dt was null at this point and NullPointerException was caused in catch part
		//TM-19/01/2015: changes made to add the following message only if Blank
		if(StringUtils.isBlank(report.strMessage))
			report.strMessage = "See Detailed Results";
		//	
		isTableFound = false;
		ExcelUtility.writeReport(report);
	}
	
	public static boolean isNavigationComplete(String nextControlName) throws InterruptedException
	{
		if((nextControlName == "" || nextControlName.equalsIgnoreCase(null))){
			return true;
		}
		else{
			WebElement elementNext = Automation.driver.findElement(By.cssSelector(nextControlName));
			
			if (elementNext.getAttribute("class").contains("disabled")){
				return true;
			}
			else{
				
				//Slowly scroll the mouse
				for (int second = 0;; second++) {
			        if(second >=3){
			            break;
			        }
			            ((JavascriptExecutor) Automation.driver).executeScript("window.scrollBy(0,600)", ""); //y value '400' can be altered
			            Thread.sleep(500);
				}								
								
				elementNext.click();
				Thread.sleep(500);
			}
		}
		return false;
	}
	
	public static void fetchTableData(HSSFSheet layoutSheet, String tableID, int templateRowCount, 
			int pageCount, String startRow,int rIndex) throws InterruptedException, IOException
	{
		int templateIndex, colNoInt;
		String strXPath="", columnName="", controlName="", colNo="", controlType="";
		HSSFRow layoutRow;
	
		for(templateIndex=1;templateIndex<templateRowCount;templateIndex++)//column-wise loop
		{
			layoutRow = layoutSheet.getRow(templateIndex);
			columnName = WebHelper.getCellData("ColumnName", layoutSheet, templateIndex,inputHashTable);
			controlName = WebHelper.getCellData("ControlName", layoutSheet, templateIndex,inputHashTable);
			colNo = WebHelper.getCellData("Column", layoutSheet, templateIndex,inputHashTable);									
			controlType = WebHelper.getCellData("ControlType", layoutSheet, templateIndex,inputHashTable);
			colNoInt = Integer.parseInt(colNo);

			if(rIndex == Integer.parseInt(startRow.toString()))
			{
				if (pageCount == 0) {
					columns.add(columnName.toString());
				}
			}

			if(controlType == "" || controlType.equalsIgnoreCase(null))
			{
				strXPath = tableID + "/tbody/tr[" + (rIndex+1) + "]/td[" + (colNoInt+1) + "]"; 	
				String sVal = Automation.driver.findElement(By.xpath(strXPath)).getText();
				columnsData.add(sVal);								
			}									
			else
			{
				strXPath = tableID + "/tbody/tr["+ (rIndex+1) + "]/td[" + (colNoInt+1) + "]/" + controlName; //TM-10/02/2015: suggestion by Dhiraj
				String sVal = Automation.driver.findElement(By.xpath(strXPath)).getAttribute("value");
				columnsData.add(sVal);	
			}
		}
		
		rows.add(columnsData);
		columnsData =new ArrayList<String>();
	}

	public static HSSFRow createHeader(HSSFSheet actualSheet,List<String> columns)
	{
		HSSFRow actualRow = actualSheet.getRow(0);
		HSSFCell testCaseID = actualRow.createCell(0);
		HSSFCell transactionType = actualRow.createCell(1);
		HSSFCell Date = actualRow.createCell(2);
		Iterator<String> iterator = columns.iterator();
		int count =0;
		if(iterator.hasNext()==true)
		{
			count = count +1;
			//@SuppressWarnings("unused")
			HSSFCell dynamicColumns = actualRow.createCell(count);
		}
		return actualRow;
	}

	
	public static HSSFSheet createActualSheet(String transactionType,List<String> columns,List<List<String>> columnData,String actualPath,String SheetName) throws IOException,Exception
	{
		FileOutputStream out =null;
		FileOutputStream out1 = null;
		POIFSFileSystem lPOIfs = null;
		InputStream in = null;
		HSSFWorkbook workBook = null;
		HSSFSheet workSheet = null;
		//	String ActualPath = Automation.configHashMap.get("EXPECTEDVALUESPATH").toString()+transactionType + "_Actual.xls";
		File lFile= new File(actualPath);
		if(lFile.exists()&&SheetName.equalsIgnoreCase("Expected"))
		{
			lFile.delete();
			System.out.println("Deleted Existing Expected Sheet");
		}
		int columnSize  = columns.size();
		int columnIndex =0;
		HSSFCell actualCell =null;
		HSSFCell valuesCell = null;
		if(!lFile.exists())
		{
			try
			{
				workBook = new HSSFWorkbook();
				workSheet = workBook.createSheet(SheetName);
				HSSFRow actualRowHeader = workSheet.createRow(0);
				int rowNum = 0;
				HSSFRow valuesRow = workSheet.createRow(rowNum+1);
				currentRowIndex = rowNum;
				//Iterator<String> iterator = columns.iterator();
				int rowCount = rows.size();
				for(int rIndex=0;rIndex<rowCount;rIndex++)
				{
					if(rIndex>0)
					{
						valuesRow = workSheet.createRow(rowNum + 1 + rIndex);
					}
					columnSize = rows.get(rIndex).size();
					for(columnIndex = columnSize;columnIndex>=1;columnIndex--)
					{
						actualCell = actualRowHeader.createCell(columnSize - columnIndex);
						valuesCell = valuesRow.createCell(columnSize - columnIndex);
						actualCell.setCellValue(columns.get(columnSize - columnIndex));
						valuesCell.setCellValue(rows.get(rIndex).get(columnSize - columnIndex));
						System.out.print(valuesCell.toString());
					}
				}
				out = new FileOutputStream(actualPath);
				workBook.write(out);
			}
			catch(IOException ioe)
			{
				ioe.getLocalizedMessage();
			}
			finally
			{
				out.flush();
				out.close();
			}
		}
		else
		{
			try
			{
				in = new FileInputStream(actualPath);
				lPOIfs = new POIFSFileSystem(in);
				workBook = new HSSFWorkbook(lPOIfs);
				workSheet = workBook.getSheet(SheetName);
				int lastRow = workSheet.getLastRowNum();
				currentRowIndex = lastRow;
				HSSFRow row = workSheet.getRow(lastRow + 1);		
				if(row == null)
				{
					row = workSheet.createRow(lastRow + 1);
				}
				int rowCount=rows.size();
				for(int rIndex=0;rIndex<rowCount;rIndex++)
				{
					if(rIndex>0)
					{
						row = workSheet.createRow(lastRow + 1 + rIndex);
					}
					int cSize = rows.get(rIndex).size();

					for(columnIndex = cSize;columnIndex>=1;columnIndex--)
					{
						valuesCell = row.getCell(cSize - columnIndex);
						if(valuesCell == null)
						{
							valuesCell  = row.createCell(cSize - columnIndex);
							valuesCell.setCellType(Cell.CELL_TYPE_STRING);
							valuesCell.setCellValue(rows.get(rIndex).get(cSize - columnIndex));
						}
					}
				}
				out1 =new FileOutputStream(actualPath);
				workBook.write(out1);
			}
			catch(Exception ioe)
			{
				TransactionMapping.pauseFun(ioe.getLocalizedMessage() + " from CreateActualSheet Function ");
			}
			finally
			{
				out1.flush();
				out1.close();
				//System.out.println("HELLO WORLD");
			}
		}
		return workSheet;
	}

	@SuppressWarnings("incomplete-switch")
	public static WebElement getElementByType(String controlID,String controlName) throws IOException
	{
		WebElement element = null;
		try
		{
			WebHelper.ControlIdEnum controlId = WebHelper.ControlIdEnum.valueOf(controlID);	
			switch(controlId)
			{
			case ClassName:
				element = Automation.driver.findElement(By.className(controlName));
				break;
			case Id:
			case HTMLID:
				element = Automation.driver.findElement(By.id(controlName));
				break;
			case Name:
				element = Automation.driver.findElement(By.name(controlName));
				break;
			case TagName:
				element = Automation.driver.findElement(By.tagName(controlName));
				break;
			case XPath:
				element = Automation.driver.findElement(By.xpath(controlName));
				break;
			}
		}
		catch(Exception e){
			TransactionMapping.pauseFun(e.getMessage() + " from getElementByType Function");
		}
		return element;
	}


/* Following functions are written for Tabular Data verification which is not presented as actual tables
 * 
 * 
 * 
 */

	public static void verifyTabularData(String testcaseID, String transactionType, List<String> columns, List<List<String>> columnData) throws IOException,Exception
	{
		List<String> sheetList = new ArrayList<String>();		
		String transactionName = transactionType;
		sheetList = getExpectedObservedSheetDetails(transactionType, testcaseID);
		String actualPath =sheetList.get(0);
		String expectedPath =sheetList.get(1);

		//Create actual sheet with the values observed on the screen
		WebVerification.createActualValueSheet(testcaseID, transactionName, columns, columnData, actualPath, "ActualValues");
				
		File expectedFile= new File(expectedPath);
		Reporter report = new Reporter();
		
		if(expectedFile.exists()){
			HSSFSheet expectedSht = ExcelUtility.GetSheet(expectedPath, "Expected");
			HSSFSheet actualSheet = ExcelUtility.GetSheet(actualPath, "ActualValues");
			int expectedRowcount = ExcelUtility.getRowCount(expectedSht);
			//TM-28/09/2015: Compare actual and expected sheet only if the row count of expected sheet is greater than 0 else FAIL the complete TC and record appropriate message in Results.csv
			if(expectedRowcount > 0)
				report = ExcelUtility.CompareExcelRowWise(actualSheet,expectedSht,columns,columns,testcaseID.toString(),transactionName);
			else{
				report.strStatus = "FAIL";
				report.strMessage = "No rows found in Expected Sheet| Actual Sheet created";
			}
			report.setReport(report);
			
		}
		else
		{
			report.strStatus = "FAIL";
			report.strMessage = "Expected Sheet not found| Actual Sheet created";
			TransactionMapping.pauseFun(report.strMessage);
			
		}
		
		
	}
	
	public static List<String> getExpectedObservedSheetDetails(String transactionType,String testcaseID) throws IOException,Exception
	{
		HSSFSheet vTableSheet = ExcelUtility.GetSheet(Automation.configHashMap.get("VERIFICATIONTABLELISTPATH").toString(),"VerificationTables");
		int rowCount = vTableSheet.getLastRowNum()+1;
		vTableListMap = WebHelper.getValueFromHashMap(vTableSheet);
		Reporter report = new Reporter();
		HSSFSheet actualSheet=null;	
		String ActualPath = null;	
		String expectedSheetPath = null;
		String SheetName = "ActualValues";
		vdate = new Date();
		WebHelper.frmDate =new Date();
		List<String> sheetList = new ArrayList<String>();
		for(int rowIndex=1;rowIndex<rowCount;rowIndex++)
		{
			try
			{
				HSSFRow vRow = vTableSheet.getRow(rowIndex);
				String executeFlag = WebHelper.getCellData("Verify", vTableSheet, rowIndex,inputHashTable);
				String vTransaction = WebHelper.getCellData("TransactionType", vTableSheet, rowIndex,inputHashTable);
				/*if(isTableFound == true && !vTransaction.equalsIgnoreCase(transactionType))
				{
					break;
				}*/
				
				if(executeFlag.toString().equalsIgnoreCase("Y") && vTransaction.toString().equalsIgnoreCase(transactionType.toString()))
				{ 
					isTableFound = true;								
					
					String expectedDirPath = WebHelper.getCellData("ExpectedDataAdditionalPath", vTableSheet, rowIndex,inputHashTable);
					String expectedSheet = WebHelper.getCellData("ExpectedDataSheet", vTableSheet, rowIndex,inputHashTable);
					ActualPath = Automation.configHashMap.get("EXPECTEDVALUESPATH").toString()+ expectedDirPath + "\\" +transactionType + "_Actual.xls";
					expectedSheetPath=Automation.configHashMap.get("EXPECTEDVALUESPATH").toString()+expectedDirPath.toString()+"\\"+expectedSheet.toString();
					sheetList.add(ActualPath);
					sheetList.add(expectedSheetPath);
				}
				
			}
				
			catch(Exception e)
			{
				report.toDate = Automation.dtFormat.format(new Date());//TM:18-03-2015-changed from dt to new Date() as dt was null at this point and NullPointerException was caused
				report.strStatus = "FAIL";
				report.strMessage = e.getMessage();
				ExcelUtility.writeReport(report);
				TransactionMapping.pauseFun(e.getMessage());
			}			
			
		}
		return sheetList;	
					
	}
			
	public static HSSFSheet createActualValueSheet(String testcaseID, String transactionType,List<String> columns,List<List<String>> columnData,String observedPath,String SheetName) throws IOException,Exception
	{
		FileOutputStream out =null;
		FileOutputStream out1 = null;
		POIFSFileSystem lPOIfs = null;
		InputStream in = null;
		HSSFWorkbook workBook = null;
		HSSFSheet workSheet = null;
		
		//Pre append test attributes and date to the Columns
		ArrayList<String> columnsFinal = new ArrayList<String>();
		ArrayList<String> columnsTestAtributes = new ArrayList<String>(
				Arrays.asList("TestCaseID","TransactionType","CurrentDate"));		
		
		columnsFinal.addAll(columnsTestAtributes);
		columnsFinal.addAll(columns);
		
		//Pre append test attributes and date to the ColumnData
		List<List<String>> finalColumnData = new ArrayList<List<String>>();
		finalColumnData = getActualDataWithTestAttributes( testcaseID,  transactionType, columnData);
		System.out.println(finalColumnData);
		
		//	String ActualPath = Automation.configHashMap.get("EXPECTEDVALUESPATH").toString()+transactionType + "_Actual.xls";
		File lFile= new File(observedPath);
		if(lFile.exists()&&SheetName.equalsIgnoreCase("ActualValues"))
		{
			lFile.delete();
			System.out.println("Deleted Existing Expected Sheet");
		}
		int columnSize  = columnsFinal.size();
		int columnIndex =0;
		HSSFCell actualCell =null;
		HSSFCell valuesCell = null;
		if(!lFile.exists())
		{
			try
			{
				workBook = new HSSFWorkbook();
				workSheet = workBook.createSheet("ActualValues");
				HSSFRow actualRowHeader = workSheet.createRow(0);
				int rowNum = 0;
				HSSFRow valuesRow = workSheet.createRow(rowNum+1);
				
				//Iterator<String> iterator = columnsFinal.iterator();
				int rowCount = finalColumnData.size();
				for(int rIndex=0;rIndex<rowCount;rIndex++)
				{
					if(rIndex>0)
					{
						valuesRow = workSheet.createRow(rowNum + 1 + rIndex);
					}
					columnSize = finalColumnData.get(rIndex).size();
					for(columnIndex = columnSize;columnIndex>=1;columnIndex--)
					{
						actualCell = actualRowHeader.createCell(columnSize - columnIndex);
						valuesCell = valuesRow.createCell(columnSize - columnIndex);
						actualCell.setCellValue(columnsFinal.get(columnSize - columnIndex));
						valuesCell.setCellValue(finalColumnData.get(rIndex).get(columnSize - columnIndex));
						System.out.print(valuesCell.toString());
					}
				}
				out = new FileOutputStream(observedPath);
				workBook.write(out);
			}
			catch(IOException ioe)
			{
				ioe.getLocalizedMessage();
			}
			finally
			{
				out.flush();
				out.close();
			}
		}
		else
		{
			try
			{
				in = new FileInputStream(observedPath);
				lPOIfs = new POIFSFileSystem(in);
				workBook = new HSSFWorkbook(lPOIfs);
				workSheet = workBook.getSheet(SheetName);
				int lastRow = workSheet.getLastRowNum();
			
				HSSFRow row = workSheet.getRow(lastRow + 1);		
				if(row == null)
				{
					row = workSheet.createRow(lastRow + 1);
				}
				int rowCount=finalColumnData.size();
				for(int rIndex=0;rIndex<rowCount;rIndex++)
				{
					if(rIndex>0)
					{
						row = workSheet.createRow(lastRow + 1 + rIndex);
					}
					int cSize = finalColumnData.get(rIndex).size();

					for(columnIndex = cSize;columnIndex>=1;columnIndex--)
					{
						valuesCell = row.getCell(cSize - columnIndex);
						if(valuesCell == null)
						{
							valuesCell  = row.createCell(cSize - columnIndex);
							valuesCell.setCellType(Cell.CELL_TYPE_STRING);
							valuesCell.setCellValue(finalColumnData.get(rIndex).get(cSize - columnIndex));
						}
					}
				}
				out1 =new FileOutputStream(observedPath);
				workBook.write(out1);
			}
			catch(Exception ioe)
			{
				TransactionMapping.pauseFun(ioe.getLocalizedMessage() + " from CreateActualSheet Function ");
			}
			finally
			{
				out1.flush();
				out1.close();
				//System.out.println("HELLO WORLD");
			}
		}
		return workSheet;
	}
	
	public static List<List<String>> getActualDataWithTestAttributes(String testcaseID, String transactionType, List<List<String>> columnData){
		
		List<List<String>> finalColumnData = new ArrayList<List<String>>();
		
		ArrayList<String> columnsTestAtributes = new ArrayList<String>(
				Arrays.asList( testcaseID,  transactionType, Automation.dtFormat.format(new Date())));
		
		for(int rIndex=0;rIndex<columnData.size();rIndex++)
		{
			
			ArrayList<String> columnsFinal = new ArrayList<String>();
			columnsFinal.addAll(columnsTestAtributes);
			columnsFinal.addAll(columnData.get(rIndex));
			
			finalColumnData.add(columnsFinal);
			
		}
		
		return finalColumnData;
	}


}


