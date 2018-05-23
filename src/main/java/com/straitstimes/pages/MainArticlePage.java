package com.straitstimes.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.testng.Assert;

import com.straits_times.support.Log;
import com.straits_times.support.Utils;

public class MainArticlePage extends LoadableComponent<MainArticlePage>{

	private WebDriver driver;
	private boolean isPageLoaded;

	@FindBy (xpath = "//figure[@itemprop='image']//img")
	WebElement mainArticle_img;

	@Override
	protected void isLoaded() throws Error {
		// TODO Auto-generated method stub
		
		if (!isPageLoaded) {
			Assert.fail();
		}
		
		if (isPageLoaded && !(Utils.waitForElement(driver, mainArticle_img))) {
			Log.fail("Home Page did not open up. Site might be down.", driver);
		}
		
	}
	
	@Override
	protected void load() {
		// TODO Auto-generated method stub
		isPageLoaded = true;
		Utils.waitForPageLoad(driver);
		
	}
	
	
	public MainArticlePage(WebDriver driver) {
		// TODO Auto-generated constructor stub
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
		PageFactory.initElements(finder, this);
	}
	
	public String getMainArticle_URL(WebDriver driver){
		return driver.getCurrentUrl();
	}
}
