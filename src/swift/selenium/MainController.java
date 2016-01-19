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

	/**Finds the Start Pointer in the MainController Sheet and executes the Transaction**/
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
			//TM: commented the following to avoid continue
			/*if(controllerRow.getCell(execFlag).toString().equals(null))
			{
				continue;
			}*/		
			//TM: following 'if' is replacement of the above
			if(controllerRow.getCell(execFlag) != null) {
				if(controllerRow.getCell(execFlag).toString().equals("Y"))
				{
					colCount = controllerRow.getLastCellNum()+1;
					for(int colIndex=execFlag+1;colIndex<colCount;colIndex++)
					{
						HSSFCell cellVal = controllerRow.getCell(colIndex); 
						
						//TM: commented the following code to avoid continue
						/*if(cellVal == null)
						{
							System.out.println("START not Found");
							continue;

						}
						else */
						//TM: following new if added to check for null and else part for the same.
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
			
			
			//TM: Commented the code to avoid continue
			/*if(executeFlag == null)
			{
				System.out.println("Execute Flag is not Set");
				continue;
			}*/
			
			if(executeFlag != null){
				if(executeFlag.toString().equalsIgnoreCase("Y"))
				{								
					for(int columnIndex=startCol+1;columnIndex<colCount&&!pauseExecution;columnIndex++)
					{			
						controllerTransactionType = controllerRow.getCell(columnIndex);
						//TM: commented the following code to avoid continue
						/*if(controllerTransactionType == null || controllerTransactionType.getStringCellValue().equals(""))
						{
							System.out.println("No Transaction Found in the Maincontroller at Cell : "+columnIndex);
							continue;
						}*/
						
						//TM: Updated following sysout to give an understanding of what is getting printed on the console
						System.out.println("Value of controllerTransactionType: "+controllerTransactionType);
						
						//TM: wrapped the PAUSE if into another if as replacement of above commented if
						if(controllerTransactionType != null && StringUtils.isNotBlank(controllerTransactionType.getStringCellValue())){
							if(controllerTransactionType.toString().equalsIgnoreCase("PAUSE"))
							{
								TransactionMapping.pauseFun("Do You Wish To Continue");
							}
							else
							{
								report = TransactionMapping.TransactionInputData(controllerTransactionType.getStringCellValue(),
										controllerTestCaseID.getStringCellValue(),testDesciption,controllerGroupName.getStringCellValue());
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

	
}
