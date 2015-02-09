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
import java.util.HashMap;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;

public class TransactionMapping {

	private static String operationType ="";

	public static Reporter TransactionInputData(HSSFCell controllerTestCaseID,HSSFCell controllertransactionType,String filePath) throws Exception
	{
		Reporter report = new Reporter();
		HashMap<String, Object> inputHashTable = new HashMap<String, Object>();
		HSSFSheet workSheet = ExcelUtility.GetSheet(Automation.configHashMap.get("TRANSACTION_INPUT_FILEPATH").toString(), "Web_Transaction_Input_Files");
		int rowCount = workSheet.getLastRowNum()+1;
		for(int rowIndex=1;rowIndex<rowCount&&!MainController.pauseExecution;rowIndex++)
		{
			String transactionCode = WebHelper.getCellData("TransactionCode", workSheet, rowIndex, inputHashTable);
			String transactionType = WebHelper.getCellData("TransactionType", workSheet, rowIndex, inputHashTable);
			String directoryPath = WebHelper.getCellData("DirPath", workSheet, rowIndex, inputHashTable).toString();//
			String inputExcel = WebHelper.getCellData("InputSheet", workSheet, rowIndex, inputHashTable).toString();
			if(transactionType.toString().equalsIgnoreCase(controllertransactionType.toString()))
			{
				if(transactionCode != null&&directoryPath == null && controllertransactionType.toString().equalsIgnoreCase(transactionType.toString()))
				{
					report.strInputPath = "";
					report.strOperationType = "";
					report.strTransactioncode = transactionCode;
					WebDriver.DataInput("", controllerTestCaseID.toString(), transactionType, transactionCode, "");
					break;
				}
				
				if(!transactionType.toString().startsWith("Verify"))
				{
					operationType = "Input";
				}

				if(transactionType.toString().startsWith("Verify") && (!directoryPath.toString().isEmpty()) && (!inputExcel.toString().isEmpty()))
				{
					operationType = "InputandVerfiy";

				}
				else if(transactionType.toString().startsWith("Verify") && (directoryPath.toString().isEmpty()) && (inputExcel.toString().isEmpty()))
				{
					operationType = "Verify";
				}
				if(controllertransactionType.toString().equalsIgnoreCase(transactionType.toString()))
				{
					if((directoryPath == null||inputExcel == null)&& operationType !="Verify")
					{
						MainController.pauseFun("Please Enter the directory or excelsheet name");
					}
					else
					{
						String inputFilePath=null;
						if(operationType != "Verify")
						{
							inputFilePath = Automation.configHashMap.get("INPUT_DATA_FILEPATH").toString() + directoryPath.toString() + "\\" + inputExcel.toString();
						}					
						System.out.println(inputFilePath);
						report.strInputPath = inputFilePath;
						report.strOperationType = operationType;
						report.strTransactioncode = transactionCode;						
						WebDriver.DataInput(inputFilePath,controllerTestCaseID.toString(),transactionType,transactionCode,operationType);
						break;
					}
				}
			}
			else if(!transactionType.toString().equalsIgnoreCase(controllertransactionType.toString())&& rowIndex == rowCount-1)
			{
				MainController.pauseFun("Transaction "+MainController.controllerTransactionType.toString()+" Not Found");
				ExcelUtility.writeReport(SwiftSeleniumWeb.WebDriver.report);
			}
		}
		return null;
	}

}
