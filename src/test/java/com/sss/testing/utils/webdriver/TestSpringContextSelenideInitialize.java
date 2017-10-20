package com.sss.testing.utils.webdriver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test webdriver initialize
 */
@ContextConfiguration(locations = {"classpath*:webdriver-selenide-context.xml"})
@DirtiesContext
public class TestSpringContextSelenideInitialize extends AbstractTestNGSpringContextTests {

    @Autowired
    @Qualifier("selenideCustomized")
    private WebDriver selenideWebdriver;

    @Test
    public void startDefaultBrowser() {
        Selenide.open("https://www.mail.ru/");
        assertThat("Invalid browser name, should be " + BrowserType.CHROME + " but actual " + Configuration.browser,
                Configuration.browser, is(BrowserType.CHROME));
    }

}
