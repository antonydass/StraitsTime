package com.straits_times.support;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;

/**
 * Wrapper for Selenium WebDriver actions which will be performed on browser
 * 
 * Wrappers are provided with exception handling which throws Skip Exception on
 * occurrence of NoSuchElementException
 * 
 */
public class AppActions {

	public static String MOUSE_HOVER_JS = "var evObj = document.createEvent('MouseEvents');"
			+ "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);"
			+ "arguments[0].dispatchEvent(evObj);";

	/**
	 * Wrapper to type a text in browser text field
	 * 
	 * @param txt
	 *            : WebElement of the Text Field
	 * @param txtToType
	 *            : Text to type [String]
	 * @param driver
	 *            : WebDriver Instances
	 * @param elementDescription
	 *            : Description about the WebElement
	 * @throws Exception
	 */
	public static void typeOnTextField(MobileElement txt, String txtToType,
			AppiumDriver<MobileElement> driver, String elementDescription) throws Exception {

		if (!AppUtils.waitForElementVisible(driver, txt))
			throw new Exception(elementDescription
					+ " field not found in page!!");

		try {
			txt.clear();
			txt.click();
			txt.sendKeys(txtToType);
//			txt.setValue(txtToType);
		} catch (NoSuchElementException e) {
			throw new Exception(elementDescription
					+ " field not found in page!!");

		}

	}// typeOnTextField

	/**
	 * Scroll to view the particular element
	 * @param driver
	 * @param element
	 */
	public static void scrollToViewElement(AppiumDriver<MobileElement> driver, WebElement element){
		try {
			driver.hideKeyboard();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		Dimension size=driver.manage().window().getSize();
		try {
			if(element.isDisplayed()){
				Log.event("element is displayed");
			}
		} catch (Exception e) {
			again:	for(int i=0;i<=5;i++){
				int y_start=(int)(size.height*0.60);
				int y_end=(int)(size.height*0.30);
				int x=size.width/2;
				driver.swipe(x,y_start,x,y_end,4000);
				try {
					if(element.isDisplayed())
						break;
				}
				catch (Exception e2) {
					Log.event("Element not available for this swipe");
					continue again;
				}
			} 
		}

	}
	
	
	/**
	 * Scroll down action
	 */
	
	public static void scrolldown(AppiumDriver<MobileElement> driver){
		Dimension size=driver.manage().window().getSize();
		int y_start=(int)(size.height*0.80);
		int y_end=(int)(size.height*0.20);
		int x=size.width/2;
		driver.swipe(x,y_start,x,y_end,4000);
	}
	
	
	/**
	 * Scroll down action
	 */
	
	public static void scrollDownLittle(AppiumDriver<MobileElement> driver){
		Dimension size=driver.manage().window().getSize();
		int y_start=(int)(size.height*0.50);
		int y_end=(int)(size.height*0.45);
		int x=size.width/2;
		driver.swipe(x,y_start,x,y_end,1000);
	}
	
	/**
	 * Scroll to view the end
	 * @param driver
	 * @param element
	 */
	public static void scrollToEnd(AppiumDriver<MobileElement> driver){
		Dimension size=driver.manage().window().getSize();
		int y_start=(int)(size.height*0.80);
		int y_end=(int)(size.height*0.20);
		int x=size.width/2;
		driver.swipe(x,y_start,x,y_end,4000);
	}
	
	/**
	 * Scroll to view the top
	 * @param driver
	 * @param element
	 */
	public static void scrollToTop(AppiumDriver<MobileElement> driver){
		Dimension size=driver.manage().window().getSize();
		int y_start=(int)(size.height*0.20);
		int y_end=(int)(size.height*0.80);
		int x=size.width/2;
		driver.swipe(x,y_start,x,y_end,4000);
	}

	/**
	 * Wrapper to click on button/text/radio/checkbox in browser
	 * 
	 * @param btn
	 *            : WebElement of the Button Field
	 * @param driver
	 *            : WebDriver Instances
	 * @param elementDescription
	 *            : Description about the WebElement
	 */

	public static int countElements(String xpath, AppiumDriver<MobileElement> driver) {
		return driver.findElements(By.xpath(xpath)).size();
	}

	public static void clickOnElement(MobileElement btn, AppiumDriver<MobileElement> driver,
			String elementDescription) throws Exception {

		if (!Utils.waitForElement(driver, btn, 5))
			throw new Exception(elementDescription + " not found in page!!");

		try {
			btn.click();
		} catch (NoSuchElementException e) {
			throw new Exception(elementDescription + " not found in page!!");
		}

	}// clickOnButton

	/**
	 * Wrapper to get a text from the provided WebElement
	 * 
	 * @param driver
	 *            : WebDriver Instance
	 * @param fromWhichTxtShldExtract
	 *            : WebElement from which text to be extract in String format
	 * @return: String - text from web element
	 * @param elementDescription
	 *            : Description about the WebElement
	 * @throws Exception
	 */
	public static String getText(AppiumDriver<MobileElement> driver,
			MobileElement fromWhichTxtShldExtract, String elementDescription)
					throws Exception {

		String textFromHTMLAttribute = "";

		try {
			textFromHTMLAttribute = fromWhichTxtShldExtract.getText().trim();

			if (textFromHTMLAttribute.isEmpty())
				textFromHTMLAttribute = fromWhichTxtShldExtract
				.getAttribute("textContent");

		} catch (NoSuchElementException e) {
			throw new Exception(elementDescription + " not found in page!!");
		}

		return textFromHTMLAttribute;

	}// getText

	/**
	 * Wrapper to get a text from the provided WebElement's Attribute
	 * 
	 * @param driver
	 *            : WebDriver Instance
	 * @param fromWhichTxtShldExtract
	 *            : WebElement from which text to be extract in String format
	 * @param attributeName
	 *            : Attribute Name from which text should be extracted like
	 *            "style, class, value,..."
	 * @return: String - text from web element
	 * @param elementDescription
	 *            : Description about the WebElement
	 * @throws Exception
	 */
	public static String getTextFromAttribute(AppiumDriver<MobileElement> driver,
			MobileElement fromWhichTxtShldExtract, String attributeName,
			String elementDescription) throws Exception {

		String textFromHTMLAttribute = "";

		try {
			textFromHTMLAttribute = fromWhichTxtShldExtract.getAttribute(
					attributeName).trim();
		} catch (NoSuchElementException e) {
			throw new Exception(elementDescription + " not found in page!!");
		}

		return textFromHTMLAttribute;

	}// getTextFromAttribute

	/**
	 * Select drop down value and doesn't wait for spinner.
	 *
	 * @param elementLocator
	 *            the element locator
	 * @param valueToBeSelected
	 *            the value to be selected
	 * @throws Exception 
	 */
	public static void selectDropDownValue(AppiumDriver<MobileElement> driver,
			MobileElement dropDown, String valueToBeSelected) throws Exception {
		dropDown.click();
		boolean status=true;
		String temp = null;
		while (status){
			if(driver.findElement(By.id("numberpicker_input")).getAttribute("text").toLowerCase().equals(valueToBeSelected.toLowerCase())){
				driver.findElement(By.id("fragment_select_size_continue_button")).click();
				status=false;
				break;
			}else{
				MobileElement element1 = driver.findElement(By.id("numberpicker_input"));
				temp=element1.getAttribute("text");
				try {
					int leftX = element1.getLocation().getX();
					int rightX = leftX + element1.getSize().getWidth();
					int middleX = (rightX + leftX) / 2;
					int upperY = element1.getLocation().getY();
					int lowerY = upperY + element1.getSize().getHeight();
					int middleY = (upperY + lowerY) / 2;
					Double EndY = (middleY*1.6)/2;
					String endY = (EndY.toString()).split("\\.")[0];
					int endYint = Integer.parseInt(endY);
					driver.swipe(middleX, middleY, middleX, endYint, 2500);
					if(driver.findElement(By.id("numberpicker_input")).getAttribute("text").equals(temp)){
						driver.swipe(middleX, middleY, middleX, endYint, 2500);
					}
					if(driver.findElement(By.id("numberpicker_input")).getAttribute("text").equals(temp)){
						driver.swipe(middleX, middleY, middleX, endYint, 2500);
					}
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
			if(driver.findElement(By.id("numberpicker_input")).getAttribute("text").equals(temp)){
				Log.fail("Mentioned state is not available in the State dropdown",driver);
				break;
			}
			
		}
	}
}// BrowserActions