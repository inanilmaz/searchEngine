package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;

import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;

@Service
public class FJPService {
    private ForkJoinPool fjp = new ForkJoinPool();
    private HashSet<String> page = new HashSet<>();

    public boolean createFJP(Site site){
        fjp.invoke(new IndexingService(site));
        page.addAll(fjp.invoke(new IndexingService(site)));
        return !page.isEmpty();
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
