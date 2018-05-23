package com.straits_times.support;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import com.straits_times.support.EnvironmentPropertiesReader;
import com.straits_times.support.Log;
import com.straits_times.support.StopWatch;
import com.straits_times.support.Utils;
import com.straits_times.support.WebDriverFactory;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

/**
 * Util class consists wait for page load,page load with user defined max time
 * and is used globally in all classes and methods
 * 
 */
public class Utils {
	private static EnvironmentPropertiesReader configProperty = EnvironmentPropertiesReader.getInstance();
	public static int maxElementWait = 3;
	static String xpathSpinner = "//UIAApplication[1]/UIAWindow[1]/UIAActivityIndicator[1]";
	private static By allSpinners = By.cssSelector(xpathSpinner);
	public static ExpectedCondition<Boolean> appPageLoad;
	
	
	static {
		appPageLoad = new ExpectedCondition<Boolean>() {
			public final Boolean apply(final WebDriver driver) {
				List<WebElement> spinners = driver.findElements(allSpinners);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				// To wait click events to trigger
				try {
					Thread.sleep(250);
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				spinners = driver.findElements(allSpinners);
				for (WebElement spinner : spinners) {
					try {
						if (spinner.isDisplayed()) {
							return false;
						}
					} catch (NoSuchElementException ex) {
						ex.printStackTrace();
					}
				}
				return true;
			}
		};
	}
	
	
	/**
	 * waitForPageLoad waits for the page load with default page load wait time
	 * 
	 * @param driver
	 *            : Webdriver
	 */
	public static void waitForPageLoad(final WebDriver driver) {
		waitForPageLoad(driver, WebDriverFactory.maxPageLoadWait);
	}

	

	/**
	 * waitForPageLoad waits for the page load with custom page load wait time
	 * 
	 * @param driver
	 *            : Webdriver
	 * @param maxWait
	 *            : Max wait duration
	 */
	public static void waitForPageLoad(final WebDriver driver, int maxWait) {
		long startTime = StopWatch.startTime();
		FluentWait<WebDriver> wait = new WebDriverWait(driver, maxWait).pollingEvery(500, TimeUnit.MILLISECONDS)
				.ignoring(StaleElementReferenceException.class).withMessage("Page Load Timed Out");
		try {

			if (configProperty.getProperty("documentLoad").equalsIgnoreCase("true"))
				wait.until(WebDriverFactory.documentLoad);

			if (configProperty.getProperty("imageLoad").equalsIgnoreCase("true"))
				wait.until(WebDriverFactory.imagesLoad);

			if (configProperty.getProperty("framesLoad").equalsIgnoreCase("true"))
				wait.until(WebDriverFactory.framesLoad);

			String title = driver.getTitle().toLowerCase();
			String url = driver.getCurrentUrl().toLowerCase();
			String pageSource = driver.findElement(By.cssSelector("body")).getText().toLowerCase();
			Log.event("Page URL:: " + url);

			if(pageSource.contains("this site can’t be reached")){
				driver.navigate().refresh();
				try{
					Alert alert = driver.switchTo().alert();
					System.out.println(alert.getText());
					alert.accept();
				}
				catch(Exception e){
					Log.event("Error :: " + e);
				}
			}
			
			if ("the page cannot be found".equalsIgnoreCase(title) || title.contains("is not available")
					|| url.contains("/error/") || url.toLowerCase().contains("/errorpage/")) {
				Assert.fail("Site is down. [Title: " + title + ", URL:" + url + "]");
			}
			
		} catch (TimeoutException e) {
			driver.navigate().refresh();
			wait.until(WebDriverFactory.documentLoad);
			wait.until(WebDriverFactory.imagesLoad);
			wait.until(WebDriverFactory.framesLoad);
		}
		Log.event("Page Load Wait: (Sync)", StopWatch.elapsedTime(startTime));

	} // waitForPageLoad
	


	/**
	 * To get the test orientation
	 * 
	 * <p>
	 * if test run on sauce lab device return landscape or portrait or valid
	 * message, otherwise check local device execution and return landscape or
	 * portrait or valid message
	 * 
	 * @return dataToBeReturned - portrait or landscape or valid message
	 */
	public static String getTestOrientation() {
		String dataToBeReturned = null;
		boolean checkExecutionOnSauce = false;
		boolean checkDeviceExecution = false;
		checkExecutionOnSauce = (System.getProperty("SELENIUM_DRIVER") != null
				|| System.getenv("SELENIUM_DRIVER") != null) ? true : false;

		if (checkExecutionOnSauce) {
			checkDeviceExecution = ((System.getProperty("runUserAgentDeviceTest") != null)
					&& (System.getProperty("runUserAgentDeviceTest").equalsIgnoreCase("true"))) ? true : false;
			if (checkDeviceExecution) {
				dataToBeReturned = (System.getProperty("deviceOrientation") != null)
						? System.getProperty("deviceOrientation") : "no sauce run system variable: deviceOrientation ";
			} else {
				dataToBeReturned = "sauce browser test: no orientation";
			}
		} else {
			checkDeviceExecution = (configProperty.hasProperty("runUserAgentDeviceTest")
					&& (configProperty.getProperty("runUserAgentDeviceTest").equalsIgnoreCase("true"))) ? true : false;
			if (checkDeviceExecution) {
				dataToBeReturned = configProperty.hasProperty("deviceOrientation")
						? configProperty.getProperty("deviceOrientation")
						: "no local run config variable: deviceOrientation ";
			} else {
				dataToBeReturned = "local browser test: no orientation";
			}
		}
		return dataToBeReturned;
	}

	/**
	 * To wait for the specific element on the page
	 * 
	 * @param driver
	 *            : Webdriver
	 * @param element
	 *            : Webelement to wait for
	 * @return boolean - return true if element is present else return false
	 */
	public static boolean waitForElement(WebDriver driver, WebElement element) {
		return waitForElement(driver, element, maxElementWait);
	}

	/**
	 * To wait for the specific element on the page
	 * 
	 * @param driver
	 *            : Webdriver
	 * @param element
	 *            : Webelement to wait for
	 * @param maxWait
	 *            : Max wait duration
	 * @return boolean - return true if element is present else return false
	 */
	public static boolean waitForElement(WebDriver driver, WebElement element, int maxWait) {
		boolean statusOfElementToBeReturned = false;
		long startTime = StopWatch.startTime();
		WebDriverWait wait = new WebDriverWait(driver, maxWait);
		try {
			WebElement waitElement = wait.until(ExpectedConditions.visibilityOf(element));
			if (waitElement.isDisplayed() && waitElement.isEnabled()) {
				statusOfElementToBeReturned = true;
				Log.event("Element is displayed:: " + element.toString());
			}
		} catch (Exception e) {
			statusOfElementToBeReturned = false;
			Log.event("Unable to find a element after " + StopWatch.elapsedTime(startTime) + " sec ==> "
					+ element.toString());
		}
		return statusOfElementToBeReturned;
	}

	public static WebDriver switchWindows(WebDriver driver, String windowToSwitch, String opt,
			String closeCurrentDriver) throws Exception {

		WebDriver currentWebDriver = driver;
		WebDriver assingedWebDriver = driver;
		boolean windowFound = false;
		ArrayList<String> multipleWindows = new ArrayList<String>(assingedWebDriver.getWindowHandles());

		for (int i = 0; i < multipleWindows.size(); i++) {

			assingedWebDriver.switchTo().window(multipleWindows.get(i));

			if (opt.equals("title")) {
				if (assingedWebDriver.getTitle().trim().equals(windowToSwitch)) {
					windowFound = true;
					break;
				}
			} else if (opt.equals("url")) {
				if (assingedWebDriver.getCurrentUrl().contains(windowToSwitch)) {
					windowFound = true;
					break;
				}
			} // if

		} // for

		if (!windowFound)
			throw new Exception("Window: " + windowToSwitch + ", not found!!");
		else {
			if (closeCurrentDriver.equals("true"))
				currentWebDriver.close();
		}

		return assingedWebDriver;

	}// switchWindows

	/**
	 * Switching between tabs or windows in a browser
	 * 
	 * @param driver
	 *            -
	 */
	public static void switchToNewWindow(WebDriver driver) {
		String winHandle = driver.getWindowHandle();
		for (String index : driver.getWindowHandles()) {
			if (!index.equals(winHandle)) {
				driver.switchTo().window(index);
				break;
			}
		}
		if (!((RemoteWebDriver) driver).getCapabilities().getBrowserName().matches(".*safari.*")) {
			((JavascriptExecutor) driver).executeScript(
					"if(window.screen){window.moveTo(0, 0); window.resizeTo(window.screen.availWidth, window.screen.availHeight);};");
		}
	}
	
	/**
	 * Method to switch to another Tab
	 * @param windowHandle
	 * @param driver
	 */
	public static void switchToWindow(String windowHandle, WebDriver driver) {
			for (String index : driver.getWindowHandles()) {
			if (!index.equals(windowHandle)) {
				driver.switchTo().window(index);
				break;
			}
		}
		if (!((RemoteWebDriver) driver).getCapabilities().getBrowserName().matches(".*safari.*")) {
			((JavascriptExecutor) driver).executeScript(
					"if(window.screen){window.moveTo(0, 0); window.resizeTo(window.screen.availWidth, window.screen.availHeight);};");
		}
	}

	/**
	 * To compare two HashMap values,then print unique list value and print
	 * missed list value
	 * 
	 * @param expectedList
	 *            - expected element list
	 * @param actualList
	 *            - actual element list
	 * @return statusToBeReturned - returns true if both the lists are equal,
	 *         else returns false
	 */
	public static boolean compareTwoHashMap(Map<String, String> expectedList, Map<String, String> actualList) {
		List<String> missedkey = new ArrayList<String>();
		HashMap<String, String> missedvalue = new HashMap<String, String>();
		try {
			for (String k : expectedList.keySet()) {
				if (!(actualList.get(k).equalsIgnoreCase(expectedList.get(k)))) {
					missedvalue.put(k, actualList.get(k));
					Log.failsoft("Missed Values:: Expected List:: " + missedvalue);
					return false;
				}
			}
			for (String y : actualList.keySet()) {
				if (!expectedList.containsKey(y)) {
					missedkey.add(y);
					Log.failsoft("Missed keys:: Actual List:: " + missedkey);
					return false;
				}
			}
		} catch (NullPointerException np) {
			return false;
		}
		return true;
	}
	
	/**
	 * To compare two Linked list HashMap values,then print unique list value and print
	 * missed list value
	 * 
	 * @param expectedList
	 *            - expected element list
	 * @param actualList
	 *            - actual element list
	 * @return returns true if both the lists are equal,
	 *         else returns false
	 */
	
	public static boolean compareTwoLinkedListHashMap(LinkedList<LinkedHashMap<String, String>> expectedList, LinkedList<LinkedHashMap<String, String>> actualList, String[]... noNeed) {
		int size = expectedList.size();
		if(noNeed.length > 0){
			for(int i=0; i<noNeed.length;i++){
				System.out.println("Removed Key from Expected:: " + noNeed[i][i]);
				expectedList.remove(noNeed[i][i]);
				System.out.println("Removed Key from Actual  :: " + noNeed[i][i]);
				actualList.remove(noNeed[i][i]);
			}
		}
		boolean flag = true;
		try {
			for(int i = 0 ; i < size ; i++){
				if(!Utils.compareTwoHashMap(expectedList.get(i), actualList.get(i)))
					flag = false;
			}
		} catch (NullPointerException np) {
			return false;
		}
		return flag;
	}
	

	/**
	 * To compare two array list values,then print unique list value and print
	 * missed list value
	 * 
	 * @param expectedElements
	 *            - expected element list
	 * @param actualElements
	 *            - actual element list
	 * @return statusToBeReturned - returns true if both the lists are equal,
	 *         else returns false
	 */
	public static boolean compareTwoList(List<String> expectedElements, List<String> actualElements) {
		boolean statusToBeReturned = false;
		List<String> uniqueList = new ArrayList<String>();
		List<String> missedList = new ArrayList<String>();
		for (String item : expectedElements) {
			if (actualElements.contains(item)) {
				uniqueList.add(item);
			} else {
				missedList.add(item);
			}
		}
		/*Collections.sort(expectedElements);
		Collections.sort(actualElements);*/
		if (expectedElements.equals(actualElements)) {
			Log.event("All elements checked on this page:: " + uniqueList);
			statusToBeReturned = true;
		} else {
			Log.failsoft("Missing element on this page:: " + missedList);
			statusToBeReturned = false;
		}
		return statusToBeReturned;
	}
	
	public static boolean compareTwoList1(List<String> expectedElements, List<String> actualElements) {
		boolean statusToBeReturned = false;
		List<String> missedList = new ArrayList<String>();
		for(int i = 0 ; i < expectedElements.size(); i++){
			if(!(expectedElements.get(i).equals(actualElements.get(i)))){
				statusToBeReturned = false;
				missedList.add(expectedElements.get(i));
			}
		}
		Log.failsoft("Missing element on this page:: " + missedList);
		return statusToBeReturned;
	}
	
	/**
	 * To sort LinkedList of Product
	 * @param actualList
	 * @return
	 * @throws Exception
	 */
	public static LinkedList<LinkedHashMap<String, String>> sortLinkedListProduct(LinkedList<LinkedHashMap<String, String>> actualList)throws Exception{
		LinkedList<LinkedHashMap<String, String>> listToReturn = new LinkedList<LinkedHashMap<String, String>>();

		actualList = makeUnique(actualList);

		LinkedList<String> list = new LinkedList<String>();
		int size = actualList.size();

		for(int x = 0; x < size; x++)
			list.add(actualList.get(x).get("ProductName"));

		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Collator.getInstance().compare(o1, o2);
			}
		});

		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(list.get(i).equals(actualList.get(j).get("ProductName"))){
					listToReturn.add(actualList.get(j));
				}
			}
		}

		//printing sorted list
		/*System.out.println("-----------------------------------------");
		for(int y = 0 ;y < size; y++)
			System.out.println(list.get(y));
		System.out.println("-----------------------------------------");*/
		return listToReturn;
	}

	
	/**
	 * To sort LinkedList of address
	 * @param actualList
	 * @return
	 * @throws Exception
	 */
	public static LinkedList<LinkedHashMap<String, String>> sortLinkedListAddress(LinkedList<LinkedHashMap<String, String>> actualList)throws Exception{
		LinkedList<LinkedHashMap<String, String>> listToReturn = new LinkedList<LinkedHashMap<String, String>>();
		actualList = makeUniqueAddress(actualList);
		LinkedList<String> list = new LinkedList<String>();
		int size = actualList.size();
		for(int x = 0; x < size; x++)
			list.add(actualList.get(x).get("FirstName"));

		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Collator.getInstance().compare(o1, o2);
			}
		});

		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				if(list.get(i).equals(actualList.get(j).get("FirstName"))){
					LinkedHashMap<String, String> listToAdd = new LinkedHashMap<String, String>(actualList.get(i));
					listToReturn.add(listToAdd);
					continue;
				}
			}
		}

		/*//printing sorted list
		System.out.println("-----------------------------------------");
		for(int y = 0 ;y < size; y++)
			System.out.println(list.get(y));
		System.out.println("-----------------------------------------");*/
		return listToReturn;
	}

	/**
	 * To make the linked list of linkedHash map unique
	 * @param hashMap
	 * @return
	 * @throws Exception
	 * 
	 * created by Dhanapal.K
	 */
	public static LinkedList<LinkedHashMap<String, String>> makeUnique(LinkedList<LinkedHashMap<String, String>> hashMap)throws Exception{
		int nosProduct = hashMap.size();
		for(int i = 0; i < nosProduct; i++){
			for(int j = i+1 ; j < nosProduct; j++){
				if(hashMap.get(i).get("ProductName").equals(hashMap.get(j).get("ProductName")))
					if(hashMap.get(i).get("Color").equals(hashMap.get(j).get("Color")))
						if(hashMap.get(i).get("Size").equals(hashMap.get(j).get("Size"))){
							int qty = Integer.parseInt(hashMap.get(i).get("Quantity")) + Integer.parseInt(hashMap.get(j).get("Quantity"));
							hashMap.get(i).put("Quantity", Integer.toString(qty));
							hashMap.remove(j);
							nosProduct = hashMap.size();
							j--;
						}
			}
		}
		
		return hashMap;
	}

	public static LinkedList<LinkedHashMap<String, String>> makeUniqueAddress(LinkedList<LinkedHashMap<String, String>> hashMap)throws Exception{
		int nosProduct = hashMap.size();
		for(int i = 0; i < nosProduct; i++){
			for(int j = i+1 ; j < nosProduct; j++){
				System.out.print("i value " + hashMap.get(i).get("FirstName"));
				System.out.print("j value " + hashMap.get(j).get("FirstName"));
				if(hashMap.get(i).get("FirstName").equals(hashMap.get(j).get("FirstName")))
					if(hashMap.get(i).get("Address").equals(hashMap.get(j).get("Address"))){
							hashMap.remove(j);
							nosProduct = hashMap.size();
							j--;
							
					}
			}
		}
		
		return hashMap;
	}
	
	/**
	 * Verify the css property for an element
	 * 
	 * @param element
	 *            - WebElement for which to verify the css property
	 * @param cssProperty
	 *            - the css property name to verify
	 * @param actualValue
	 *            - the actual css value of the element
	 * @return boolean
	 */
	public static boolean verifyCssPropertyForElement(WebElement element, String cssProperty, String actualValue) {
		boolean result = false;

		String actualClassProperty = element.getCssValue(cssProperty);

		if (actualClassProperty.contains(actualValue)) {
			result = true;
		}
		return result;
	}

	/**
	 * To get the value of an input field.
	 * 
	 * @param element
	 *            - the input field you need the value/text of
	 * @param driver
	 *            -
	 * @return text of the input's value
	 */
	public static String getValueOfInputField(WebElement element, WebDriver driver) {
		String sDataToBeReturned = null;
		if (Utils.waitForElement(driver, element)) {
			sDataToBeReturned = element.getAttribute("value");
		}
		return sDataToBeReturned;
	}

	/**
	 * To wait for the specific element which is in disabled state on the page
	 * 
	 * @param driver
	 *            - current driver object
	 * @param element
	 *            - disabled webelement
	 * @param maxWait
	 *            - duration of wait in seconds
	 * @return boolean - return true if disabled element is present else return
	 *         false
	 */
	public static boolean waitForDisabledElement(WebDriver driver, WebElement element, int maxWait) {
		boolean statusOfElementToBeReturned = false;
		long startTime = StopWatch.startTime();
		WebDriverWait wait = new WebDriverWait(driver, maxWait);
		try {
			WebElement waitElement = wait.until(ExpectedConditions.visibilityOf(element));
			if (!waitElement.isEnabled()) {
				statusOfElementToBeReturned = true;
				Log.event("Element is displayed and disabled:: " + element.toString());
			}
		} catch (Exception ex) {
			statusOfElementToBeReturned = false;
			Log.event("Unable to find disabled element after " + StopWatch.elapsedTime(startTime) + " sec ==> "
					+ element.toString());
		}
		return statusOfElementToBeReturned;
	}

	/**
	 * Wait until element disappears in the page
	 * 
	 * @param driver
	 *            - driver instance
	 * @param element
	 *            - webelement to wait to have disaapear
	 * @return true if element is not appearing in the page
	 */
	public static boolean waitUntilElementDisappear(WebDriver driver, final WebElement element) {
		final boolean isNotDisplayed;

		WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, 60);
		isNotDisplayed = wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver webDriver) {
				boolean isPresent = false;
				try {
					if (element.isDisplayed()) {
						isPresent = false;
						Log.event("Element " + element.toString() + ", is still visible in page");
					}
				} catch (Exception ex) {
					isPresent = true;
					Log.event("Element " + element.toString() + ", is not displayed in page ");
					return isPresent;
				}
				return isPresent;
			}
		});
		return isNotDisplayed;
	}
	
	/**
	 * Wait until element disappears in the page
	 * 
	 * @param driver
	 *            - driver instance
	 * @param element
	 *            - webelement to wait to have disaapear
	 * @return true if element is not appearing in the page
	 */
	public static boolean waitUntilElementDisappear(WebDriver driver, final WebElement element, int maxWait) {
		final boolean isNotDisplayed;

		WebDriverWait wait = (WebDriverWait) new WebDriverWait(driver, maxWait);
		isNotDisplayed = wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver webDriver) {
				boolean isPresent = false;
				try {
					if (element.isDisplayed()) {
						isPresent = false;
						Log.event("Element " + element.toString() + ", is still visible in page");
					}
				} catch (Exception ex) {
					isPresent = true;
					Log.event("Element " + element.toString() + ", is not displayed in page ");
					return isPresent;
				}
				return isPresent;
			}
		});
		return isNotDisplayed;
	}

	/**
	 * To get run platform from the config.Property files
	 * 
	 * @return String - return mobile/desktop value
	 */
	public static String getRunPlatForm() {
		String dataToBeReturned = null;
				
		if ((configProperty.hasProperty("runMobile")
				&& configProperty.getProperty("runMobile").equalsIgnoreCase("true"))
				|| (configProperty.hasProperty("runUserAgentDeviceTest")
						&& configProperty.getProperty("runUserAgentDeviceTest").equalsIgnoreCase("true"))|| (System.getenv("runUserAgentDeviceTest") != null
								&& System.getenv("runUserAgentDeviceTest").equalsIgnoreCase("true")) ) {
			
			String device = ( System.getenv("deviceName") != null)?  System.getenv("deviceName"): configProperty.hasProperty("deviceName")? configProperty.getProperty("deviceName"): null ;
					
			dataToBeReturned = device.endsWith("mobile")? "mobile" : device.endsWith("tablet")? "tablet": "desktop";
		} else {
			dataToBeReturned = "desktop";
		}
		Log.event("Running platform type:: " + dataToBeReturned);
		return dataToBeReturned;
	}// getRunPlatForm

	/**
	 * To generate random number from '0 to Maximum Value' or 'Minimum Value ---- Maximum Value'
	 * @param max - maximum bound
	 * @param min - origin bound
	 * @return - random number between 'min to max' or '0 to max'
	 * @throws Exception
	 */
	public static int getRandom(int min, int max)throws Exception{
		Random random = new Random();
		int rand;
		if(min == 0)
			rand = random.nextInt(max);
		else
			rand = ThreadLocalRandom.current().nextInt(min, max);
				
		return rand;
	}
	
	/**
	 * To verify the page url contains the given word
	 * @param driver
	 * @param stringContains
	 * @return boolean
	 */
	public static boolean verifyPageURLContains(final WebDriver driver, String hostURL, String stringContains) {
		boolean status = false;
		String url = null;
		try {
			url = driver.getCurrentUrl();
			if(url==null) {
				url = ((JavascriptExecutor) driver).executeScript("return document.URL;").toString();
			}
		} catch (Exception e) {
			url = ((JavascriptExecutor) driver).executeScript("return document.URL;").toString();
			// TODO: handle exception
		}
		if(url.contains("production")) {
			if(url.contains(hostURL.split("https://storefront:dcp-preview@")[1]) && url.contains(stringContains)) {
				status = true;
			}
		} else if(url.contains(hostURL.split("https://")[1]) && url.contains(stringContains)) {
			status = true;
		}
		
		return status;
	}
	
	 /**
     * Round to certain number of decimals
     * 
     * @param d
     * @param decimalPlace the numbers of decimals
     * @return
     */
    public static float round(double d, int decimalPlace) {
         return BigDecimal.valueOf(d).setScale(decimalPlace,BigDecimal.ROUND_HALF_UP).floatValue();
    }
    
    /**
     * checkPriceWithDollar
     * @param price
     * @return
     */
    public static String checkPriceWithDollar(String price){
    	
    	if(!price.startsWith("$")){
    		Log.failsoft( price + " does not contains '$' value");
    	}
    	
    	return price;
    }
    
    /**
     * copyHashMap
     * @param actual
     * @param ignore
     * @return
     * @throws Exception
     */
    public static LinkedHashMap<String, String> copyHashMap(LinkedHashMap<String, String> actual, String ignore)throws Exception{
		List<String> indexes = new ArrayList<String>(actual.keySet());
		LinkedHashMap<String, String> expected = new LinkedHashMap<String, String>();
		
		for(int i = 0; i < indexes.size(); i++){
			if(!indexes.get(i).equals(ignore))
				expected.put(indexes.get(i), actual.get(indexes.get(i)));
		}
	
		return expected;
	}
	
    /**
     * copyLinkedListHashMap
     * @param actual
     * @param ignore
     * @return
     * @throws Exception
     */
	public static LinkedList<LinkedHashMap<String, String>> copyLinkedListHashMap(LinkedList<LinkedHashMap<String, String>> actual, String ignore)throws Exception{
		int size = actual.size();
		LinkedList<LinkedHashMap<String, String>> expected = new LinkedList<LinkedHashMap<String, String>>();
		for(int j = 0; j < size; j++){
			List<String> indexes = new ArrayList<String>(actual.get(j).keySet());
			LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();
			for(int i = 0; i < indexes.size(); i++){
				if(!indexes.get(i).equals(ignore))
					hashMap.put(indexes.get(i), actual.get(j).get(indexes.get(i)));
			}
			expected.add(hashMap);
		}
		return expected;
	}

	
public static boolean sortCompartPrintPaymentDetails(LinkedHashMap<String, LinkedHashMap<String, String>> paymentDetails1, LinkedHashMap<String, LinkedHashMap<String, String>> paymentDetails2, String[]... ignore)throws Exception{
		boolean flag = true;
		List<String> outterIndexOfFirst = new ArrayList<String>(paymentDetails1.keySet());
		List<String> outterIndexOfSecond = new ArrayList<String>(paymentDetails2.keySet());
		
		if(outterIndexOfFirst.toString().contains("GiftCard")){
			LinkedList<LinkedHashMap<String, String>> giftCardDetailsInFirst = new LinkedList<LinkedHashMap<String, String>>();
			LinkedList<LinkedHashMap<String, String>> giftCardDetailsInSecond = new LinkedList<LinkedHashMap<String, String>>();
			
			for(int i = 0; i < outterIndexOfFirst.size(); i++){
				if(outterIndexOfFirst.get(i).contains("GiftCard")){
					giftCardDetailsInFirst.add(paymentDetails1.get(outterIndexOfFirst.get(i)));
				}
				
				if(outterIndexOfSecond.get(i).contains("GiftCard")){
					giftCardDetailsInSecond.add(paymentDetails2.get(outterIndexOfSecond.get(i)));
				}
			}
			
			flag = compareTwoLinkedListHashMap(giftCardDetailsInFirst, giftCardDetailsInSecond, ignore);
			
		}

		if(outterIndexOfFirst.toString().contains("BRD")){
			LinkedList<LinkedHashMap<String, String>> giftCardDetailsInFirst = new LinkedList<LinkedHashMap<String, String>>();
			LinkedList<LinkedHashMap<String, String>> giftCardDetailsInSecond = new LinkedList<LinkedHashMap<String, String>>();
			
			for(int i = 0; i < outterIndexOfFirst.size(); i++){
				if(outterIndexOfFirst.get(i).contains("BRD")){
					giftCardDetailsInFirst.add(paymentDetails1.get(outterIndexOfFirst.get(i)));
				}
				
				if(outterIndexOfSecond.get(i).contains("BRD")){
					giftCardDetailsInSecond.add(paymentDetails2.get(outterIndexOfSecond.get(i)));
				}
			}
			
			flag = compareTwoLinkedListHashMap(giftCardDetailsInFirst, giftCardDetailsInSecond);
		}

		if(outterIndexOfFirst.toString().contains("CreditCard")){
			LinkedList<LinkedHashMap<String, String>> giftCardDetailsInFirst = new LinkedList<LinkedHashMap<String, String>>();
			LinkedList<LinkedHashMap<String, String>> giftCardDetailsInSecond = new LinkedList<LinkedHashMap<String, String>>();
			
			for(int i = 0; i < outterIndexOfFirst.size(); i++){
				if(outterIndexOfFirst.get(i).contains("CreditCard")){
					giftCardDetailsInFirst.add(paymentDetails1.get(outterIndexOfFirst.get(i)));
				}
				
				if(outterIndexOfSecond.get(i).contains("CreditCard")){
					giftCardDetailsInSecond.add(paymentDetails2.get(outterIndexOfSecond.get(i)));
				}
			}
			
			flag = compareTwoLinkedListHashMap(giftCardDetailsInFirst, giftCardDetailsInSecond);
		}
		
		return flag;
	}
}
