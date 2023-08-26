package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.repositories.PageRepositories;
import searchengine.utils.IndexingTask;
import searchengine.utils.SiteAndPageTableService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

@Service
public class Indexing {
    private ForkJoinPool fjp;
    @Autowired
    private PageRepositories pageRepositories;
    @Autowired
    private SitesList sitesList;
    @Autowired
    private SiteAndPageTableService siteAndPageTableService;
    private Map<String, Boolean> indexingStatusMap;

    public Indexing() {
        fjp = new ForkJoinPool();
        indexingStatusMap = new HashMap<>();
    }

    public boolean startIndexing() {
        if (!fjp.isShutdown()) {
            siteAndPageTableService.deleteAllEntries();
            for (Site site : sitesList.getSites()) {
                indexingStatusMap.put(site.getUrl(), false);
            }
            for (Site site : sitesList.getSites()) {
                String url = site.getUrl();
                siteAndPageTableService.createNewSite(site);
                Boolean isIndexing = fjp.invoke(new IndexingTask(url, url, siteAndPageTableService,
                        new HashSet<>(), pageRepositories));
                siteAndPageTableService.updateStatusToIndexed();
                if (!isIndexing) {
                    siteAndPageTableService.updateStatusToFailed(
                            "Error occurred during indexing.", url);
                } else {
                    indexingStatusMap.put(site.getUrl(), true);
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean stopIndexing() {
        System.out.println("Остановка fjp: " + !fjp.isShutdown());
        if (!fjp.isShutdown()) {
            for (String url : indexingStatusMap.keySet()) {
                Boolean isIndexingSuccessful = indexingStatusMap.get(url);
                if (!isIndexingSuccessful) {
                    siteAndPageTableService.updateStatusToFailed("Индексация остановлена пользователем", url);
                }
            }
            fjp.shutdownNow();
            resetForkJoinPool();
            return true;
        } else {
            return false;
        }
    }

    public void resetForkJoinPool() {
        fjp = new ForkJoinPool();
    }
}
