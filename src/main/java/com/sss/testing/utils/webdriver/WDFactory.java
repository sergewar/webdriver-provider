package com.sss.testing.utils.webdriver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.sss.testing.utils.webdriversinstaller.Driver;
import com.sss.testing.utils.webdriversinstaller.InstallWebDrivers;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.codeborne.selenide.Configuration.remote;
import static com.codeborne.selenide.WebDriverRunner.setWebDriver;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.openqa.selenium.remote.BrowserType.CHROME;
import static org.openqa.selenium.remote.BrowserType.FIREFOX;
import static org.openqa.selenium.remote.BrowserType.OPERA;

/**
 * Created by sshtubey on 22/02/2017.
 */
public class WDFactory {

    private String testName = null;

    /**
     * default constructor
     */
    public WDFactory() {

    }

    /**
     * Run FF by default
     *
     * @return Webdriver
     */
    public WebDriver createWebDriver(WDSettings wdSettings) {
        WebDriver driver;
        String browser = wdSettings.getBrowser();
        switch (browser) {
            case BrowserTypes.CHROME:
                prepareChromeExe();
                driver = new ChromeDriver(WDType.forBrowserType(BrowserTypes.CHROME).capabilities(wdSettings));
                break;

            case BrowserTypes.FIREFOX:
            case BrowserTypes.FF:
                prepareFirefoxExe();
                driver = new FirefoxDriver(WDType.forBrowserType(BrowserTypes.FIREFOX).capabilities(wdSettings));
                break;

            case BrowserTypes.INTERNET_EXPLORER:
                prepareIEExe();
                driver = new InternetExplorerDriver(WDType.forBrowserType(BrowserTypes.INTERNET_EXPLORER).capabilities(wdSettings));
                break;

            case BrowserTypes.OPERA:
                prepareOperaExe();
                driver = new OperaDriver(WDType.forBrowserType(BrowserTypes.OPERA).capabilities(wdSettings));
                break;

            default:
                throw new IllegalArgumentException(String.format("Unknown selected browser: '%s'", browser));
        }

        checkNotNull(driver, "Browser driver is null");
        driver.getTitle();
        return driver;
    }

    /**
     * Prepares a RemoteWebDriver - tests will be ran on the Selenium Grid.
     *
     * @return RemoteWebDriver
     */
    public WebDriver createRemoteWebDriver(WDSettings wdSettings) {
        String browser = wdSettings.getBrowser();
        try {
            DesiredCapabilities capabilities;
            switch (browser) {
                case CHROME:
                    capabilities = WDType.forBrowserType(CHROME).capabilities(wdSettings);
                    break;
                case FIREFOX:
                    capabilities = WDType.forBrowserType(FIREFOX).capabilities(wdSettings);
                    break;
                case OPERA:
                    capabilities = WDType.forBrowserType(OPERA).capabilities(wdSettings);
                    break;
                default:
                    throw new NotImplementedException("No desired capabilities for remote browser: " + browser);
            }

            if (testName != null) {
                capabilities.setCapability("name", testName);
            }
            return new RemoteWebDriver(new URL(wdSettings.getHostGrid()), capabilities);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid 'remote' parameter: " + remote, e);
        }
    }

    /**
     * Runs the specified WebDriver in the specified runtime mode.
     *
     * @param wdSettings  a browser for running.
     *                    For local host - a type browser is being got from .properties file.
     *                    For remote (Selenium Grid) - the type browser is being got from mvn command line
     *                    (For example: <code>-Dbrowser=firefox</code>)
     * @param runtimeMode Specified runtime mode.
     */
    public void createCustomizedSelenide(WDSettings wdSettings, String runtimeMode) {
        if ((remote != null) && (!remote.isEmpty())) {
            WebDriverRunner.setWebDriver(this.createRemoteSelenideWebDriver(wdSettings));
            WebDriverRunner.getWebDriver().manage().window().maximize();
            return;
        }

        String browser = wdSettings.getBrowser();
        /*
         * Do NOT DELETE this assignment!
         * Otherwise when try to read a value of the 'Configuration.browser' - the value may not be an actual browser.
         */
        Configuration.browser = browser;

        /*
         * Prepare WebDriver in the case if tests are being ran locally.
         * Settings will be get from the .properties-file(s).
         */
        switch (browser) {
            case BrowserTypes.CHROME:
                prepareChromeExe();
                setWebDriver(
                        new ChromeDriver(
                                WDType.forBrowserType(BrowserTypes.CHROME).capabilities(wdSettings)
                        )
                );
                break;

            case BrowserTypes.FIREFOX:
            case BrowserTypes.FF:
                prepareFirefoxExe();
                setWebDriver(
                        new FirefoxDriver(
                                WDType.forBrowserType(BrowserTypes.FIREFOX).capabilities(wdSettings)
                        )
                );
                break;

            case BrowserTypes.INTERNET_EXPLORER:
                prepareIEExe();
                setWebDriver(new InternetExplorerDriver());
                break;

            case BrowserTypes.OPERA:
                prepareOperaExe();
                setWebDriver(
                        new OperaDriver(
                                WDType.forBrowserType(BrowserTypes.OPERA).capabilities(wdSettings)
                        )
                );
                break;

            default:
                throw new IllegalArgumentException(String.format("Unknown selected browser: '%s'", browser));
        }

        if (RuntimeModes.DEBUG.equals(runtimeMode)) {
            java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            WebDriverRunner.getWebDriver().manage().window().setPosition(new Point((int) (screenSize.getWidth() / 2), 1));
        } else {
            // TODO: Delete the condition after released of the Firefox 55. see issue: https://github.com/mozilla/geckodriver/issues/820
            if (!browser.equals(BrowserTypes.FIREFOX)) {
                WebDriverRunner.getWebDriver().manage().window().maximize();
            }
        }
    }

    /**
     * Runs the specified WebDriver in the specified runtime mode.
     *
     * @param wdSettings  a browser for running.
     *                    For local host - a type browser is being got from .properties file.
     *                    For remote (Selenium Grid) - the type browser is being got from mvn command line
     *                    (For example: <code>-Dbrowser=firefox</code>)
     * @param runtimeMode Specified runtime mode.
     * @param tstName     Имя теста, будет отображаться на dashboard-е Zalenium.
     */
    public void createCustomizedSelenide(WDSettings wdSettings, String runtimeMode, String tstName) {
        this.testName = tstName;
        createCustomizedSelenide(wdSettings, runtimeMode);
    }

    /**
     * Runs the specified WebDriver in the specified runtime mode.
     *
     * @param wdSettings     a browser for running.
     *                    For local host - a type browser is being got from .properties file.
     *                    For remote (Selenium Grid) - the type browser is being got from mvn command line
     *                    (For example: <code>-Dbrowser=firefox</code>)
     * @param runtimeMode Specified runtime mode.
     */
    public void createNativeSelenide(WDSettings wdSettings, String runtimeMode) {
        if ((remote != null) && (!remote.isEmpty())) {
            WebDriverRunner.setWebDriver(createRemoteSelenideWebDriver(wdSettings));
            WebDriverRunner.getWebDriver().manage().window().maximize();
            return;
        }

        String browser = wdSettings.getBrowser();
        /*
         * Do NOT DELETE this assignment!
         * Otherwise when try to read a value of the 'Configuration.browser' - the value may not be an actual browser.
         */
        Configuration.browser = browser;

        /*
        Prepare WebDriver in the case if tests are being ran locally.
        Settings will be get from the .properties-file(s).
         */
        switch (browser) {
            case BrowserTypes.CHROME:
                prepareChromeExe();
                break;

            case BrowserTypes.FIREFOX:
            case BrowserTypes.FF:
                prepareFirefoxExe();
                break;

            case BrowserTypes.INTERNET_EXPLORER:
                prepareIEExe();
                break;

            case BrowserTypes.OPERA:
                prepareOperaExe();
                break;

            default:
                throw new IllegalArgumentException(String.format("Unknown selected browser: '%s'", browser));
        }

        if (RuntimeModes.DEBUG.equals(runtimeMode)) {
            java.awt.Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            WebDriverRunner.getWebDriver().manage().window().setPosition(new Point((int) (screenSize.getWidth() / 2), 1));
        } else {
            WebDriverRunner.getWebDriver().manage().window().maximize();
        }
    }

    /**
     * Runs the specified WebDriver in the specified runtime mode.
     *
     * @param wdSettings  a browser for running.
     *                    For local host - a type browser is being got from .properties file.
     *                    For remote (Selenium Grid) - the type browser is being got from mvn command line
     *                    (For example: <code>-Dbrowser=firefox</code>)
     * @param runtimeMode Specified runtime mode.
     * @param tstName     Имя теста, будет отображаться на dashboard-е Zalenium.
     */
    public void createNativeSelenide(WDSettings wdSettings, String runtimeMode, String tstName) {
        this.testName = tstName;
        createNativeSelenide(wdSettings, runtimeMode);
    }



    private WebDriver createRemoteSelenideWebDriver(WDSettings wdSettings) {
        return createRemoteWebDriver(wdSettings.setHostGrid(remote));
    }

    /**
     * Prepares executable Chrome WebDriver for job.
     */
    private static void prepareChromeExe() {
        final String driverSystemProperty = "webdriver.chrome.driver";
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String exceptionText = String.format("No GeckoDriver for architecture type '%s' of OS '%s'", osArch, osName);
        Driver driver = new Driver();
        driver.setName("chromedriver");
        driver.setVersion("2.32.0");
        if (osName.toLowerCase().contains("windows")) {
            driver.setPlatform("windows");
            driver.setBit("32");
        } else if (osName.toLowerCase().contains("linux")) {
            driver.setPlatform("linux");
            if (osArch.contains(String.valueOf(32))) {
                driver.setBit("32");
            } else if (osArch.contains(String.valueOf(64))) {
                driver.setBit("64");
            } else {
                throw new IllegalArgumentException(exceptionText);
            }
        } else {
            throw new IllegalArgumentException(String.format("Unknown Operation System '%s'", osName));
        }

        System.setProperty(driverSystemProperty, getPathForDriver(driver).toString());
        try {
            new InstallWebDrivers().installDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("linux".equals(driver.getPlatform())) {
            setExecutableForLinux(getPathForDriver(driver));
        }
    }

    /**
     * Prepares executable Firefox WebDriver for job.
     */
    private static void prepareFirefoxExe() {
        final String driverSystemProperty = "webdriver.gecko.driver";
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String exceptionText = String.format("No GeckoDriver for architecture type '%s' of OS '%s'", osArch, osName);
        Driver driver = new Driver();
        driver.setName("geckodriver");
        driver.setVersion("0.19.0");
        if (osName.toLowerCase().contains("windows")) {
            driver.setPlatform("windows");
        } else if (osName.toLowerCase().contains("linux")) {
            driver.setPlatform("linux");
        } else {
            throw new IllegalArgumentException(String.format("Unknown Operation System '%s'", osName));
        }
        if (osArch.contains(String.valueOf(32))) {
            driver.setBit("32");
        } else if (osArch.contains(String.valueOf(64))) {
            driver.setBit("64");
        } else {
            throw new IllegalArgumentException(exceptionText);
        }

        System.setProperty(driverSystemProperty, getPathForDriver(driver).toString());
        try {
            new InstallWebDrivers().installDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("linux".equals(driver.getPlatform())) {
            setExecutableForLinux(getPathForDriver(driver));
        }
    }

    /**
     * Prepares executable Opera WebDriver for job.
     */
    private static void prepareOperaExe() {
        final String driverSystemProperty = "webdriver.opera.driver";
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String exceptionText = String.format("No Opera driver for architecture type '%s' of OS '%s'", osArch, osName);
        Driver driver = new Driver();
        driver.setName("operadriver");
        driver.setVersion("2.29");
        if (osName.toLowerCase().contains("windows")) {
            driver.setPlatform("windows");
        } else if (osName.toLowerCase().contains("linux")) {
            driver.setPlatform("linux");
        } else {
            throw new IllegalArgumentException(String.format("Unknown Operation System '%s'", osName));
        }
        if (osArch.contains(String.valueOf(32))) {
            driver.setBit("32");
        } else if (osArch.contains(String.valueOf(64))) {
            driver.setBit("64");
        } else {
            throw new IllegalArgumentException(exceptionText);
        }

        System.setProperty(driverSystemProperty, getPathForDriver(driver).toString());
        try {
            new InstallWebDrivers().installDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("linux".equals(driver.getPlatform())) {
            setExecutableForLinux(getPathForDriver(driver));
        }
    }

    /**
     * Prepares executable Internet Explorer WebDriver for job.
     */
    private static void prepareIEExe() {
        final String driverSystemProperty = "webdriver.ie.driver";
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String exceptionText = String.format("No IE driver for architecture type '%s' of OS '%s'", osArch, osName);
        Driver driver = new Driver();
        driver.setName("internetexplorerdriver");
        driver.setVersion("3.5.1");
        if (osName.toLowerCase().contains("windows")) {
            driver.setPlatform("windows");
            if (osArch.contains(String.valueOf(32))) {
                driver.setBit("32");
            } else if (osArch.contains(String.valueOf(64))) {
                driver.setBit("64");
            } else {
                throw new IllegalArgumentException(exceptionText);
            }
        } else if (osName.toLowerCase().contains("linux")) {
            throw new IllegalArgumentException(String.format("Unknown Operation System '%s'", osName));
        }

        System.setProperty(driverSystemProperty, getPathForDriver(driver).toString());
        try {
            new InstallWebDrivers().installDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Path getPathForDriver(Driver driver) {
        return Paths.get(System.getProperty("user.dir") + "/drivers/" + driver.getFileName());
    }

    private static void setExecutableForLinux(Path exeFile) {
        if (!new File(exeFile.toString()).setExecutable(true)) {
            throw new RuntimeException("Failed setting the access permission to allow execute operations for: " + exeFile.toString());
        }
    }
}
