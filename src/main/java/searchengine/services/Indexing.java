package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.Task;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SearchIndexRepositories;
import searchengine.utils.IndexingTask;
import searchengine.utils.SiteAndPageTableService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class Indexing {
    private ForkJoinPool fjp;
    @Autowired
    private PageRepositories pageRepositories;
    @Autowired
    private SitesList sitesList;
    @Autowired
    private SiteAndPageTableService siteAndPageTableService;
    @Autowired
    private SearchIndexRepositories searchIndexRepositories;
    @Autowired
    private LemmaRepositories lemmaRepositories;
    private final Map<String, Boolean> indexingStatusMap;
    private AtomicBoolean shouldStop = new AtomicBoolean(false);

    public Indexing() {
        fjp = new ForkJoinPool();
        indexingStatusMap = new HashMap<>();
    }

    public boolean isIndexingInProgress() {
        return fjp.isShutdown();
    }

    public void startIndexing() {
        siteAndPageTableService.deleteAllEntries();
        for (Site site : sitesList.getSites()) {
            indexingStatusMap.put(site.getUrl(), false);
        }
        for (Site site : sitesList.getSites()) {
            String url = site.getUrl();
            siteAndPageTableService.createNewSite(site);
            Task task = new Task();
            task.setUrl(url);
            task.setDomain(url);
            task.setSiteAndPageTableService(siteAndPageTableService);
            task.setUniqPage(new HashSet<>());
            task.setPageRepositories(pageRepositories);
            task.setSearchIndexRepositories(searchIndexRepositories);
            task.setLemmaRepositories(lemmaRepositories);
            Boolean isIndexing = fjp.invoke(new IndexingTask(task,shouldStop));
            siteAndPageTableService.updateStatusToIndexed();
            if (!isIndexing) {
                siteAndPageTableService.updateStatusToFailed(
                        "Error occurred during indexing.", url);
            } else {
                indexingStatusMap.put(site.getUrl(), true);
            }
        }
    }

    public boolean stopIndexing() {
        System.out.println("Остановка fjp: " + !fjp.isShutdown());
        if (!fjp.isShutdown()) {
            for (String url : indexingStatusMap.keySet()) {
                Boolean isIndexingSuccessful = indexingStatusMap.get(url);
                if (!isIndexingSuccessful) {
                    shouldStop.set(true);
                    siteAndPageTableService.updateStatusToFailed("Индексация остановлена пользователем", url);
                }
            }
            fjp.shutdownNow();
            resetForkJoinPool();
            return true;
        }else {
            return false;
        }
    }

    public void resetForkJoinPool() {
        fjp = new ForkJoinPool();
    }
}
