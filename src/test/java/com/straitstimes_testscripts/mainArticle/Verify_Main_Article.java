package com.straitstimes_testscripts.mainArticle;

import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.straits_times.support.DataProviderUtils;
import com.straits_times.support.EnvironmentPropertiesReader;
import com.straits_times.support.Log;
import com.straits_times.support.TestDataExtractor;
import com.straits_times.support.WebDriverFactory;
import com.straitstimes.pages.HomePage;
import com.straitstimes.pages.LoginPage;
import com.straitstimes.pages.MainArticlePage;

import junit.framework.Assert;



public class Verify_Main_Article {
	

	EnvironmentPropertiesReader environmentPropertiesReader;
	String webSite = "";
	public static String workbookName = "testdata\\data\\straits-time.xls";
	public static String sheetName = "Login";
	
	@BeforeTest(alwaysRun = true)
	public void init(ITestContext context) {
		webSite = (System.getProperty("webSite") != null ? System
				.getProperty("webSite") : context.getCurrentXmlTest()
				.getParameter("webSite"));
	}
	
	@Test(groups = { "desktop" }, description = "To Verify Main Article page details", dataProviderClass = DataProviderUtils.class, dataProvider = "parallelTestDataProvider")
	public void TC_Verify_Main_Article(String browser) throws Exception {
		HashMap<String, String> testData = TestDataExtractor.initTestData(workbookName, sheetName);
		// Get the web driver instance
		final WebDriver driver = WebDriverFactory.get(browser);
		Log.testCaseInfo(testData);
		
		String user_Name = testData.get("UserName");
		String password = testData.get("Password");
		
		try{
			LoginPage loginPage = new LoginPage(driver, webSite).get();
			Log.message("1. Navigated to 'Straits times Home Page!", driver);
			
			loginPage.clickOnLoginButton();
			Log.message("2. Clicked Login Button", driver);
			
			HomePage homePage =loginPage.navigate_HomePage_After_signed(user_Name, password);
			Log.message("3. Navigated to 'Straits times Home Page after login to the application!", driver);
			
			System.out.println(" articlename " + homePage.getUserText_in_UI().trim());
			
			Assert.assertEquals(user_Name.trim(), homePage.getUserText_in_UI().trim(), "User name is not matched after login to the application please check the app");
			
			if(homePage.getArticleAttribute().contains("jpeg"))
				Log.message("Article image is displayed");
	
			
			String article_attribute = homePage.getImageSource();
			
			MainArticlePage mainArticlePage= homePage.naviagteToMainArticle();
			
			String current_url = driver.getCurrentUrl();
			
			if(current_url.contains(article_attribute))
				Assert.assertTrue("Successfully Navigate to Currenturl", true);
			else
				Assert.fail("Main article is not opened Please check the application");
		}catch(Exception e){
			Log.exception(e, driver);
		}finally {
			Log.endTestCase();
			driver.quit();
		}
		
	}
		
		
}
