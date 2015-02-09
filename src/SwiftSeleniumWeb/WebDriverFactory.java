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
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;

public class WebDriverFactory {

	private static WebDriverFactory instance;

	private static final String CHROME_DRIVER_LOCATION = "webdriver.chrome.driver";
	private static final String CHROME_DRIVER_NOT_DEFINED = "notDefined";
	private final String chromeDriverEnv;
	private final Map<String, WebDriverCreator> drivers = new HashMap<String, WebDriverCreator>();

	public WebDriverFactory() {
		
		initializeWebDriverCreators();
		chromeDriverEnv = System.getProperty("user.dir")+"\\libs\\chromedriver.exe";		
		System.setProperty(CHROME_DRIVER_LOCATION, (chromeDriverEnv != null) ? chromeDriverEnv : CHROME_DRIVER_NOT_DEFINED);
	}

	public static WebDriverFactory getInstance() {
		if (instance == null) {
			instance = new WebDriverFactory();
		}
		return instance;
	}

	private boolean isChromeDriverInstalled() {
		return System.getProperty(CHROME_DRIVER_LOCATION) != CHROME_DRIVER_NOT_DEFINED;
	}

	private void initializeWebDriverCreators() {
		drivers.put("msie", new IEWebDriverCreator());
		drivers.put("chrome", new ChromeWebDriverCreator());
		drivers.put("chromium", new ChromiumWebDriverCreator());
		drivers.put("saucelab", new SauceLabWebDriverCreator());
		drivers.put("ghost", new GhostWebDriverCreator());
		drivers.put("grid", new GridWebDriverCreator());
		drivers.put("remotegrid", new RemoteGridWebDriverCreator());
	}

	public WebDriver createDriver(String driverName) {
		if (drivers.containsKey(driverName))
		{			
			return drivers.get(driverName).create();
		}
		else
		{
			EventFiringWebDriver eventFiringWebDriver;
			WebDriver fireFoxDriver = new FirefoxDriver();
			eventFiringWebDriver = new EventFiringWebDriver(fireFoxDriver);
			String listenerEnabled = System.getProperty("driverlistener", "false");
			if (listenerEnabled.equals("true")) {
				return eventFiringWebDriver;
			} else {
				return fireFoxDriver;
			}
		}
	}

	/*private FirefoxProfile getFirefoxProfile() {
		FirefoxProfile profile = new FirefoxProfile();
		profile.setAssumeUntrustedCertificateIssuer(true);
		// in order to enable natives on linux
		profile.setEnableNativeEvents(true);

		// automatically download files:
		profile.setPreference("browser.download.folderList", 2);
		profile.setPreference("browser.download.manager.showWhenStarting", false);
		profile.setPreference("browser.download.dir", System.getProperty("java.io.tmpdir"));
		profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/msexcel");

		if (isFirefoxExtensionsEnabled()) {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			URL firebug = classLoader.getResource("extensions/firefox/firebug-1.7X.0b4.xpi");
			URL fireStarter = classLoader.getResource("extensions/firefox/fireStarter-0.1a6.xpi");
			URL netExport = classLoader.getResource("extensions/firefox/netExport-0.8b21.xpi");
			try {
				File firebugFile = new File(firebug.toURI());
				File fireStarterFile = new File(fireStarter.toURI());
				File netExprotFile = new File(netExport.toURI());
				try {
					profile.addExtension(firebugFile);
					profile.addExtension(fireStarterFile);
					profile.addExtension(netExprotFile);
					profile.setPreference("extensions.firebug.onByDefault", true);
					profile.setPreference("extensions.firebug.currentVersion", "2.0");
					profile.setPreference("extensions.firebug.previousPlacement", 1);
					profile.setPreference("extensions.firebug.defaultPanelName", "net");
					profile.setPreference("extensions.firebug.net.enableSites", true);
					profile.setPreference("extensions.firebug.netexport.alwaysEnableAutoExport", "true");
					profile.setPreference("extensions.firebug.netexport.defaultLogDir", "c:\\");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
		return profile;
	}*/

	/** ------------------------------------------------------------------------**/
	/**							 WebDriver Creators                             **/
	/** ------------------------------------------------------------------------**/

	private interface WebDriverCreator {
		WebDriver create();
	}

	private class IEWebDriverCreator implements WebDriverCreator {
		@Override
		public WebDriver create() {
			DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();		
			ieCapabilities.setCapability("unexpectedAlertBehaviour" , "ignore");
			//ieCapabilities.setCapability("enablePersistentHover", true);
			File file = new File(System.getProperty("user.dir") + "/libs/IEDriverServer.exe");
			System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
			return new InternetExplorerDriver(ieCapabilities);
		}
	}

	private class ChromeWebDriverCreator implements WebDriverCreator {
		@Override
		public WebDriver create() {
			if (!isChromeDriverInstalled()) {
				throw new RuntimeException("Chrome driver not installed");
			}
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--ignore-certificate-errors");
			options.addArguments("--disable-popup-blocking");
			options.addArguments("--disable-translate");
			options.addArguments("--start-maximized");
			return new ChromeDriver(options);
		}
	}

	private class ChromiumWebDriverCreator implements WebDriverCreator {
		@Override
		public WebDriver create() {
			if (!isChromeDriverInstalled()) {
				throw new RuntimeException("Chrome/Chromium driver not installed");
			}
			ChromeOptions options = new ChromeOptions();
			options.setBinary(new File("/usr/bin/chromium-browser"));
			options.addArguments("--ignore-certificate-errors");
			options.addArguments("--disable-popup-blocking");
			options.addArguments("--disable-translate");
			options.addArguments("--start-maximized");
			return new ChromeDriver(options);
		}
	}

	private class GridWebDriverCreator implements WebDriverCreator {

		@Override
		public WebDriver create() {
			DesiredCapabilities capability = DesiredCapabilities.firefox();
			FirefoxProfile profile = new FirefoxProfile();
			profile.setEnableNativeEvents(true);
			capability.setCapability(FirefoxDriver.PROFILE, profile);
			WebDriver driver = null;
			try {
				driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), capability);
				//SessionId sessionId = ((RemoteWebDriver) driver).getSessionId();
				//log.info("Running test on node: " + SeleniumGridHandler.getNodeIpBySessionId(sessionId));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return driver;
		}

	}

	private class RemoteGridWebDriverCreator implements WebDriverCreator {

		@Override
		public WebDriver create() {
			DesiredCapabilities capability = DesiredCapabilities.firefox();
			FirefoxProfile profile = new FirefoxProfile();
			profile.setEnableNativeEvents(true);
			capability.setCapability(FirefoxDriver.PROFILE, profile);
			WebDriver driver = null;
			try {
				driver = new RemoteWebDriver(new URL("http://10.48.11.144:4444/wd/hub"), capability);
				//SessionId sessionId = ((RemoteWebDriver) driver).getSessionId();
				//log.info("Running test on node: " + SeleniumGridHandler.getNodeIpBySessionId(sessionId));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return driver;
		}

	}

	private class SauceLabWebDriverCreator implements WebDriverCreator {
		@Override
		public WebDriver create() {
			DesiredCapabilities capabillities = DesiredCapabilities.firefox();
			capabillities.setCapability("version", "16");
			capabillities.setCapability("platform", org.openqa.selenium.Platform.XP);
			capabillities.setCapability("name", "TalentLink on Sauce");
			try {
				URL sauceURL = new URL("http://michaldec:48beee6d-1691-457d-9ac1-af71347faf9d@ondemand.saucelabs.com:80/wd/hub");
				return new RemoteWebDriver(sauceURL, capabillities);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class GhostWebDriverCreator implements WebDriverCreator {
		@Override
		public WebDriver create() {
			String ghostDriverUrl = System.getProperty("ghost.url", "http://localhost:4444");
			try {
				return new RemoteWebDriver(new URL(ghostDriverUrl), DesiredCapabilities.chrome());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
