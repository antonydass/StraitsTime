package com.straitstimes.app.pages;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.LoadableComponent;

import com.straits_times.support.AppActions;
import com.straits_times.support.BrowserActions;
import com.straits_times.support.Log;


import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import junit.framework.Assert;

public class MenuPage extends LoadableComponent<MenuPage>{

	AppiumDriver<MobileElement> driver;
	private boolean isPageLoaded;
	
	@AndroidFindBy (xpath = "//android.widget.TextView[@text='LOGIN']")
	MobileElement BtnLogin; 
	
	@AndroidFindBy(id="login-user-name")
	MobileElement txt_UserName;
	
	@AndroidFindBy(id="Menu bar") 
	MobileElement Btn_menu_bar;

	@Override
	protected void isLoaded() throws Error {
		// TODO Auto-generated method stub
		
		if (!isPageLoaded) {
			Assert.fail();
		}
		
		if (!BtnLogin.isDisplayed()) 
			Log.fail("Menu Screen did not open up. App might be down.", driver);
		
	}

	@Override
	protected void load() {
		// TODO Auto-generated method stub
		isPageLoaded = true;
		
	}
	
	public MenuPage(AppiumDriver<MobileElement> driver) {
		this.driver = driver;
		PageFactory.initElements(new AppiumFieldDecorator(driver), this);
		// TODO Auto-generated constructor stub
	}
	
	
	public AppLoginPage navigatetoLoginPage() throws Exception{
		AppActions.clickOnElement(BtnLogin, driver, "clicked login button");
		return new AppLoginPage(driver);
	}
	
	public String getUserText_App() throws Exception{
		return BrowserActions.getText(driver, txt_UserName, "getting user name after login from the application");
	}
	
	public AppHomePage navigateToHomePage() throws Exception{
		AppActions.clickOnElement(Btn_menu_bar, driver, "clicked Menu button");
		return new AppHomePage(driver);
	}
	

}
