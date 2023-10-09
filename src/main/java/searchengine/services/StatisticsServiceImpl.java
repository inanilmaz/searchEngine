package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteTable;
import searchengine.model.StatusEnum;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SiteRepositories;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private final SitesList sites;
    @Autowired
    private PageRepositories  pageRepositories;
    @Autowired
    private LemmaRepositories lemmaRepositories;
    @Autowired
    private SiteRepositories siteRepositories;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            SiteTable siteTable = siteRepositories.findByUrl(site.getUrl());
            if(siteTable == null){
                item.setName(site.getName());
                item.setUrl(site.getUrl());
                item.setPages(0);
                item.setLemmas(0);
                item.setStatus(StatusEnum.FAILED);
                item.setError("");
                total.setPages(0);
                total.setIndexing(false);
                total.setLemmas(0);
                detailed.add(item);
            }else {
                item.setName(site.getName());
                item.setUrl(site.getUrl());
                int pages = (int) pageRepositories.count();
                int lemmas = (int) lemmaRepositories.count();
                item.setPages(pages);
                item.setLemmas(lemmas);
                item.setStatus(siteTable.getStatus());
                item.setError(siteTable.getLastError() != null ? siteTable.getLastError() : "Ошибок нет!");
                item.setStatusTime(siteTable.getStatusTime());
                total.setPages(total.getPages() + pages);
                total.setLemmas(total.getLemmas() + lemmas);
                detailed.add(item);

            }
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
