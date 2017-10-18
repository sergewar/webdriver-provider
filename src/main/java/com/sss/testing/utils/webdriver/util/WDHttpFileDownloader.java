package com.sss.testing.utils.webdriver.util;

import com.codeborne.selenide.Configuration;
import com.sss.testing.utils.webdriver.WDSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Downloader for Selenium Grid node via HTTP.
 */
public class WDHttpFileDownloader {

    /**
     * Path to folder via HTTP, where save files on nodes
     * <p>Example: <code>http://grid.selenium:30080/saved_file_folder/</code>
     */
    private static String rootUrl() {
        return WDSettings.getHostGrid() + ":" + WDSettings.getHostGridHttpFolderPort()
                + WDSettings.getDownloadPathLinuxTarget() + "/";
    }

    /**
     * Обращается к папке для скачивания (<code>rootUrl</code>) по HTTP, вычитывает список файлов из неё.
     *
     * @return Полный список файлов из папки. <code>key</code> - имя файла, <code>value</code> - href
     * @throws IOException exception
     */
    public static Map<String, String> getFileList() throws IOException {
        Document doc = null;

        /* max attempts of save file from grid */
        int attemptCountMax = 3;

        /* trying copy */
        for (int i = 1; i <= attemptCountMax; i++) {
            try {
                doc = Jsoup.connect(rootUrl()).get();
                break;
            } catch (org.jsoup.HttpStatusException e) {
                int remainedAttempts = attemptCountMax - i;
                if (e.getStatusCode() == 502) {
                    System.out.println("[WARNING] Occurred: " + e.toString() + ". Remained attempts: " + remainedAttempts);
                } else {
                    throw e;
                }
                /* If can't copy throw Exception */
                if (remainedAttempts == 0) {
                    throw e;
                }
            }
        }
        Map<String, String> fileNames = new HashMap<>();
        assert doc != null;
        for (Element file : doc.select("a")) {
            if (!file.text().contains(".."))
                fileNames.put(file.text(), file.attr("href"));
        }
        return fileNames;
    }

    /**
     * Возвращает разницу между двумя списками файлов (до скачивания и после). Разница счивается новыми файлами.
     *
     * @param filesBefore Список файлов до скачивания. <code>key</code> - имя файла, <code>value</code> - href
     * @param filesAfter  Список файлов после скачивания. <code>key</code> - имя файла, <code>value</code> - href
     * @return Разница между списками (новые файлы). Если список пустой, то size() == 0.
     * <code>key</code> - имя файла, <code>value</code> - href не абсолютный.
     */
    public static Map<String, String> getNewFileUrls(Map<String, String> filesBefore, Map<String, String> filesAfter) {
        Set<String> namesBefore = filesBefore.keySet();
        Set<String> namesAfter = filesAfter.keySet();
        namesAfter.removeIf(namesBefore::contains);

        Map<String, String> newFiles = new HashMap<>();
        namesAfter.forEach(name -> newFiles.put(name, filesAfter.get(name)));

        /* Формирует список для имен с полным url к файлам для дальнейшго скачивания. */
        Map<String, String> newFileUrls = new HashMap<>();
        newFiles.forEach((name, href) -> newFileUrls.put(name, rootUrl() + href));

        /* Проверить возможность обращения к файлам, если исключение не возникает, то файл готов к скачиванию.
         * Проверка введена для браузера Firefox, пока файл в процессе скачинвания, похоже, его имя не изменяется
         */
        Map<String, String> resUrls = new HashMap<>();
        for (Map.Entry<String, String> entry : newFileUrls.entrySet()) {
            try {
                new URL(entry.getValue()).openStream();
                resUrls.put(entry.getKey(), entry.getValue());
            } catch (IOException ignored) {
                /* В случае исключения файл не будет копироваться на локалку */
            }

        }
        return resUrls;
    }

    /**
     * Ожидает скачивания новых файлов заданное время.
     *
     * @param filesBefore           Список файлов, взятый перед скачиванием. С ним будет сравниваться текущий список,
     *                              чтобы понять появились ли новые файлы или нет.
     * @param timeoutInMilliseconds Время ожидания скачивания.
     * @return Список с новыми файлами:
     * <code>key</code> - имя файла, <code>value</code> - полный URL файла для скачивания.
     * <p>Если за указанный промежуток времени файлы не были скачены - список будет пустой.
     * @throws IOException On error
     */
    public static Map<String, String> waitForDownloading(Map<String, String> filesBefore, long timeoutInMilliseconds) throws IOException {
        Map<String, String> newFileUrls;
        StopWatch sw = new StopWatch();
        sw.start();
        do {
            /* Взять список файлов после скачивания */
            Map<String, String> filesAfter = getFileList();

            /* Вернуть URL-ы только для новых файлов */
            newFileUrls = getNewFileUrls(filesBefore, filesAfter);
            newFileUrls = filter(newFileUrls);
        } while (newFileUrls.isEmpty() && (sw.getTime() < timeoutInMilliseconds));
        return newFileUrls;
    }

    /**
     * Ожидает скачивания новых файлов. По умолчанию время ожидания берётся из {@link Configuration}.timeout.
     *
     * @param filesBefore Список файлов, взятый перед скачиванием. С ним будет сравниваться текущий список,
     *                    чтобы понять появились ли новые файлы или нет.
     * @return Список с новыми файлами.
     * <p>Если за указанный промежуток времени файлы не были скачены - список будет пустой.
     * @throws IOException On error
     */
    public static Map<String, String> waitForDownloading(Map<String, String> filesBefore) throws IOException {
        return waitForDownloading(filesBefore, Configuration.timeout);
    }

    /**
     * Копирует файлы с удалённого пути на локалку по имеющимся ссылкам.
     *
     * @param newFileUrls Список, содрежащий имена файлов и их ссылки по HTTP.
     * @return Список путей, куда и откуда были скопированы файлы.
     * <code>size() == 0</code> - никакие файлы не были скопированы.
     * @throws IOException if source URL cannot be opened,
     *                     <p> if destination is a directory,
     *                     <p> if destination cannot be written,
     *                     <p> if destination needs creating but can't be,
     *                     <p> if an IO error occurs during copying
     */
    public static Map<Path, String> copyToLocal(Map<String, String> newFileUrls) throws IOException {
        Map<Path, String> copied = new HashMap<>();

        /* Для всего списка ... */
        newFileUrls.forEach((name, url) -> {
            /* ... делается формирование локального пути куда будет скопирован текущий файл */
            Path path = Paths.get(System.getProperty("user.dir"))
                    .resolve(WDSettings.getDownloadPathLocal())
                    .resolve(name);
            copied.put(path, url);
        });

        for (Map.Entry<Path, String> entry : copied.entrySet()) {
            Path path = entry.getKey();
            String url = entry.getValue();

            /* Максимально число попыток скопировать файл */
            int attemptCountMax = 3;

            /* Циклично пытаться выполнить копирование */
            for (int i = 1; i <= attemptCountMax; i++) {
                try {
                    FileUtils.copyURLToFile(new URL(url), path.toFile());
                } catch (IOException e) {
                    int remainedAttempts = attemptCountMax - i;
                    if (e.getMessage().contains("Server returned HTTP response code: 504 for URL")) {
                        System.out.println(
                                "[WARNING] Occurred: " + e.toString() + "." +
                                        "\nRemained attempts: " + remainedAttempts
                        );
                    } else {
                        throw e;
                    }
                    /* Если попыток не осталось - бросить полученный Exception */
                    if (remainedAttempts == 0) {
                        throw e;
                    }
                }
            }
        }

        return copied;
    }

    /**
     * Дожидается загрузки новых файлов и копирует их на локалку.
     *
     * @param filesBefore Список файлов в папке до скачивания.
     * @return Список путей, куда и откуда были скопированы файлы.
     * Если файлы не были скопированы - список будет пустой.
     * @throws IOException if source URL cannot be opened,
     *                     <p> if destination is a directory,
     *                     <p> if destination cannot be written,
     *                     <p> if destination needs creating but can't be,
     *                     <p> if an IO error occurs during copying
     */
    public static Map<Path, String> waitForAndCopy(Map<String, String> filesBefore) throws IOException {
        return copyToLocal(waitForDownloading(filesBefore));
    }

    /**
     * Фильтрует список от временных (ещё нескаченных) файлов.
     *
     * @param newFileUrls Список для фильтрации.
     * @return Отфильтрованный список.
     */
    private static Map<String, String> filter(Map<String, String> newFileUrls) {
        Map<String, String> filtered = new HashMap<>();
        newFileUrls.forEach((name, href) -> {
            if (!name.endsWith(".crdownload") && !name.endsWith(".tmp")) {
                filtered.put(name, href);
            }
        });
        return filtered;
    }
}
