package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.Site;
import searchengine.model.Page;
import searchengine.utils.SiteAndPageTableService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;


public class Indexing extends RecursiveTask<Set<Page>> {

    private Connection.Response response = null;

    private final List<Indexing> taskList = new ArrayList<>();
    private final HashSet<Page> pageSet;

    private final SiteAndPageTableService siteAndPageTableService;
    private String url;
    private final HashSet<String> uniqPage;


    public Indexing(String url, SiteAndPageTableService siteAndPageTableService,
                    HashSet<Page> pageSet, HashSet<String> uniqPage) {
        this.siteAndPageTableService = siteAndPageTableService;
        this.url = url;
        this.pageSet = pageSet;
        this.uniqPage = uniqPage;
    }
    public synchronized void threadSleep(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkPage(String href, String url) {
        return
                href.contains(url.replace("https://www.", "")) &&
                !href.contains("#") &&
                !href.contains("pdf") &&
                !href.equals(url);
    }


    public void crawlPage(int statusCode) throws IOException {
        Document doc = response.parse();
        Elements links = doc.select("a[href]");
        for(Element link : links){
            String href = link.attr("abs:href");
            if(checkPage(href,url)){
                System.out.println(href);
                if(uniqPage.add(href)){
                    String content = doc.getAllElements().toString();
                    pageSet.add(siteAndPageTableService.createNewPage(statusCode,href, content));
                    Indexing task = new Indexing(href,siteAndPageTableService
                            ,pageSet, uniqPage);
                    taskList.add(task);
                    task.fork();
                    siteAndPageTableService.updateDateTime();
                }
            }
        }
        for(Indexing indexingService : taskList){
            indexingService.join();
        }
        siteAndPageTableService.updateStatusToIndexed();

    }


    public void parsePage() throws IOException {
        response = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .execute();
        int statusCode = response.statusCode();
        crawlPage(statusCode);
    }

    @Override
    protected Set<Page> compute() {
        try {
            parsePage();
            return pageSet;
        } catch (IOException e) {
            siteAndPageTableService.updateStatusToFailed(e.getMessage());
            return new HashSet<>();
        }
    }

}
