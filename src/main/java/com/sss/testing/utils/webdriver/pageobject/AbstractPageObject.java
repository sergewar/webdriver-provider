package com.sss.testing.utils.webdriver.pageobject;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sss.testing.utils.webdriver.util.IsAjaxDone;

/**
 *
 */
public abstract class AbstractPageObject {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPageObject.class);
    private static final int TIMEOUT = 15;
    private final WebDriver webdriver;

    /**
     * Default page object constructor
     * @param webDriver for page
     * @param container By.container of page
     */
    public AbstractPageObject(WebDriver webDriver, By container) {
        new WebDriverWait(webDriver, getTimeout()).ignoring(StaleElementReferenceException.class)
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(container));
        this.webdriver = webDriver;
        waitForAjaxIsDone();
        waitForInnerElement(container);
        PageFactory.initElements(new DefaultElementLocatorFactory(webdriver.findElement(container)), this);
    }

    private void waitForInnerElement(By by) {
        if (by != null) {
            new WebDriverWait(getWebDriver(), getTimeout())
                    .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
        }
    }

    protected WebDriver getWebDriver() {
        return webdriver;
    }

    protected int getTimeout() {
        return TIMEOUT;
    }

    /**
     * Return text from popup alert
     * @return text from alert popup
     */
    public String getAlertText() {
        try {
            WebDriverWait wait = new WebDriverWait(webdriver, 2);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = webdriver.switchTo().alert();
            return alert.getText();
        } catch (Exception e) {
            LOGGER.info("Alert is absent");
        }
        return null;
    }

    /**
     * Accept alert popup
     */
    public void acceptAlert() {
        try {
            WebDriverWait wait = new WebDriverWait(webdriver, 2);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = webdriver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            LOGGER.info("Alert is absent");
        }
    }

    private void waitForAjaxIsDone() {
        if (waitForAjaxIsDoneInSec() != null && waitForAjaxIsDoneInSec() > 0) {
            new WebDriverWait(getWebDriver(), waitForAjaxIsDoneInSec()).until(new IsAjaxDone());
        }
    }

    /**
     * Setup a timeout for waiting until ajax request is done.
     * @return second for wait
     */
    protected Integer waitForAjaxIsDoneInSec() {
        return null;
    }
}
