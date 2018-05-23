package com.straitstime_app_testscripts.mainArticle;

import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.straits_times.support.AppiumDriverFactory;
import com.straits_times.support.DataProviderUtils;
import com.straits_times.support.EnvironmentPropertiesReader;
import com.straits_times.support.Log;
import com.straits_times.support.TestDataExtractor;
import com.straits_times.support.WebDriverFactory;
import com.straitstimes.app.pages.AppHomePage;
import com.straitstimes.app.pages.AppLoginPage;
import com.straitstimes.app.pages.MenuPage;
import com.straitstimes.pages.HomePage;
import com.straitstimes.pages.LoginPage;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import junit.framework.Assert;

public class Verify_MainArticle {
	
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
	
	@Test(groups = { "Mobile" }, description = "To verify the Latest Tab article Details", dataProviderClass = DataProviderUtils.class, dataProvider = "parallelTestDataProvider")
	public void TC_Verify_Main_Article(String browser) throws Exception {
		HashMap<String, String> testData = TestDataExtractor.initTestData(workbookName, sheetName);
		
		AppiumDriver<MobileElement> driver = AppiumDriverFactory.get();
		Log.testCaseInfo(testData);

		Log.testCaseInfo(testData);
		
		String user_Name = testData.get("UserName");
		String password = testData.get("Password");
		
		
		try{
			
			AppHomePage appHomePage = new AppHomePage(driver);
			Log.message("1.Successfully launched app");
			
			appHomePage.click_On_GetStarted();
			Log.message("2.Successfully Naviagte to app home page");
			
			MenuPage menuPage = appHomePage.navigate_Menu_Screen();
			Log.message("3.Successfully opened menu in the app");
			
			AppLoginPage loginPage = menuPage.navigatetoLoginPage();
			Log.message("4.Successfully Naviagte to login page");
			
			appHomePage = loginPage.navigate_To_HomePage_After_signin(user_Name, password);
			Log.message("5. Redirect to home page after login to the application");
			
			menuPage = appHomePage.navigate_Menu_Screen();
			Log.message("6. Redirect to MenuPage");
			Assert.assertEquals(user_Name, menuPage.getUserText_App(), "User name is not matched after login to the application please check the app");
			
			appHomePage = menuPage.navigateToHomePage();
			Log.message("7. Redirect to HomePage");
			
			appHomePage.clickLatest_tab();
			Log.message("8. Clicked Latest button");
			
		}catch(Exception e){
			Log.exception(e, driver);
		}
		
	}

}
