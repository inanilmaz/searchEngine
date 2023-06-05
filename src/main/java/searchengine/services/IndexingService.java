package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteTable;
import searchengine.model.StatusEnum;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SiteRepositories;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingService {

    private final SiteRepositories siteRepositories;
    public void indexingSite(){
        SitesList sitesList = new SitesList();
        for(Site site : sitesList.getSites()){
            SiteTable siteTable = new SiteTable();
            siteTable.setName(site.getName());
            siteTable.setUrl(site.getUrl());
            siteTable.setStatusTime(LocalDateTime.now());
            siteTable.setStatus(StatusEnum.INDEXING);
            siteRepositories.delete(siteTable);
            siteRepositories.save(siteTable);
        }
    }

}
