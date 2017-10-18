package com.sss.testing.utils.webdriver;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * Test webdriver initialize
 */
@ContextConfiguration(locations = {"classpath:webdriver-context.xml"})
@DirtiesContext
public class TestSpringContextWebdriverInitialize extends AbstractTestNGSpringContextTests {

    @Autowired
    private WebDriver wdInstance;

    @Test
    public void startDefaultBrowser() {
        wdInstance.navigate().to("https://www.ya.ru/");
    }
}
