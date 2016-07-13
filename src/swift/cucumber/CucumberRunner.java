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

package swift.cucumber;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import swift.selenium.Automation;
import swift.selenium.TransactionMapping;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)

@CucumberOptions(plugin = {
		"pretty",
		"html:target/cucumber",
		"json:target_json/cucumber.json",
		"junit:taget_junit/cucumber.xml"
		})

public class CucumberRunner {
	public static JFrame frame;
	
	@BeforeClass
	public static void setUp() throws Exception{
		try {			
			Automation.setUp();

		} catch (Exception e) {
			TransactionMapping.report.strStatus = "FAIL";
			if (TransactionMapping.testCaseID != null)
				TransactionMapping.report.strTestcaseId = TransactionMapping.testCaseID
						.toString();
			TransactionMapping.report.strTrasactionType = TransactionMapping.transactionType
					.toString();
			try {
				TransactionMapping.pauseFun("TestCase: "
						+ TransactionMapping.testCaseID
						+ ", Tranasction: "
						+ TransactionMapping.transactionType
						+ ", Error: " + e.getMessage());
			} catch (IOException e1) {
				TransactionMapping.pauseFun("File Not Found");
			}
		}
	}

	@AfterClass
	public static void tearDown() {
		// TM: 16-01-2015
		frame = new JFrame("Swift Framework");
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setLocationRelativeTo(null);
		JOptionPane.showMessageDialog(frame, "Execution Completed");
		frame.dispose();
		// Runtime.getRuntime().exec("wscript.exe Report.vbs");//Provide the path to the Report.vbs file incase not present in the Project folders
		// Runtime.getRuntime().exec("wscript.exe DetailedReport.vbs");
		Automation.driver.quit();
	}

}
