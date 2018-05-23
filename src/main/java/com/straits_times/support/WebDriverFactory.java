package com.straits_times.support;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.xml.XmlTest;

import com.straits_times.support.EnvironmentPropertiesReader;
import com.straits_times.support.HarSummary;
import com.straits_times.support.Log;
import com.straits_times.support.StopWatch;
import com.straits_times.support.WebDriverFactory;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarLog;

/**
 * WebdriverFactory class used to get a web driver instance, depends on the user
 * requirement as driverHost, driverPort and browserName we adding the
 * desiredCapabilities and other static action initialized here and some methods
 * used to retrieve the Hub and node information. It also consists page wait
 * load for images/frames/document
 */

public class WebDriverFactory {

	private static Logger logger = LoggerFactory.getLogger(WebDriverFactory.class);
	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();

	static String driverHost;
	static String driverPort;
	static String browserName;
	static String deviceName;
	static URL hubURL;
	static Proxy zapProxy = new Proxy();

	static DesiredCapabilities ieCapabilities = DesiredCapabilities.internetExplorer();
	static DesiredCapabilities firefoxCapabilities = DesiredCapabilities.firefox();
	static DesiredCapabilities chromeCapabilities = DesiredCapabilities.chrome();
	static DesiredCapabilities safariCapabilities = DesiredCapabilities.safari();
	static DesiredCapabilities edgeCapabilities = DesiredCapabilities.edge();
	static DesiredCapabilities iOS_SimulatorCapabilities = new DesiredCapabilities();
	static DesiredCapabilities androidSimulatorCapabilities = new DesiredCapabilities();
	static DesiredCapabilities iOSDeviceCapabilities = DesiredCapabilities.iphone();
	static ChromeOptions opt = new ChromeOptions();
	static FirefoxProfile fp = new FirefoxProfile();
	public static ExpectedCondition<Boolean> documentLoad;
	public static ExpectedCondition<Boolean> framesLoad;
	public static ExpectedCondition<Boolean> imagesLoad;
	public static int maxPageLoadWait = 90;

	static {
		try {
			documentLoad = new ExpectedCondition<Boolean>() {
				public final Boolean apply(final WebDriver driver) {
					final JavascriptExecutor js = (JavascriptExecutor) driver;
					boolean docReadyState = false;
					try {
						docReadyState = (Boolean) js.executeScript(
								"return (function() { if (document.readyState != 'complete') {  return false; } if (window.jQuery != null && window.jQuery != undefined && window.jQuery.active) { return false;} if (window.jQuery != null && window.jQuery != undefined && window.jQuery.ajax != null && window.jQuery.ajax != undefined && window.jQuery.ajax.active) {return false;}  if (window.angular != null && angular.element(document).injector() != null && angular.element(document).injector().get('$http').pendingRequests.length) return false; return true;})();");
					} catch (WebDriverException e) {
						docReadyState = true;
					}
					return docReadyState;

				}
			};

			imagesLoad = new ExpectedCondition<Boolean>() {
				public final Boolean apply(final WebDriver driver) {
					boolean docReadyState = true;
					try {
						JavascriptExecutor js;
						List<WebElement> images = driver.findElements(By.cssSelector("img[src]"));
						for (int i = 0; i < images.size(); i++) {
							try {
								js = (JavascriptExecutor) driver;
								docReadyState = docReadyState && (Boolean) js.executeScript(
										"return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0",
										images.get(i));
								if (!docReadyState) {
									break;
								}
							} catch (StaleElementReferenceException e) {
								images = driver.findElements(By.cssSelector("img[src]"));
								i--;
								continue;
							} catch (WebDriverException e) {

								// setting the true value if any exception arise
								// Ex:: inside frame or switching to new windows
								// or
								// switching to new frames
								docReadyState = true;
							}
						}
					} catch (WebDriverException e) {
						docReadyState = true;
					}
					return docReadyState;
				}
			};

			framesLoad = new ExpectedCondition<Boolean>() {
				public final Boolean apply(final WebDriver driver) {
					boolean docReadyState = true;
					try {
						JavascriptExecutor js;
						List<WebElement> frames = driver.findElements(By.cssSelector("iframe[style*='hidden']"));
						for (WebElement frame : frames) {
							try {
								driver.switchTo().defaultContent();
								driver.switchTo().frame(frame);
								js = (JavascriptExecutor) driver;
								docReadyState = docReadyState
										&& (Boolean) js.executeScript("return (document.readyState==\"complete\")");
								driver.switchTo().defaultContent();
								if (!docReadyState) {
									break;
								}
							} catch (WebDriverException e) {
								docReadyState = true;
							}
						}
					} catch (WebDriverException e) {
						docReadyState = true;
					} finally {
						driver.switchTo().defaultContent();
					}
					return docReadyState;
				}
			};

			XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
			driverHost = System.getProperty("hubHost") != null ? System.getProperty("hubHost")
					: test.getParameter("deviceHost");
			driverPort = System.getProperty("hubPort") != null ? System.getProperty("hubPort")
					: test.getParameter("devicePort");

			maxPageLoadWait = configProperty.getProperty("maxPageLoadWait") != null
					? Integer.valueOf(configProperty.getProperty("maxPageLoadWait")) : maxPageLoadWait;

			opt.addArguments("--ignore-certificate-errors");
			opt.addArguments("--disable-bundled-ppapi-flash");
			opt.addArguments("--disable-extensions");
			opt.addArguments("--disable-web-security");
			opt.addArguments("--always-authorize-plugins");
			opt.addArguments("--allow-running-insecure-content");
			opt.addArguments("--test-type");
			opt.addArguments("--enable-npapi");
			chromeCapabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, true);

			try {
				hubURL = new URL("http://" + driverHost + ":" + driverPort + "/wd/hub");
			} catch (MalformedURLException e) {
				// e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Method to get instance of web driver using default parameters
	 * 
	 * @return driver
	 * @throws MalformedURLException
	 */
	public static WebDriver get() throws MalformedURLException {
		browserName = System.getProperty("browserName") != null ? System.getProperty("browserName")
				: Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest().getParameter("browserName")
						.toLowerCase();
		return get(browserName, null);
	}

	/**
	 * Method to get instance of web driver using browser details
	 * 
	 * @return driver
	 * @throws MalformedURLException
	 */
	public static WebDriver get(String browserSetup) throws MalformedURLException {
//		if(configProperty.getProperty("runMobileApp").equals("true"))   
//			return AppiumDriverFactory.get();
//		else
			return get(browserSetup, null);
	}

	/**
	 * Webdriver to get the web driver with browser name and platform and
	 * setting the desired capabilities for browsers
	 * 
	 * @param browserWithPlatform
	 *            : Browser With Platform
	 * @return driver: WebDriver Instance
	 * @throws MalformedURLException
	 */
	public static WebDriver get(String browserWithPlatform, Proxy proxy) throws MalformedURLException {
		String browser = null;
		String platform = null;
		String browserVersion = null;
		String appiumVersion = null;
		String deviceOrientation = null;

		String sauceUserName = null;
		String sauceAuthKey = null;
		WebDriver driver = null;
		String userAgent = null;
		long startTime = StopWatch.startTime();
		
		// Get invoking test name to pass on to Jenkins
		String callerMethodName = new Exception().getStackTrace()[2].getMethodName();
		Log.event("TestCaseID:: "+callerMethodName);
		
		String driverInitializeInfo[] = null;	
		// Handling System property variable overridden on parallel execution
		// till web driver initialization part
		synchronized (System.class) {
					// To support local to local execution by grid configuration
		if (browserWithPlatform.contains("_")) {
			browser = browserWithPlatform.split("_")[0].toLowerCase().trim();
			platform = browserWithPlatform.split("_")[1].toUpperCase().trim();
		} else {
			platform = "ANY";
		}

		try {
			if ("chrome".equalsIgnoreCase(browser)) {

					chromeCapabilities.setCapability(ChromeOptions.CAPABILITY, opt);
					chromeCapabilities.setPlatform(Platform.fromString(platform));
					if (proxy != null)
						chromeCapabilities.setCapability(CapabilityType.PROXY, proxy);

					driver = new RemoteWebDriver(hubURL, chromeCapabilities);
				
			} else if ("iexplorer".equalsIgnoreCase(browser)) {
				ieCapabilities.setCapability("enablePersistentHover", false);
				ieCapabilities.setCapability("ignoreZoomSetting", true);
				ieCapabilities.setCapability("nativeEvents", false);
				ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
						true);
				ieCapabilities.setPlatform(Platform.fromString(platform));

				if (proxy != null)
					ieCapabilities.setCapability(CapabilityType.PROXY, proxy);

				driver = new RemoteWebDriver(hubURL, ieCapabilities);
			} else if ("edge".equalsIgnoreCase(browser)) {
				edgeCapabilities.setPlatform(Platform.fromString(platform));
				driver = new RemoteWebDriver(hubURL, edgeCapabilities);
			} else if ("safari".equalsIgnoreCase(browser)) {
				driver = new RemoteWebDriver(hubURL, safariCapabilities);
			} else if ("zap".equalsIgnoreCase(browser)) { // To run a ZAP TC's
															// Use Browser opt
															// as zap
				Proxy zapChromeProxy = new Proxy();
				zapChromeProxy.setHttpProxy("localhost:8080");
				zapChromeProxy.setFtpProxy("localhost:8080");
				zapChromeProxy.setSslProxy("localhost:8080");
				chromeCapabilities.setCapability(ChromeOptions.CAPABILITY, opt);
				chromeCapabilities.setCapability(CapabilityType.PROXY, zapChromeProxy); 
				chromeCapabilities.setPlatform(Platform.fromString(platform));
				driver = new RemoteWebDriver(hubURL, chromeCapabilities);
			} else {
				synchronized (WebDriverFactory.class) {
					firefoxCapabilities.setCapability("unexpectedAlertBehaviour", "ignore");
					firefoxCapabilities.setPlatform(Platform.fromString(platform));
					driver = new RemoteWebDriver(hubURL, firefoxCapabilities);
				}
				driver.manage().timeouts().pageLoadTimeout(maxPageLoadWait, TimeUnit.SECONDS);
			}
			Assert.assertNotNull(driver,
					"Driver did not intialize...\n Please check if hub is running / configuration settings are corect.");

			if (!"ANDROID".equalsIgnoreCase(platform)) {
				driver.manage().window().maximize();
			}
		} catch (UnreachableBrowserException e) {
			e.printStackTrace();
			throw new SkipException("Hub is not started or down.");
		} catch (WebDriverException e) {

			try {
				if (driver != null) {
					driver.quit();
				}
			} catch (Exception e1) {
				e.printStackTrace();
			}

			if (e.getMessage().toLowerCase().contains("error forwarding the new session empty pool of vm for setup")) {
				throw new SkipException("Node is not started or down.");
			} else
				if (e.getMessage().toLowerCase().contains("error forwarding the new session empty pool of vm for setup")
						|| e.getMessage().toLowerCase().contains("cannot get automation extension")
						|| e.getMessage().toLowerCase().contains("chrome not reachable")) {
				Log.message("&emsp;<b> --- Re-tried as browser crashed </b>");
				try {
					driver.quit();
				} catch (WebDriverException e1) {
					e.printStackTrace();
				}
				driver = get();
			} else {
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Exception encountered in getDriver Method." + e.getMessage().toString());
		} finally {
			// ************************************************************************************************************
			// * Update start time of the tests once free slot is assigned by
			// RemoteWebDriver - Just for reporting purpose
			// *************************************************************************************************************/
			try {
				Field f = Reporter.getCurrentTestResult().getClass().getDeclaredField("m_startMillis");
				f.setAccessible(true);
				f.setLong(Reporter.getCurrentTestResult(), Calendar.getInstance().getTime().getTime());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		Log.event("Driver::initialize::Get", StopWatch.elapsedTime(startTime));
		Log.addTestRunMachineInfo(driver);
		return driver;
		}

	}

	/**
	 * generates an md5 HMAC digest based on the provided key and message.
	 * 
	 * @param keyString
	 *            Secret key
	 * @param msg
	 *            The message to be authenticated
	 * @return the digest
	 */
	public static String newHMACMD5Digest(String keyString, String msg) {
		String sEncodedString = null;
		try {
			SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacMD5");
			Mac mac = Mac.getInstance("HmacMD5");
			mac.init(key);

			byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

			StringBuffer hash = new StringBuffer();

			for (int i = 0; i < bytes.length; i++) {
				String hex = Integer.toHexString(0xFF & bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			sEncodedString = hash.toString();
		} catch (UnsupportedEncodingException e) {
		} catch (InvalidKeyException e) {
		} catch (NoSuchAlgorithmException e) {
		}
		return sEncodedString;
	}

	/**
	 * Get the test session Node IP address,port when executing through Grid
	 * 
	 * @param driver
	 *            : Webdriver
	 * @return: Session ID
	 * @throws Exception
	 *             : Selenium Exception
	 */
	public static final String getTestSessionNodeIP(final WebDriver driver) throws Exception {

		XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		driverHost = System.getProperty("hubHost") != null ? System.getProperty("hubHost")
				: test.getParameter("deviceHost");
		driverPort = test.getParameter("devicePort");
		HttpHost host = new HttpHost(driverHost, Integer.parseInt(driverPort));
		HttpClient client = HttpClientBuilder.create().build();
		URL testSessionApi = new URL("http://" + driverHost + ":" + driverPort + "/grid/api/testsession?session="
				+ ((RemoteWebDriver) driver).getSessionId());
		BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST",
				testSessionApi.toExternalForm());
		HttpResponse response = client.execute(host, r);
		JSONObject object = new JSONObject(EntityUtils.toString(response.getEntity()));
		String nodeIP = object.getString("proxyId").toLowerCase();
		nodeIP = nodeIP.replace("http://", "");
		nodeIP = nodeIP.replaceAll(":[0-9]{1,5}", "").trim();
		return nodeIP;
	}

	/**
	 * Get the test session Hub IP address,port when executing through Grid
	 * 
	 * @param driver
	 *            : Webdriver
	 * @return: Session ID
	 * @throws Exception
	 *             : Selenium Exception
	 */
	public static final String getHubSession(final WebDriver driver) throws Exception {

		XmlTest test = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		driverHost = System.getProperty("hubHost") != null ? System.getProperty("hubHost")
				: test.getParameter("deviceHost");
		driverPort = test.getParameter("devicePort");
		HttpHost host = new HttpHost(driverHost, Integer.parseInt(driverPort));
		HttpClient client = HttpClientBuilder.create().build();
		URL testSessionApi = new URL("http://" + driverHost + ":" + driverPort + "/grid/api/testsession?session="
				+ ((RemoteWebDriver) driver).getSessionId());
		BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST",
				testSessionApi.toExternalForm());
		HttpResponse response = client.execute(host, r);
		JSONObject object = new JSONObject(EntityUtils.toString(response.getEntity()));
		String nodeIP = object.getString("proxyId").toLowerCase();
		nodeIP = nodeIP.replace("http://", "");
		nodeIP = nodeIP.replaceAll(":[0-9]{1,5}", "").trim();
		return nodeIP;
	}

	/**
	 * To print the Har Summary details
	 * 
	 * @param har
	 */
	public static void printHarData(Har har) {
		HarLog log = har.getLog();
		List<HarEntry> harEntries = log.getEntries();
		Long totalSize = 0L;
		int callCount = 0;
		long totalTime = 0;
		for (HarEntry entry : harEntries) {
			callCount++;
			if (entry.getResponse() == null) {
				continue;
			}
			// System.out.println( "entry:" + entry.getRequest().getUrl() );
			totalSize += entry.getResponse().getBodySize();
			totalTime += entry.getTime(TimeUnit.MILLISECONDS);
		}
		HarSummary summary = new HarSummary((double) totalSize / 1024, callCount, totalTime);
		Log.message("#################<b>PERF DATA</b>###################");
		Log.message("<br>");
		Log.message("Call count : " + summary.getCallCount());
		Log.message("Size : " + summary.getTotalPayloadSize() / 1024 + " MB");
		Log.message("Total load time : " + summary.getTotalLoadTime() / 1000 + " seconds");
	}
}
