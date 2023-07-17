package searchengine.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.repositories.PageRepositories;
import searchengine.services.Indexing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Service
public class FJPService {
    private ForkJoinPool fjp = new ForkJoinPool();
    @Autowired
    private PageRepositories pageRepositories;
    @Autowired
    private SitesList sitesList;
    @Autowired
    private SiteAndPageTableService siteAndPageTableService;
    private Map<String,Boolean> indexingStatusMap = new HashMap<>();


    public boolean createFJP() {
        if (!fjp.isShutdown()) {
            for(Site site : sitesList.getSites()){
                indexingStatusMap.put(site.getUrl(),false);
            }
            siteAndPageTableService.deleteAllEntries();
            for (Site site : sitesList.getSites()) {
                String url = site.getUrl();
                siteAndPageTableService.createNewSite(site);
                Boolean isIndexing = fjp.invoke(new Indexing(url, url, siteAndPageTableService,
                        new HashSet<>(), pageRepositories));
                if (!isIndexing) {
                    siteAndPageTableService.updateStatusToFailed(
                            "Error occurred during indexing.",url);
                }else {
                    indexingStatusMap.put(site.getUrl(),true);
                }
            }
            return false;
        } else {
            return true;
        }
    }
    public boolean stopFJP(){
        if(fjp.isShutdown()){
            for(String url : indexingStatusMap.keySet()){
                Boolean isIndexingSuccessful = indexingStatusMap.get(url);
                if(!isIndexingSuccessful){
                    siteAndPageTableService.updateStatusToFailed("Индексация остановлена пользователем",url);
                }
            }
            fjp.shutdown();
            return true;
        }else {
            return false;
        }

    }
}
