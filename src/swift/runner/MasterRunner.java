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
		Automation.LoadConfigData(args[0]);
		
		
		if (Automation.configHashMap.containsKey("BDD_SUPPORT")){
			bddSupport = Automation.configHashMap.get("BDD_SUPPORT").toString();
			//Automation.setUp();		
		}			
		
		if (bddSupport.trim().equalsIgnoreCase("true")) {
			org.junit.runner.JUnitCore.main("swift.cucumber.CucumberRunner");
	    } else  {
	    	WebDriver.main(args);
	    }

	}

}
