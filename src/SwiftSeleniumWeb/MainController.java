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
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public class MainController {
	private static HashMap<String,Object> sheetValues = new HashMap<String,Object>();
	private static int startCol =0;
	private static int startRow =0;
	private static HSSFRow controllerRow=null;
	public static boolean pauseExecution = false;
	public static HSSFCell controllerGroupName = null;
	public static HSSFCell controllerTestCaseID = null;
	public static HSSFCell controllerTransactionType = null;
	protected static ResultSet result = null;
	public static String testDesciption = null;

	/**
	 * Finds the Start Pointer in the MainController Sheet and executes the Transaction
	 * 
	 * @param FilePath
	 * @return
	 * @throws Exception
	 */
	public static Reporter ControllerData(String FilePath) throws Exception
	{
		Reporter report =new Reporter();	
		HSSFSheet reqSheet = ExcelUtility.GetSheet(Automation.configHashMap.get("CONTROLLER_FILEPATH").toString(), "MainControlSheet");
		sheetValues = WebHelper.getValueFromHashMap(reqSheet);
		int execFlag = Integer.parseInt(sheetValues.get("ExecuteFlag").toString());
		int rowCount = reqSheet.getLastRowNum()+1;
		int colCount=0;
		boolean isStartFound = false;
		for(int rowindex=0;rowindex<rowCount&&!isStartFound;rowindex++)
		{
			controllerRow = reqSheet.getRow(rowindex);
			
			if(controllerRow.getCell(execFlag) != null) {
				if(controllerRow.getCell(execFlag).toString().equals("Y"))
				{
					colCount = controllerRow.getLastCellNum()+1;
					for(int colIndex=execFlag+1;colIndex<colCount;colIndex++)
					{
						HSSFCell cellVal = controllerRow.getCell(colIndex); 
						
						if(cellVal != null){
							if(cellVal.toString().equalsIgnoreCase("START"))
							{
								startCol = colIndex;
								startRow = rowindex;
								isStartFound = true;
								break;

							}
						}
						else{
							System.out.println("START not Found");
						}
						
					}
				}
				else
				{
					System.out.println("Execute Flag is N");
				}
			}
			
		}

		for(int rowIndex=startRow;rowIndex<rowCount;rowIndex++)
		{ 
			pauseExecution = false;
			controllerRow = reqSheet.getRow(rowIndex);
			colCount = controllerRow.getLastCellNum()+1;
			testDesciption = WebHelper.getCellData("Test_Description", reqSheet, rowIndex, sheetValues);
			HSSFCell executeFlag=	controllerRow.getCell(execFlag);
			controllerTestCaseID = controllerRow.getCell(Integer.parseInt(sheetValues.get("TestCaseID").toString()));
			controllerGroupName = controllerRow.getCell(Integer.parseInt(sheetValues.get("GroupName").toString()));

			if(controllerTestCaseID.getStringCellValue().equalsIgnoreCase("") || controllerTestCaseID.equals(null))
			{
				System.out.println("No KeyWord Found");
				continue;
			}
			
							
			if(executeFlag != null){
				if(executeFlag.toString().equalsIgnoreCase("Y"))
				{								
					for(int columnIndex=startCol+1;columnIndex<colCount&&!pauseExecution;columnIndex++)
					{			
						controllerTransactionType = controllerRow.getCell(columnIndex);
						
						System.out.println("Value of controllerTransactionType: "+controllerTransactionType);
						
						if(controllerTransactionType != null && StringUtils.isNotBlank(controllerTransactionType.getStringCellValue())){
							if(controllerTransactionType.toString().equalsIgnoreCase("PAUSE"))
							{
								pauseFun("Do You Wish To Continue");
							}
							else
							{
								report = TransactionMapping.TransactionInputData(controllerTestCaseID,controllerTransactionType,Automation.configHashMap.get("TRANSACTION_INPUT_FILEPATH").toString());
							}
						}
						else
						{
							System.out.println("No Transaction Found in the Maincontroller at Cell : "+columnIndex);
						}
						
					}
				}
			}else{
				System.out.println("Execute Flag is not Set");
			}
			
		}
		startCol=execFlag+1;
		return report;
	}

	/**
	 * Pauses the Execution
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public static boolean pauseFun(String message) throws IOException
	{		
	
		String userInteraction = "TRUE";
		try
		{
			
			SwiftSeleniumWeb.WebDriver.report.setStrGroupName(MainController.controllerGroupName.toString());
			SwiftSeleniumWeb.WebDriver.report.setStrTestcaseId(MainController.controllerTestCaseID.toString());
			SwiftSeleniumWeb.WebDriver.report.setStrTestDescription(testDesciption);
			SwiftSeleniumWeb.WebDriver.report.setStrTrasactionType(MainController.controllerTransactionType.toString());
			WebHelper.toDate = new Date();
			SwiftSeleniumWeb.WebDriver.report.setStrMessage(message);
			SwiftSeleniumWeb.WebDriver.report.setToDate(Automation.dtFormat.format(WebHelper.toDate));		
			
			WebHelper.saveScreenShot();
			if(message == null)
			{			
				message = "TestCase: "+controllerTestCaseID +" Tranasction: "+controllerTransactionType+" Error: Unknown...";
				WebDriver.report.strMessage = message;
			}	
			if(Automation.configHashMap.size()!= 0)
			{
				try {
					if(Automation.configHashMap.get("USERINTERACTION").toString()==null)
					{

						throw new Exception("Null Value Found for UserInteractioin Parameter");

					}
					else
					{
						userInteraction = Automation.configHashMap.get("USERINTERACTION").toString();
					}
				}
				catch (Exception e)
				{

					JOptionPane.showConfirmDialog(WebDriver.frame, "Null Value Found for UserInteractioin Parameter");
				}
			}
			
			/**Don't mark status as FAIL if transaction name is PAUSE**/
			if(!controllerTransactionType.toString().equalsIgnoreCase("PAUSE"))
			{
				SwiftSeleniumWeb.WebDriver.report.setStrStatus("FAIL");
			}
			
			if(!userInteraction.equalsIgnoreCase("FALSE"))
			{
				WebDriver.frame.setVisible( true );
				WebDriver.frame.setAlwaysOnTop(true);
				WebDriver.frame.setLocationRelativeTo(null);

				int response = JOptionPane.showConfirmDialog(WebDriver.frame,message,"SwiftFramework",JOptionPane.YES_NO_OPTION);
				System.out.println(response);				
				if(response == JOptionPane.YES_OPTION)
				{
					pauseExecution = true;
				}
				else if(response == 1)
				{					
					/**Call error reporting and stop execution**/
					ExcelUtility.writeReport(WebDriver.report);								
				}
				else
				{
					System.out.println("You have pressed cancel" +response);
					pauseExecution =true;
				}
			}
			else
			{
				SwiftSeleniumWeb.WebDriver.report.setStrMessage(message);
				pauseExecution = true;
			}
		}
		finally
		{
			WebDriver.frame.dispose();
		}
		return pauseExecution;
	}
}
