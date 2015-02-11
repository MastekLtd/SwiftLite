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
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class WebDriver {
	
	public static Reporter report=new Reporter();
	public static JFrame frame = new JFrame("SWIFT FRAMEWORK");
	
	public static void main(String args[]) throws IOException
	{
		try
		{	
			Automation.LoadConfigData();
			Automation.setUp();
			MainController.ControllerData(Automation.configHashMap.get("CONTROLLER_FILEPATH").toString());			
		}
		catch(Exception e)
		{
			report.strStatus = "FAIL";
			report.strTestcaseId = MainController.controllerTestCaseID.toString();
			report.strTrasactionType = MainController.controllerTransactionType.toString();			
			try {
				MainController.pauseFun("TestCase: "+MainController.controllerTestCaseID +", Tranasction: "+MainController.controllerTransactionType+", Error: "+e.getMessage());
			} catch (IOException e1) {
				MainController.pauseFun("File Not Found");
			}
		}
		finally
		{
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);
			frame.setLocationRelativeTo(null);
			JOptionPane.showMessageDialog(frame, "Execution Completed");		
			frame.dispose();			
			Automation.driver.quit();		
		}
	}


	public static void DataInput(String filePath,String testcaseID,String transactionType,String transactionCode,String operationType) throws Exception
	{
		if(transactionCode == null)
		{
			transactionCode = transactionType; 
		}
		
		if(!operationType.isEmpty())
		{
			System.out.println(transactionCode);

			ExcelUtility.GetDataFromValues(filePath, testcaseID.toString(), transactionType.toString());
		}
	}
}

