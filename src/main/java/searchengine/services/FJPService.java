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
    private HashSet<PageTable> page = new HashSet<>();
    @Autowired
    private PageRepositories pageRepositories;
    @Autowired
    private SitesList sitesList;

    public boolean createFJP(){
        if(fjp.isShutdown()){
            for(Site site : sitesList.getSites()) {
                page.addAll(fjp.invoke(new IndexingService(site,site.getUrl())));
            }
            pageRepositories.saveAll(page);
            return false;
        }else {
            return true;
        }
    }
    public boolean stopFJP(){
        if(!fjp.isShutdown()){
            fjp.shutdown();
            return true;
        }else {
            return false;
        }

    }
}
