package searchengine.services;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.PageTable;
import searchengine.model.SiteTable;
import searchengine.model.StatusEnum;
import searchengine.repositories.SiteRepositories;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RecursiveTask;

@Service
public class IndexingService extends RecursiveTask<Set<String>> {

    private final SiteRepositories siteRepositories;
    private String modifiedUrl;
    private Document doc;
    private final Site site;
    private List<IndexingService> listIndexing = new ArrayList<>();
    private Set<PageTable> pagesTable = new HashSet<>();

    public IndexingService(SiteRepositories siteRepositories, Site site) {
        this.siteRepositories = siteRepositories;
        this.site = site;
    }

    public void indexingSite(){
        SiteTable siteTable = new SiteTable();
        siteTable.setName(site.getName());
        siteTable.setUrl(site.getUrl());
        siteTable.setStatusTime(LocalDateTime.now());
        siteTable.setStatus(StatusEnum.INDEXING);
        siteRepositories.delete(siteTable);
        siteRepositories.save(siteTable);
    }
    public boolean checkPage(String href, String url){
        if(href.contains(modifiedUrl)
                && !href.contains("#")
                && !href.contains("pdf")
                && !href.equals(url)){
            return true;
        }
        return false;
    }
    public void changeUrl(String url){
        int start = url.indexOf(".");
        modifiedUrl = url.substring(start).replaceFirst(".","");
    }
    @SneakyThrows
    @Override
    protected Set<String> compute() {
        indexingSite();
        doc = Jsoup.connect(site.getUrl()).timeout(5000).get();
        Thread.sleep(100);
        Elements pages = doc.select("a[href]");
        for(Element page : pages){
            String href = page.attr("abs:href");
            changeUrl(site.getUrl());
            if(checkPage(href,site.getUrl())){
                IndexingService index = new IndexingService(siteRepositories, site);
                listIndexing.add(index);
                PageTable pageTable = new PageTable();
                //добавить данные в pageTable;
                index.fork();
            }
        }
        for(IndexingService index : listIndexing){
            index.join();
        }
        return null;
    }
}
