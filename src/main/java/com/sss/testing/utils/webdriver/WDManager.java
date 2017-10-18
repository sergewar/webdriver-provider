package com.sss.testing.utils.webdriver;

import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sshtubey on 22/02/2017.
 */
public class WDManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WDManager.class);

    private static final String PROPERTY_NAME_SELENIUM_GRID = "host.grid";
    private static final String PROPERTY_NAME_BROWSER_TYPE = "browser";
    private static final List<String> SUPPORTED_BROWSER
            = Arrays.asList(BrowserType.FIREFOX, BrowserType.CHROME, "ie9", "ie11", "IE", BrowserType.HTMLUNIT);

    private String hubUrl;
    private WDType driverType;
    private final WDFactory driverFactory;

    private static ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<WebDriver>();

    /**
     * Webdriver manager default constructor
     */
    public WDManager() {
        this(new WDFactory());
    }

    /**
     * Webdriver manager parametrized constructor
     *
     * @param driverFactory Webdriver factory
     */
    public WDManager(WDFactory driverFactory) {
        this(WDType.forBrowserType(getBrowserTypeFromConfig()), driverFactory);

    }

    public WDManager(WDType wdType, WDFactory driverFactory) {
        initHubUrl();
        this.driverFactory = driverFactory;
        this.driverType = wdType;
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
        return !"".equals(hubUrl) ? getRemoteWebDriver() : getLocalWebDriver();
    }

    public static WebDriver getCurrentWebDriver() {
        return webDriverThreadLocal.get();
    }

    private WebDriver getLocalWebDriver() {
        LOGGER.debug("Connecting to local web driver...");
        return driverFactory.createWebDriver(driverType.getBrowserType());
    }

    private WebDriver getRemoteWebDriver() {
        LOGGER.debug("Connecting to remote web driver...");
        return driverFactory.createRemoteWebDriver(hubUrl, driverType.getBrowserType());
    }

    private static String getBrowserTypeFromConfig() {
        String type = System.getProperty(PROPERTY_NAME_BROWSER_TYPE);
        if (type != null && SUPPORTED_BROWSER.contains(type)) {
            return type;
        }
        return BrowserType.CHROME;
    }

    private void initHubUrl() {
        String gridUrl = System.getProperty(PROPERTY_NAME_SELENIUM_GRID);
        this.hubUrl = null;
        if (gridUrl == null) {
            LOGGER.debug("Selenium Grid URL is null, running in singleThread mode");
            return;
        } else {
            this.hubUrl = gridUrl;
            LOGGER.debug("Selenium Grid URL: {}", gridUrl);
        }
    }

    public boolean isRemoteRun() {
        return !"".equals(hubUrl);
    }
}
