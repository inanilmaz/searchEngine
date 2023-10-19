package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SearchIndexRepositories;
import searchengine.utils.SiteAndPageTableService;

import java.util.HashSet;
@Getter
@Setter
public class Task {
    private String url;
    private String domain;
    private SiteAndPageTableService siteAndPageTableService;
    private HashSet<String> uniqPage;
    private PageRepositories pageRepositories;
    private SearchIndexRepositories searchIndexRepositories;
    private LemmaRepositories lemmaRepositories;
}
