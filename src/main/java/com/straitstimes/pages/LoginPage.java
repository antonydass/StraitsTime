package com.straitstimes.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.testng.Assert;

import com.straits_times.support.BrowserActions;
import com.straits_times.support.Log;
import com.straits_times.support.Utils;


public class LoginPage extends LoadableComponent<LoginPage>{
	private String appURL;
	private WebDriver driver;
	private boolean isPageLoaded;

	@FindBy(css="li[class='nav-login']")
	WebElement login_btn;
	
	@FindBy(css="button[type='submit']")
	WebElement submitBtn;
	
	@FindBy(id="j_username")
	WebElement username_Txt;
	
	@FindBy(id="j_password")
	WebElement password_Txt;
	
	@Override
	protected void isLoaded() throws Error {
		// TODO Auto-generated method stub
		
		if (!isPageLoaded) {
			Assert.fail();
		}

		if (isPageLoaded && !(Utils.waitForElement(driver, login_btn))) {
			Log.fail("Login page did not open up.", driver);
		}

		
	}

	@Override
	protected void load() {
		// TODO Auto-generated method stub
		
		isPageLoaded = true;
		driver.get(appURL);
		Utils.waitForPageLoad(driver);
		
	}

	public LoginPage(WebDriver driver, String url) {
		appURL = url;
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
		PageFactory.initElements(finder, this);
	}
	

	public void clickOnLoginButton()throws Exception{
		BrowserActions.clickOnElement(login_btn, driver, "click login button");
	}
	
	public HomePage navigate_HomePage_After_signed(String username, String pwd) throws Exception{
		BrowserActions.typeOnTextField(username_Txt, username, driver, "Entered user name");
		BrowserActions.typeOnTextField(password_Txt, pwd, driver, "Entered pwd");
		BrowserActions.clickOnElement(submitBtn, driver, "click submit button");
		
		return new HomePage(driver);
	}
}
