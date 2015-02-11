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

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.Screen;

public class WebHelper {

	public static HashMap<String,Object> structureHeader = new HashMap<String, Object>();
	public static HashMap<String,Object> valuesHeader =new HashMap<String, Object>();
	public static Date frmDate = null;
	public static File file = new File(Automation.configHashMap.get("VERIFICATIONRESULTSPATH").toString());
	public static PrintStream print=null;
	public static HSSFCell testcaseID =null;
	public static HSSFCell transactionType=null;
	public static String month;
	public static Robot robot;
	public static Boolean isIntialized=false;
	public static int fieldVerFailCount = 0;
	public static String wsdl_url, request_url, request_xml;

	public static enum ControlTypeEnum
	{I,NC,V,O,T,WebEdit,WebElement,Date,SikuliType,SikuliButton,ListBox,Radio,WebButton,WebLink,CheckBox,WebTable,Browser,Menu,Wait,WebImage,URL,WebList,Alert,Window,Robot,January, February, March, April, May, June, July, August, September, October, November, December,
	ActionClick,ActionDoubleClick,ActionClickandEsc,ActionMouseOver,Read,Write,WaitForJS,CloseWindow,FileUpload,JSScript,Calendar,CalendarNew,CalendarIPF,CalendarEBP,WaitForEC,IFrame,ScrollTo};
	
	public static enum ControlIdEnum {Id,HTMLID,Name,XPath,XPathValue,ClassName,TagName,LinkText,TagText,TagValue,TagOuterText,LinkValue,CSSSelector,Id_p,HTMLID_p,XPath_p};

	public static String control;
	public static Date toDate=null;
	public static String testCase = null;
	public static HSSFDataFormat format = null;
	public static Wait<WebDriver> wait = new WebDriverWait(Automation.driver,Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));
	public static Screen sikuliScreen = null;
	public static List<String> searchValue1 = null;
	public static int loopRow = 1;// Added for Action Loop
	public static String columnName;
	public static Boolean pageLoaded = false;
	public static Boolean isDynamicNumFound=false;

	public static void GetCellInfo(String FilePath,HSSFRow rowValues,int valuesRowIndex,int valuesRowCount) throws IOException // newly Added two Variables for Action Loop
	{
		try
		{	
			frmDate = new Date();
			isDynamicNumFound = true;
			List<WebElement> controlList = null;
			String ctrlValue= null;
			InputStream myXls = new FileInputStream(FilePath);
			HSSFWorkbook workBook = new HSSFWorkbook(myXls);
			format  = workBook.createDataFormat();
			HSSFSheet sheetStructure = workBook.getSheet("Structure");
			int rowCount = sheetStructure.getLastRowNum()+1;
			HSSFSheet headerValues = ExcelUtility.GetSheet(FilePath, "Values");
			System.out.println(Automation.dtFormat.format(frmDate));
			String fromDate = Automation.dtFormat.format(frmDate);
			SwiftSeleniumWeb.WebDriver.report.setFromDate(fromDate);
			structureHeader = getValueFromHashMap(sheetStructure);
			columnName = null;
			int dynamicIndexNumber;//Added for Action Loop
			String imageType,indexVal,controlName,executeFlag,action,logicalName,controltype,controlID,dynamicIndex,newDynamicIndex,rowNo,colNo;//newly Added for Action Loop

			//Setting of default reporting values before executing a transaction
			SwiftSeleniumWeb.WebDriver.report.setStrMessage("");
			SwiftSeleniumWeb.WebDriver.report.setStrStatus("PASS");	

			for(int rowIndex=1; rowIndex<rowCount&&!MainController.pauseExecution; rowIndex++)
			{					
				controlName = getCellData("ControlName",sheetStructure,rowIndex,structureHeader);
				executeFlag = getCellData("ExecuteFlag",sheetStructure,rowIndex,structureHeader);

				if(executeFlag.toString().equals("Y"))
				{
					WebElement webElement = null;
					imageType = getCellData("ImageType", sheetStructure, rowIndex, structureHeader);
					action = getCellData("Action",sheetStructure,rowIndex,structureHeader);
					logicalName = getCellData("LogicalName",sheetStructure,rowIndex,structureHeader);
					controltype = getCellData("ControlType",sheetStructure,rowIndex,structureHeader);
					controlID = getCellData("ControlID",sheetStructure,rowIndex,structureHeader);
					indexVal = getCellData("Index",sheetStructure,rowIndex,structureHeader);
					columnName = getCellData("ColumnName", sheetStructure, rowIndex, structureHeader);
					rowNo = getCellData("RowNo", sheetStructure, rowIndex, structureHeader);
					colNo = getCellData("ColumnNo", sheetStructure, rowIndex, structureHeader);					
					dynamicIndex = getCellData("DynamicIndex",sheetStructure,rowIndex,structureHeader);

					if (action.equalsIgnoreCase("LOOP"))
					{
						loopRow = rowIndex+1;
					}

					if ((valuesRowIndex!=ExcelUtility.firstRow)&&(dynamicIndex.length()>0)) //valuesRowIndex
					{

						dynamicIndexNumber =  Integer.parseInt(dynamicIndex.substring(dynamicIndex.length()-1,dynamicIndex.length()));	

						if(ExcelUtility.dynamicNum == 0)
						{
							ExcelUtility.dynamicNum = dynamicIndexNumber+1;	
							isDynamicNumFound = false;

						}
						else if(ExcelUtility.dynamicNum !=0 && isDynamicNumFound)
						{
							ExcelUtility.dynamicNum = ExcelUtility.dynamicNum +1;
							isDynamicNumFound = false;
						}
												
						newDynamicIndex = dynamicIndex.replace(String.valueOf(dynamicIndexNumber) ,  String.valueOf(ExcelUtility.dynamicNum));
						controlName = controlName.replace(dynamicIndex, newDynamicIndex );
					}

					/**Stop the execution of the current test case unexpected alert**/
					control = controltype.toString();
					if(isAlertPresent(control) == true)
					{
						break;
					}											

					if(!action.equalsIgnoreCase("LOOP")&&!action.equalsIgnoreCase("END_LOOP"))
					{
						if(valuesHeader.isEmpty()== true)
						{
							valuesHeader = getValueFromHashMap(headerValues);
						}
						Object actualValue=null;
						if(logicalName!=null){
							actualValue = valuesHeader.get(logicalName.toString());}//headerRow.getCell(colIndex);
						if(actualValue == null)
						{
							System.out.println("Null");
						}
						else
						{
							ctrlValue = getCellData(logicalName,headerValues, valuesRowIndex, valuesHeader);

							testcaseID = rowValues.getCell(Integer.parseInt(valuesHeader.get("TestCaseID").toString()));

							if(testcaseID == null)
							{
								testCase = "";
							}
							else
							{
								testCase = testcaseID.toString();
							}
							transactionType = rowValues.getCell(Integer.parseInt(valuesHeader.get("TransactionType").toString()));	
						}

						if ((action.equals("I")&&!ctrlValue.isEmpty())||(action.equals("V")&&!ctrlValue.isEmpty())|| !action.equals("I")) 
						{
							if(!controltype.startsWith("Sikuli"))
							{
								if(!action.equalsIgnoreCase("LOOP")&&!controltype.equalsIgnoreCase("Wait")&&!action.equalsIgnoreCase("END_LOOP")&&
										!controltype.equalsIgnoreCase("Browser")&&!controltype.equalsIgnoreCase("Window")&&!controltype.equalsIgnoreCase("Alert")&&
										!controltype.equalsIgnoreCase("URL")&&!controltype.equalsIgnoreCase("WaitForJS")&&!controltype.contains("Robot") && 
										!controltype.equalsIgnoreCase("Calendar")&&!controltype.equalsIgnoreCase("CalendarNew")&&!controltype.equalsIgnoreCase("CalendarIPF")&&!controltype.equalsIgnoreCase("CalendarEBP")&&										
										(!action.equalsIgnoreCase("Read")||((action.equalsIgnoreCase("Read")&& !controlName.isEmpty())))&&
										!controltype.equalsIgnoreCase("JSScript")&&!controltype.equalsIgnoreCase("DB")&& !controlID.equalsIgnoreCase("XML")&& !controltype.startsWith("Process")
										&& !controltype.startsWith("Destroy")&& !controltype.startsWith("ReadSikuli") &&!controltype.equalsIgnoreCase("WebService"))
								{
									if((indexVal.equalsIgnoreCase("")||indexVal.equalsIgnoreCase("0"))&& !controlID.equalsIgnoreCase("TagValue")&&!controlID.equalsIgnoreCase("TagText"))
									{
										webElement = getElementByType(controlID, controlName,control,imageType,ctrlValue);															

									}
									else
									{
										controlList = getElementsByType(controlID, controlName,control,imageType,ctrlValue);

										if(controlList != null && controlList.size() > 1)
										{
											webElement = GetControlByIndex(indexVal, controlList, controlID, controlName, control,ctrlValue); //, ISelenium selenium)
										}
										else
										{
											break;
										}
									}
								}
							}
							else
							{
								sikuliScreen = new Screen();
							}
						}

						/***	Perform action on the identified control	***/
						doAction(imageType,controltype,controlID,controlName,ctrlValue,logicalName,action,webElement,true,sheetStructure,headerValues,rowIndex,rowCount,rowNo,colNo);
					}

					if (action == "END_LOOP" && (valuesRowCount != valuesRowIndex))
					{
						loopRow = 1;
						break;				
					}

				}
				else
				{
					System.out.println("ExecuteFlag is N");
				}	
			}

			//Setting of reporting values after execution in case of no exception
			Date toDate = new Date();
			SwiftSeleniumWeb.WebDriver.report.setFromDate(Automation.dtFormat.format(frmDate));
			SwiftSeleniumWeb.WebDriver.report.setStrIteration(Automation.configHashMap.get("CYCLENUMBER").toString());
			SwiftSeleniumWeb.WebDriver.report.setStrTestcaseId(MainController.controllerTestCaseID.toString());
			SwiftSeleniumWeb.WebDriver.report.setStrGroupName(MainController.controllerGroupName.toString());
			SwiftSeleniumWeb.WebDriver.report.setStrTrasactionType(MainController.controllerTransactionType.toString());
			SwiftSeleniumWeb.WebDriver.report.setStrTestDescription(MainController.testDesciption);
			SwiftSeleniumWeb.WebDriver.report.setToDate(Automation.dtFormat.format(toDate));

			//Setting status for field verification failures
			if (fieldVerFailCount > 0)
			{
				SwiftSeleniumWeb.WebDriver.report.setStrMessage("Check Detailed Results");
				SwiftSeleniumWeb.WebDriver.report.setStrStatus("FAIL");	
			}
		}
		catch(Exception e)
		{
			MainController.pauseFun(e.getMessage());			
		}
		finally
		{
			structureHeader.clear();
			valuesHeader.clear();
			ExcelUtility.writeReport(SwiftSeleniumWeb.WebDriver.report);
			fieldVerFailCount = 0;
		}
	}
	@SuppressWarnings("null")
	public static String getCellData(String reqValue,HSSFSheet reqSheet,int rowIndex,HashMap<String,Object> inputHashTable) throws IOException
	{
		HSSFCell reqCell=null;
		Object actualvalue=null;		
		String req = "";
		DataFormatter fmt = new DataFormatter();		
		if(inputHashTable.isEmpty()== true)
		{
			inputHashTable=getValueFromHashMap(reqSheet);
		}
		HSSFRow rowActual = reqSheet.getRow(rowIndex);
		if(inputHashTable.get(reqValue)== null)
		{		
			SwiftSeleniumWeb.WebDriver.report.setStrMessage("Column "+ reqValue +" not Found. Please Check input Sheet");
			MainController.pauseFun("Column "+ reqValue +" not Found. Please Check input Sheet");				
		}
		else
		{
			actualvalue = inputHashTable.get(reqValue);//rowHeader.getCell(colIndex).toString();			
			if(actualvalue != null)
			{
				int colIndex = Integer.parseInt(actualvalue.toString());
				reqCell = rowActual.getCell(colIndex);
				if(reqCell == null)
				{				
					System.out.println(reqValue +" is Null");
				}
				else
				{
					int type = reqCell.getCellType();
					switch(type)
					{
					case HSSFCell.CELL_TYPE_BLANK:
						req = "";
						break;
					case HSSFCell.CELL_TYPE_NUMERIC:
						req = fmt.formatCellValue(reqCell);								
						break;
					case HSSFCell.CELL_TYPE_STRING:
						req = reqCell.getStringCellValue();
						break;
					case HSSFCell.CELL_TYPE_BOOLEAN:
						req = Boolean.toString(reqCell.getBooleanCellValue());
						break;
					case HSSFCell.CELL_TYPE_ERROR:
						req = "error";
						break;
					case HSSFCell.CELL_TYPE_FORMULA:
						req = reqCell.getCellFormula();
						break;
					}
				}
			}

			else 
			{
				req = reqCell.getStringCellValue();
				System.out.println("null");
			}
		}
		return req;
	}

	public static HashMap<String, Object> getValueFromHashMap(HSSFSheet reqSheet)
	{
		HashMap<String, Object> inputHashTable = new HashMap<String, Object>();
		HSSFRow rowHeader = reqSheet.getRow(0);
		int columnCount = rowHeader.getPhysicalNumberOfCells();
		for(int colIndex=0;colIndex<columnCount;colIndex++)
		{
			inputHashTable.put(rowHeader.getCell(colIndex).toString(), colIndex);
		}
		return inputHashTable;
	}


	public static Reporter WriteToDetailResults(String expectedValue,String actualValue,String columnName) throws IOException
	{
		Reporter report = new Reporter();
		report.setReport(report);
		report=report.getReport();
		String passCount="";
		String failCount="";
		report.strTestcaseId =MainController.controllerTestCaseID.toString();
		report.strTrasactionType = transactionType.toString();
		report.strTestDescription = MainController.testDesciption;
		if(expectedValue.equalsIgnoreCase(actualValue))
		{		
			report.strActualValue = actualValue;
			report.strExpectedValue = expectedValue;
			report.strStatus = "PASS";
			report.toDate = Automation.dtFormat.format(frmDate);
			passCount = "1";
			failCount="0";
		}
		else
		{
			report.strActualValue = "FAIL|"+actualValue +"|"+expectedValue;
			report.strExpectedValue = expectedValue;
			report.strStatus = "FAIL";
			report.toDate = Automation.dtFormat.format(frmDate);
			failCount = "1";
			passCount = "0";
			//DS:30-05-2014
			fieldVerFailCount += 1;
		}

		if(file.exists() == false)
		{
			print = new PrintStream(file);
		} 

		print = new PrintStream(new FileOutputStream(file, true));
		int	usedRows = count(file);
		if(usedRows == 0)
		{
			print.print("Iteration,TestCaseID,TransactionType,CurrentDate,RowType,Status,PassCount,FailCount");
			print.println();
		}
		usedRows = count(file);

		print.print(ExcelUtility.myChar+Automation.configHashMap.get("CYCLENUMBER").toString()+ExcelUtility.myChar+","+ExcelUtility.myChar+report.strTestcaseId+
				ExcelUtility.myChar+","+ExcelUtility.myChar+
				report.strTrasactionType+ExcelUtility.myChar+","
				+ExcelUtility.myChar+report.toDate+ExcelUtility.myChar+","+
				ExcelUtility.myChar+"Field: "+columnName+ExcelUtility.myChar+","+
				ExcelUtility.myChar+report.strStatus+ExcelUtility.myChar+","+
				ExcelUtility.myChar+passCount+ExcelUtility.myChar+","+
				ExcelUtility.myChar+failCount+ExcelUtility.myChar+","+
				ExcelUtility.myChar+report.strActualValue+ExcelUtility.myChar);
		print.println();
		return report;
	}

	public static int count(File filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n')
						++count;
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}

	/**
	 * Locate a particular Web Element
	 * 
	 * @param controlId
	 * @param controlName
	 * @param controlType
	 * @param imageType
	 * @param controlValue
	 * @return
	 * @throws Exception
	 */
	public static WebElement getElementByType(String controlId, String controlName, String controlType,String imageType,String controlValue) throws Exception
	{

		WebElement controlList = null;
		ControlIdEnum controlID = ControlIdEnum.valueOf(controlId);	
		try
		{
			switch (controlID)
			{

			case Id:
				controlList = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(controlName)));
				break;

			case XPath:				
				controlList = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(controlName)));
				break;
			case XPathValue:
				controlList = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(controlValue)));
				break;	
				
			case Name:
				controlList = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(controlName)));	
				break;
				
			case ClassName:		 	
				controlList =  wait.until(ExpectedConditions.presenceOfElementLocated(By.className(controlName)));	
				break;
				
			case LinkText:
				controlList = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(controlName)));
				break;
				
			case LinkValue:				
				controlList = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(controlValue)));
				break;

			case TagText:
			case TagValue:
			case TagOuterText:
				controlList = wait.until(ExpectedConditions.elementToBeClickable(By.tagName(imageType)));
				break;

			case CSSSelector:
				controlList = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(controlName)));
				break;
			default:
				break;

			}
			return controlList;
		}
		catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}


	/**
	 * Get list of elements
	 * 
	 * @param controlId
	 * @param controlName
	 * @param controlType
	 * @param imageType
	 * @param controlValue
	 * @return
	 * @throws Exception
	 */
	public static List<WebElement> getElementsByType(String controlId, String controlName, String controlType,String imageType,String controlValue) throws Exception
	{

		List<WebElement> controlList = null;
		ControlIdEnum controlID = ControlIdEnum.valueOf(controlId);	
		try
		{
			switch (controlID)
			{

			case Id:
			case HTMLID:
				controlList =wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id(controlName)));
				break;

			case Name:
				controlList =wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.name(controlName)));
				break;

			case XPath:
				controlList =wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(controlName)));
				break;
				
			case ClassName:
				controlList =  wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className(controlName)));
				break;
				
			case TagText:
			case TagValue:
			case TagOuterText:
				controlList =wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName(imageType)));
				break;

			case LinkText:
				controlList =wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.linkText(controlName)));
				break;

			case LinkValue:				
				controlList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.linkText(controlValue)));
				break;

			case CSSSelector:
				controlList = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(controlName)));
				break;			

			default:
				break;
			}
			return controlList;
		}
		catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}
	}


	public static WebElement GetControlByIndex(String indexVal, List<WebElement> lstControl, String controlID, String controlName, String controlType,String controlValue) throws Exception//, ISelenium selenium)
	{
		try
		{
			int indxValue = 0;
			if(!indexVal.equalsIgnoreCase("") && indexVal.length() > 0)
			{
				indxValue = Integer.valueOf(indexVal).intValue();
			}
			if (lstControl.size() > 1 && indexVal.equalsIgnoreCase(""))
			{
				//MainController.pauseFun("More than one control found please provide the index");
				System.out.println("More than one control found please provide the index");
			}
			else if (lstControl.size() == 1)
			{
				indexVal = "0";
			}

			int index = 0;
			for (int buttonIndex = 0; buttonIndex < lstControl.size(); buttonIndex++)
			{
				if (controlID.equalsIgnoreCase("TagValue")&& (lstControl.get(buttonIndex).getAttribute("value")!=null))
				{
					if(lstControl.get(buttonIndex).getAttribute("value").equalsIgnoreCase(controlValue)) 
					{
						return lstControl.get(buttonIndex);						
					}				

				}
				else if(controlID.equalsIgnoreCase("TagText")&&(lstControl.get(buttonIndex).getAttribute("text")!=null))
				{
					if(lstControl.get(buttonIndex).getAttribute("text").equalsIgnoreCase(controlValue)) 
					{
						return lstControl.get(buttonIndex);
					}
				}

				else
				{
					if (index == indxValue)
					{
						return lstControl.get(buttonIndex);
					}
					index += 1;
				}

			}
			return null;
		}
		catch (Exception ex)
		{
			throw new Exception(ex.getMessage());
		}			
	}

	/**
	 * This class performs the reuired action on an element
	 * 
	 * @param imageType
	 * @param controlType
	 * @param controlId
	 * @param controlName
	 * @param ctrlValue
	 * @param logicalName
	 * @param action
	 * @param webElement
	 * @param Results
	 * @param strucSheet
	 * @param valSheet
	 * @param rowIndex
	 * @param rowcount
	 * @param rowNo
	 * @param colNo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("incomplete-switch")
	public static String doAction(String imageType,String controlType,String controlId,String controlName, String ctrlValue,String logicalName,String action,WebElement webElement,Boolean Results,HSSFSheet strucSheet,HSSFSheet valSheet,int rowIndex,int rowcount,String rowNo,String colNo) throws Exception
	{
		List<WebElement> WebElementList = null;
		String currentValue =null;		
		String uniqueNumber = "";
		ControlTypeEnum controlTypeEnum = ControlTypeEnum.valueOf(controlType);
		ControlTypeEnum actionName  = ControlTypeEnum.valueOf(action.toString());
		if(controlType.contains("Robot")&&!isIntialized)
		{
			robot = new Robot();
			isIntialized = true;
		}

		if(action.toString().equalsIgnoreCase("I") && !ctrlValue.equalsIgnoreCase("")||action.toString().equalsIgnoreCase("Read")||action.toString().equalsIgnoreCase("Write") || action.toString().equalsIgnoreCase("V") && !ctrlValue.equalsIgnoreCase("") || action.toString().equalsIgnoreCase("NC") || action.toString().equalsIgnoreCase("T") && !ctrlValue.equalsIgnoreCase(""))			
		{
			try
			{
				switch(controlTypeEnum)
				{
				
				case WebEdit:
					switch(actionName)
					{
					case Read:						
						uniqueNumber = ReadFromExcel(ctrlValue);
						webElement.clear();
						webElement.sendKeys(uniqueNumber);
						break;
					case Write:
						writeToExcel(ctrlValue, webElement, controlId, controlType, controlName, rowNo, colNo);
						break;
					case I:
						if(!ctrlValue.equalsIgnoreCase("null"))
						{
							webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
							webElement.clear();							
							webElement.sendKeys(ctrlValue);
						}
						else
						{
							webElement.clear();
						}
						break;
					case V:
						currentValue = webElement.getText();
						break;
					}
					break;

				case WebButton:				
					switch(actionName)
					{
					case I:								
						if(ctrlValue.equalsIgnoreCase("Y") || ctrlValue.equalsIgnoreCase("Yes"))
						{
							webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
							webElement.click();	
						}
						break;
					case NC:
						webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
						webElement.click();
						break;
					case V:
						if(webElement.isDisplayed())
						{
							if(webElement.isEnabled() == true)
								currentValue = "True";
							else
								currentValue = "False";
						}
					}
					break;

				case WebElement:
					switch(actionName)
					{	
					case I:
						webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
						webElement.click();
						break;

					case Read:
						uniqueNumber = ReadFromExcel(ctrlValue);
						webElement.clear();
						webElement.sendKeys(uniqueNumber);
						break;
					case Write:
						writeToExcel(ctrlValue, webElement, controlId, controlType, controlName, rowNo, colNo);
						break;
					case V:
						boolean textPresent = false;
						textPresent = webElement.getText().contains(ctrlValue);						
						if(textPresent == false)
							currentValue = webElement.getText();
						else
							currentValue = ctrlValue;
						break;
					}
					break;
					
				case JSScript:
					((JavascriptExecutor)Automation.driver).executeScript(controlName, ctrlValue);
					break;

				case Wait:
					Thread.sleep(Integer.parseInt(controlName)*1000);
					break;

				case CheckBox:
					switch(actionName)
					{
					case I:
						if(ctrlValue.equalsIgnoreCase("Y") || ctrlValue.equalsIgnoreCase("Yes"))
						{
							webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
							webElement.click();
						}
						break;
					case NC:
						webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
						webElement.click();
						break;
					}
					break;

				case Radio:
					switch(actionName)
					{
					case I:
						if(ctrlValue.equalsIgnoreCase("Y") || ctrlValue.equalsIgnoreCase("Yes"))
						{
							webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
							if(!webElement.isSelected())
							{						
								webElement.click();
							}
						}
						break;
					case NC:
						webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
						if(!webElement.isSelected())
						{						
							webElement.click();
						}
						break;
					case V:
						if(webElement.isSelected())
						{
							currentValue = webElement.getAttribute(controlName.toString());
						}
						break;
					}
					break;	

				case WebLink:
				case CloseWindow://added this Case to bypass page loading after clicking the event
					switch(actionName)
					{
					case Read:
						uniqueNumber = ReadFromExcel(ctrlValue);
						WebElementList = getElementsByType(controlId, controlName, controlType, imageType, uniqueNumber);
						webElement=	GetControlByIndex("", WebElementList, controlId, controlName, controlType, uniqueNumber);						
						webElement.click();
						break;
					case Write:
						writeToExcel(ctrlValue, webElement, controlId, controlType, controlName, rowNo, colNo);
						break;
					case I:					
						if(controlId.equalsIgnoreCase("LinkValue"))
						{
							webElement.click();
						}							
						else
						{
							if(ctrlValue.equalsIgnoreCase("Y") || ctrlValue.equalsIgnoreCase("Yes"))
							{
								webElement.click();
							}
						}
						break;
					case NC:
						webElement.click();
						break;
					}
					break;	

				case WaitForJS:
					waitForCondition();
					break;

				case ListBox:
				case WebList:
					switch(actionName)
					{					
					case Read:
						uniqueNumber = ReadFromExcel(ctrlValue);
						new Select(webElement).selectByVisibleText(uniqueNumber);
						break;
					case Write:
						writeToExcel(ctrlValue, webElement, controlId, controlType, controlName, rowNo, colNo);
						break;
					case I:
						webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
						ExpectedCondition<Boolean> isTextPresent =	CommonExpectedConditions.textToBePresentInElement(webElement, ctrlValue);
						if(isTextPresent != null)
						{
							if(webElement != null)
							{
								new Select(webElement).selectByVisibleText(ctrlValue);
							}
						}
						break;
					case V:
						currentValue = new Select(webElement).getFirstSelectedOption().getText();
						if(currentValue.isEmpty())
						{
							currentValue = new Select(webElement).getFirstSelectedOption().getAttribute("value");
						}
						break;	
					}
					break;

				case IFrame:
					Automation.driver = Automation.driver.switchTo().frame(controlName);
					break;

				case Browser:	
					//Thread.sleep(3000); //DS:Check if required
					Set<String> handlers = Automation.driver.getWindowHandles();
					handlers = Automation.driver.getWindowHandles();
					for(String handler : handlers)
					{
						Automation.driver =Automation.driver.switchTo().window(handler);								
						if (Automation.driver.getTitle().equalsIgnoreCase(controlName))
						{
							System.out.println("Focus on window with title: "+ Automation.driver.getTitle());					
							break;
						}
					}					
					break;	
					
				case URL:
					switch(actionName)
					{
					case I:					
						Automation.driver.navigate().to(ctrlValue);
						break;
					case NC:
						break;
					}
					break;
					
				case Menu:
					webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
					webElement.click();
					break;

				case Alert:
					switch(actionName)
					{
					case V:
						Alert alert =Automation.driver.switchTo().alert();					
						if(alert!=null)
						{
							currentValue = alert.getText();
							System.out.println(currentValue);
							alert.accept();
						}
						break;
					}
					break;
					
				case WebImage:
					webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));					
					webElement.click();
					Thread.sleep(5000);
					for(int Seconds=0;Seconds<=Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString());Seconds++)
					{
						if(!((Automation.driver.getWindowHandles().size())>1))
						{
							webElement.click();
							Thread.sleep(5000);
						}
						else
						{
							break;
						}
					}	
					break;

				case ActionClick:
					webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
					Actions builderClick = new Actions(Automation.driver);
					Action clickAction = builderClick.moveToElement(webElement).clickAndHold().release().build();
					clickAction.perform();
					break;

				case ActionDoubleClick:
					webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
					Actions builderdoubleClick = new Actions(Automation.driver);
					Action doubleClickAction = builderdoubleClick.moveToElement(webElement).click().build();
					doubleClickAction.perform();
					doubleClickAction.perform();						
					break;

				case ActionClickandEsc:
					webElement =wait.until(ExpectedConditions.elementToBeClickable(webElement));
					Actions clickandEsc = new Actions(Automation.driver);
					Action clickEscAction = clickandEsc.moveToElement(webElement).click().sendKeys(Keys.ENTER,Keys.ESCAPE).build();
					clickEscAction.perform();
					break;					

				case ActionMouseOver:
					Actions builderMouserOver = new Actions(Automation.driver);
					Action mouseOverAction = builderMouserOver.moveToElement(webElement).build();
					mouseOverAction.perform();
					break;

				case CalendarNew:
					Boolean isCalendarDisplayed = Automation.driver.switchTo().activeElement().isDisplayed();
					System.out.println(isCalendarDisplayed);
					if(isCalendarDisplayed == true)
					{

						String[] dtMthYr = ctrlValue.split("/");
						Thread.sleep(2000);
						//String[] CurrentDate = dtFormat.format(frmDate).split("/");
						WebElement Monthyear =  Automation.driver.findElement(By.xpath("//table/thead/tr/td[2]"));
						String Monthyear1 = Monthyear.getText();
						String[] Monthyear2 = Monthyear1.split(",");
						Monthyear2[1] = Monthyear2[1].trim();

						month = CalendarSnippet.getMonthForString(Monthyear2[0]);

						while(!Monthyear2[1].equalsIgnoreCase(dtMthYr[2]))
						{
							if(Integer.parseInt(Monthyear2[1])>Integer.parseInt(dtMthYr[2]))
							{
								WebElement yearButton =  Automation.driver.findElement(By.cssSelector("td:contains('«')"));
								yearButton.click();
								Monthyear2[1] = Integer.toString(Integer.parseInt(Monthyear2[1])-1);
							}
							else if(Integer.parseInt(Monthyear2[1])<Integer.parseInt(dtMthYr[2]))
							{
								WebElement yearButton =  Automation.driver.findElement(By.cssSelector("td:contains('»')"));
								yearButton.click();
								Monthyear2[1] = Integer.toString(Integer.parseInt(Monthyear2[1])+1);
							}
						}

						while(!month.equalsIgnoreCase(dtMthYr[1]))
						{
							if(Integer.parseInt(month)>Integer.parseInt(dtMthYr[1]))
							{
								WebElement monthButton =  Automation.driver.findElement(By.cssSelector("td:contains('‹')"));
								monthButton.click();
								if (Integer.parseInt(month)< 11){
									month = "0"+Integer.toString(Integer.parseInt(month)-1);
								}
								else {
									month = Integer.toString(Integer.parseInt(month)-1);
								}

							}
							else if(Integer.parseInt(month)<Integer.parseInt(dtMthYr[1]))
							{
								WebElement monthButton =  Automation.driver.findElement(By.cssSelector("td:contains('›')"));
								monthButton.click();
								if (Integer.parseInt(month)< 9){
									month = "0"+Integer.toString(Integer.parseInt(month)+1);
								}
								else {
									month = Integer.toString(Integer.parseInt(month)+1);
								}
							}
						}

						WebElement dateButton =  Automation.driver.findElement(By.cssSelector("td.day:contains('"+dtMthYr[0]+"')"));
						System.out.println(dateButton);
						dateButton.click();

					}
					else
					{
						System.out.println("Calendar not Diplayed");
					}
					break;

				case CalendarIPF:
					String[] dtMthYr = ctrlValue.split("/");
					Thread.sleep(2000);
					String year = dtMthYr[2];
					String monthNum = dtMthYr[1];
					String day = dtMthYr[0];

					//Xpath for Year, mMnth & Days
					String xpathYear = "//div[@class='datepicker datepicker-dropdown dropdown-menu datepicker-orient-left datepicker-orient-bottom']/div[@class='datepicker-years']";
					String xpathMonth = "//div[@class='datepicker datepicker-dropdown dropdown-menu datepicker-orient-left datepicker-orient-bottom']/div[@class='datepicker-months']";
					String xpathDay = "//div[@class='datepicker datepicker-dropdown dropdown-menu datepicker-orient-left datepicker-orient-bottom']/div[@class='datepicker-days']";

					//Selecting year in 3 steps
					Automation.driver.findElement(By.xpath(xpathDay + "/table/thead/tr[1]/th[2]")).click();
					Automation.driver.findElement(By.xpath(xpathMonth + "/table/thead/tr/th[2]")).click();
					Automation.driver.findElement(By.xpath(xpathYear + "/table/tbody/tr/td/span[@class='year'][contains(text(),"+ year +")]")).click();

					//Selecting month in 1 step	
					Automation.driver.findElement(By.xpath(xpathMonth + "/table/tbody/tr/td/span["+ monthNum +"]")).click();	

					//Selecting day in 1 step
					Automation.driver.findElement(By.xpath(xpathDay + "/table/tbody/tr/td[@class='day'][contains(text(),"+ day +")]")).click();						

				case CalendarEBP:
					String[] dtMthYrEBP = ctrlValue.split("/");	
					Thread.sleep(2000);
					String yearEBP = dtMthYrEBP[2];					
					String monthNumEBP=	 CalendarSnippet.getMonthForInt(Integer.parseInt(dtMthYrEBP[1])).substring(0, 3);
					String dayEBP = dtMthYrEBP[0];
					
					//common path used for most of the elements
					String pathToVisibleCalendar = "//div[@class='ajax__calendar'][contains(@style, 'visibility: visible;')]/div";
					
					//following is to click the title once to reach the year page
					wait.until(ExpectedConditions.elementToBeClickable(By.xpath(pathToVisibleCalendar+"/div[@class='ajax__calendar_header']/div[3]/div"))).click();
					//check if 'Dec' is visibly clickable after refreshing
					wait.until(ExpectedConditions.elementToBeClickable(By.xpath(pathToVisibleCalendar+"/div/div/table/tbody/tr/td/div[contains(text(), 'Dec')]")));
					//following is to click the title once again to reach the year page
					Automation.driver.findElement(By.xpath(pathToVisibleCalendar+"/div[@class='ajax__calendar_header']/div[3]/div")).click();
					
					//common path used for most of the elements while selection of year, month and date
					pathToVisibleCalendar="//div[@class='ajax__calendar'][contains(@style, 'visibility: visible;')]/div/div/div/table/tbody/tr/td";
					
					//each of the following line selects the year, month and date
					wait.until(ExpectedConditions.elementToBeClickable(By.xpath(pathToVisibleCalendar+"/div[contains(text(),"+ yearEBP +")]"))).click();
					wait.until(ExpectedConditions.elementToBeClickable(By.xpath(pathToVisibleCalendar+"/div[@class='ajax__calendar_month'][contains(text(),'"+ monthNumEBP +"')]"))).click();
					wait.until(ExpectedConditions.elementToBeClickable(By.xpath(pathToVisibleCalendar+"/div[@class='ajax__calendar_day'][contains(text(),"+ dayEBP +")]"))).click();
					
					break;					

				/**Code for window popups**/	
				case Window:
					switch(actionName)
					{							
					case O:						
						String parentHandle = Automation.driver.getWindowHandle();	
						for (String winHandle : Automation.driver.getWindowHandles())
						{
							Automation.driver.switchTo().window(winHandle);
							if (Automation.driver.getTitle().equalsIgnoreCase(controlName))
							{
								Automation.driver.close();
							}
						}
						Automation.driver.switchTo().window(parentHandle);						
						break;
					}
					break;		

				case WebTable:
					switch(actionName)
					{
					case Read:
						ReadFromExcel(ctrlValue);
						break;
					case Write:
						writeToExcel(ctrlValue, webElement, controlId, controlType,controlName,rowNo,colNo);						
						break;
					case NC:
						WebElement table = webElement;
						List<WebElement> tableRows =  table.findElements(By.tagName("tr"));
						int tableRowIndex = 0;
						//int tableColumnIndex = 0;
						boolean matchFound = false;
						for(WebElement tableRow : tableRows)
						{
							tableRowIndex += 1;
							List<WebElement> tableColumns = tableRow.findElements(By.tagName("td"));
							if(tableColumns.size()>0)
							{		
								for(WebElement tableColumn :tableColumns)
									if(tableColumn.getText().equals(ctrlValue))
									{
										matchFound = true;
										System.out.println(tableRowIndex);					
										List<Object> elementProperties = getPropertiesOfWebElement(tableColumns.get(Integer.parseInt(colNo)),imageType);
										controlName = elementProperties.get(0).toString();
										if(controlName.equals(""))
										{
											controlName = elementProperties.get(1).toString();
										}
										controlType = elementProperties.get(2).toString();
										webElement = (WebElement) elementProperties.get(3);
										doAction(imageType, controlType, controlId, controlName, ctrlValue, logicalName, action, webElement, Results, strucSheet, valSheet, tableRowIndex, rowcount, rowNo, colNo);
										break;
									}									
								if(matchFound)
								{
									break;
								}
							}

						}							
						break;
					case V:
						WriteToDetailResults(ctrlValue, "",logicalName);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						break;
					}
					break;

				case Robot:
					if(controlName.equalsIgnoreCase("SetFilePath"))
					{
						StringSelection stringSelection = new StringSelection(ctrlValue);					
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);	
						robot.delay(1000);
						robot.keyPress(KeyEvent.VK_CONTROL);
						robot.keyPress(KeyEvent.VK_V);
						robot.keyRelease(KeyEvent.VK_V);
						robot.keyRelease(KeyEvent.VK_CONTROL);

					}
					else if(controlName.equalsIgnoreCase("TAB"))
					{
						robot.keyPress(KeyEvent.VK_TAB);
						robot.keyRelease(KeyEvent.VK_TAB);	
					}
					else if(controlName.equalsIgnoreCase("SPACE"))
					{
						robot.keyPress(KeyEvent.VK_SPACE);	  				
						robot.keyRelease(KeyEvent.VK_SPACE);
					}
					else if(controlName.equalsIgnoreCase("ENTER"))
					{
						robot.keyPress(KeyEvent.VK_ENTER);
						robot.keyRelease(KeyEvent.VK_ENTER);
					}
					break;
					
				case WaitForEC:
					wait.until(CommonExpectedConditions.elementToBeClickable(webElement));
					break;

				case SikuliType:
					sikuliScreen.type(controlName,ctrlValue);
					break;

				case SikuliButton:
					sikuliScreen.click(controlName);
					System.out.println("Done");
					break;

				case Date:						
					Calendar cal = new GregorianCalendar();
					int i = cal.get(Calendar.DAY_OF_MONTH);
					if(i>=31)
					{
						i=i-10;
					}
					break;

				case FileUpload:
					webElement.sendKeys(ctrlValue);
					break;		
					
				case ScrollTo:						
					Locatable element = (Locatable) webElement;
					Point p= element.getCoordinates().onScreen();
					JavascriptExecutor js = (JavascriptExecutor) Automation.driver;  
					js.executeScript("window.scrollTo(" + p.getX() + "," + (p.getY()+150) + ");");						
					break;	
					
				default:
					System.out.println("U r in Default");
					break;
				}
			}
			catch(WebDriverException we)
			{
				throw new Exception("Error Occurred from Do Action "+controlName + we.getMessage());
			}
			catch(Exception e)
			{
				throw new Exception(e.getMessage());
			}
		}

		if(action.toString().equalsIgnoreCase("V") && !ctrlValue.equalsIgnoreCase(""))
		{
			if(Results == true)
			{
				SwiftSeleniumWeb.WebDriver.report = WriteToDetailResults(ctrlValue, currentValue, logicalName);
			}
		}

		return currentValue;

	}
	public static String getMonth() {
		return month;
	}

	/** Checks if an Alert is Present returns Boolean Value  **/
	public static Boolean isAlertPresent(String controlType)
	{
		try {			
			if (!controlType.equalsIgnoreCase("Alert"))
			{
				Alert alert = Automation.driver.switchTo().alert();	
				if(alert != null)
				{	
					SwiftSeleniumWeb.WebDriver.report.strMessage=alert.getText();					
					alert.accept();							
					SwiftSeleniumWeb.WebDriver.report.strStatus = "FAIL";
					MainController.pauseFun(SwiftSeleniumWeb.WebDriver.report.strMessage);
					return true;
				}
			}
		} catch (Exception e) {				
			return false;
		}
		return false;		
	}

	/** This Functions Waits for the HTMLPage To Load **/
	public static Boolean waitForCondition() throws IOException
	{
		ExpectedCondition<Boolean> expCondition=null;	

		try
		{
			//wait = new WebDriverWait(Automation.driver,Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));//Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString())
			expCondition = new ExpectedCondition<Boolean>() {		

				@Override
				public Boolean apply(WebDriver driver) {	
					return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");				
				}
			};
		}
		catch(WebDriverException e)
		{
			MainController.pauseFun("From PageLoaded Function" + e.getMessage());
		}
		catch(Exception e)
		{
			MainController.pauseFun("Timed Out after waiting");
		}

		return wait.until(expCondition);
	}

	/** This Functions Waits for the Ajax Controls To Load on the page **/
	public static Boolean waitFroAjax() throws InterruptedException, IOException
	{
		Boolean ajaxIsComplete = false;
		try
		{

			while (true) // Handle timeout somewhere
			{

				ajaxIsComplete = (Boolean)((JavascriptExecutor)Automation.driver).executeScript("return window.jQuery != undefined && jQuery.active == 0");
				if (ajaxIsComplete)
				{
					break;
				}
				Thread.sleep(100);
			}
		}
		catch (Exception e) {
			MainController.pauseFun("Ajax controls Loading "+e.getMessage());
		}
		return ajaxIsComplete;
	}

	public static String ReadFromExcel(String controlValue) throws IOException
	{
		HSSFSheet uniqueNumberSheet =null;
		String uniqueTestcaseID = "";
		HashMap<String ,Object> uniqueValuesHashMap = null;
		//HSSFRow uniqueRow = null;
		String uniqueNumber = null;
		try
		{			
			uniqueNumberSheet  = ExcelUtility.GetSheet(Automation.configHashMap.get("TRANSACTION_INFO").toString(), "DataSheet");			
			uniqueValuesHashMap =  getValueFromHashMap(uniqueNumberSheet);
			int rowCount = uniqueNumberSheet.getPhysicalNumberOfRows();

			for (int rIndex = 1; rIndex < rowCount; rIndex++)
			{
				//uniqueRow = uniqueNumberSheet.getRow(rIndex);
				if(controlValue.equals(""))
				{
					uniqueTestcaseID = getCellData("TestCaseID", uniqueNumberSheet, rIndex, uniqueValuesHashMap);
				}
				else
				{
					uniqueTestcaseID = controlValue;
				}
				//String uniqueTransactionType = getCellData("TransactionType", uniqueNumberSheet, rIndex, uniqueValuesHashMap);
				if(MainController.controllerTestCaseID.toString().equals(uniqueTestcaseID))//&& MainController.controllerTransactionType.toString().equals(uniqueTransactionType)
				{
					return uniqueNumber = getCellData(columnName,uniqueNumberSheet,rIndex,uniqueValuesHashMap);

				}						
			}			

		}
		catch(Exception e)
		{
			MainController.pauseFun(e.getMessage()+" from ReadFromExcel Function");
		}
		return uniqueNumber;
	}
	public static Boolean writeToExcel(String ctrlValue,WebElement webElement,String controlId,String controlType,String controlName,String rowNo,String colNo) throws Exception
	{
		try
		{
			FileInputStream in = new FileInputStream(Automation.configHashMap.get("TRANSACTION_INFO").toString());
			HSSFWorkbook uniqueWB = new HSSFWorkbook(in);
			HSSFSheet uniqueNumberSheet = uniqueWB.getSheet("DataSheet");				
			HashMap<String,Object> uniqueValuesHashMap =  getValueFromHashMap(uniqueNumberSheet);
			HSSFRow uniqueRow = null;				
			int rowNum = uniqueNumberSheet.getPhysicalNumberOfRows();	

			for (int rIndex = 0; rIndex < rowNum; rIndex++)
			{
				uniqueRow = uniqueNumberSheet.getRow(rIndex);					
				String uniqueTestcaseID = getCellData("TestCaseID", uniqueNumberSheet, rIndex, uniqueValuesHashMap);					

				if(MainController.controllerTestCaseID.toString().equals(uniqueTestcaseID))//&& MainController.controllerTransactionType.toString().equals(uniqueTransactionType)
				{
					uniqueRow = uniqueNumberSheet.getRow(rIndex);
					break;
				}
				else if(rIndex == rowNum-1)
				{
					uniqueRow= uniqueNumberSheet.createRow(rowNum);
				}
			}	

			if(controlType.equalsIgnoreCase("WebTable"))
			{
				//TM:commented and updated the following 'if' statement
				//if(Integer.valueOf(rowNo).equals(null)||Integer.valueOf(colNo).equals(null))
				if(StringUtils.isBlank(rowNo) || StringUtils.isBlank(colNo))
				{
					MainController.pauseFun("RowNumber or ColumnNumber is Missing");
					return false;
				}
				else
				{
					//ctrlValue = Automation.selenium.getTable(controlName+"."+ Integer.parseInt(rowNo) +"." + Integer.parseInt(colNo));
					ctrlValue = Automation.driver.findElement(By.xpath(controlName +"/tr["+rowNo+"]/td["+colNo+"]")).getText();
				}
			}
			else if(controlType.equalsIgnoreCase("ListBox")||controlType.equalsIgnoreCase("WebList"))
			{
				ctrlValue = new Select(webElement).getFirstSelectedOption().toString();
			}

			else if(controlType.equalsIgnoreCase("DB"))
			{
				System.out.println(ctrlValue);
			}
			else
			{
				ctrlValue = webElement.getText();
			}

			HSSFCell uniqueTestCaseID = uniqueRow.createCell(Integer.parseInt(uniqueValuesHashMap.get("TestCaseID").toString()));
			HSSFCell uniqueCell = uniqueRow.createCell(Integer.parseInt(uniqueValuesHashMap.get(columnName).toString()));
			uniqueTestCaseID.setCellValue(MainController.controllerTestCaseID.toString());				
			uniqueCell.setCellValue(ctrlValue);						
			in.close();
			FileOutputStream out =new FileOutputStream(Automation.configHashMap.get("TRANSACTION_INFO").toString());
			uniqueWB.write(out);						
		}
		catch(FileNotFoundException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw e;
		}
		return true;
	}

	public static List<Object> getPropertiesOfWebElement(WebElement webElement,String imageType)
	{
		List<WebElement> elements = webElement.findElements(By.tagName(imageType));
		WebElement element = elements.get(0);
		List<Object> elementProperties = new ArrayList<Object>();
		String elementType = element.getAttribute("type");
		String elementTagName	= element.getTagName();
		//String elementClassName = element.getClass().toString();
		String controlType = "";
		String id ="";
		String name = "";
		if(elementType.equals("text")&& elementTagName.equals("input"))
		{
			id = element.getAttribute("id");
			name = element.getAttribute("name");
			controlType = "WebEdit";
		}
		else if(elementType.contains("checkbox") && elementTagName.equals("input"))
		{
			id = element.getAttribute("id");
			name = element.getAttribute("name");
			controlType = "CheckBox";
		}
		else if (elementType.contains("listbox")&& elementTagName.equals("select"))
		{
			id = element.getAttribute("id");
			name = element.getAttribute("name");
			controlType = "WebList";
		}
		else if (elementType.contains("radio")&& elementTagName.equals("input"))
		{
			id = element.getAttribute("id");
			name = element.getAttribute("name");
			controlType = "Radio";
		}
		else if (elementType.contains("")&& elementTagName.equals("a"))
		{
			id = element.getAttribute("id");
			name = element.getAttribute("name");
			controlType = "WebLink";
		}
		elementProperties.add(id);
		elementProperties.add(name);
		elementProperties.add(controlType);
		elementProperties.add((Object)element);
		return elementProperties;
	}

	public static void saveScreenShot(){
		if (!(Automation.driver instanceof TakesScreenshot)) {

			System.out.println("Not able to take screenshot: Current WebDriver does not support TakesScreenshot interface.");
			return;
		}

		File scrFile;
		try {

			scrFile = ((TakesScreenshot)Automation.driver).getScreenshotAs(OutputType.FILE);
		} 
		catch (Exception e){
			System.out.println("Taking screenshot failed for: " +SwiftSeleniumWeb.WebDriver.report.strTestcaseId);
			// e.printStackTrace();
			return;
		}
		String date = null;
		
		
		if(StringUtils.isNotBlank(SwiftSeleniumWeb.WebDriver.report.frmDate))
            date = SwiftSeleniumWeb.WebDriver.report.frmDate.replaceAll("[-/: ]","");
		else
            SwiftSeleniumWeb.WebDriver.report.setFromDate(Automation.dtFormat.format(new Date()));		
		
		String fileName = SwiftSeleniumWeb.WebDriver.report.strTestcaseId + "_" + SwiftSeleniumWeb.WebDriver.report.strTrasactionType+ "_"+date;
		String location = System.getProperty("user.dir") +"\\Resources\\Results\\ScreenShots\\"+ fileName+".jpeg";
		SwiftSeleniumWeb.WebDriver.report.strScreenshot = "file:\\\\"+location;

		try {

			FileUtils.copyFile(scrFile, new File(location));

		} 
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

}