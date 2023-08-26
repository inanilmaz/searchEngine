package searchengine.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.model.SiteTable;
import searchengine.model.StatusEnum;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SearchIndexRepositories;
import searchengine.repositories.SiteRepositories;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SiteAndPageTableService {

    @Autowired
    private SiteRepositories siteRepositories;

    @Autowired
    private PageRepositories pageRepositories;
    @Autowired
    private LemmaRepositories lemmaRepositories;
    @Autowired
    private SearchIndexRepositories searchIndexRepositories;
    private final SiteTable siteTable = new SiteTable();
    public Page createNewPage(int statusCode, String href, String content){
        Page pageTable = new Page();
        pageTable.setSiteId(siteTable);
        String path = href.replaceAll(siteTable.getUrl(),"");
        if(path.equals("")){
            path = "/";
        }
        pageTable.setPath(path);
        pageTable.setContent(content);
        pageTable.setCode(statusCode);
        return pageTable;
    }

    public void createNewSite(searchengine.config.Site site) {
        siteTable.setName(site.getName());
        siteTable.setUrl(site.getUrl());
        siteTable.setStatusTime(LocalDateTime.now());
        siteTable.setStatus(StatusEnum.INDEXING);
        siteRepositories.save(siteTable);
    }

    public void deleteAllEntries() {
        if(searchIndexRepositories != null){
            searchIndexRepositories.deleteAll();
        }
        if(lemmaRepositories != null){
            lemmaRepositories.deleteAll();
        }
        if (pageRepositories != null) {
            pageRepositories.deleteAll();
        }

        if (siteRepositories != null) {
            siteRepositories.deleteAll();
        }
    }

    public void updateStatusToFailed(String errorMessage, String siteUrl) {
        SiteTable site = siteRepositories.findByUrl(siteUrl);
        if (site != null) {
            site.setStatus(StatusEnum.FAILED);
            site.setLastError(errorMessage);
            siteRepositories.save(site);
        }
    }

    public void updateDateTime() {
        List<SiteTable> allSiteTables = siteRepositories.findAll();
        for (SiteTable st : allSiteTables) {
            if (siteTable.getUrl().equals(st.getUrl())) {
                st.setStatusTime(LocalDateTime.now());
                siteRepositories.save(st);
            }
        }
    }

    public void updateStatusToIndexed() {
        siteTable.setStatus(StatusEnum.INDEXED);
        siteRepositories.save(siteTable);
    }
}

