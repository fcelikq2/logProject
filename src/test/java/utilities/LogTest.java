package utilities;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.poi.ss.usermodel.*;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestResult;
import org.testng.annotations.*;


import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class LogTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    static ExtentReports extent;
    ExtentHtmlReporter reporter;
    ExtentTest logger;
    DesiredCapabilities caps;

    @BeforeSuite
    public void beforeSuite() {
        extent = new ExtentReports();
        reporter = new ExtentHtmlReporter("./Reports/AutomationReport.html");
        extent.attachReporter(reporter);
    }


    @BeforeClass
    public void Setup() {
      /*  WebDriverManager.chromedriver().setup();
        Map<String, String> mobileEmulation = new HashMap<>();
        mobileEmulation.put("deviceName", "iPhone 8");
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
        driver = new ChromeDriver(chromeOptions);*/
            WebDriverManager.chromedriver().setup();
            caps = DesiredCapabilities.chrome();
            LoggingPreferences logPrefs = new LoggingPreferences();
            logPrefs.enable(LogType.BROWSER, Level.ALL);
            caps.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
        caps.setCapability(ChromeOptions.CAPABILITY, chromeOptions());
        driver = new ChromeDriver(caps);
        driver.manage().timeouts().implicitlyWait(8,TimeUnit.MILLISECONDS);
        driver.manage().timeouts().pageLoadTimeout(30000,TimeUnit.MILLISECONDS);
        wait = new WebDriverWait(driver, 8);

    }

    @BeforeMethod
    public void beforeMethod(Method method) {
        logger = extent.createTest(method.getName());
        logger.info("Test Başladı");
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.fail(result.getThrowable().getMessage());
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.info("Test Bitti");
        }
    }

    @AfterClass()
    public void AfterClass() {
        driver.quit();
    }

    @AfterSuite()
    public void afterSuite() {
        extent.flush();
    }
    private ChromeOptions chromeOptions() {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--test-type");
        chromeOptions.addArguments("--disable-popup-blocking");
        chromeOptions.addArguments("--ignore-certificate-errors");
        chromeOptions.addArguments("--disable-translate");
        chromeOptions.addArguments("--start-maximized");
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        return chromeOptions;
    }




    ArrayList<String> categoryList = new ArrayList<>();
    int i =0;
    public void readExcel() {
        try {
            Workbook workbook = WorkbookFactory.create(new File("./Siteler/siteler.xlsx"));
            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
            while (sheetIterator.hasNext()) {
                sheetIterator.next();
                Sheet sheet;
                sheet = workbook.getSheetAt(i);
                i++;
                Iterator<Row> rowIterator = sheet.rowIterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        String data = cell.getStringCellValue();
                        categoryList.add(data);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void logTest() throws InterruptedException {
        readExcel();
        for (String url:categoryList){
                driver.get(url);
                analyzeLog(driver.getCurrentUrl());
                clickElementByXpath("//*[@id='ResimliMenu1']/li[2]/a");
                analyzeLog(driver.getCurrentUrl());
                try {
                    clickElementByXpath("//*[@class='productImage']");
                }catch (Exception e){
                    clickElementByXpath("//*[@class='lazyImage editable']");
                    clickElementByXpath("//*[@class='productImage']");
                }
                analyzeLog(driver.getCurrentUrl());
        }
    }

    public void analyzeLog(String categoryListUrl) throws InterruptedException {
        Thread.sleep(3000);
        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            String level = String.valueOf(entry.getLevel());
            if (level.contains("SEVERE")){
                String log = new Date( entry.getTimestamp()) +" "+ entry.getMessage();
                logger.fail(categoryListUrl + log);
            }
        }
    }
    //Navigate Methods
    protected void navigateToURL(String url) {
        driver.get(url);
    }
    protected void getCurrentUrl(String url) {
        driver.get(url);
    }


    protected WebElement findElementByXpath(String xpath) {
        waitUntilPresenceOfElementXpath(xpath);
        return driver.findElement(By.xpath(xpath));
    }

    //Click Method


    protected void clickElementByXpath(String xpath) {
        waitUntilElementIsClickableXpath(xpath);
        findElementByXpath(xpath).click();
    }

    //Wait Method
    protected void waitUntilElementIsClickableXpath(String xpath) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(findElementByXpath(xpath)));
        }catch (Exception e){
            System.out.println("devamke");
        }
    }
    protected void waitUntilPresenceOfElementXpath(String xpath) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        }catch (Exception e){
            System.out.println("devamke");
        }
    }

}
