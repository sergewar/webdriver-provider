package com.sss.testing.utils.webdriver;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sshtubey on 22/02/2017.
 */
public class WDManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WDManager.class);

//    private static final String PROPERTY_NAME_SELENIUM_GRID = "host.grid";
//    private static final String PROPERTY_NAME_BROWSER_TYPE = "browser";

    private String hubUrl;
    private final WDFactory driverFactory;
    private final WDSettings wdSettings;

    private static ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<WebDriver>();

    /**
     * Webdriver manager parametrized constructor
     *
     * @param wdSettings Webdriver settings
     */
    public WDManager(WDSettings wdSettings) {
        this.driverFactory = new WDFactory();
        this.wdSettings = wdSettings;
//        this.driverType = WDType.forBrowserType(wdSettings.getBrowser());
    }

    /**
     * Return Webdriver instance
     *
     * @return Webdriver
     */
    public WebDriver getWebDriverInstance() {
        WebDriver driver = createWebDriver();
        try {
            LOGGER.debug("Driver capabilities: " + ((HasCapabilities) ((EventFiringWebDriver) driver)
                    .getWrappedDriver()).getCapabilities());
        } catch (ClassCastException e) {
            LOGGER.debug("Driver capabilities: " + ((HasCapabilities) driver).getCapabilities());
        }
        webDriverThreadLocal.set(driver);
        return driver;
    }

    /**
     * Create Webdriver instance
     *
     * @return webdriver
     */
    private WebDriver createWebDriver() {
        return isRemoteRun() ? getRemoteWebDriver() : getLocalWebDriver();
    }

    public static WebDriver getCurrentWebDriver() {
        return webDriverThreadLocal.get();
    }

    private WebDriver getLocalWebDriver() {
        LOGGER.debug("Connecting to local web driver...");
        return driverFactory.createWebDriver(wdSettings);
    }

    private WebDriver getRemoteWebDriver() {
        LOGGER.debug("Connecting to remote web driver...");
        return driverFactory.createRemoteWebDriver(wdSettings);
    }

    public boolean isRemoteRun() {
        return !"".equals(wdSettings.getHostGrid());
    }
}
