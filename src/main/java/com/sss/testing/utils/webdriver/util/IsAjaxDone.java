package com.sss.testing.utils.webdriver.util;


import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 *
 */
public class IsAjaxDone implements ExpectedCondition<Boolean> {

    /**
     * Returns true if no ajax is either in-flight or deferred/waiting/queued
     *
     * @param driver The webdriver instance
     * @return Boolean.TRUE if all tests of ajax silence pass.
     */
    @Override
    public Boolean apply(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        Boolean clientCheck = (Boolean) jsExecutor.executeScript(
                "return !AjaxSupport.hasActiveConnections() && !AjaxSupport.hasHibernatingRequests()");
        return clientCheck != null ? clientCheck : true;
    }
}
