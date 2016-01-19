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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Automation {
	public static HashMap<String, Object> configHashMap = new HashMap<String, Object>();
	public static ResultSet result = null;
	public static WebDriver driver;
	public static enum browserTypeEnum {None, InternetExplorer, FireFox, Chrome, Safari};
	public static String browser = "None";
	public static browserTypeEnum browserType = browserTypeEnum.valueOf(browser);
	public static WebDriverFactory webDriverObj = new WebDriverFactory();
	public static DateFormat dtFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public static void setUp() throws Exception {
		if (driver != null) {
			WebHelper.wait = null;
			driver.quit();
			
		}
		
		
		if(CalendarSnippet.isProcessRunning("IEDriverServer.exe") )
		{
			CalendarSnippet.killProcess("IEDriverServer.exe");
		}
		if(CalendarSnippet.isProcessRunning("chromedriver.exe"))
		{
			CalendarSnippet.killProcess("chromedriver.exe");
		}		


		try {
			
			if (!StringUtils.equalsIgnoreCase("none", configHashMap.get("BROWSERTYPE").toString())){
				Automation.browser = Automation.configHashMap.get("BROWSERTYPE").toString();
			}
			browserType = browserTypeEnum.valueOf(browser);

			switch (browserType) {
			case InternetExplorer:
				driver = getIEDriverInstance();
				driver.manage().deleteAllCookies();
				driver.manage().window().maximize();
				break;
			case FireFox:
				driver = getFFDriverInstance();
				driver.manage().deleteAllCookies();
				driver.manage().window().maximize();
				break;

			case Chrome:
				driver = getChromeDriverInstance();
				driver.manage().deleteAllCookies();				
				break;

			// TM-20/01/2015-Case added for Safari
			case Safari:
				driver = getSafariDriverInstance();
				driver.manage().window().maximize();
				break;
			case None:
				System.out.println("The browser setup need to be done in Input file...");
				break;
			}
			if (driver != null) {
				WebHelper.wait = new WebDriverWait(Automation.driver,Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));
			}
			//if (!StringUtils.equalsIgnoreCase("none", configHashMap.get("BROWSERTYPE").toString())){
				//WebHelper.wait = new WebDriverWait(Automation.driver,Integer.parseInt(Automation.configHashMap.get("TIMEOUT").toString()));
			//}
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			TransactionMapping.pauseFun("Null Values Found in Automation.SetUp Function");
		} catch (Exception e) {
			e.printStackTrace();
			TransactionMapping.pauseFun("Error from Automation.Setup " + e.getMessage());
		}
	}

	/** Returns an IE Driver's Instance **/
	public static WebDriver getIEDriverInstance() throws InterruptedException,
			Exception {
		// TM:Commented the following code as driver is defined global
		return webDriverObj.createDriver("msie");
	}

	/** Returns a FireFox Driver's Instance **/
	public static WebDriver getFFDriverInstance() throws Exception {
		// TM: commented the following code as driver is defined global
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("network.automatic-ntlm-auth.trusted-uris",
				"masteknet.com");// for https
		return new FirefoxDriver(profile);
	}

	/** Returns a Chrome Driver's Instance **/
	public static WebDriver getChromeDriverInstance() throws Exception {
		// TM: commented the following code as driver is defined global
		return webDriverObj.createDriver("chrome");
	}

	/** Returns a Safari Driver Instance **/
	public static WebDriver getSafariDriverInstance() throws IOException {
		// TM: commented the following code as driver is defined global
		return webDriverObj.createDriver("safari");
	}

	/** Loads the Config sheet into HashMap **/
	public static void LoadConfigData(String configFile) throws IOException {

		try {

			Date initialDate = new Date();
			String strInitialDate = dtFormat.format(initialDate);
			TransactionMapping.report.setFromDate(strInitialDate);

			DataFormatter format = new DataFormatter();
			//String projectPath = System.getProperty("user.dir");
			//String configPath = projectPath + "\\CommonResources\\Config.xls";
			HSSFSheet configSheet = ExcelUtility.GetSheet(configFile, "Config");
			for (int rowIndex = 1; rowIndex <= configSheet.getLastRowNum(); rowIndex++) 
			{
				HSSFRow rowActual = configSheet.getRow(rowIndex);
				String parameterName = format.formatCellValue(rowActual.getCell(0));
				String value = format.formatCellValue(rowActual.getCell(1));
				// Following 'if' is replacement of above, checks if parameterName and value are neither null nor Blank
				if (StringUtils.isNotBlank(parameterName) || StringUtils.isNotBlank(value)) {
					configHashMap.put(parameterName, value);
				}

			}
		} catch (NullPointerException npe) {
			TransactionMapping.pauseFun("Null Values Found in Config Sheet");
			npe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			TransactionMapping.pauseFun(e.getMessage() + " From LoadConfig Function");
		}
	}
}
