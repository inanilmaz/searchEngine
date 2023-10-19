package searchengine.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.model.Task;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class IndexingTask extends RecursiveTask<Boolean> {

    private Connection.Response response = null;

    private final Task task;
    private final AtomicBoolean shouldStop;
    private final SaveLemma lemmaServise;


    public IndexingTask(Task task, AtomicBoolean shouldStop) {
        this.task = task;
        this.shouldStop = shouldStop;
        lemmaServise = new SaveLemma(task.getLemmaRepositories(),task.getSearchIndexRepositories());
    }

    public synchronized void threadSleep() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println("thread sleep" + e);
            throw new RuntimeException(e);
        }
    }

    private boolean checkPage(String href, String url) {
        try {
            if (!href.startsWith("http://") && !href.startsWith("https://")) {
                return false;
            }

            URL hrefURL = new URL(href);
            URL baseURL = new URL(url);
            String hrefDomain = hrefURL.getHost();
            String baseDomain = baseURL.getHost();
            return hrefDomain.equals(baseDomain) &&
                    !href.contains("#") &&
                    !href.endsWith(".pdf") &&
                    !href.endsWith(".jpg") &&
                    !href.equals(url);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void crawlPage(int statusCode) throws IOException {
        Document doc = response.parse();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String href = link.attr("abs:href");
            if (checkPage(href, task.getUrl()) && task.getUniqPage().add(href)) {
                String content = doc.getAllElements().toString();
                Page page = task.getSiteAndPageTableService().createNewPage(statusCode, href, content);
                task.getPageRepositories().save(page);
                saveLemma(doc,page,statusCode);
                task.setUrl(href);
                IndexingTask fjpTask = new IndexingTask(task,shouldStop);
                fjpTask.fork();
                fjpTask.join();
                task.getSiteAndPageTableService().updateDateTime();
                threadSleep();
            }
        }
    }
    public void saveLemma(Document doc, Page page, int statusCode){
        if (statusCode != 400 && statusCode != 500) {
            try {
                lemmaServise.saveOrUpdateLemma(doc, page);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("SaveLemma");
        }
    }

    public void parsePage() throws IOException {
        response = Jsoup.connect(task.getUrl())
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .referrer("http://www.google.com")
                .execute();
        int statusCode = response.statusCode();
        crawlPage(statusCode);
    }

    @Override
    protected Boolean compute() {
        try {
            parsePage();
            return true;
        } catch (IOException e) {
            System.out.println("compute exception: " + e);
            task.getSiteAndPageTableService().updateStatusToFailed(e.getMessage(),task.getDomain());
            return false;
        }
    }
}
