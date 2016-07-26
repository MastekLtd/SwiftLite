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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class WebDriver {
	
	private static Reporter report=new Reporter();
	public static JFrame frame = new JFrame("SWIFT FRAMEWORK");
	
	public static void main(String args[]) throws IOException
	{
		try
		{	
			//Automation.LoadConfigData();		
		    Automation.setUp();			
			MainController.ControllerData(Automation.configHashMap.get("CONTROLLER_FILEPATH").toString());			
		}
		catch(Exception e)
		{			
			report.strStatus = "FAIL";
			if(MainController.controllerTestCaseID != null)
				report.strTestcaseId = MainController.controllerTestCaseID.toString();
			report.strTrasactionType = MainController.controllerTransactionType.toString();		
			try {
				TransactionMapping.pauseFun("TestCase: "+MainController.controllerTestCaseID +", Tranasction: "+MainController.controllerTransactionType+", Error: "+e.getMessage());
			} catch (IOException e1) {
				TransactionMapping.pauseFun("File Not Found");
			}
		}
		finally
		{	
			//TM: 16-01-2015
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);
			frame.setLocationRelativeTo(null);
			JOptionPane.showMessageDialog(frame, "Execution Completed");		
			frame.dispose();	
			//Runtime.getRuntime().exec("wscript.exe Report.vbs");//Provide the path to the Report.vbs file incase not present in the Project folders
			//Runtime.getRuntime().exec("wscript.exe DetailedReport.vbs");
			//SS:21-10-2015 Handled Null condition for non-browser based operation such as Web service.
			if(Automation.driver != null)			{
				Automation.driver.quit();
			}
			
		}
 	}


	
}

