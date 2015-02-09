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
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.lang.StringUtils;
//import java.util.concurrent.TimeUnit;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebDriverBackedSelenium;
//import com.thoughtworks.selenium.DefaultSelenium;
//import com.thoughtworks.selenium.Selenium;
//import com.thoughtworks.selenium.webdriven.WebDriverBackedSelenium;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Automation {
	public static HashMap<String, Object> configHashMap = new HashMap<String, Object>();
	public static ResultSet result = null;
	public static WebDriver driver;
	public static enum browserTypeEnum {InternetExplorer,FireFox,Chrome};
	public static String browser =null;
	public static browserTypeEnum browserType = null;
	public static WebDriverFactory webDriverObj = new WebDriverFactory();
	public static DateFormat dtFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static void setUp() throws Exception
	{	

		if(CalendarSnippet.isProcessRunning("IEDriverServer.exe") )
		{
			CalendarSnippet.killProcess("IEDriverServer.exe");
		}
		if(CalendarSnippet.isProcessRunning("chromedriver.exe"))
		{
			CalendarSnippet.killProcess("chromedriver.exe");
		}		

		try
		{

			browser	= configHashMap.get("BROWSERTYPE").toString();
			browserType = browserTypeEnum.valueOf(browser);
			Object baseURL = configHashMap.get("BASEURL");

			switch(browserType)
			{
			case InternetExplorer:
				driver = getIEDriverInstance();				
				driver.manage().window().maximize();
				driver.get(baseURL.toString());
				break;			

			case FireFox:
				driver = getFFDriverInstance();
				driver.manage().window().maximize();
				driver.navigate().to(baseURL.toString());
				break;

			case Chrome:
				driver = getChromeDriverInstance();
				driver.manage().window().maximize();
				driver.get(baseURL.toString());
				break;
			}

		}
		catch(NullPointerException npe)
		{
			MainController.pauseFun("Null Values Found in Automation.SetUp Function");
		}
		catch(Exception e)
		{
			MainController.pauseFun("Error from Automation.Setup " + e.getMessage());
		}
	}

	/**Loads the Config sheet into HashMap**/
	public static void LoadConfigData() throws IOException, SQLException, ClassNotFoundException, URISyntaxException
	{
		
		try {
			
			Date initialDate = new Date();
			String strInitialDate = dtFormat.format(initialDate);
			SwiftSeleniumWeb.WebDriver.report.setFromDate(strInitialDate);		
	
			DataFormatter format = new DataFormatter();
			String projectPath = System.getProperty("user.dir");					
			String configPath = projectPath + "\\CommonResources\\Config.xls";			
			HSSFSheet configSheet = ExcelUtility.GetSheet(configPath, "Config");			
			int rowCount = configSheet.getLastRowNum()+1;

			for(int rowIndex=1;rowIndex<rowCount;rowIndex++)
			{
				HSSFRow rowActual = configSheet.getRow(rowIndex);
				String parameterName = format.formatCellValue(rowActual.getCell(0));				
				String value = format.formatCellValue(rowActual.getCell(1));
				
				if(StringUtils.isNotBlank(parameterName) || StringUtils.isNotBlank(value)){
					configHashMap.put(parameterName,value);
				}
							
			}
		} 
		catch(NullPointerException npe)
		{
			MainController.pauseFun("Null Values Found in Config Sheet");			
		}
		catch(Exception e)
		{
			MainController.pauseFun(e.getMessage()+ " From LoadConfig Function");
		}
	}

	/**Returns an IE Driver's Instance**/
	public static WebDriver getIEDriverInstance() throws InterruptedException,Exception
	{		
		return webDriverObj.createDriver("msie");
	}

	/**Returns a FireFox Driver's Instance**/
	public static WebDriver getFFDriverInstance() throws Exception
	{
		return new FirefoxDriver();
	}

	/**Returns a Chrome Driver's Instance**/
	public static WebDriver getChromeDriverInstance() throws Exception
	{
		return webDriverObj.createDriver("chrome");
	}	
}
