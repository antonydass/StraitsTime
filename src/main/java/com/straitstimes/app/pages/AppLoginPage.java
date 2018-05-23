package com.straitstimes.app.pages;

import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.LoadableComponent;

import com.straits_times.support.AppActions;
import com.straits_times.support.Log;
import com.straitstimes.pages.HomePage;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import junit.framework.Assert;

public class AppLoginPage extends LoadableComponent<AppLoginPage>{
	
	AppiumDriver<MobileElement> driver;
	private boolean isPageLoaded;

	@AndroidFindBy(id = "activity_login_emailText")
	MobileElement userName_txtField;
	
	@AndroidFindBy(xpath = "activity_login_password_editText")
	MobileElement Password_txtField;
	
	@AndroidFindBy(xpath = "//android.widget.TextView[@text='Continue']")
	MobileElement BtnContinue;
	
	public AppLoginPage(AppiumDriver<MobileElement> driver) {
		// TODO Auto-generated constructor stub
		this.driver = driver;
		PageFactory.initElements(new AppiumFieldDecorator(driver), this);
	}



	@Override
	protected void isLoaded() throws Error {
		// TODO Auto-generated method stub
		
		if (!isPageLoaded) {
			Assert.fail();
		}
		
		if (!BtnContinue.isDisplayed()) 
			Log.fail("Login Screen did not open up. App might be down.", driver);
		
	}



	@Override
	protected void load() {
		// TODO Auto-generated method stub
		isPageLoaded = true;		
	}
	
	
	public AppHomePage navigate_To_HomePage_After_signin(String userName, String Password) throws Exception{
		AppActions.typeOnTextField(userName_txtField, userName, driver, "username");
		AppActions.typeOnTextField(Password_txtField, Password, driver, "Password");
		AppActions.clickOnElement(BtnContinue, driver, "clicked continue  button");
		
		return new AppHomePage(driver);
	}

}
