package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.repositories.PageRepositories;
import searchengine.utils.SiteAndPageTableService;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

public class Indexing extends RecursiveTask<Boolean> {

    private Connection.Response response = null;

    private final SiteAndPageTableService siteAndPageTableService;
    private String url;
    private final HashSet<String> uniqPage;
    private final String domain;
    private final PageRepositories pageRepositories;
    private static final Object lock = new Object();

    public Indexing(String url, String domain, SiteAndPageTableService siteAndPageTableService,
                    HashSet<String> uniqPage, PageRepositories pageRepositories) {
        this.siteAndPageTableService = siteAndPageTableService;
        this.url = url;
        this.uniqPage = uniqPage;
        this.domain = domain;
        this.pageRepositories = pageRepositories;
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
            if (checkPage(href, url) && uniqPage.add(href)) {
                String content = doc.getAllElements().toString();
                Page page = siteAndPageTableService.createNewPage(statusCode, href, content);
                if(!page.getPath().equals("")){
                    pageRepositories.save(page);
                    Indexing task = new Indexing(href, this.domain, siteAndPageTableService,
                            uniqPage, pageRepositories);
                    task.fork();
                    task.join();
                    siteAndPageTableService.updateDateTime();
                    threadSleep();
                }
            }
        }
    }

    public void parsePage() throws IOException {
        response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .referrer("http://www.google.com")
                .execute();
        int statusCode = response.statusCode();
        crawlPage(statusCode);
    }

    @Override
    protected Boolean compute() {
        try {
            Thread.currentThread().setName("MyWorkerThread-" + url);
            String threadName = Thread.currentThread().getName();
            System.out.println("Thread " + threadName + " is starting");
            parsePage();
            synchronized (lock) {
                System.out.println("Thread " + threadName + " has finished.");
            }
            return true;
        } catch (IOException e) {
            System.out.println("compute exception: " + e);
            siteAndPageTableService.updateStatusToFailed(e.getMessage(),domain);
            return false;
        }
    }
}
