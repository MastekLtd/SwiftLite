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
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import org.apache.commons.lang3.StringUtils;

import swift.selenium.TransactionMapping;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;

public class StepDefinitions {
	public String testID="";
	public String testDescription="";
	public String groupName="";
	static WebDriver driver;

	@Before
	public void before(Scenario scenario) {		
		updateTestDetails(scenario);
	}

	@Given("(.*)$")
	public void user_on_webpage(String transaction) throws Throwable {
		String transactionname = "";
		// Write code here that turns the phrase above into concrete actions
		if (transaction.contains("<") && transaction.contains(">"))
			transactionname = transaction.substring(
					transaction.indexOf("<") + 1, transaction.indexOf(">"));

		if (!StringUtils.isBlank(transactionname))
			TransactionMapping.TransactionInputData(transactionname,
					this.testID, this.testDescription, this.groupName);
	}	
	
	/**
	 * This method captures the GroupName, TestID and TestDescription details of each testcase under execution
	 * @param scenario
	 */
	private void updateTestDetails(Scenario scenario){
		String featureScenario = scenario.getName();
		for(String tag : scenario.getSourceTagNames()){
			groupName = groupName + " " + tag.substring(0);
            System.out.println("Tag: " + tag);
        }
		if (featureScenario.contains("<") && featureScenario.contains(">")){
			this.testID = featureScenario.substring(
					featureScenario.indexOf("<") + 1,
					featureScenario.indexOf(">"));
			this.testDescription = featureScenario.substring(0, featureScenario.indexOf("<"));//starting from index 1 to skip @ sign of tags
		}

		System.out.println("Complete Scenario is :-" +featureScenario);
		System.out.println("Test Description is:- "+ this.testDescription);
		System.out.println("Group Name is:- "+ this.groupName);
		
		System.out.println("============================================================================================");
	}
}
