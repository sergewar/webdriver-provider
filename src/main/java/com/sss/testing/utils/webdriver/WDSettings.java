package com.sss.testing.utils.webdriver;

import com.codeborne.selenide.Configuration;

import java.nio.file.Paths;

/**
 * Settings from the 'settings.properties' file.
 */
public final class WDSettings {

    private WDSettings() {
        //hide constructor
    }

    /**
     * @return Название папки проекта для скачивания файлов на удалённых нода.
     */
    public static String getDownloadPathLinuxTarget() {
        return System.getProperty("download.path.linux.target");
    }

    /**
     * @return absolute path for download files
     */
    public static String getDownloadPath() {
        if (Configuration.remote != null) {
            return System.getProperty("download.path.linux") + getDownloadPathLinuxTarget();
        } else {
            String localPath = System.getProperty("download.path.local");
            return Paths.get(System.getProperty("user.dir"))
                    .resolve(localPath)
                    .toString();
        }
    }

    /**
     * @return Относительный путь для скачивания на локалку.
     */
    public static String getDownloadPathLocal() {
        return System.getProperty("download.path.local");
    }

    /**
     * @return URL грида.
     */
    public static String getHostGrid() {
        return System.getProperty("host.grid");
    }

    /**
     * @return Порт на гриде, по которому можно обращаться к папке со скаченными файлами.
     */
    public static String getHostGridHttpFolderPort() {
        return System.getProperty("host.grid.http_folder.port");
    }

    public static String getLanguageSettings() {
        return System.getProperty("language");
    }

}
