package com.sss.testing.utils.webdriver;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.annotation.Nonnull;
import java.util.HashMap;

import static com.codeborne.selenide.Configuration.pageLoadStrategy;

/**
 * Created by sshtubey on 03.03.2017.
 */
public enum WDType {

    /**
     * Firefox capabilities settings
     */
    Firefox("firefox") {
        @Override
        public DesiredCapabilities capabilities() {
            FirefoxProfile firefoxProfile = new FirefoxProfile();
            firefoxProfile.setPreference("security.warn_viewing_mixed", false);
            firefoxProfile.setPreference("security.mixed_content.block_active_content", false);
            firefoxProfile.setPreference("plugins.update.url", "null");
            firefoxProfile.setPreference("plugins.update.notifyUser", false);
            firefoxProfile.setPreference("intl.accept_languages", "ru");

            /*
             * Never ask for proxy
             */
            firefoxProfile.setPreference("network.proxy.type", 0);

            /*
             * Set Location to store files after downloading.
             */
            String downloadPath = WDSettings.getDownloadPath();
            firefoxProfile.setPreference("browser.download.dir", downloadPath);
            firefoxProfile.setPreference("browser.download.folderList", 2);

            /*
             * Set Preference to not show file download confirmation dialogue
             * using MIME types Of different file extension types.
             */
            firefoxProfile.setPreference(
                    "browser.helperApps.neverAsk.saveToDisk",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;"//MIME types Of MS Excel File.
                            + "application/pdf;"
                            + "application/vnd.openxmlformats-officedocument.wordprocessingml.document;" //MIME types Of MS doc File.
                            + "text/plain;"
                            + "text/csv"
            );
            firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
            firefoxProfile.setPreference("pdfjs.disabled", true);

            DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
            desiredCapabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            desiredCapabilities.setCapability(CapabilityType.SUPPORTS_JAVASCRIPT, true);
            desiredCapabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
            return desiredCapabilities;
        }
    },

    /**
     * Chrome capabilities settings
     */
    Chrome("chrome") {
        @Override
        public DesiredCapabilities capabilities() {
            ChromeOptions options = new ChromeOptions();
            options.addArguments(String.format("--lang=%s", WDSettings.getLanguageSettings()));
            options.addArguments("--start-maximized");

            String downloadPath = WDSettings.getDownloadPath();
            HashMap<String, Object> chromePrefs = new HashMap<>();
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("download.default_directory", downloadPath);
            chromePrefs.put("download.prompt_for_download", false);
            chromePrefs.put("download.directory_upgrade", true);
            chromePrefs.put("savefile.default_directory", downloadPath);
            chromePrefs.put("savefile.type", 1);
            chromePrefs.put("plugins.always_open_pdf_externally", true); /* Disable PDF viewer */
            if (Configuration.remote != null) {
                chromePrefs.put("profile.default_content_setting_values.popups", 1);
            }
            options.setExperimentalOption("prefs", chromePrefs);

            DesiredCapabilities capabilities = DesiredCapabilities.chrome();
            capabilities.setCapability(CapabilityType.PAGE_LOAD_STRATEGY, pageLoadStrategy);
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            capabilities.setCapability(ChromeOptions.CAPABILITY, options);
            return capabilities;
        }
    },

    /**
     * Opera capabilities settings
     */
    Opera("opera") {
        @Override
        public DesiredCapabilities capabilities() {
            DesiredCapabilities capabilities = DesiredCapabilities.operaBlink();
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            return capabilities;
        }
    };

    ////////////////////////////////////////////////////////////////
    // ATTRIBUTES

    private final String browserType;
    private final boolean remoteSupported;

    ////////////////////////////////////////////////////////////////
    // CONSTRUCTORS

    private WDType(String browserType) {
        this(browserType, true);
    }

    private WDType(String browserType, boolean remoteSupported) {
        this.browserType = browserType;
        this.remoteSupported = remoteSupported;
    }

    ////////////////////////////////////////////////////////////////
    // METHODS

    /**
     * @return capabilities
     */
    public abstract DesiredCapabilities capabilities();

    /**
     * @param browserType browser type
     * @return webdriver type
     */
    public static WDType forBrowserType(@Nonnull String browserType) {
        String type = browserType.toLowerCase();
        for (WDType driverType : values()) {
            if (driverType.browserType.equals(type)) {
                return driverType;
            }
        }
        throw new IllegalArgumentException("Unknown browser type '" + browserType + "'");
    }

    /**
     * @param browserType browser type
     * @return is know browser type
     */
    public static boolean isKnownBrowserType(@Nonnull String browserType) {
        try {
            if (forBrowserType(browserType) != null) {
                return true;
            }
        } catch (IllegalArgumentException e) {
            // ignore the exception
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS

    public String getBrowserType() {
        return browserType;
    }

    public boolean isRemoteSupported() {
        return remoteSupported;
    }
}
