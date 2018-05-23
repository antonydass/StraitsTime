package com.straitstimes.app.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.LoadableComponent;

import com.straits_times.support.AppActions;
import com.straits_times.support.Log;


import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import junit.framework.Assert;

public class AppHomePage extends LoadableComponent<AppHomePage>{
	
	AppiumDriver<MobileElement> driver;
	private boolean isPageLoaded;
	
	@AndroidFindBy(id="I Agree")
	MobileElement agree_button;
	
	@AndroidFindBy(xpath = "")
	MobileElement pager_count;
	
	
	@AndroidFindBy(id="Get started") 
	MobileElement Btn_Get_Start;
	
	@AndroidFindBy(id="Menu bar") 
	MobileElement Btn_menu_bar;
	
	@AndroidFindBy (xpath = "//android.widget.TextView[@text='HOME']")
	MobileElement BtnHome;
	
	@AndroidFindBy (xpath = "//android.widget.TextView[@text='LATEST']")
	MobileElement BtnLatest;
	
	@Override
	protected void isLoaded() throws Error {
		// TODO Auto-generated method stub
		
		if (!isPageLoaded) {
			Assert.fail();
		}

		
		try {
			if(agree_button.isDisplayed()){
				AppActions.clickOnElement(agree_button, driver, "Get Started");
			}
		} catch (Exception e) {
			//  =TODO: handle exception
		}
		
		
		if (!Btn_Get_Start.isDisplayed()  || !BtnHome.isDisplayed()) 
			Log.fail("Home Screen did not open up. App might be down.", driver);
	}
	@Override
	protected void load() {
		// TODO Auto-generated method stub
		isPageLoaded = true;

	}
	
	public AppHomePage(AppiumDriver<MobileElement> driver) {
		this.driver = driver;
		PageFactory.initElements(new AppiumFieldDecorator(driver), this);	
	}
	
	public void click_On_GetStarted() throws Exception{
		AppActions.clickOnElement(Btn_Get_Start, driver, "Get Started");
	}
	
	public MenuPage navigate_Menu_Screen() throws Exception{
		AppActions.clickOnElement(Btn_menu_bar, driver, "menu bar");
		return new MenuPage(driver);
		
	}
	
	public void clickLatest_tab() throws Exception{
		AppActions.clickOnElement(BtnLatest, driver, "clicked latest tab ");
	}

}
