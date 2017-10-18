package com.sss.testing.utils.webdriver.util;

import com.codeborne.selenide.Configuration;
import com.sss.testing.utils.webdriver.WDManager;
import org.apache.commons.lang3.time.StopWatch;
import com.sss.testing.utils.webdriver.WDSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * WebDriver file downloader
 */
public class WDFileDownloader {

    @Autowired
    @Qualifier("webDriverFactory")
    private WDManager wdFactory;

    private static boolean isRemoteRun;

    private WDFileDownloader() {
//        hide constructor
        isRemoteRun = false;
//        isRemoteRun = wdFactory.isRemoteRun();
    }

    /**
     * clean folder for download
     *
     * @throws IOException If an I/O error occurs
     */
    public static void cleanDownloadFolder() throws IOException {
        File downloadFolder = new File(WDSettings.getDownloadPath());
        String[] list = downloadFolder.list();
        if (list == null) return;
        for (String folderItem : list) {
            Path fileToBeDeleted = Paths.get(downloadFolder.toString(), folderItem);
            Files.deleteIfExists(fileToBeDeleted);
        }
    }

    /**
     * waiting for first downloaded file
     *
     * @param timeoutInMilliseconds timeout
     * @return absolute path to downloaded file. <p> null - file was not found.
     */
    public static Path waitForFirstDownloadedFile(long timeoutInMilliseconds) {
        StopWatch sw = new StopWatch();
        File downloadFolder = new File(WDSettings.getDownloadPath());
        String[] list;
        List<String> allFiles = new ArrayList<>();
        List<String> files = new ArrayList<>();

        sw.start();
        do {
            list = downloadFolder.list();
            if (list != null && list.length > 0)
                allFiles = Arrays.asList(list);
            /*
             * filter for files that downloading now
             */
            allFiles.forEach(file -> {
                if (!file.endsWith(".crdownload") && !file.endsWith(".tmp")) {
                    files.add(file);
                }
            });
        } while (files.isEmpty() && sw.getTime() < timeoutInMilliseconds);

        if (files.isEmpty()) {
            return null;
        }

        return Paths.get(downloadFolder.toString(), files.get(0));
    }

    /**
     * Waiting finish of loading first file. Default timeout 10 seconds.
     *
     * @return Absolute path to downloaded file. <p> null - file wasn't found.
     */
    public static Path waitForFirstDownloadedFile() {
        return waitForFirstDownloadedFile(10_000);
    }

    /**
     * Clean or remember:
     * for local run - clean folder for download
     * for remote run (Selenium Grid) - remember files list before download
     *
     * @return files list before download
     * <code>null</code> - folder is clean, without files
     * @throws IOException If an I/O error occurs
     */
    public static Map<String, String> cleanOrRemember() throws IOException {
        /* local run */
        if (Configuration.remote == null || !isRemoteRun) {
            cleanDownloadFolder();
            return null;
        /* Selenium Grid */
        } else {
            return WDHttpFileDownloader.getFileList();
        }
    }
}
