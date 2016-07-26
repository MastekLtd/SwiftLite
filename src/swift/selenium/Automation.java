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

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Automation {
	public static HashMap<String, Object> configHashMap = new HashMap<String, Object>();
	public static HashMap<String,Object> appiumConfigMap = new HashMap<String,Object>();
	public static ResultSet result = null;
	public static WebDriver driver;
	public static enum browserTypeEnum {None, InternetExplorer, FireFox, Chrome, Safari, AppiumDriver};
	public static String browser = "None";
	public static browserTypeEnum browserType = browserTypeEnum.valueOf(browser);
	public static WebDriverFactory webDriverObj = new WebDriverFactory();
	public static DateFormat dtFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public static void setUp() throws Exception {
		if (driver != null) {
			WebHelper.wait = null;
			driver.quit();
			
		}
		
		// 03-Jun-16:SS  No need to check if Process is running, we can directly go for Kill operation
		String extn="";		
		String osName = System.getProperty("os.name");
		
		if (osName.contains("Mac") || osName.contains("Linux")) {
			extn="";
		
		}
		else{
			extn=".exe";
		}
	
		
		CalendarSnippet.killProcess("IEDriverServer"+extn);		
		CalendarSnippet.killProcess("chromedriver"+extn);
		CalendarSnippet.killProcess("node"+ extn);
		
	

		try {
			
			if (!StringUtils.equalsIgnoreCase("none", configHashMap.get("BROWSERTYPE").toString())){
				Automation.browser = Automation.configHashMap.get("BROWSERTYPE").toString();
			}
			browserType = browserTypeEnum.valueOf(browser);

			switch (browserType) {
			
			case AppiumDriver:
				
				AppiumDriverLocalService service = null;
				
				File classPathRoot = new File(System.getProperty("user.dir"));
				
				
				// Read Appium Config and set the desired capabilities
				String Appium_Path = appiumConfigMap.get("APPIUM_PATH").toString();
				String Appium_REMOTE_URL = appiumConfigMap.get("REMOTE_URL").toString();
				int Appium_REMOTE_PORT = Integer.parseInt(appiumConfigMap.get("REMOTE_PORT").toString());
			
				DesiredCapabilities capabilities = new DesiredCapabilities();

				String platformname = appiumConfigMap.get("PLATFORMNAME").toString();
				capabilities.setCapability("platformName", platformname);

				String deviceName = appiumConfigMap.get("DEVICENAME").toString();
				capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, deviceName);
				
				
				String udid = appiumConfigMap.get("UDID").toString();
				if (!udid.equalsIgnoreCase("NA")) 
				{
				
					capabilities.setCapability(MobileCapabilityType.UDID, udid);
				}

				String browserName = appiumConfigMap.get("BROWSERNAME").toString();
				if (browserName.equalsIgnoreCase("NA")) 
				{
					capabilities.setCapability(CapabilityType.BROWSER_NAME, "");					
				} 
				else 
				{
					capabilities.setCapability(CapabilityType.BROWSER_NAME,browserName);
				}

				String platform = appiumConfigMap.get("PLATFORM").toString();
				capabilities.setCapability(CapabilityType.PLATFORM, platform);

				String appPakcage = appiumConfigMap.get("APP_PACKAGE").toString();
				if (!appPakcage.equalsIgnoreCase("NA")) 
				{
					capabilities.setCapability("appPackage", appPakcage);				
				}

				String appPath = appiumConfigMap.get("APP_PATH").toString();
				if (!appPath.equalsIgnoreCase("NA")) 
				{
					capabilities.setCapability("app", appPath);
				}

				String appActivity = appiumConfigMap.get("APP_ACTIVITY").toString();
				if (!appActivity.equalsIgnoreCase("NA")) 
				{
					capabilities.setCapability("appActivity", appActivity);
				}
			
				String service_url="";
				
				// Start the Appium Service based on OS
				if (osName.contains("Windows")) {
					
					try {
						 service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
						 		.withAppiumJS(new File(Appium_Path + "\\node_modules\\appium\\bin\\Appium.js"))
						 		.usingDriverExecutable(new File(Appium_Path + "\\node.exe"))
								.withArgument(GeneralServerFlag.LOG_LEVEL, "info")
								.withIPAddress(Appium_REMOTE_URL)
								.usingPort(Appium_REMOTE_PORT)
								.withLogFile(new File(new File(classPathRoot, File.separator), "Appium.log")));
						 
						service.start();
						Thread.sleep(5000);
						service_url = service.getUrl().toString();
					}
					catch (Exception e)
					{
						e.printStackTrace();

					}					
					 
				  } else if (osName.contains("Mac")) {
					  
					  try {
					   service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
					   			.withAppiumJS(new File(Appium_Path + "//lib//node_modules//appium//build//lib//main.js"))
					   			.usingDriverExecutable(new File(Appium_Path + "//Cellar//node//6.2.0//bin//node"))
					   			.withArgument(GeneralServerFlag.LOG_LEVEL, "info")
					   			.withIPAddress(Appium_REMOTE_URL)
								.usingPort(Appium_REMOTE_PORT)								
								.withLogFile(new File(new File(classPathRoot, File.separator), "Appium.log")));
					   
					   service.start();
					   Thread.sleep(5000);
					   service_url = service.getUrl().toString();
					   
					  }
					  
					  catch (Exception e)
						{
							e.printStackTrace();

						}				   
				  
				  } else if (osName.contains("Linux")) {
				  
					  try {
					   service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
					   			.withAppiumJS(new File("/home/rushikesh/.linuxbrew/lib/node_modules/appium/bin/appium.js"))
					   			.usingDriverExecutable(new File(Appium_Path + "//usr//local//n//versions//node//6.0.0//bin//node"))
					   			.withArgument(GeneralServerFlag.LOG_LEVEL, "info")
					   			.withIPAddress(Appium_REMOTE_URL)
								.usingPort(Appium_REMOTE_PORT)								
								.withLogFile(new File(new File(classPathRoot, File.separator), "Appium.log")));
					   
					   service.start();
					   Thread.sleep(5000);
					   service_url = service.getUrl().toString();
					   
					  }
					  
					  catch (Exception e)
						{
							e.printStackTrace();
	
						}					   
				   
			 
				  }else {
					  TransactionMapping.pauseFun("Mobile Automation Support for this OS "+osName+" is yet to be added");
				   
				  	}
				
				// Initiate the driver based on Mobile OS
				if (platformname.contains("iOS")){
					driver = new IOSDriver(new URL(service_url), capabilities);
					
					
				}else if (platformname.contains("Android")) {
					driver = new AndroidDriver(new URL(service_url), capabilities);
				}
				
				break;			
				
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
				driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
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
			
			if(configHashMap.containsKey("MOBILETEST")){
				if(configHashMap.get("MOBILETEST").toString().equalsIgnoreCase("true"))
				{
					String appiumconfigPath = configHashMap.get("APPIUM_CONFIG_PATH").toString();	
					HSSFSheet appiumconfigSheet = ExcelUtility.GetSheet(appiumconfigPath, "Config");			
					int arowCount = appiumconfigSheet.getLastRowNum()+1;
	
					for(int rowIndex=1;rowIndex<arowCount;rowIndex++)
					{
						HSSFRow rowActual = appiumconfigSheet.getRow(rowIndex);
						String parameterName = format.formatCellValue(rowActual.getCell(0));				
						String value = format.formatCellValue(rowActual.getCell(1));
						
						if(StringUtils.isNotBlank(parameterName) || StringUtils.isNotBlank(value)){
							appiumConfigMap.put(parameterName,value);
						}
					}
					
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
