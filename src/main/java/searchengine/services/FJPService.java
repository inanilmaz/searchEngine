package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.PageTable;
import searchengine.repositories.PageRepositories;

import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;

@Service
public class FJPService {
    private ForkJoinPool fjp = new ForkJoinPool();
    private HashSet<String> page = new HashSet<>();
    @Autowired
    private PageRepositories pageRepositories;
    @Autowired
    private SitesList sitesList;
    @Autowired
    private SiteAndPageTableService siteAndPageTableService;


    public boolean createFJP() {
        if (!fjp.isShutdown()) {
            siteAndPageTableService.deleteAllEntries();
            for (Site site : sitesList.getSites()) {
                String url = site.getUrl();
                siteAndPageTableService.createNewSite(site);
                page.addAll(fjp.invoke(new IndexingService(site, url,siteAndPageTableService,
                        pageRepositories)));
            }
//            pageRepositories.saveAll(page);
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
