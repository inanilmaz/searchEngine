package searchengine.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.repositories.PageRepositories;
import searchengine.services.Indexing;

import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;

@Service
public class FJPService {
    private ForkJoinPool fjp = new ForkJoinPool();
    private HashSet<Page> pages = new HashSet<>();
    @Autowired
    private PageRepositories pageRepositories;
    @Autowired
    private SitesList sitesList;
    @Autowired
    private SiteAndPageTableService siteAndPageTableService;


    public boolean createFJP() {
        if (!fjp.isShutdown()) {
            siteAndPageTableService.deleteAllEntries();
            HashSet<String> uniqPage = new HashSet<>();
            for (Site site : sitesList.getSites()) {
                String url = site.getUrl();
                siteAndPageTableService.createNewSite(site);
                pages.addAll(fjp.invoke(new Indexing(url,siteAndPageTableService,
                        pages, uniqPage)));
            }
            pageRepositories.saveAll(pages);
            return false;
        } else {
            return true;
        }
    }
    public boolean stopFJP(){
        if(fjp.isShutdown()){
            fjp.shutdown();
            return true;
        }else {
            return false;
        }

    }
}
