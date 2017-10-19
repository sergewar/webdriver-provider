package com.sss.testing.utils.webdriver;

import com.codeborne.selenide.Configuration;
import org.openqa.selenium.remote.BrowserType;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Settings from the 'webdriversettings.properties' file.
 */
public final class WDSettings {
    private static final List<String> SUPPORTED_BROWSER
            = Arrays.asList(BrowserType.FIREFOX, BrowserType.CHROME, "ie9", "ie11", "IE", BrowserType.HTMLUNIT);

    private String language;
    private String browser;
    private String downloadPathLocal;
    private String downloadPathLinux;
    private String downloadPathLinuxTarget;
    private String hostGrid;
    private String hostGridPort;

    public WDSettings(String language,
                      String browser,
                      String downloadPathLocal,
                      String downloadPathLinux,
                      String downloadPathLinuxTarget,
                      String hostGrid,
                      String hostGridPort) {
        this.language = language;
        this.browser = browser;
        this.downloadPathLocal = downloadPathLocal;
        this.downloadPathLinux = downloadPathLinux;
        this.downloadPathLinuxTarget = downloadPathLinuxTarget;
        this.hostGrid = hostGrid;
        this.hostGridPort = hostGridPort;
    }


    public String getLanguage() {
        return language;
    }

    public String getBrowser() {
        if (browser != null && SUPPORTED_BROWSER.contains(browser)) {
            return browser;
        }
        return BrowserType.CHROME;
    }

    public WDSettings setBrowser(String browser) {
        this.browser = browser;
        return this;
    }

    /**
     * @return folder for download on remote nodes
     */
    public String getDownloadPathLinuxTarget() {
        return downloadPathLinux + downloadPathLinuxTarget;
//        return System.getProperty("download.path.linux") + System.getProperty("download.path.linux.target");
    }

    /**
     * @return absolute path for download files
     */
    public String getDownloadPath() {
        if (Configuration.remote != null) {
            return getDownloadPathLinuxTarget();
        } else {
            String localPath = getDownloadPathLocal();
            return Paths.get(System.getProperty("user.dir"))
                    .resolve(localPath)
                    .toString();
        }
    }

    /**
     * @return relative path for download on local
     */
    public String getDownloadPathLocal() {
        return downloadPathLocal;
//        return System.getProperty("download.path.local");
    }

    /**
     * @return selenium grid URL
     */
    public String getHostGrid() {
        return hostGrid;
//        return System.getProperty("host.grid");
    }

    /**
     * @return selenium grid port for access to downloaded file
     */
    public String getHostGridPort() {
        return hostGridPort;
//        return System.getProperty("host.grid.port");
    }

    public WDSettings setHostGrid(String hostGrid) {
        this.hostGrid = hostGrid;
        return this;
    }

    public String getLanguageSettings() {
        return language;
//        return System.getProperty("language");
    }

}
