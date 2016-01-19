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
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.sikuli.script.Screen;

import swift.selenium.Automation.browserTypeEnum;

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
	public static SmartRobot smartRobot;
	public static Boolean isIntialized=false;
	public static int fieldVerFailCount = 0;
	public static String wsdl_url, request_url, request_xml, response_fileName, response_url;

	public static enum ControlTypeEnum
	{I,NC,V,VA,O,T,F,WebEdit,AjaxWebList,WebElement,Date,SikuliType,SikuliButton,ListBox,Radio,WebButton,WebLink,CheckBox,WebTable,Browser,Menu,Wait,WebImage,URL,BrowserAuth,BrowserType,WebList,Alert,Window,Robot,January, February, March, April, May, June, July, August, September, October, November, December,
	ActionClick,ActionDoubleClick,ActionClickandEsc,ActionMouseOver,Read,Write,WaitForJS,CloseWindow,DB,FileUpload,JSScript,Calendar,CalendarNew,CalendarIPF,CalendarEBP,WaitForEC,IFrame,ScrollTo,WebService,JSONResp};
	
	public static enum ControlIdEnum {Id,HTMLID,Name,XPath,ClassName,TagName,LinkText,TagText,TagValue,TagOuterText,LinkValue,CSSSelector,Id_p,HTMLID_p,XPath_p,AjaxPath};

	public static String control;
	public static Date toDate=null;
	public static String testCase = null;
	public static HSSFDataFormat format = null;
	public static Wait<WebDriver> wait;
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
			//String testCase = null;
			String ctrlValue= null;
			//HSSFRow structureRow=null;
			InputStream myXls = new FileInputStream(FilePath);
			HSSFWorkbook workBook = new HSSFWorkbook(myXls);
			format  = workBook.createDataFormat();
			HSSFSheet sheetStructure = workBook.getSheet("Structure");
			//HSSFCell controlValue=null;
			int rowCount = sheetStructure.getLastRowNum()+1;
			HSSFSheet headerValues = ExcelUtility.GetSheet(FilePath, "Values");
			//HSSFRow headerRow = headerValues.getRow(0);
			System.out.println(Automation.dtFormat.format(frmDate));
			String fromDate = Automation.dtFormat.format(frmDate);
			TransactionMapping.report.setFromDate(fromDate);
			structureHeader = getValueFromHashMap(sheetStructure);
			columnName = null;
			int dynamicIndexNumber;//Added for Action Loop
			String imageType,indexVal,controlName,executeFlag,action,logicalName,controltype,controlID,dynamicIndex,newDynamicIndex,rowNo,colNo;//newly Added for Action Loop

			//Setting of default reporting values before executing a transaction
			TransactionMapping.report.setStrMessage("");
			TransactionMapping.report.setStrStatus("PASS");	

			for(int rowIndex=1; rowIndex<rowCount; rowIndex++)
			{					
				//structureRow = sheetStructure.getRow(rowIndex);
				controlName = getCellData("ControlName",sheetStructure,rowIndex,structureHeader);//structureRow.getCell(3);
				executeFlag = getCellData("ExecuteFlag",sheetStructure,rowIndex,structureHeader);//structureRow.getCell(0);

				if(executeFlag.toString().equals("Y"))
				{
					WebElement webElement = null;
					imageType = getCellData("ImageType", sheetStructure, rowIndex, structureHeader);
					action = getCellData("Action",sheetStructure,rowIndex,structureHeader);//structureRow.getCell(1);
					logicalName = getCellData("LogicalName",sheetStructure,rowIndex,structureHeader);//structureRow.getCell(2);
					controltype = getCellData("ControlType",sheetStructure,rowIndex,structureHeader);//structureRow.getCell(4);
					controlID = getCellData("ControlID",sheetStructure,rowIndex,structureHeader);//structureRow.getCell(6);
					indexVal = getCellData("Index",sheetStructure,rowIndex,structureHeader);//structureRow.getCell(7);
					columnName = getCellData("ColumnName", sheetStructure, rowIndex, structureHeader);
					rowNo = getCellData("RowNo", sheetStructure, rowIndex, structureHeader);
					colNo = getCellData("ColumnNo", sheetStructure, rowIndex, structureHeader);					
					dynamicIndex = getCellData("DynamicIndex",sheetStructure,rowIndex,structureHeader);//Added code for Loop

					if (action.equalsIgnoreCase("LOOP"))
					{
						loopRow = rowIndex+1;
					}

					// if rownum != 1 , then do below steps 
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

					if(!action.equalsIgnoreCase("LOOP")&&!action.equalsIgnoreCase("END_LOOP"))
					{
						//boolean isControlValueFound =false;
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
							//int colIndex = Integer.parseInt(actualValue.toString());
							//controlValue = rowValues.getCell(colIndex);
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
										!controltype.equalsIgnoreCase("URL")&&!controltype.equalsIgnoreCase("WaitForJS")&&!controltype.contains("Robot") &&!controltype.equalsIgnoreCase("BrowserType") &&!controltype.equalsIgnoreCase("BrowserAuth") 
										&& !controltype.equalsIgnoreCase("Calendar")&&!controltype.equalsIgnoreCase("CalendarNew")&&!controltype.equalsIgnoreCase("CalendarIPF")&&!controltype.equalsIgnoreCase("CalendarEBP")&&										
										(!action.equalsIgnoreCase("Read")||((action.equalsIgnoreCase("Read")&& !controlName.isEmpty())))&&
										!controltype.equalsIgnoreCase("JSScript")&&!controltype.equalsIgnoreCase("DB")&& !controlID.equalsIgnoreCase("XML")&& !controltype.startsWith("Process")
										&& !controltype.startsWith("Destroy")&& !controltype.startsWith("ReadSikuli") &&!controltype.equalsIgnoreCase("WebService") &&!controltype.equalsIgnoreCase("JSONResp") && !action.equalsIgnoreCase("VA"))
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
			TransactionMapping.report.setFromDate(Automation.dtFormat.format(frmDate));
			TransactionMapping.report.setStrIteration(Automation.configHashMap.get("CYCLENUMBER").toString());
			TransactionMapping.report.setStrTestcaseId(TransactionMapping.testCaseID.toString());
			//TransactionMapping.report.setStrGroupName(MainController.controllerGroupName.toString());
			TransactionMapping.report.setStrTrasactionType(TransactionMapping.transactionType.toString());
			//TransactionMapping.report.setStrTestDescription(MainController.testDesciption);
			TransactionMapping.report.setToDate(Automation.dtFormat.format(toDate));

			//Setting status for field verification failures
			if (fieldVerFailCount > 0)
			{
				TransactionMapping.report.setStrMessage("Check Detailed Results");
				TransactionMapping.report.setStrStatus("FAIL");	
			}
		}
		catch(Exception e)
		{
			TransactionMapping.pauseFun(e.getMessage());			
		}
		finally
		{
			structureHeader.clear();
			valuesHeader.clear();
			ExcelUtility.writeReport(TransactionMapping.report);
			fieldVerFailCount = 0;			
		}
	}
	
	/**
	 * This method reads and returns data from each cell of a provided worksheet
	 * 
	 * @param reqValue
	 * @param reqSheet
	 * @param rowIndex
	 * @param inputHashTable
	 * @return
	 * @throws IOException
	 */
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
			TransactionMapping.report.setStrMessage("Column "+ reqValue +" not Found. Please Check input Sheet");
			TransactionMapping.pauseFun("Column "+ reqValue +" not Found. Please Check input Sheet");				
		}
		else
		{
			actualvalue = inputHashTable.get(reqValue);//rowHeader.getCell(colIndex).toString();			
			if(actualvalue != null)
			{
				int colIndex = Integer.parseInt(actualvalue.toString());
				reqCell = rowActual.getCell(colIndex);
				//TM 27-04-2015: Updated the code for formula in cells
				if(reqCell == null)
				{				
					System.out.println(reqValue +" is Null");
				}
				else
				{
					HSSFWorkbook wb = reqCell.getSheet().getWorkbook() ; //TM-05/05/2015: Get workbook instance from the worksheet
					HSSFFormulaEvaluator.evaluateAllFormulaCells(wb); //TM-05/05/2015: To refresh all the formulas in the worksheet
                    FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                    
                    CellValue cellValue = evaluator.evaluate(reqCell);
                    int type=0;
                    if(cellValue!=null)
                    {
                        type= cellValue.getCellType() ;
                    }
                    else
                    {
                       type = reqCell.getCellType();
                    }

					switch(type)
					{
					case HSSFCell.CELL_TYPE_BLANK:
						req = "";
						break;
					case HSSFCell.CELL_TYPE_NUMERIC:
						if (DateUtil.isCellDateFormatted(reqCell))	
						{
							 SimpleDateFormat form = new SimpleDateFormat(Automation.configHashMap.get("DATEFORMAT").toString());
							 req = form.format(reqCell.getDateCellValue());
						}							
						else
							req = fmt.formatCellValue(reqCell, evaluator);				
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

/**
 * This method is called only incase of FIELD VERIFICATION
 * @param expectedValue
 * @param actualValue
 * @param columnName
 * @return
 * @throws IOException
 */
	public static Reporter WriteToDetailResults(String expectedValue,String actualValue,String columnName) throws IOException
	{
		Reporter report = new Reporter();
		report.setReport(report);
		report=report.getReport();
		String passCount="";
		String failCount="";
		report.strTestcaseId =TransactionMapping.testCaseID.toString();
		report.strGroupName = TransactionMapping.report.strGroupName;
		report.strTestDescription = TransactionMapping.report.strTestDescription;
		report.strTrasactionType = transactionType.toString();
		//report.strTestDescription = MainController.testDesciption;
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
	 * This method locates and returns a web element on the basis of the type of locator provided
	 * 
	 * @param controlId - type of locator as mentioned in Input sheet
	 * @param controlName - Id, Xpath etc. of the web element depending upon the locator
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
			case HTMLID:
				controlList = wait.until(ExpectedConditions.elementToBeClickable(By.id(controlName)));
				break;

			case XPath:
				controlList = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(controlName)));
				break;
				
			case Name:
				controlList = wait.until(ExpectedConditions.elementToBeClickable(By.name(controlName)));	
				break;
				
			case ClassName:		 	
				controlList =  wait.until(ExpectedConditions.elementToBeClickable(By.className(controlName)));	
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
			
			//GAIC AJAX controls - TM:02/02/2015	
			case AjaxPath:
				controlList = Automation.driver.findElement(By.xpath(controlName+"[contains(text(),'"+controlValue+"')]"));
				break;
				
			case Id_p:
			case HTMLID_p:
				controlList = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(controlName)));
				break;

			case XPath_p:
				controlList = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(controlName)));
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
				if (controlID.equalsIgnoreCase("TagValue")&& (lstControl.get(buttonIndex).getAttribute("value")!=null))/*(controlID.equalsIgnoreCase("Id") || controlID.equalsIgnoreCase("Name")||controlID.equalsIgnoreCase("XPath")
						|| controlID.equalsIgnoreCase("LinkText") || controlID.equalsIgnoreCase("ClassName") ||
						controlID.equalsIgnoreCase("TagName"))*/
					//					(lstControl[buttonIndex].GetAttribute(GlobalVariable.CONTROLVALUE).Equals(controlName)))||
				{
					if(lstControl.get(buttonIndex).getAttribute("value").equalsIgnoreCase(controlValue))//changed by IK on 23-01-2014 for HSCIC 
					{
						return lstControl.get(buttonIndex);						
					}				

				}
				else if(controlID.equalsIgnoreCase("TagText")&&(lstControl.get(buttonIndex).getAttribute("text")!=null))
				{
					if(lstControl.get(buttonIndex).getAttribute("text").equalsIgnoreCase(controlValue))//changed by IK on 23-01-2014 for HSCIC 
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

	@SuppressWarnings("incomplete-switch")
	public static String doAction(String imageType,String controlType,String controlId,String controlName, String ctrlValue,String logicalName,String action,WebElement webElement,Boolean Results,HSSFSheet strucSheet,HSSFSheet valSheet,int rowIndex,int rowcount,String rowNo,String colNo) throws Exception
	{
		List<WebElement> WebElementList = null;
		String currentValue =null;
		//HSSFSheet uniqueNumberSheet =null;
		String uniqueNumber = "";
		//HashMap<String ,Object> uniqueValuesHashMap = null;
		//HSSFRow uniqueRow = null;
		ControlTypeEnum controlTypeEnum = ControlTypeEnum.valueOf(controlType);
		ControlTypeEnum actionName  = ControlTypeEnum.valueOf(action.toString());
		if(controlType.contains("Robot")&&!isIntialized)
		{
			robot = new Robot();
			isIntialized = true;
		}

		if(action.toString().equalsIgnoreCase("I") && !ctrlValue.equalsIgnoreCase("")||action.toString().equalsIgnoreCase("Read")||action.toString().equalsIgnoreCase("Write") || action.toString().equalsIgnoreCase("V") && !ctrlValue.equalsIgnoreCase("") || action.toString().equalsIgnoreCase("NC") || action.toString().equalsIgnoreCase("T") && !ctrlValue.equalsIgnoreCase("")|| action.toString().equalsIgnoreCase("F") && !ctrlValue.equalsIgnoreCase("") || action.toString().equalsIgnoreCase("VA") && !ctrlValue.equalsIgnoreCase("") || action.toString().equalsIgnoreCase("O") )			
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
							webElement.click();	
						}
						break;
					case NC:
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
							currentValue = Boolean.toString(textPresent);
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
							webElement.click();
						}
						break;
					case NC:
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
							if(!webElement.isSelected())
							{						
								webElement.click();
							}
						}
						break;
					case NC:					
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
					case F:
						if(webElement != null)
						{
							currentValue = "Y";
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
                        if(!ctrlValue.contains(",")){     
                        	currentValue = new Select(webElement).getFirstSelectedOption().getText();
                        	if(currentValue.isEmpty())
                        	{
                        		currentValue = new Select(webElement).getFirstSelectedOption().getAttribute("value");
                        	}

                        	break;
                        }
                        else{
                        	currentValue=new String();
                        	List<WebElement> currentValues= new ArrayList<WebElement>();
                        	currentValues = new Select(webElement).getOptions();

                        	for(int j =0;j<currentValues.size();j++){
                        		if(j+1 == currentValues.size())
                        			currentValue= currentValue.concat(currentValues.get(j).getText());
                        		else {
                        			currentValue= currentValue.concat(currentValues.get(j).getText()+",");
                        			}
                        	}
                        	break;
                        }
					}
					break;
					
				//New code for AJAX Dropdown with dojo
				case AjaxWebList:
					switch(actionName)
					{	
					case I:
						webElement.click();
						break;
					case VA:
						Thread.sleep(20000);
						currentValue=new String();
                    	List<WebElement> currentValues= new ArrayList<WebElement>();
                    	currentValues = Automation.driver.findElements(By.xpath(controlName));

                    	for(int j =0;j<currentValues.size();j++){
                    		if(j+1 == currentValues.size())
                    			currentValue= currentValue.concat(currentValues.get(j).getText());
                    		else {
                    			currentValue= currentValue.concat(currentValues.get(j).getText()+",");
                    			}
                    	}                    	
                    	break;
					}
					break;
				case IFrame:
					Automation.driver = Automation.driver.switchTo().frame(controlName);
					break;

				case Browser:	
					Set<String> handlers = null;
					handlers = Automation.driver.getWindowHandles();
					for(String handler : handlers)
					{
						Automation.driver =Automation.driver.switchTo().window(handler);
						if (Automation.driver.getTitle().contains(controlName))
						{
							System.out.println("Focus on window with title: "+ Automation.driver.getTitle());					
							break;
						}
					}					
					break;	
				
				case BrowserType:
					switch(actionName)
					{	
					case I:
						if (StringUtils.isNotBlank(ctrlValue)){
							Automation.browser = Automation.configHashMap.get("BROWSERTYPE").toString();
							if (StringUtils.equalsIgnoreCase("none", Automation.browser)){
								Automation.browser	= ctrlValue.toString();
								Automation.browserType = Automation.browserTypeEnum.valueOf(Automation.browser);
								Automation.setUp();		
								wait = new WebDriverWait(Automation.driver,Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));
								//Thread.sleep(5000);
							}
						}
					break;
					case NC:
						if (StringUtils.isNotBlank(controlName)){
							Automation.browser = Automation.configHashMap.get("BROWSERTYPE").toString();
							if (StringUtils.equalsIgnoreCase("none", Automation.browser)){
								Automation.browser	= controlName.toString();
								Automation.browserType = Automation.browserTypeEnum.valueOf(Automation.browser);
								Automation.setUp();		
								wait = new WebDriverWait(Automation.driver,Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));
								//Thread.sleep(5000);
							}
						}
					break;	
					}
				break;
				case BrowserAuth:
					final String details[] = ctrlValue.split(",");
						Runnable thread2 = new Runnable() {
							public void run() {								
								try {										
									smartRobot = new SmartRobot();	
									Thread.sleep(5000);
									smartRobot.type(details[0].toString());// Enter username		
									Thread.sleep(2000);
									smartRobot.pressTab();// Click Keyboard Tab										
									smartRobot.type(details[1].toString());// Enter password		
									Thread.sleep(2000);
									smartRobot.pressEnter();// Click Enter button
									Thread.sleep(5000);
								} catch (Exception e) {										
									e.printStackTrace();
								}
							}
						};
						Thread thr2 = new Thread(thread2);
						thr2.start();			
					break;
					
				case URL:			
					final String url = ctrlValue;
					if(!StringUtils.isBlank(ctrlValue)){
						if(Automation.browser.equals("InternetExplorer")){					
							Automation.driver.navigate().to(url);
							Thread.sleep(5000);
						}
						else					
						{
							Runnable thread1 = new Runnable() {
								public void run() {
									try {
									Automation.driver.navigate().to(url);
									Thread.sleep(5000);
									} catch (InterruptedException e) {									
										e.printStackTrace();
									}
								}
							};
							Thread thr1 = new Thread(thread1);
							thr1.start();
						}			
					}					
				 break;
					
				case Menu:
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
					webElement.sendKeys(Keys.TAB);
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
					Actions builderClick = new Actions(Automation.driver);
					Action clickAction = builderClick.moveToElement(webElement).clickAndHold().release().build();
					clickAction.perform();
					break;

				case ActionDoubleClick:
					Actions builderdoubleClick = new Actions(Automation.driver);
					builderdoubleClick.doubleClick(webElement).perform();//TM-27/01/2015 :- commented following code and used this code for simultaneous clicks
					//Action doubleClickAction = builderdoubleClick.moveToElement(webElement).click().build();
					//doubleClickAction.perform();
					//doubleClickAction.perform();						
					break;

				case ActionClickandEsc:					
					Actions clickandEsc = new Actions(Automation.driver);
					Action clickEscAction = clickandEsc.moveToElement(webElement).click().sendKeys(Keys.ENTER,Keys.ESCAPE).build();
					clickEscAction.perform();
					break;					

				case ActionMouseOver:
					Actions builderMouserOver = new Actions(Automation.driver);
					Action mouseOverAction = builderMouserOver.moveToElement(webElement).build();
					mouseOverAction.perform();
					break;

				case Calendar:
					//	Thread.sleep(5000);
					Boolean isCalendarDisplayed = Automation.driver.switchTo().activeElement().isDisplayed();
					System.out.println(isCalendarDisplayed);					
					if(isCalendarDisplayed == true)
					{
						String[] dtMthYr = ctrlValue.split("/");
						WebElement Year = WaitTool.waitForElement(Automation.driver, By.name("year"), Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));//Automation.driver.findElement(By.name("year"));
						while(!Year.getAttribute("value").equalsIgnoreCase(dtMthYr[2]))
						{
							if(Integer.parseInt(Year.getAttribute("value"))>Integer.parseInt(dtMthYr[2]))
							{
								WebElement yearButton =  WaitTool.waitForElement(Automation.driver, By.id("button1"), Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));//Automation.driver.findElement(By.id("button1"));
								yearButton.click();
							}
							else if(Integer.parseInt(Year.getAttribute("value"))<Integer.parseInt(dtMthYr[2]))
							{
								WebElement yearButton =  WaitTool.waitForElement(Automation.driver, By.id("Button5"), Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));//Automation.driver.findElement(By.id("Button5"));
								yearButton.click();
							}
						}
						Select date = new Select(WaitTool.waitForElement(Automation.driver, By.name("month"), Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString())));
						month = CalendarSnippet.getMonthForInt(Integer.parseInt(dtMthYr[1]));	
						date.selectByVisibleText(month);
						WebElement Day = WaitTool.waitForElement(Automation.driver, By.id("Button6"), Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));//Automation.driver.findElement(By.id("Button6"));
						int day=6;
						while(Day.getAttribute("value")!= null)
						{
							Day= WaitTool.waitForElement(Automation.driver, By.id("Button"+day), Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));//Automation.driver.findElement(By.id("Button"+day));
							if(Day.getAttribute("value").toString().equalsIgnoreCase(dtMthYr[0]))
							{
								Day.click();
								break;
							}
							day++;
						}
					}
					else
					{
						System.out.println("Calendar not Diplayed");
					}
					//Automation.selenium.click(controlName);
					break;

				case CalendarNew:
					isCalendarDisplayed = Automation.driver.switchTo().activeElement().isDisplayed();
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
                            if (Automation.driver.getTitle().contains(controlName))
                            {      
                                   ( ( JavascriptExecutor ) Automation.driver ).executeScript( "window.onbeforeunload = function(e){};" );//By Tripti: 16/02/2015
                                   Automation.driver.close();                                                 
                            }
                            else
                            {
                                   parentHandle=winHandle;
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
								
								robot.delay(5000);
								robot.keyPress(KeyEvent.VK_CONTROL);
								robot.keyPress(KeyEvent.VK_V);
								robot.keyRelease(KeyEvent.VK_V);
								robot.keyRelease(KeyEvent.VK_CONTROL);
		
							}
							else if(controlName.equalsIgnoreCase("Type"))
							{
								robot.keyPress(KeyEvent.VK_M);
								robot.keyRelease(KeyEvent.VK_TAB);
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
					
				case DB:
					switch(actionName)
					{
					case Write:
						ResultSet rs =JDBCConnection.establishDBConn("", ctrlValue);
						rs.next(); 
						ctrlValue = String.valueOf(rs.getLong("COL_1"));
						rs.close();
						writeToExcel(ctrlValue, webElement, controlId, controlType, controlName, rowNo, colNo);
						break;
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
					//Locatable element = (Locatable) webElement; //TM:04/03/2015-commented as incorrect
					//Point p= element.getCoordinates().onScreen();//TM:04/03/2015-commented as incorrect
					Point p = webElement.getLocation();//TM:04/03/2015-New correct code
					System.out.println("X,Y co-ordinates of textbox is:- " + p);//TM:04/03/2015-New correct code
					JavascriptExecutor js = (JavascriptExecutor) Automation.driver;  //TM:04/03/2015-New correct code
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
		//TM-02/02/2015: Radio button found ("F") & AJAX control ("VA")
		if((action.toString().equalsIgnoreCase("V")||action.toString().equalsIgnoreCase("F")||action.toString().equalsIgnoreCase("VA")) && !ctrlValue.equalsIgnoreCase(""))
		{
			if(Results == true)
			{
				TransactionMapping.report = WriteToDetailResults(ctrlValue, currentValue, logicalName);
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
					TransactionMapping.report.strMessage=alert.getText();					
					alert.accept();							
					TransactionMapping.report.strStatus = "FAIL";
					TransactionMapping.pauseFun(TransactionMapping.report.strMessage);
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
			TransactionMapping.pauseFun("From PageLoaded Function" + e.getMessage());
		}
		catch(Exception e)
		{
			TransactionMapping.pauseFun("Timed Out after waiting");
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
			TransactionMapping.pauseFun("Ajax controls Loading "+e.getMessage());
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
				if(TransactionMapping.testCaseID.toString().equals(uniqueTestcaseID))//&& MainController.controllerTransactionType.toString().equals(uniqueTransactionType)
				{
					return uniqueNumber = getCellData(columnName,uniqueNumberSheet,rIndex,uniqueValuesHashMap);

				}						
			}			

		}
		catch(Exception e)
		{
			TransactionMapping.pauseFun(e.getMessage()+" from ReadFromExcel Function");
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

				if(TransactionMapping.testCaseID.toString().equals(uniqueTestcaseID))//&& MainController.controllerTransactionType.toString().equals(uniqueTransactionType)
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
					TransactionMapping.pauseFun("RowNumber or ColumnNumber is Missing");
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
			uniqueTestCaseID.setCellValue(TransactionMapping.testCaseID.toString());				
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
			System.out.println("Taking screenshot failed for: " +TransactionMapping.report.strTestcaseId);
			// e.printStackTrace();
			return;
		}
		String date = null;
		
		
		if(StringUtils.isNotBlank(TransactionMapping.report.frmDate))
            date = TransactionMapping.report.frmDate.replaceAll("[-/: ]","");
		else
			TransactionMapping.report.setFromDate(Automation.dtFormat.format(new Date()));		
		
		String fileName = TransactionMapping.report.strTestcaseId + "_" + TransactionMapping.report.strTrasactionType+ "_"+date;
		//TM:19/01/2015 - Changes made to save screenshots in jpeg format rather that png since they are heavier
		String location = System.getProperty("user.dir") +"\\Resources\\Results\\ScreenShots\\"+ fileName+".jpeg";
		TransactionMapping.report.strScreenshot = "file:\\\\"+location;

		try {

			FileUtils.copyFile(scrFile, new File(location));

		} 
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

}