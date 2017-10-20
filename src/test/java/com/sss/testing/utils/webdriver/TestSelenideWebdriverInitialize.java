package com.sss.testing.utils.webdriver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.hamcrest.Matchers;
import org.openqa.selenium.remote.BrowserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.remote.BrowserType.CHROME;
import static org.openqa.selenium.remote.BrowserType.FIREFOX;

/**
 * Test webdriver initialize
 */
@ContextConfiguration(locations = {"classpath*:test-webdriver-context.xml"})
@DirtiesContext
public class TestSelenideWebdriverInitialize extends AbstractTestNGSpringContextTests {

    @Autowired
    @Qualifier("wdSettings")
    private WDSettings wdSettings;

    @Test
    public void test_language() throws Exception {
        assertThat("Incorrect language value",
                wdSettings.getLanguageSettings(),
                Matchers.isIn(Arrays.asList("ru", "en"))
        );
    }

    /**
     * run selenide with chrome with customized profile
     *
     * @throws Exception
     */
    @Test
    public void testDefaultRunChromeCustomizedProfile() throws Exception {
        WDFactory wdFactory = new WDFactory();
        wdFactory.createCustomizedSelenide(wdSettings, RuntimeModes.RUN);
        Selenide.open("https://www.google.ru/");
        Selenide.close();
        WebDriverRunner.closeWebDriver();
    }

    @Test
    public void testCustomizesBrowserNameRunFirefoxCustomizedProfile() throws Exception {
        WDFactory wdFactory = new WDFactory();
        wdFactory.createCustomizedSelenide(wdSettings.setBrowser(FIREFOX), RuntimeModes.RUN);
        assertThat("Invalid browser name, should be " + BrowserType.FIREFOX + " but actual " + Configuration.browser,
                Configuration.browser, is(BrowserType.FIREFOX));
        Selenide.open("https://www.google.ru/");
        Selenide.close();
        WebDriverRunner.closeWebDriver();
    }

    @Test
    public void testCustomizesBrowserNameRunChromeCustomizedProfile() throws Exception {
        WDFactory wdFactory = new WDFactory();
        wdFactory.createCustomizedSelenide(wdSettings.setBrowser(CHROME), RuntimeModes.RUN);
        assertThat("Invalid browser name, should be " + BrowserType.CHROME + " but actual " + Configuration.browser,
                Configuration.browser, is(BrowserType.CHROME));
        Selenide.open("https://www.google.ru/");
        Selenide.close();
        WebDriverRunner.closeWebDriver();
    }

    @Test
    public void testCustomizesBrowserNameRunFirefoxDefaultProfile() throws Exception {
        WDFactory wdFactory = new WDFactory();
        wdFactory.createCustomizedSelenide(wdSettings.setBrowser(FIREFOX), RuntimeModes.RUN);
        Selenide.open("https://www.google.ru/");
        Selenide.close();
        WebDriverRunner.closeWebDriver();
    }

    @Test
    public void testCustomizesBrowserNameRunChromeDefaultProfile() throws Exception {
        WDFactory wdFactory = new WDFactory();
        wdFactory.createNativeSelenide(wdSettings.setBrowser(CHROME), RuntimeModes.RUN);
        Selenide.open("https://www.google.ru/");
        Selenide.close();
        WebDriverRunner.closeWebDriver();
    }


}
