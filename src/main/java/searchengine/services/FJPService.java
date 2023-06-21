package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
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

    public boolean createFJP(Site site){
        fjp.invoke(new IndexingService(site));
        page.addAll(fjp.invoke(new IndexingService(site)));
        if(page.isEmpty()){
            return true;
        }else {
            pageRepositories.saveAll(page);
            return false;
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
