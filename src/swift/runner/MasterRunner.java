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

package swift.runner;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.junit.runner.JUnitCore;
import swift.selenium.Automation;
import swift.selenium.WebDriver;


public class MasterRunner {

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String bddSupport = "false";
		JFrame frame = new JFrame("Swift Framework");		
		if (args.length == 0){			
					
			JOptionPane.showMessageDialog(frame, 
					"Please specify the Config File path in 'Arguments' tab from Menu Run=>Run Configuration > Click MasterRunner. For more info, "
					+ "please refer to User Guide 'Framework Installation' Section");
			System.exit(0);
		}
		else{
			Automation.LoadConfigData(args[0]);
		}
		
		
		if (Automation.configHashMap.containsKey("BDD_SUPPORT")){
			bddSupport = Automation.configHashMap.get("BDD_SUPPORT").toString();			
		}			
		
		/* Check if BDD Support is turned ON if yes then run Cucumber runner else run in Normal mode
		 i.e. (Test running from MainController) */
		
		if (bddSupport.trim().equalsIgnoreCase("true")) {
			
			if (!(Automation.configHashMap.containsKey("GLUE") 
					&& Automation.configHashMap.containsKey("TAGS") 
					&& Automation.configHashMap.containsKey("FEATURES"))){
				JOptionPane.showMessageDialog(frame, "Make sure Parameters 'GLUE', 'TAGS' and 'FEATURES' exist in Config File");
				System.exit(0);
					
			}
			else{
				
				String glueName= Automation.configHashMap.get("GLUE").toString();
				String featurePaths= Automation.configHashMap.get("FEATURES").toString();
				String tagName= Automation.configHashMap.get("TAGS").toString();
				
				featurePaths=featurePaths.replace(" ", "").trim();
				featurePaths= featurePaths.replaceAll(",", " ");
				
				tagName= tagName.trim().replace(",", ",@");
				tagName = "@"+tagName;
				JUnitCore junitRunner = new JUnitCore();			
				System.setProperty("cucumber.options", "--glue "+glueName+" --tags "+ tagName+ " "+ featurePaths);				
				junitRunner.run(swift.cucumber.CucumberRunner.class);
			
			}			
			
	    } else  {//(Test running from MainController) 
	    	WebDriver.main(args);
	    }

	}

}
