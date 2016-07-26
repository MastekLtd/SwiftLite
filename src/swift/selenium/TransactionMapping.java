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
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;



public class TransactionMapping {

	private static String operationType = "";
	public static boolean pauseExecution = false;
	public static Reporter report = new Reporter();
	public static String testCaseID = ""; // This needs to be modified
	public static Date toDate = null;
	public static String transactionType;
	public static JFrame frame = new JFrame("SWIFT FRAMEWORK");
	
	public static Reporter TransactionInputData(String transaction,
			String testId, String description, String group) throws Exception {

		HashMap<String, Object> inputHashTable = new HashMap<String, Object>();
		HSSFSheet workSheet = ExcelUtility.GetSheet(Automation.configHashMap
				.get("TRANSACTION_INPUT_FILEPATH").toString(),
				"Web_Transaction_Input_Files");
		int rowCount = workSheet.getLastRowNum() + 1;
		transactionType = transaction;
		testCaseID = testId;
		for (int rowIndex = 1; rowIndex < rowCount; rowIndex++) {
			String transactionCode = getCellData("TransactionCode", workSheet,
					rowIndex, inputHashTable);
			String transactionType = getCellData("TransactionType", workSheet,
					rowIndex, inputHashTable);
			String directoryPath = getCellData("DirPath", workSheet, rowIndex,
					inputHashTable).toString();
			String inputExcel = getCellData("InputSheet", workSheet, rowIndex,
					inputHashTable).toString();
			
			 String TestFilePath = Automation.configHashMap.get(
					"INPUT_DATA_FILEPATH").toString()
					+ directoryPath.toString()
					+ File.separator
					+ inputExcel.toString();
		 
			if (transactionType.toString().equalsIgnoreCase(
					transaction.toString())) {
				if (transactionCode != null
						&& directoryPath == null
						&& transaction.toString().equalsIgnoreCase(
								transactionType.toString())) {
					report.strInputPath = "";
					report.strOperationType = "";
					report.strTransactioncode = transactionCode;
					report.strTestDescription = description;
					DataInput("", testCaseID.toString(), transactionType,
							transactionCode, "");
					break;
				}

				if (!transactionType.toString().startsWith("Verify")) {
					operationType = "Input";
				}

				if (transactionType.toString().startsWith("Verify")
						&& (!directoryPath.toString().isEmpty())
						&& (!inputExcel.toString().isEmpty())) {
					operationType = "InputandVerfiy";

				} else if (transactionType.toString().startsWith("Verify")
						&& (directoryPath.toString().isEmpty())
						&& (inputExcel.toString().isEmpty())) {
					operationType = "Verify";
				}
				if (transaction.toString().equalsIgnoreCase(
						transactionType.toString())) {
					if ((directoryPath == null || inputExcel == null)
							&& operationType != "Verify") {
						pauseFun("Please Enter the directory or excelsheet name");
					} else {
						String inputFilePath = null;
						if (operationType != "Verify") {
							inputFilePath = Automation.configHashMap.get(
									"INPUT_DATA_FILEPATH").toString()
									+ directoryPath.toString()
									+ File.separator
									+ inputExcel.toString();
						}
						System.out.println(inputFilePath);
						report.strInputPath = inputFilePath;
						report.strOperationType = operationType;
						report.strTestDescription = description;
						report.strTransactioncode = transactionCode;
						report.strGroupName = group;
						DataInput(inputFilePath, testCaseID.toString(),
								transactionType, transactionCode, operationType);
						break;
					}
				}
			} else if (!transactionType.toString().equalsIgnoreCase(
					transaction.toString())
					&& rowIndex == rowCount - 1) {
				pauseFun("Transaction " + transaction.toString() + " Not Found");
				ExcelUtility.writeReport(report);
			}
		}

		return null;

	}
	@SuppressWarnings("null")
	public static String getCellData(String reqValue, HSSFSheet reqSheet,
			int rowIndex, HashMap<String, Object> inputHashTable)
			throws IOException {
		HSSFCell reqCell = null;
		Object actualvalue = null;
		String req = "";
		DataFormatter fmt = new DataFormatter();
		if (inputHashTable.isEmpty() == true) {
			inputHashTable = getValueFromHashMap(reqSheet);
		}
		HSSFRow rowActual = reqSheet.getRow(rowIndex);
		if (inputHashTable.get(reqValue) == null) {
			report.setStrMessage("Column " + reqValue
					+ " not Found. Please Check input Sheet");
			pauseFun("Column " + reqValue
					+ " not Found. Please Check input Sheet");
		} else {
			actualvalue = inputHashTable.get(reqValue);// rowHeader.getCell(colIndex).toString();
			if (actualvalue != null) {
				int colIndex = Integer.parseInt(actualvalue.toString());
				reqCell = rowActual.getCell(colIndex);
				if (reqCell == null) {
					System.out.println(reqValue + " is Null");
				} else {
					int type = reqCell.getCellType();
					switch (type) {
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

			else {
				req = reqCell.getStringCellValue();
				System.out.println("null");
			}
		}
		return req;
	}

	/** Pauses the Execution **/
	public static boolean pauseFun(String message) throws IOException {

		/**
		 * DS:18-07-2014:Replacing timeout in msg String tempMsg =
		 * "Timed out after CONFIGTIMEOUT seconds waiting for presence of element located by"
		 * ; tempMsg = tempMsg.replace("CONFIGTIMEOUT",
		 * Automation.configHashMap.get("TIMEOUT").toString()); if(message!=
		 * null) message = message.replace(tempMsg, "Element not found");
		 **/

		String userInteraction = "TRUE";
		try {

			/*
			 * if(controllerGroupName !=null)
			 * report.setStrGroupName(controllerGroupName.toString());
			 */
			if (testCaseID != null)
				report.setStrTestcaseId(testCaseID.toString());
			// report.setStrTestDescription(testDesciption);
			// report.setStrTrasactionType(controllerTransactionType.toString());
			toDate = new Date();
			report.setStrMessage(message);
			report.setToDate(Automation.dtFormat.format(toDate));

			saveScreenShot();
			if (message == null) {
				message = "TestCase: " + testCaseID + " Tranasction: "
						+ transactionType + " Error: Unknown...";
				report.strMessage = message;
			}
			if (Automation.configHashMap.size() != 0) {
				try {
					if (Automation.configHashMap.get("USERINTERACTION")
							.toString() == null) {

						throw new Exception(
								"Null Value Found for UserInteractioin Parameter");

					} else {
						userInteraction = Automation.configHashMap.get(
								"USERINTERACTION").toString();
					}
				} catch (Exception e) {

					JOptionPane.showConfirmDialog(frame,
							"Null Value Found for UserInteractioin Parameter");
				}
			}

			/** Don't mark status as FAIL if transaction name is PAUSE **/
			if (!transactionType.toString().equalsIgnoreCase("PAUSE")) {
				report.setStrStatus("FAIL");
			}

			if (!userInteraction.equalsIgnoreCase("FALSE")) {
				frame.setVisible(true);
				frame.setAlwaysOnTop(true);
				frame.setLocationRelativeTo(null);

				int response = JOptionPane.showConfirmDialog(frame, message,
						"SwiftFramework", JOptionPane.YES_NO_OPTION);
				System.out.println(response);
				if (response == JOptionPane.YES_OPTION) {
					pauseExecution = true;
				} else if (response == 1) {
					/** Call error reporting and stop execution **/
					ExcelUtility.writeReport(report);
					/** TM-29-04-2015: Commented the following code **/
					// System.gc();
					// WebDriver.frame.dispose();
					// Automation.driver.quit();
					// System.exit(0);
				} else {
					System.out.println("You have pressed cancel" + response);
					pauseExecution = true;
				}
			} else {
				report.setStrMessage(message);
				pauseExecution = true;
			}
		} finally {
			frame.dispose();
		}
		return pauseExecution;
	}

	public static void DataInput(String filePath, String testcaseID,
			String transactionTypeFetched, String transactionCode, String operationType)
			throws Exception {
		if (transactionCode == null) {
			transactionCode = transactionTypeFetched;
		}
		System.out.println(transactionCode);

		if (operationType.equalsIgnoreCase("InputandVerfiy")
				&& !operationType.isEmpty()) {
			ExcelUtility.GetDataFromValues(filePath, testcaseID.toString(),
					transactionTypeFetched.toString());
			WebVerification.performVerification(transactionType, testcaseID);
		} else if (!operationType.equalsIgnoreCase("Verify")
				&& !operationType.isEmpty()) {
			ExcelUtility.GetDataFromValues(filePath, testcaseID.toString(),
					transactionTypeFetched.toString());
		} else if (!operationType.equalsIgnoreCase("Input")
				&& !operationType.isEmpty()) {
			WebVerification.performVerification(transactionType, testcaseID);
		}
	}

	public static HashMap<String, Object> getValueFromHashMap(HSSFSheet reqSheet) {
		HashMap<String, Object> inputHashTable = new HashMap<String, Object>();
		HSSFRow rowHeader = reqSheet.getRow(0);
		int columnCount = rowHeader.getPhysicalNumberOfCells();
		for (int colIndex = 0; colIndex < columnCount; colIndex++) {
			inputHashTable
					.put(rowHeader.getCell(colIndex).toString(), colIndex);
		}
		return inputHashTable;
	}

	public static void saveScreenShot() {
		if (!(Automation.driver instanceof TakesScreenshot)) {

			System.out
					.println("Not able to take screenshot: Current WebDriver does not support TakesScreenshot interface.");
			return;
		}

		File scrFile;
		try {

			scrFile = ((TakesScreenshot) Automation.driver)
					.getScreenshotAs(OutputType.FILE);
		} catch (Exception e) {
			System.out.println("Taking screenshot failed for: "
					+ report.strTestcaseId);
			// e.printStackTrace();
			return;
		}
		String date = null;

		if (StringUtils.isNotBlank(report.frmDate))
			date = report.frmDate.replaceAll("[-/: ]", "");
		else
			report.setFromDate(Automation.dtFormat.format(new Date()));

		String fileName = report.strTestcaseId + "_" + report.strTrasactionType
				+ "_" + date;
		// TM:19/01/2015 - Changes made to save screenshots in jpeg format
		// rather that png since they are heavier
		String location = Automation.configHashMap.get("INPUT_DATA_FILEPATH").toString()
				+ File.separator+ ".."+File.separator+"Results" + File.separator +"ScreenShots"+ File.separator + fileName + ".jpeg";
		report.strScreenshot = "file:\\\\" + location;

		try {

			FileUtils.copyFile(scrFile, new File(location));

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}
}
