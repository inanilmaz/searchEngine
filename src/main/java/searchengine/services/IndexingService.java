package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.Site;
import searchengine.model.SiteTable;
import searchengine.repositories.PageRepositories;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;


public class IndexingService extends RecursiveTask<Set<String>> {

    private final Site site;
    private Connection.Response response = null;
    private SiteTable updateSiteTable;

    private List<IndexingService> taskList = new ArrayList<>();
    private HashSet<String> pageSet = new HashSet<>();

    private SiteAndPageTableService siteAndPageTableService;
    private String url;
    private PageRepositories pageRepositories;


    public IndexingService(Site site, String url, SiteAndPageTableService siteAndPageTableService, PageRepositories pageRepositories,
                           HashSet<String> pageSet) {
        this.siteAndPageTableService = siteAndPageTableService;
        this.url = url;
        this.site = site;
        this.pageRepositories = pageRepositories;
        this.pageSet = pageSet;
    }

    private boolean checkPage(String href, String url) {
        return href.contains(changeUrl(url)) &&
                !href.contains("#") &&
                !href.contains("pdf") &&
                !href.equals(url);
    }
    public String changeUrl(String url){
        int start = url.indexOf(".");
        return url.substring(start).replaceFirst(".","");
    }

    public void crawlPage(int statusCode) throws IOException {
        Document doc = response.parse();
        Elements links = doc.select("a[href]");
        for(Element link : links){
            String href = link.attr("abs:href");
            if(checkPage(href,site.getUrl())){
                String content = doc.getAllElements().toString();
                int previousSize = pageSet.size();
                pageSet.add(href);
                if (pageSet.size() > previousSize) {
                    pageRepositories.save(siteAndPageTableService.createNewPage(statusCode,href, content));
                    IndexingService task = new IndexingService(site,href,siteAndPageTableService,
                            pageRepositories,pageSet);
                    taskList.add(task);
                    task.fork();
                    siteAndPageTableService.updateDateTime();
                }
            }
        }
        for(IndexingService indexingService : taskList){
            indexingService.join();
        }
        siteAndPageTableService.updateStatusToIndexed();

    }


    public void parsePage() throws IOException {
        response = Jsoup.connect(site.getUrl()).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .execute();
        int statusCode = response.statusCode();
        crawlPage(statusCode);
    }

    @Override
    protected Set<String> compute() {
        try {
            parsePage();
            return pageSet;
        } catch (IOException e) {
            siteAndPageTableService.updateStatusToFailed(e.getMessage());
            return new HashSet<>();
        }
    }

}
