package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SiteRepositories;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingService {
    private final SitesList sites;
    private final SiteRepositories siteRepositories;
    private final PageRepositories pageRepositories;
    public void indexing(){
        List<Site> siteList = sites.getSites();
        for(Site site : siteList){
        }
    }

}
