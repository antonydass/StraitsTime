package com.straitstimes.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.testng.Assert;

import com.straits_times.support.BrowserActions;
import com.straits_times.support.Log;
import com.straits_times.support.Utils;

public class HomePage extends LoadableComponent<HomePage>{
	
	
	private WebDriver driver;
	private boolean isPageLoaded;
	
	@FindBy(xpath="//li[contains(@class,'nav-singapore')]/a[contains(@href,'singapore')]")
	WebElement singapore_header;
	
	@FindBy(css="li[class='nav-login']")
	WebElement login_btn;
	
	@FindBy(css="a[class='name navbar-brand']")
	WebElement straits_times_txt;
	
	@FindBy (css = "a[name='login-user-name']")
	WebElement txt_UserName;
	 
	//@FindBy(id="close-ad-button")
	//WebElement close_add_Btn;
	
	@FindBy(xpath = "//div[@class='media-group fadecount1']//div[contains(@class,'file-image')]")
	WebElement article_img;
	
	@FindBy (xpath = "//div[contains(@class,'ae9e4dab5d501b3cf2fbf6020decbcd8')]//a[@class='block-link']")
	WebElement article_source;
	
	String close_add_Btn = "close-ad-button";

	@Override
	protected void isLoaded() throws Error {
		if (!isPageLoaded) {
			Assert.fail();
		}
		
		Utils.waitForPageLoad(driver);
		
		
		if (isPageLoaded && !(Utils.waitForElement(driver, straits_times_txt))) {
			Log.fail("Home Page did not open up. Site might be down.", driver);
		}
		
		
		
	}

	@Override
	protected void load() {
		// TODO Auto-generated method stub
		isPageLoaded = true;
		Utils.waitForPageLoad(driver);
		
	}

	public HomePage(WebDriver driver) {
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
		PageFactory.initElements(finder, this);
	}
	
	
	public String getUserText_in_UI() throws Exception{
		
		return BrowserActions.getText(driver, txt_UserName, "getting user name after login from the application");
	}
	
	
	public String getArticleAttribute()throws Exception{
		return BrowserActions.getTextFromAttribute(driver, article_img, "class", "articel attribute");
	}
	
	
	
	public String getImageSource() throws Exception{
		return BrowserActions.getTextFromAttribute(driver, article_source, "href", "article img source");
	}
	
	public MainArticlePage naviagteToMainArticle()throws Exception{
		BrowserActions.clickOnElement(article_img, driver, "clicked img");
		return new MainArticlePage(driver).get();
	
		
	}
	
}
