package searchengine.services;

import lombok.RequiredArgsConstructor;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

@Service
@RequiredArgsConstructor
public class IndexingService extends RecursiveTask<Set<String>> {

    private final SiteRepositories siteRepositories;
    private String modifiedUrl;
    private Document doc;
    private List<String> urls;
    private HashSet<PageTable> pageSet = new HashSet<>();
    private List<IndexingService> listIndexing = new ArrayList<>();
    public void indexingSite() throws IOException, InterruptedException {
        SitesList sitesList = new SitesList();
        for(Site site : sitesList.getSites()){
            SiteTable siteTable = new SiteTable();
            siteTable.setName(site.getName());
            siteTable.setUrl(site.getUrl());
            siteTable.setStatusTime(LocalDateTime.now());
            siteTable.setStatus(StatusEnum.INDEXING);
            siteRepositories.delete(siteTable);
            siteRepositories.save(siteTable);
            urls.add(siteTable.getUrl());
        }
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
        for(String url:urls){
            doc = Jsoup.connect(url).timeout(5000).get();
            Thread.sleep(100);
            Elements pages = doc.select("a[href]");
            for(Element page : pages){
                String href = page.attr("abs:href");
                changeUrl(url);
                if(checkPage(href, url)){
                    IndexingService index = new IndexingService(siteRepositories);
                    listIndexing.add(index);
                    index.fork();
                }
            }
        }
        for(IndexingService index : listIndexing){
            index.join();
        }
        return null;
    }
}
